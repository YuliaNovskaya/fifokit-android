package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RosterDayDetailsTest {
    private val start = RosterStartDate(2026, 11, 10)

    @Test
    fun selectedDateReportsWorkOffAndPreStart() {
        val before = rosterDayDetails(RosterStartDate(2026, 11, 9), start, RosterPattern.TWO_ONE, HolidayRegion.WA)
        assertTrue(before.isBeforeStart)
        assertFalse(before.isWorkDay)

        val first = rosterDayDetails(RosterStartDate(2026, 11, 10), start, RosterPattern.TWO_ONE, HolidayRegion.WA,
            today = RosterStartDate(2026, 11, 10))
        assertTrue(first.isWorkDay)
        assertTrue(first.isToday)
        assertEquals(0L, first.daysFromStart)

        val off = rosterDayDetails(RosterStartDate(2026, 11, 24), start, RosterPattern.TWO_ONE, HolidayRegion.WA)
        assertFalse(off.isWorkDay)
        assertFalse(off.isBeforeStart)
        assertEquals(14L, off.daysFromStart)
    }

    @Test
    fun selectedDateIncludesHolidayNameForChosenRegion() {
        val nsw = rosterDayDetails(RosterStartDate(2026, 10, 5), start, RosterPattern.TWO_ONE, HolidayRegion.NSW)
        assertEquals("Labour Day", nsw.holidayName)
        val wa = rosterDayDetails(RosterStartDate(2026, 10, 5), start, RosterPattern.TWO_ONE, HolidayRegion.WA)
        assertNull(wa.holidayName)
    }
}
