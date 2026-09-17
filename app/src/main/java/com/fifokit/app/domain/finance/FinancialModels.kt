package com.fifokit.app.domain.finance

enum class PayRateType {
    HOURLY,
    DAILY,
    ANNUAL_SALARY
}

data class PayInput(
    val rateType: PayRateType = PayRateType.HOURLY,
    val rate: Double = 0.0,
    val hoursPerWorkDay: Double = 12.0,
    val allowancePerWorkDay: Double = 0.0
)

data class EarningsResult(
    val workDaysPerYear: Int,
    val restDaysPerYear: Int,
    val workHoursPerYear: Double,
    val baseEarnings: Double,
    val allowances: Double,
    val totalGrossEarnings: Double
)

data class FinancialGoal(
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val contributionPerPay: Double = 0.0,
    val payFrequencyDays: Int = 14
)

data class FinancialGoalResult(
    val amountRemaining: Double,
    val contributionsRequired: Int,
    val daysRequired: Int
)