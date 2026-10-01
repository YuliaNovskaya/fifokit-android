package com.fifokit.app.data.cloud.model

data class CloudRosterInvite(
    val inviteId: String = "",
    val rosterId: String = "",
    val ownerId: String = "",
    val rosterName: String = "",
    val role: String = "VIEWER",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val status: String = "PENDING",
    val recipientLimit: Int = 1,
    val acceptedBy: String = "",
    val acceptedAt: Long = 0L
)