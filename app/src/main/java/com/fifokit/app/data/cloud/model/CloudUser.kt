package com.fifokit.app.data.cloud.model

data class CloudUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val schemaVersion: Int = 1
)