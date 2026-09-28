package com.fifokit.app.data.cloud

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RosterSharingCleanupRepository(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    suspend fun deleteAllSharingDataForUser(
        userId: String
    ) {
        val ownedShares =
            firestore
                .collection(FirestorePaths.ROSTER_SHARES)
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .documents

        val receivedShares =
            firestore
                .collection(FirestorePaths.ROSTER_SHARES)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents

        val ownedInvites =
            firestore
                .collection(FirestorePaths.ROSTER_INVITES)
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .documents

        val acceptedInvites =
            firestore
                .collection(FirestorePaths.ROSTER_INVITES)
                .whereEqualTo("acceptedBy", userId)
                .get()
                .await()
                .documents

        val documents =
            (
                    ownedShares +
                            receivedShares +
                            ownedInvites +
                            acceptedInvites
                    )
                .distinctBy {
                    it.reference.path
                }

        deleteDocuments(documents)
    }

    private suspend fun deleteDocuments(
        documents: List<DocumentSnapshot>
    ) {
        documents
            .chunked(400)
            .forEach { chunk ->

                val batch =
                    firestore.batch()

                chunk.forEach { document ->
                    batch.delete(
                        document.reference
                    )
                }

                batch.commit().await()
            }
    }
}