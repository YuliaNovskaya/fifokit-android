package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRosterInvite
import com.google.firebase.auth.FirebaseAuth
import com.fifokit.app.domain.sharing.SharingLimitReachedException
import com.fifokit.app.domain.sharing.SharingPolicy
import java.util.UUID

class RosterInviteManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val inviteRepository: RosterInviteRepository = RosterInviteRepository(),
    private val sharingRepository: RosterSharingRepository = RosterSharingRepository()
) {

    suspend fun createInvite(
        rosterId: String,
        rosterName: String,
        isPro: Boolean
    ): String {
        val ownerId = auth.currentUser?.uid
            ?: error("User must be signed in")

        val activeRecipientCount =
            sharingRepository
                .getSharesForOwner(ownerId)
                .map { it.userId }
                .filter { it.isNotBlank() }
                .distinct()
                .size

        val pendingInviteCount =
            inviteRepository
                .getPendingInvitesForOwner(ownerId)
                .size

        if (
            !SharingPolicy.canCreateInvite(
                activeRecipientCount =
                    activeRecipientCount,
                pendingInviteCount =
                    pendingInviteCount,
                isPro = isPro
            )
        ) {
            throw SharingLimitReachedException(
                limit =
                    SharingPolicy.recipientLimit(
                        isPro
                    ),
                isPro = isPro
            )
        }

        val inviteId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val invite = CloudRosterInvite(
            inviteId = inviteId,
            rosterId = rosterId,
            ownerId = ownerId,
            rosterName = rosterName,
            role = "VIEWER",
            createdAt = now,
            expiresAt = now + INVITE_EXPIRY_MS,
            status = "PENDING"
        )

        inviteRepository.createInvite(invite)

        return inviteId
    }

    suspend fun acceptInvite(
        inviteId: String
    ) {
        val userId = auth.currentUser?.uid
            ?: error("User must be signed in")

        val invite = inviteRepository.getInvite(inviteId)
            ?: error("Invite not found")

        if (invite.status != "PENDING") {
            error("Invite is no longer available")
        }

        if (System.currentTimeMillis() > invite.expiresAt) {
            error("Invite has expired")
        }

        if (invite.ownerId == userId) {
            error("You cannot accept your own invite")
        }

        sharingRepository.grantViewerAccess(
            rosterId = invite.rosterId,
            ownerId = invite.ownerId,
            userId = userId,
            inviteId = invite.inviteId
        )

        inviteRepository.updateInvite(
            invite.copy(
                status = "ACCEPTED",
                acceptedBy = userId,
                acceptedAt = System.currentTimeMillis()
            )
        )
    }

    companion object {
        private const val INVITE_EXPIRY_MS = 7L * 24L * 60L * 60L * 1000L
    }
}