package com.fifokit.app.domain.finance

import java.time.LocalDateTime

data class PayLine(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val hours: Double,
    val hourlyRate: Double,
    val gross: Double,
    val appliedRuleIds: List<String>
)

data class ShiftPayBreakdown(
    val shift: PaidShift,
    val lines: List<PayLine>,
    val baseGross: Double,
    val allowances: Double,
    val totalGross: Double
)

data class PayBreakdown(
    val shifts: List<ShiftPayBreakdown>,
    val fixedAllowances: Double,
    val totalHours: Double,
    val totalGross: Double,
    val effectiveHourlyRate: Double
)
