package com.fifokit.app.data.cloud.model

data class CloudRoster(
    val id: String = "",
    val name: String = "",
    val pattern: String = "",
    val isCustomRoster: Boolean = false,
    val customWorkDays: Int = 0,
    val customOffDays: Int = 0,
    val startDate: String = "",
    val isActive: Boolean = false,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 1
)