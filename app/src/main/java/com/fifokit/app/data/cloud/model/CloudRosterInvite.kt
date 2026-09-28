package com.fifokit.app.data.cloud.model

data class CloudRosterInvite(
    val inviteId: String = "",
    val rosterId: Long = 0,
    val ownerId: String = "",
    val rosterName: String = "",
    val role: String = "VIEWER",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val status: String = "PENDING",
    val acceptedBy: String = "",
    val acceptedAt: Long = 0L
)