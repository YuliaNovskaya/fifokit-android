package com.fifokit.app.domain.finance

import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.AustralianState
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
        assertEquals(
            66.6667,
            result.equivalentHourlyRate ?: 0.0,
            0.001
        )
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
        assertEquals(
            62.5,
            result.equivalentHourlyRate ?: 0.0,
            0.001
        )
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
    fun publicHolidaysWorkedFollowActiveRoster() {
        val result =
            FinanceCalculator.calculateAnnualEarnings(
                input = PayInput(
                    rateType = PayRateType.DAILY,
                    rate = 1.0
                ),
                pattern = RosterPattern.TWO_ONE,
                rosterStartDate = LocalDate.of(
                    2026,
                    1,
                    1
                ),
                year = 2026,
                selectedStates =
                    setOf(
                        AustralianState.WA
                    )
            )

        assertEquals(
            11,
            result.publicHolidaysWorked
        )
    }

    @Test
    fun hourlyPipIsAddedToAnnualEarnings() {
        val result =
            FinanceCalculator.calculateAnnualEarnings(
                input = PayInput(
                    rateType = PayRateType.HOURLY,
                    rate = 50.0,
                    hoursPerWorkDay = 10.0,
                    pipType = PipType.PER_HOUR,
                    pipValue = 5.0
                ),
                workDaysPerYear = 100
            )

        assertEquals(
            5000.0,
            result.pipEarnings,
            0.001
        )

        assertEquals(
            55000.0,
            result.totalGrossEarnings,
            0.001
        )
    }

    @Test
    fun dailyPipIsAddedToAnnualEarnings() {
        val result =
            FinanceCalculator.calculateAnnualEarnings(
                input = PayInput(
                    rateType = PayRateType.DAILY,
                    rate = 800.0,
                    hoursPerWorkDay = 12.0,
                    pipType = PipType.PER_DAY,
                    pipValue = 50.0
                ),
                workDaysPerYear = 100
            )

        assertEquals(
            5000.0,
            result.pipEarnings,
            0.001
        )

        assertEquals(
            85000.0,
            result.totalGrossEarnings,
            0.001
        )
    }

    @Test
    fun percentagePipUsesBaseEarnings() {
        val result =
            FinanceCalculator.calculateAnnualEarnings(
                input = PayInput(
                    rateType = PayRateType.ANNUAL_SALARY,
                    rate = 150000.0,
                    pipType = PipType.PERCENT_BASE,
                    pipValue = 10.0
                ),
                workDaysPerYear = 200
            )

        assertEquals(
            15000.0,
            result.pipEarnings,
            0.001
        )

        assertEquals(
            165000.0,
            result.totalGrossEarnings,
            0.001
        )
    }

    @Test
    fun fixedPipIsAddedOnce() {
        val result =
            FinanceCalculator.calculateAnnualEarnings(
                input = PayInput(
                    rateType = PayRateType.ANNUAL_SALARY,
                    rate = 150000.0,
                    pipType = PipType.FIXED_AMOUNT,
                    pipValue = 12000.0
                ),
                workDaysPerYear = 200
            )

        assertEquals(
            12000.0,
            result.pipEarnings,
            0.001
        )

        assertEquals(
            162000.0,
            result.totalGrossEarnings,
            0.001
        )
    }

    @Test
    fun localWeekdayAndSaturdayRatesCalculateCorrectly() {
        val result =
            FinanceCalculator.calculateLocalPay(
                weekdayHourlyRate = 50.0,
                saturdayHourlyRate = 60.0,
                hoursPerDay = 8.0
            )

        assertEquals(
            2000.0,
            result.weekdayGrossPerWeek,
            0.001
        )

        assertEquals(
            480.0,
            result.saturdayGrossPerWeek,
            0.001
        )

        assertEquals(
            2480.0,
            result.weeklyGross,
            0.001
        )

        assertEquals(
            128960.0,
            result.annualisedGross,
            0.001
        )
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
    @Test
    fun annualSalaryIncludesRosterBasedAllowances() {
        val result = FinanceCalculator.calculateAnnualEarnings(
            input = PayInput(
                rateType = PayRateType.ANNUAL_SALARY,
                rate = 150000.0,
                hoursPerWorkDay = 12.0,
                allowancePerWorkDay = 20.0
            ),
            rosterStartDate = LocalDate.of(2026, 1, 1),
            workDays = 7,
            offDays = 7,
            year = 2026
        )

        assertEquals(183, result.workDaysPerYear)
        assertEquals(3660.0, result.allowances, 0.001)
        assertEquals(153660.0, result.totalGrossEarnings, 0.001)
    }
    @Test
    fun completedFinancialGoalRequiresNoMoreContributions() {
        val result = FinanceCalculator.calculateFinancialGoal(
            FinancialGoal(
                targetAmount = 10000.0,
                currentAmount = 12000.0,
                contributionPerPay = 1000.0,
                payFrequencyDays = 14
            )
        )

        assertEquals(0.0, result.amountRemaining, 0.001)
        assertEquals(0, result.contributionsRequired)
        assertEquals(0, result.daysRequired)
    }

}