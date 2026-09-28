package com.fifokit.app.domain.sharing

import com.fifokit.app.data.cloud.model.CloudRoster

data class SharedRoster(
    val roster: CloudRoster,
    val ownerId: String,
    val role: RosterAccessRole
)