package com.fifokit.app.data.cloud.model

data class CloudRosterAccess(
    val rosterId: Long = 0,
    val ownerId: String = "",
    val userId: String = "",
    val role: String = "",
    val inviteId: String = "",
    val createdAt: Long = 0L
)