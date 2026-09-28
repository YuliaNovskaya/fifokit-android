package com.fifokit.app.data.cloud

import com.fifokit.app.domain.sharing.RosterAccessRole
import com.fifokit.app.domain.sharing.SharedRoster
import com.google.firebase.auth.FirebaseAuth

class SharedRosterManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val sharingRepository: RosterSharingRepository = RosterSharingRepository()
) {

    suspend fun loadSharedRosters(): List<SharedRoster> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return sharingRepository
            .getSharedRostersForUser(userId)
            .mapNotNull { access ->

                val role = runCatching {
                    RosterAccessRole.valueOf(access.role)
                }.getOrNull() ?: return@mapNotNull null

                val roster = sharingRepository.getSharedRoster(
                    ownerId = access.ownerId,
                    rosterId = access.rosterId
                ) ?: return@mapNotNull null

                SharedRoster(
                    roster = roster,
                    ownerId = access.ownerId,
                    role = role
                )
            }
    }
}