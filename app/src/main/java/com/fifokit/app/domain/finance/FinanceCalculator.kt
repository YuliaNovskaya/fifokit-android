package com.fifokit.app.domain.finance

import kotlin.math.ceil
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
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

        return EarningsResult(
            workDaysPerYear = workDaysPerYear,
            restDaysPerYear = 365 - workDaysPerYear,
            workHoursPerYear = workHoursPerYear,
            baseEarnings = baseEarnings,
            allowances = allowances,
            totalGrossEarnings = baseEarnings + allowances
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
        year: Int
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
            restDaysPerYear = totalDays - workDays
        )
    }

}