package com.fifokit.app.domain.finance

enum class PayRateType {
    HOURLY,
    DAILY,
    ANNUAL_SALARY
}

enum class PipType {
    NONE,
    PER_HOUR,
    PER_DAY,
    PERCENT_BASE,
    FIXED_AMOUNT
}

data class PayInput(
    val rateType: PayRateType = PayRateType.HOURLY,
    val rate: Double = 0.0,
    val hoursPerWorkDay: Double = 12.0,
    val allowancePerWorkDay: Double = 0.0,
    val pipType: PipType = PipType.NONE,
    val pipValue: Double = 0.0,
    val localRatesEnabled: Boolean = false,
    val localWeekdayHourlyRate: Double = 0.0,
    val localWeekdayBaseHours: Double = 8.0,
    val localWeekdayOvertimeHourlyRate: Double = 0.0,
    val localWeekdayOvertimeHours: Double = 0.0,
    val localSaturdayHourlyRate: Double = 0.0,
    val localSaturdayHours: Double = 8.0,
    val localSundayHourlyRate: Double = 0.0,
    val localSundayHours: Double = 0.0,
    val localHoursPerDay: Double = 8.0
)

data class EarningsResult(
    val workDaysPerYear: Int,
    val restDaysPerYear: Int,
    val workHoursPerYear: Double,
    val baseEarnings: Double,
    val pipEarnings: Double,
    val allowances: Double,
    val totalGrossEarnings: Double,
    val equivalentHourlyRate: Double? = null,
    val publicHolidaysWorked: Int = 0
)

data class LocalPayResult(
    val weekdayGrossPerWeek: Double,
    val saturdayGrossPerWeek: Double,
    val sundayGrossPerWeek: Double = 0.0,
    val weekdayBaseGrossPerWeek: Double = 0.0,
    val weekdayOvertimeGrossPerWeek: Double = 0.0,
    val weeklyGross: Double,
    val annualisedGross: Double,
    val annualHours: Double,
    val effectiveHourlyRate: Double
)

data class FinancialGoal(
    val id: String = "primary",
    val name: String = "My goal",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val contributionPerPay: Double = 0.0,
    val payFrequencyDays: Int = 14,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0L
)

data class FinancialGoalResult(
    val amountRemaining: Double,
    val contributionsRequired: Int,
    val daysRequired: Int
)