package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.cloud.model.CloudRosterAccess
import com.fifokit.app.domain.sharing.RosterAccessRole
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RosterSharingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun grantViewerAccess(
        rosterId: String,
        ownerId: String,
        userId: String,
        inviteId: String = ""
    ) {
        val shareId = "${ownerId}_${rosterId}_${userId}"

        val access = CloudRosterAccess(
            rosterId = rosterId,
            ownerId = ownerId,
            userId = userId,
            role = RosterAccessRole.VIEWER.name,
            inviteId = inviteId,
            createdAt = System.currentTimeMillis()
        )

        firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .document(shareId)
            .set(access)
            .await()
    }

    suspend fun getSharesForRoster(
        rosterId: String,
        ownerId: String
    ): List<CloudRosterAccess> {
        return firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toRosterAccessOrNull() }
            .filter { it.rosterId == rosterId }
    }

    suspend fun getSharedRostersForUser(
        userId: String
    ): List<CloudRosterAccess> {
        return firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toRosterAccessOrNull() }
    }

    suspend fun getSharedRoster(
        ownerId: String,
        rosterId: String
    ): CloudRoster? {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(ownerId)
            .collection(FirestorePaths.ROSTERS)
            .document(rosterId)
            .get()
            .await()
            .toObject(CloudRoster::class.java)
    }

    suspend fun revokeAccess(
        rosterId: String,
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

    private fun DocumentSnapshot.toRosterAccessOrNull(): CloudRosterAccess? {
        val rosterIdValue = get("rosterId")

        val rosterId =
            when (rosterIdValue) {
                is String -> rosterIdValue
                else -> return null
            }

        return CloudRosterAccess(
            rosterId = rosterId,
            ownerId = getString("ownerId").orEmpty(),
            userId = getString("userId").orEmpty(),
            role = getString("role").orEmpty(),
            inviteId = getString("inviteId").orEmpty(),
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}