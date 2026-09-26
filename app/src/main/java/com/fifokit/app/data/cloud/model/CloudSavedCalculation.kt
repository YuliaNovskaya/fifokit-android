package com.fifokit.app.data.cloud.model

data class CloudSavedCalculation(
    val id: String = "",
    val type: String = "",
    val name: String = "",

    val inputs: Map<String, String> = emptyMap(),
    val results: Map<String, String> = emptyMap(),

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 1
)