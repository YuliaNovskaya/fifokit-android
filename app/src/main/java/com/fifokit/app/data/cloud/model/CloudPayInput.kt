package com.fifokit.app.data.cloud.model

data class CloudPayInput(
    val rateType: String = "HOURLY",
    val rate: Double = 0.0,
    val hoursPerWorkDay: Double = 12.0,
    val allowancePerWorkDay: Double = 0.0,
    val pipType: String = "NONE",
    val pipValue: Double = 0.0,

    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 2
)
