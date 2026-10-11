package com.fifokit.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class PayCalculationEngineTest {
    private fun shift(start: String, end: String) =
        PaidShift(LocalDateTime.parse(start), LocalDateTime.parse(end))

    @Test fun weekendBoundaryUsesActualDates() {
        val result = PayCalculationEngine.calculate(
            PayProfile(
                baseRate = 50.0,
                rateRules = listOf(
                    RateRule("sat", category = DayCategory.SATURDAY, value = 75.0),
                    RateRule("sun", category = DayCategory.SUNDAY, value = 100.0)
                )
            ),
            listOf(shift("2026-10-09T22:00:00", "2026-10-11T02:00:00"))
        )
        assertEquals(2100.0, result.totalGross, 0.001) // Fri 2h, Sat 24h, Sun 2h
        assertEquals(28.0, result.totalHours, 0.001)
    }

    @Test fun overtimeTierChangesAtThreshold() {
        val result = PayCalculationEngine.calculate(
            PayProfile(
                baseRate = 50.0,
                overtimeRules = listOf(
                    OvertimeRule("ot1", afterHours = 8.0, untilHours = 10.0, value = 1.5),
                    OvertimeRule("ot2", afterHours = 10.0, value = 2.0)
                )
            ),
            listOf(shift("2026-10-12T06:00:00", "2026-10-12T18:00:00"))
        )
        assertEquals(750.0, result.totalGross, 0.001)
    }

    @Test fun annualSalaryIsNotOvertimeByDefault() {
        val result = PayCalculationEngine.calculate(
            PayProfile(
                basis = AdvancedPayBasis.ANNUAL_SALARY,
                baseRate = 120000.0,
                overtimeRules = listOf(OvertimeRule("ot", afterHours = 8.0, value = 2.0))
            ),
            listOf(shift("2026-10-12T06:00:00", "2026-10-12T18:00:00")),
            annualSalaryPeriodFraction = 1.0 / 52.0
        )
        assertEquals(120000.0 / 52.0, result.totalGross, 0.001)
    }

    @Test fun advancedPipPerHourIncreasesEstimatedPay() {
        val result = PayCalculationEngine.calculate(
            PayProfile(baseRate = 50.0, pipType = PipType.PER_HOUR, pipValue = 5.0),
            listOf(shift("2026-10-12T07:00:00", "2026-10-12T19:00:00"))
        )
        assertEquals(660.0, result.totalGross, 0.001)
    }

    @Test fun salaryPercentagePipAppliesOncePerPeriod() {
        val result = PayCalculationEngine.calculate(
            PayProfile(basis = AdvancedPayBasis.ANNUAL_SALARY, baseRate = 120000.0,
                pipType = PipType.PERCENT_BASE, pipValue = 10.0),
            listOf(shift("2026-10-12T07:00:00", "2026-10-12T19:00:00")),
            annualSalaryPeriodFraction = 1.0 / 12.0
        )
        assertEquals(11000.0, result.totalGross, 0.001)
    }

    @Test fun optionalWeekendsNeedNoWeekendRules() {
        val result = PayCalculationEngine.calculate(
            PayProfile(baseRate = 40.0),
            listOf(shift("2026-10-12T08:00:00", "2026-10-12T16:00:00"))
        )
        assertEquals(320.0, result.totalGross, 0.001)
    }
}
