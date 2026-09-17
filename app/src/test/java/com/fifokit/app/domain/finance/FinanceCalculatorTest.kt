package com.fifokit.app.domain.finance

import com.fifokit.app.domain.model.RosterPattern
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate

class FinanceCalculatorTest {

    @Test
    fun hourlyPayCalculatesAnnualEarnings() {
        val input = PayInput(
            rateType = PayRateType.HOURLY,
            rate = 50.0,
            hoursPerWorkDay = 12.0,
            allowancePerWorkDay = 20.0
        )

        val result = FinanceCalculator.calculateAnnualEarnings(
            input = input,
            workDaysPerYear = 100
        )

        assertEquals(1200.0, result.workHoursPerYear, 0.001)
        assertEquals(60000.0, result.baseEarnings, 0.001)
        assertEquals(2000.0, result.allowances, 0.001)
        assertEquals(62000.0, result.totalGrossEarnings, 0.001)
    }

    @Test
    fun dailyRateCalculatesCorrectly() {
        val input = PayInput(
            rateType = PayRateType.DAILY,
            rate = 800.0,
            hoursPerWorkDay = 12.0
        )

        val result = FinanceCalculator.calculateAnnualEarnings(
            input = input,
            workDaysPerYear = 200
        )

        assertEquals(160000.0, result.baseEarnings, 0.001)
    }

    @Test
    fun annualSalaryRemainsFixed() {
        val input = PayInput(
            rateType = PayRateType.ANNUAL_SALARY,
            rate = 150000.0
        )

        val result = FinanceCalculator.calculateAnnualEarnings(
            input = input,
            workDaysPerYear = 200
        )

        assertEquals(150000.0, result.baseEarnings, 0.001)
    }

    @Test
    fun rosterPatternCalculatesActualWorkDays() {
        val result = FinanceCalculator.calculateAnnualEarnings(
            input = PayInput(
                rateType = PayRateType.DAILY,
                rate = 1.0
            ),
            pattern = RosterPattern.TWO_ONE,
            rosterStartDate = LocalDate.of(2026, 1, 1),
            year = 2026
        )

        assertEquals(246, result.workDaysPerYear)
        assertEquals(119, result.restDaysPerYear)
    }

    @Test
    fun financialGoalCalculatesRequiredContributions() {
        val goal = FinancialGoal(
            targetAmount = 10000.0,
            currentAmount = 2000.0,
            contributionPerPay = 1000.0,
            payFrequencyDays = 14
        )

        val result =
            FinanceCalculator.calculateFinancialGoal(goal)

        assertEquals(8000.0, result.amountRemaining, 0.001)
        assertEquals(8, result.contributionsRequired)
        assertEquals(112, result.daysRequired)
    }
    @Test
    fun customRosterCalculatesAnnualWorkDays() {
        val result = FinanceCalculator.calculateAnnualEarnings(
            input = PayInput(
                rateType = PayRateType.DAILY,
                rate = 1.0
            ),
            rosterStartDate = LocalDate.of(2026, 1, 1),
            workDays = 7,
            offDays = 7,
            year = 2026
        )

        assertEquals(183, result.workDaysPerYear)
        assertEquals(182, result.restDaysPerYear)
    }
}