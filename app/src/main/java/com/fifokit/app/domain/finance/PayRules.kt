package com.fifokit.app.domain.finance

import java.time.DayOfWeek
import java.time.LocalTime

enum class DayCategory { WEEKDAY, SATURDAY, SUNDAY, PUBLIC_HOLIDAY }
enum class RateValueType { HOURLY_AMOUNT, MULTIPLIER, ADDITIVE_PER_HOUR }
enum class OvertimePeriod { SHIFT, WEEK }
enum class AllowanceFrequency { PER_HOUR, PER_SHIFT, PER_SWING, WEEKLY, FIXED }
enum class RateCombination { REPLACE, ADD }

data class RateRule(
    val id: String,
    val category: DayCategory? = null,
    val weekday: DayOfWeek? = null,
    val shiftType: PayShiftType? = null,
    val fromTime: LocalTime? = null,
    val untilTime: LocalTime? = null,
    val value: Double,
    val valueType: RateValueType = RateValueType.HOURLY_AMOUNT,
    val priority: Int = 0,
    val combination: RateCombination = RateCombination.REPLACE
)

data class OvertimeRule(
    val id: String,
    val period: OvertimePeriod = OvertimePeriod.SHIFT,
    val afterHours: Double,
    val untilHours: Double? = null,
    val value: Double,
    val valueType: RateValueType = RateValueType.MULTIPLIER,
    val combination: RateCombination = RateCombination.REPLACE,
    val priority: Int = 0
)

data class Allowance(
    val id: String,
    val name: String,
    val amount: Double,
    val frequency: AllowanceFrequency
)
