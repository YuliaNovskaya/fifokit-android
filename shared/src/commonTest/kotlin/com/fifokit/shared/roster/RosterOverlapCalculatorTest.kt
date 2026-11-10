package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RosterOverlapCalculatorTest {
    @Test
    fun identicalSevenSevenPatternsHaveSharedWeekOff() {
        val start = RosterStartDate(2026, 11, 1)
        val result = RosterOverlapCalculator.calculate(
            fromDate = start,
            myStart = start,
            myPattern = RosterPattern.SEVEN_SEVEN,
            partnerStart = start,
            partnerPattern = RosterPattern.SEVEN_SEVEN,
            daysToCheck = 14
        )
        assertEquals(7, result.sharedOffDays)
        assertEquals(RosterStartDate(2026, 11, 8), result.nextSharedOffPeriod?.startDate)
        assertEquals(RosterStartDate(2026, 11, 14), result.nextSharedOffPeriod?.endDate)
        assertEquals(7, result.longestSharedOffPeriod?.days)
    }

    @Test
    fun oppositeSchedulesHaveNoSharedDaysOff() {
        val start = RosterStartDate(2026, 11, 1)
        val result = RosterOverlapCalculator.calculate(
            fromDate = start,
            myStart = start,
            myPattern = RosterPattern.SEVEN_SEVEN,
            partnerStart = RosterStartDate(2026, 10, 25),
            partnerPattern = RosterPattern.SEVEN_SEVEN,
            daysToCheck = 28
        )
        assertEquals(0, result.sharedOffDays)
        assertTrue(result.upcomingSharedOffPeriods.isEmpty())
    }

    @Test
    fun daysBeforeEitherRosterStartsAreNotSharedOff() {
        val start = RosterStartDate(2026, 11, 10)
        assertNull(RosterOverlapCalculator.isWorkDay(RosterStartDate(2026, 11, 9),
            start, RosterPattern.TWO_ONE))
        val result = RosterOverlapCalculator.calculate(
            fromDate = RosterStartDate(2026, 11, 1),
            myStart = start,
            myPattern = RosterPattern.SEVEN_SEVEN,
            partnerStart = start,
            partnerPattern = RosterPattern.SEVEN_SEVEN,
            daysToCheck = 9
        )
        assertEquals(0, result.sharedOffDays)
    }
}
