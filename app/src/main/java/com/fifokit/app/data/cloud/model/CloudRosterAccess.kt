package com.fifokit.app.data.cloud.model

data class CloudRosterAccess(
    val rosterId: String = "",
    val ownerId: String = "",
    val userId: String = "",
    val role: String = "",
    val inviteId: String = "",
    val createdAt: Long = 0L,
    val isActive: Boolean = true,
    val pausedAt: Long = 0L
)