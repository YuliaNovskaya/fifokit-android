package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.cloud.model.CloudRosterAccess
import com.fifokit.app.domain.sharing.RosterAccessRole
import com.fifokit.app.domain.sharing.SharingPolicy
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source

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

    suspend fun getSharesForOwner(
        ownerId: String
    ): List<CloudRosterAccess> {
        return firestore
            .collection(FirestorePaths.ROSTER_SHARES)
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toRosterAccessOrNull() }
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

    suspend fun reconcileOwnerAccess(
        ownerId: String,
        recipientLimit: Int
    ) {
        val shares = getSharesForOwner(ownerId)

        val activeRecipientIds =
            SharingPolicy.recipientIdsToKeepActive(
                recipientEntries =
                    shares.map {
                        it.userId to it.createdAt
                    },
                limit = recipientLimit
            )

        val now = System.currentTimeMillis()

        shares.forEach { access ->
            val shouldBeActive =
                access.userId in activeRecipientIds

            if (access.isActive != shouldBeActive) {
                val shareId =
                    "${access.ownerId}_${access.rosterId}_${access.userId}"

                firestore
                    .collection(FirestorePaths.ROSTER_SHARES)
                    .document(shareId)
                    .update(
                        mapOf(
                            "isActive" to shouldBeActive,
                            "pausedAt" to
                                    if (shouldBeActive) 0L else now
                        )
                    )
                    .await()
            }
        }
    }

    suspend fun getSharedRoster(
        ownerId: String,
        rosterId: String
    ): CloudRoster? {

        val reference =
            firestore.collection(FirestorePaths.USERS)
                .document(ownerId)
                .collection(FirestorePaths.ROSTERS)
                .document(rosterId)

        val snapshot =
            try {
                reference
                    .get(Source.SERVER)
                    .await()
            } catch (e: FirebaseFirestoreException) {

                if (
                    e.code ==
                    FirebaseFirestoreException.Code.UNAVAILABLE
                ) {
                    reference
                        .get(Source.CACHE)
                        .await()
                } else {
                    throw e
                }
            }

        val roster =
            snapshot.toObject(
                CloudRoster::class.java
            ) ?: return null

        return roster.copy(
            id =
                snapshot.getString("id")
                    ?: snapshot.id,

            isCustomRoster =
                snapshot.getBoolean("isCustomRoster")
                    ?: snapshot.getBoolean("customRoster")
                    ?: roster.isCustomRoster,

            isActive =
                snapshot.getBoolean("isActive")
                    ?: snapshot.getBoolean("active")
                    ?: roster.isActive,

            isDeleted =
                snapshot.getBoolean("isDeleted")
                    ?: snapshot.getBoolean("deleted")
                    ?: roster.isDeleted
        )
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
                is Long -> rosterIdValue.toString()
                is Number -> rosterIdValue.toLong().toString()
                else -> return null
            }

        return CloudRosterAccess(
            rosterId = rosterId,
            ownerId = getString("ownerId").orEmpty(),
            userId = getString("userId").orEmpty(),
            role = getString("role").orEmpty(),
            inviteId = getString("inviteId").orEmpty(),
            createdAt = getLong("createdAt") ?: 0L,
            isActive = getBoolean("isActive") ?: true,
            pausedAt = getLong("pausedAt") ?: 0L
        )
    }
}