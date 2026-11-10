package com.fifokit.shared.finance

import com.fifokit.shared.roster.RosterPattern
import com.fifokit.shared.roster.RosterStartDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FinanceCalculatorTest {
    private val start = RosterStartDate(2026, 1, 1)

    @Test
    fun hourlyGrossMatchesBasicAndroidFormula() {
        val input = PayInput(PayRateType.HOURLY, rate = 50.0, hoursPerWorkDay = 12.0, allowancePerWorkDay = 100.0)
        val result = FinanceCalculator.calculateAnnualEarnings(input, RosterPattern.SEVEN_SEVEN, start, 2026)
        assertEquals(700.0, FinanceCalculator.grossPerWorkDay(input))
        assertEquals(183, result.workDaysPerYear)
        assertEquals(182, result.restDaysPerYear)
        assertEquals(2196.0, result.workHoursPerYear)
        assertEquals(128100.0, result.totalGrossEarnings)
    }

    @Test
    fun salaryAddsRosterAllowances() {
        val input = PayInput(PayRateType.ANNUAL_SALARY, rate = 100000.0, allowancePerWorkDay = 20.0)
        val result = FinanceCalculator.calculateAnnualEarnings(input, RosterPattern.SEVEN_SEVEN, start, 2026)
        assertEquals(100000.0, FinanceCalculator.grossPerWorkDay(input))
        assertEquals(103660.0, result.totalGrossEarnings)
    }

    @Test
    fun annualCalculationHandlesLeapYearsAndCustomCycle() {
        val result = FinanceCalculator.calculateAnnualEarnings(
            PayInput(PayRateType.DAILY, rate = 500.0),
            rosterStartDate = RosterStartDate(2028, 1, 1),
            workDays = 7, offDays = 7, year = 2028
        )
        assertEquals(184, result.workDaysPerYear)
        assertEquals(182, result.restDaysPerYear)
        assertEquals(92000.0, result.totalGrossEarnings)
    }

    @Test
    fun negativeAllowanceIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            FinanceCalculator.calculateAnnualEarnings(
                PayInput(rate = 50.0, allowancePerWorkDay = -1.0),
                RosterPattern.TWO_ONE, start, 2026
            )
        }
    }
}
