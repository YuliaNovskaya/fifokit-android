package com.fifokit.shared.finance

import com.fifokit.shared.roster.CalendarMonth
import com.fifokit.shared.roster.RosterCalculator
import com.fifokit.shared.roster.RosterPattern
import com.fifokit.shared.roster.RosterStartDate

enum class PayRateType { HOURLY, DAILY, ANNUAL_SALARY }

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

/** Gross-pay calculation matching the existing Android basic pay calculator. */
object FinanceCalculator {
    fun grossPerWorkDay(input: PayInput): Double = when (input.rateType) {
        PayRateType.HOURLY -> input.rate * input.hoursPerWorkDay + input.allowancePerWorkDay
        PayRateType.DAILY -> input.rate + input.allowancePerWorkDay
        PayRateType.ANNUAL_SALARY -> input.rate
    }

    fun calculateAnnualEarnings(
        input: PayInput,
        pattern: RosterPattern,
        rosterStartDate: RosterStartDate,
        year: Int
    ): EarningsResult = calculateAnnualEarnings(
        input, rosterStartDate, pattern.workDays, pattern.offDays, year
    )

    fun calculateAnnualEarnings(
        input: PayInput,
        rosterStartDate: RosterStartDate,
        workDays: Int,
        offDays: Int,
        year: Int
    ): EarningsResult {
        require(input.rate.isFinite() && input.rate > 0.0)
        require(input.hoursPerWorkDay.isFinite() && input.hoursPerWorkDay > 0.0)
        require(input.allowancePerWorkDay.isFinite() && input.allowancePerWorkDay >= 0.0)
        require(year in 1..9999)

        val firstDayOffset = CalendarMonth(year, 1).daysFrom(
            rosterStartDate.year, rosterStartDate.month, rosterStartDate.day
        )
        val daysInYear = (1..12).sumOf { CalendarMonth(year, it).daysInMonth }
        val workDaysPerYear = (0 until daysInYear).count {
            RosterCalculator.isWorkDay(firstDayOffset + it, workDays, offDays)
        }
        val workHoursPerYear = workDaysPerYear * input.hoursPerWorkDay
        val base = when (input.rateType) {
            PayRateType.HOURLY -> input.rate * workHoursPerYear
            PayRateType.DAILY -> input.rate * workDaysPerYear
            PayRateType.ANNUAL_SALARY -> input.rate
        }
        val allowances = input.allowancePerWorkDay * workDaysPerYear
        return EarningsResult(
            workDaysPerYear = workDaysPerYear,
            restDaysPerYear = daysInYear - workDaysPerYear,
            workHoursPerYear = workHoursPerYear,
            baseEarnings = base,
            allowances = allowances,
            totalGrossEarnings = base + allowances
        )
    }
}
