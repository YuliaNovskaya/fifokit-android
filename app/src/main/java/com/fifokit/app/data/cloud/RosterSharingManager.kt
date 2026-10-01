package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudRosterAccess
import com.google.firebase.auth.FirebaseAuth

class RosterSharingManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: RosterSharingRepository = RosterSharingRepository()
) {

    suspend fun grantViewerAccess(
        rosterId: String,
        viewerUserId: String
    ) {
        val ownerId = auth.currentUser?.uid ?: return

        repository.grantViewerAccess(
            rosterId = rosterId,
            ownerId = ownerId,
            userId = viewerUserId
        )
    }

    suspend fun getRosterShares(
        rosterId: String
    ): List<CloudRosterAccess> {
        val ownerId = auth.currentUser?.uid ?: return emptyList()

        return repository.getSharesForRoster(
            rosterId = rosterId,
            ownerId = ownerId
        )
    }

    suspend fun getOwnerShares(): List<CloudRosterAccess> {
        val ownerId = auth.currentUser?.uid ?: return emptyList()

        return repository.getSharesForOwner(
            ownerId = ownerId
        )
    }

    suspend fun revokeViewerAccess(
        rosterId: String,
        viewerUserId: String
    ) {
        val ownerId = auth.currentUser?.uid ?: return

        repository.revokeAccess(
            rosterId = rosterId,
            ownerId = ownerId,
            userId = viewerUserId
        )
    }
}