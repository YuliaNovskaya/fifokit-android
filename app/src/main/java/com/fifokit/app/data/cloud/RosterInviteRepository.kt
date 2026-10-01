package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRosterInvite
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RosterInviteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createInvite(
        invite: CloudRosterInvite
    ) {
        firestore
            .collection(FirestorePaths.ROSTER_INVITES)
            .document(invite.inviteId)
            .set(invite)
            .await()
    }

    suspend fun getPendingInvitesForOwner(
        ownerId: String,
        now: Long = System.currentTimeMillis()
    ): List<CloudRosterInvite> {
        return firestore
            .collection(FirestorePaths.ROSTER_INVITES)
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()
            .documents
            .mapNotNull {
                it.toObject(
                    CloudRosterInvite::class.java
                )
            }
            .filter {
                it.status == "PENDING" &&
                        it.expiresAt >= now
            }
    }

    suspend fun getInvite(
        inviteId: String
    ): CloudRosterInvite? {
        return firestore
            .collection(FirestorePaths.ROSTER_INVITES)
            .document(inviteId)
            .get()
            .await()
            .toObject(CloudRosterInvite::class.java)
    }

    suspend fun updateInvite(
        invite: CloudRosterInvite
    ) {
        firestore
            .collection(FirestorePaths.ROSTER_INVITES)
            .document(invite.inviteId)
            .set(invite)
            .await()
    }

    suspend fun deleteInvite(
        inviteId: String
    ) {
        firestore
            .collection(FirestorePaths.ROSTER_INVITES)
            .document(inviteId)
            .delete()
            .await()
    }
}