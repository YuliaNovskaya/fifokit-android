package com.fifokit.app.domain.finance

import kotlin.math.ceil
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.PublicHolidayProvider
import java.time.LocalDate

object FinanceCalculator {

    fun calculateAnnualEarnings(
        input: PayInput,
        workDaysPerYear: Int
    ): EarningsResult {

        val workHoursPerYear =
            workDaysPerYear * input.hoursPerWorkDay

        val baseEarnings = when (input.rateType) {
            PayRateType.HOURLY ->
                input.rate * workHoursPerYear

            PayRateType.DAILY ->
                input.rate * workDaysPerYear

            PayRateType.ANNUAL_SALARY ->
                input.rate
        }

        val allowances =
            input.allowancePerWorkDay * workDaysPerYear

        val equivalentHourlyRate =
            when (input.rateType) {
                PayRateType.DAILY ->
                    if (input.hoursPerWorkDay > 0.0) {
                        input.rate / input.hoursPerWorkDay
                    } else {
                        null
                    }

                PayRateType.ANNUAL_SALARY ->
                    if (workHoursPerYear > 0.0) {
                        input.rate / workHoursPerYear
                    } else {
                        null
                    }

                PayRateType.HOURLY ->
                    null
            }

        return EarningsResult(
            workDaysPerYear = workDaysPerYear,
            restDaysPerYear = 365 - workDaysPerYear,
            workHoursPerYear = workHoursPerYear,
            baseEarnings = baseEarnings,
            allowances = allowances,
            totalGrossEarnings = baseEarnings + allowances,
            equivalentHourlyRate = equivalentHourlyRate
        )
    }

    fun calculateFinancialGoal(
        goal: FinancialGoal
    ): FinancialGoalResult {

        val remaining =
            (goal.targetAmount - goal.currentAmount)
                .coerceAtLeast(0.0)

        if (remaining == 0.0 || goal.contributionPerPay <= 0.0) {
            return FinancialGoalResult(
                amountRemaining = remaining,
                contributionsRequired = 0,
                daysRequired = 0
            )
        }

        val contributionsRequired =
            ceil(remaining / goal.contributionPerPay).toInt()

        return FinancialGoalResult(
            amountRemaining = remaining,
            contributionsRequired = contributionsRequired,
            daysRequired = contributionsRequired * goal.payFrequencyDays
        )
    }
    fun calculateAnnualEarnings(
        input: PayInput,
        pattern: RosterPattern,
        rosterStartDate: LocalDate,
        year: Int,
        selectedStates: Set<AustralianState> = emptySet()
    ): EarningsResult {

        val firstDay = LocalDate.of(year, 1, 1)
        val lastDay = LocalDate.of(year, 12, 31)

        var workDays = 0
        var totalDays = 0

        var date = firstDay

        while (!date.isAfter(lastDay)) {
            if (
                RosterCalculator.isWorkDay(
                    date = date,
                    startDate = rosterStartDate,
                    pattern = pattern
                )
            ) {
                workDays++
            }

            totalDays++
            date = date.plusDays(1)
        }

        val result = calculateAnnualEarnings(
            input = input,
            workDaysPerYear = workDays
        )

        return result.copy(
            restDaysPerYear = totalDays - workDays,
            publicHolidaysWorked =
                countPublicHolidaysWorked(
                    year = year,
                    states = selectedStates
                ) { holidayDate ->
                    RosterCalculator.isWorkDay(
                        date = holidayDate,
                        startDate = rosterStartDate,
                        pattern = pattern
                    )
                }
        )
    }

    fun calculateAnnualEarnings(
        input: PayInput,
        rosterStartDate: LocalDate,
        workDays: Int,
        offDays: Int,
        year: Int,
        selectedStates: Set<AustralianState> = emptySet()
    ): EarningsResult {

        val firstDay = LocalDate.of(year, 1, 1)
        val lastDay = LocalDate.of(year, 12, 31)

        var workDaysInYear = 0
        var totalDays = 0
        var date = firstDay

        while (!date.isAfter(lastDay)) {

            if (
                RosterCalculator.isWorkDay(
                    date = date,
                    startDate = rosterStartDate,
                    workDays = workDays,
                    offDays = offDays
                )
            ) {
                workDaysInYear++
            }

            totalDays++
            date = date.plusDays(1)
        }

        return calculateAnnualEarnings(
            input = input,
            workDaysPerYear = workDaysInYear
        ).copy(
            restDaysPerYear = totalDays - workDaysInYear,
            publicHolidaysWorked =
                countPublicHolidaysWorked(
                    year = year,
                    states = selectedStates
                ) { holidayDate ->
                    RosterCalculator.isWorkDay(
                        date = holidayDate,
                        startDate = rosterStartDate,
                        workDays = workDays,
                        offDays = offDays
                    )
                }
        )
    }

    private fun countPublicHolidaysWorked(
        year: Int,
        states: Set<AustralianState>,
        isWorkDay: (LocalDate) -> Boolean
    ): Int {
        if (states.isEmpty()) {
            return 0
        }

        return PublicHolidayProvider
            .holidaysFor(
                year = year,
                states = states
            )
            .map { it.date }
            .distinct()
            .count(isWorkDay)
    }

}