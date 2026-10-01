package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRosterInvite
import com.fifokit.app.domain.sharing.SharingPolicy
import com.google.firebase.firestore.DocumentSnapshot
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
                it.toRosterInviteOrNull()
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
            .toRosterInviteOrNull()
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

    private fun DocumentSnapshot.toRosterInviteOrNull():
            CloudRosterInvite? {

        if (!exists()) {
            return null
        }

        val rosterId =
            when (val value = get("rosterId")) {
                is String -> value
                is Long -> value.toString()
                is Number -> value.toLong().toString()
                else -> return null
            }

        return CloudRosterInvite(
            inviteId =
                getString("inviteId")
                    ?.takeIf { it.isNotBlank() }
                    ?: id,
            rosterId = rosterId,
            ownerId = getString("ownerId").orEmpty(),
            rosterName = getString("rosterName").orEmpty(),
            role = getString("role") ?: "VIEWER",
            createdAt = getLong("createdAt") ?: 0L,
            expiresAt = getLong("expiresAt") ?: 0L,
            status = getString("status") ?: "PENDING",
            recipientLimit =
                (
                    getLong("recipientLimit")
                        ?: SharingPolicy
                            .FREE_RECIPIENT_LIMIT
                            .toLong()
                ).toInt(),
            acceptedBy = getString("acceptedBy").orEmpty(),
            acceptedAt = getLong("acceptedAt") ?: 0L
        )
    }
}