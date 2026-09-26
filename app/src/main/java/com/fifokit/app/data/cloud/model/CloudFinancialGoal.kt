package com.fifokit.app.data.cloud.model

data class CloudFinancialGoal(
    val id: String = "primary",

    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val contributionPerPay: Double = 0.0,
    val payFrequencyDays: Int = 14,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 1
)