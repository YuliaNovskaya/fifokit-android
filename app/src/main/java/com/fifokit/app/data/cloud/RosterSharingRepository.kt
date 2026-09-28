package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.cloud.model.CloudRosterAccess
import com.fifokit.app.domain.sharing.RosterAccessRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RosterSharingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun grantViewerAccess(
        rosterId: Long,
        ownerId: String,
        userId: String
    ) {
        val shareId = "${ownerId}_${rosterId}_${userId}"

        val access = CloudRosterAccess(
            rosterId = rosterId,
            ownerId = ownerId,
            userId = userId,
            role = RosterAccessRole.VIEWER.name,
            createdAt = System.currentTimeMillis()
        )

        firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .document(shareId)
            .set(access)
            .await()
    }

    suspend fun getSharesForRoster(
        rosterId: Long,
        ownerId: String
    ): List<CloudRosterAccess> {
        return firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("rosterId", rosterId)
            .get()
            .await()
            .toObjects(CloudRosterAccess::class.java)
    }

    suspend fun getSharedRostersForUser(
        userId: String
    ): List<CloudRosterAccess> {
        return firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(CloudRosterAccess::class.java)
    }

    suspend fun getSharedRoster(
        ownerId: String,
        rosterId: Long
    ): CloudRoster? {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(ownerId)
            .collection(FirestorePaths.ROSTERS)
            .document(rosterId.toString())
            .get()
            .await()
            .toObject(CloudRoster::class.java)
    }

    suspend fun revokeAccess(
        rosterId: Long,
        ownerId: String,
        userId: String
    ) {
        val shareId = "${ownerId}_${rosterId}_${userId}"

        firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .document(shareId)
            .delete()
            .await()
    }
}