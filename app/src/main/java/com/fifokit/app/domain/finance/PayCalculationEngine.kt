package com.fifokit.app.domain.finance

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * Advanced pay engine. Rules are supplied from the worker's agreement.
 * Hourly and daily rates are calculated from actual shift instants.
 * Salary is treated as a period amount, not as automatic overtime entitlement.
 */
object PayCalculationEngine {
    fun calculate(
        profile: PayProfile,
        shifts: List<PaidShift>,
        publicHolidays: Set<LocalDate> = emptySet(),
        annualSalaryPeriodFraction: Double = 1.0
    ): PayBreakdown {
        require(profile.baseRate >= 0.0)
        require(profile.pipValue >= 0.0)
        require(profile.casualLoadingPercent >= 0.0)
        require(annualSalaryPeriodFraction >= 0.0)
        require(profile.overtimeRules.all {
            it.afterHours >= 0.0 && (it.untilHours == null || it.untilHours > it.afterHours)
        })
        val ordered = shifts.sortedBy { it.start }
        val seenSwings = mutableSetOf<String>()
        var weeklyHours = mutableMapOf<Pair<Int, Int>, Double>()
        val calculated = ordered.map { shift ->
            require(profile.effectiveFrom == null || !shift.start.toLocalDate().isBefore(profile.effectiveFrom))
            require(profile.effectiveUntil == null || !shift.start.toLocalDate().isAfter(profile.effectiveUntil))
            val hours = ChronoUnit.MINUTES.between(shift.start, shift.end) / 60.0
            var elapsedShift = 0.0
            var current = shift.start
            val lines = mutableListOf<PayLine>()
            while (current.isBefore(shift.end)) {
                val nextMidnight = current.toLocalDate().plusDays(1).atStartOfDay()
                val nextRuleBoundary = profile.rateRules.flatMap { rule ->
                    listOfNotNull(rule.fromTime, rule.untilTime)
                }.map { time ->
                    var boundary = current.toLocalDate().atTime(time)
                    if (!boundary.isAfter(current)) boundary = boundary.plusDays(1)
                    boundary
                }.filter { it.isAfter(current) }.minOrNull()
                val end = listOfNotNull(shift.end, nextMidnight, nextRuleBoundary).minOrNull()!!
                val segmentHours = ChronoUnit.MINUTES.between(current, end) / 60.0
                val category = when {
                    current.toLocalDate() in publicHolidays -> DayCategory.PUBLIC_HOLIDAY
                    current.dayOfWeek == java.time.DayOfWeek.SATURDAY -> DayCategory.SATURDAY
                    current.dayOfWeek == java.time.DayOfWeek.SUNDAY -> DayCategory.SUNDAY
                    else -> DayCategory.WEEKDAY
                }
                val applicable = profile.rateRules.filter { rule ->
                    // A zero-valued absolute rate means no override; use the base rate.
                    !(rule.valueType == RateValueType.HOURLY_AMOUNT &&
                        rule.combination == RateCombination.REPLACE && rule.value <= 0.0) &&
                    (rule.category == null || rule.category == category) &&
                    (rule.weekday == null || rule.weekday == current.dayOfWeek) &&
                    (rule.shiftType == null || rule.shiftType == shift.shiftType) &&
                    matchesTimeWindow(current.toLocalTime(), rule.fromTime, rule.untilTime)
                }.sortedWith(compareByDescending<RateRule> { it.priority }.thenBy { it.id })
                val dayKey = java.time.temporal.WeekFields.ISO
                val week = current.get(dayKey.weekBasedYear()) to current.get(dayKey.weekOfWeekBasedYear())
                var used = 0.0
                while (used < segmentHours - 0.000001) {
                    val weekWorked = weeklyHours[week] ?: 0.0
                    val progress = elapsedShift + used
                    val activeOvertime = profile.overtimeRules.filter { rule ->
                        val consumed = if (rule.period == OvertimePeriod.SHIFT) progress else weekWorked
                        consumed >= rule.afterHours && (rule.untilHours == null || consumed < rule.untilHours)
                    }.maxWithOrNull(compareBy<OvertimeRule> { it.priority }.thenBy { it.id })
                    val nextThreshold = profile.overtimeRules.mapNotNull { rule ->
                        val consumed = if (rule.period == OvertimePeriod.SHIFT) progress else weekWorked
                        listOfNotNull(rule.afterHours, rule.untilHours)
                            .filter { it > consumed + 0.000001 }.minOrNull()?.minus(consumed)
                    }.minOrNull()
                    val slice = min(segmentHours - used, nextThreshold ?: (segmentHours - used))
                    var rate = profile.baseRate
                    val ids = mutableListOf<String>()
                    applicable.forEach { rule ->
                        if (rule.combination == RateCombination.REPLACE && ids.any { it.startsWith("replace:") }) return@forEach
                        rate = applyRate(rate, rule.value, rule.valueType, rule.combination)
                        ids += (if (rule.combination == RateCombination.REPLACE) "replace:" else "") + rule.id
                    }
                    if (activeOvertime != null) {
                        rate = applyRate(rate, activeOvertime.value, activeOvertime.valueType, activeOvertime.combination)
                        ids += activeOvertime.id
                    }
                    if (profile.employmentType == EmploymentType.CASUAL) rate *= 1.0 + profile.casualLoadingPercent / 100.0
                    val start = current.plusSeconds((used * 3600).toLong())
                    val finish = start.plusSeconds((slice * 3600).toLong())
                    lines += PayLine(start, finish, slice, rate, rate * slice, ids)
                    used += slice
                    weeklyHours[week] = weekWorked + slice
                }
                elapsedShift += segmentHours
                current = end
            }
            val baseGross = when (profile.basis) {
                AdvancedPayBasis.HOURLY -> lines.sumOf { it.gross }
                AdvancedPayBasis.DAILY -> profile.baseRate * hours / 24.0 // overridden below for daily shift-based pay
                AdvancedPayBasis.ANNUAL_SALARY -> 0.0
            }
            val shiftGross = if (profile.basis == AdvancedPayBasis.DAILY) profile.baseRate else baseGross
            val allowanceAmount = profile.allowances.sumOf { allowance ->
                when (allowance.frequency) {
                    AllowanceFrequency.PER_HOUR -> allowance.amount * hours
                    AllowanceFrequency.PER_SHIFT -> allowance.amount
                    AllowanceFrequency.PER_SWING ->
                        if (shift.swingId != null && seenSwings.add(allowance.id + ":" + shift.swingId)) allowance.amount else 0.0
                    else -> 0.0
                }
            }
            val pipPerShift = when (profile.pipType) {
                PipType.PER_HOUR -> profile.pipValue * hours
                PipType.PER_DAY -> profile.pipValue
                PipType.PERCENT_BASE ->
                    if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY) 0.0
                    else shiftGross * profile.pipValue / 100.0
                PipType.NONE, PipType.FIXED_AMOUNT -> 0.0
            }
            ShiftPayBreakdown(shift, if (profile.basis == AdvancedPayBasis.HOURLY) lines else emptyList(),
                shiftGross, allowanceAmount + pipPerShift, shiftGross + allowanceAmount + pipPerShift)
        }
        val fixed = profile.allowances.sumOf { allowance ->
            when (allowance.frequency) {
                AllowanceFrequency.FIXED -> allowance.amount
                AllowanceFrequency.WEEKLY -> {
                    val weeks = ordered.map {
                        val wf = java.time.temporal.WeekFields.ISO
                        it.start.get(wf.weekBasedYear()) to it.start.get(wf.weekOfWeekBasedYear())
                    }.toSet().size
                    allowance.amount * weeks
                }
                else -> 0.0
            }
        }
        val salary = if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY) profile.baseRate * annualSalaryPeriodFraction else 0.0
        val periodPip = when (profile.pipType) {
            PipType.FIXED_AMOUNT -> profile.pipValue
            PipType.PERCENT_BASE ->
                if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY) salary * profile.pipValue / 100.0 else 0.0
            else -> 0.0
        }
        val total = calculated.sumOf { it.totalGross } + fixed + salary + periodPip
        val totalHours = ordered.sumOf { ChronoUnit.MINUTES.between(it.start, it.end) / 60.0 }
        return PayBreakdown(calculated, fixed + salary + periodPip, totalHours, total, if (totalHours > 0) total / totalHours else 0.0)
    }

    private fun matchesTimeWindow(time: LocalTime, from: LocalTime?, until: LocalTime?): Boolean {
        if (from == null && until == null) return true
        if (from == null) return time.isBefore(until)
        if (until == null) return !time.isBefore(from)
        if (from == until) return true
        return if (from.isBefore(until)) {
            !time.isBefore(from) && time.isBefore(until)
        } else {
            !time.isBefore(from) || time.isBefore(until)
        }
    }

    private fun applyRate(base: Double, value: Double, type: RateValueType, combination: RateCombination): Double {
        require(value >= 0.0)
        val calculated = when (type) {
            RateValueType.HOURLY_AMOUNT -> value
            RateValueType.MULTIPLIER -> base * value
            RateValueType.ADDITIVE_PER_HOUR -> value
        }
        return if (combination == RateCombination.ADD) base + calculated else calculated
    }
}
