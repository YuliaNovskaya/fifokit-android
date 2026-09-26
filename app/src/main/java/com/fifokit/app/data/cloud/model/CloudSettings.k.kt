package com.fifokit.app.data.cloud.model

data class CloudSettings(
    val selectedStates: List<String> = listOf("WA"),

    val remindersEnabled: Boolean = true,
    val workRemindersEnabled: Boolean = true,
    val offRemindersEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,

    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 1
)