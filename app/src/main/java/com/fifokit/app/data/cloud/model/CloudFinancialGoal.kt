package com.fifokit.app.data.cloud.model

data class CloudFinancialGoal(
    val id: String = "",
    val name: String = "",

    val targetAmount: Double = 0.0,
    val savingsPerPay: Double = 0.0,
    val expectedDate: String = "",

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deviceId: String = "",
    val schemaVersion: Int = 1
)