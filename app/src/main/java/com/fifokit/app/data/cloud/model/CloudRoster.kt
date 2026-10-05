package com.fifokit.app.data.cloud.model

data class CloudRoster(
    val id: String = "",
    val name: String = "",
    val pattern: String = "",
    val isCustomRoster: Boolean = false,
    val customWorkDays: Int = 0,
    val customOffDays: Int = 0,
    val startDate: String = "",
    val isShutdownRoster: Boolean = false,
    val endDate: String? = null,
    val shutdownsJson: String = "[]",
    val selectedStates: List<String> = listOf("WA"),
    val isActive: Boolean = false,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 3,
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0L,

    val ownerId: String = "",
)