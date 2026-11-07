package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarMonthTest {
    @Test
    fun november2026StartsOnSunday() {
        val month = CalendarMonth(2026, 11)
        assertEquals(30, month.daysInMonth)
        assertEquals(6, month.firstWeekday)
        assertEquals(0L, month.daysFrom(2026, 11, 1))
    }

    @Test
    fun navigationCrossesYearBoundary() {
        assertEquals(CalendarMonth(2027, 1), CalendarMonth(2026, 12).next())
        assertEquals(CalendarMonth(2026, 12), CalendarMonth(2027, 1).previous())
        assertEquals(30L, CalendarMonth(2026, 12).daysFrom(2026, 11, 1))
        assertEquals(-31L, CalendarMonth(2026, 10).daysFrom(2026, 11, 1))
    }

    @Test
    fun leapYearAndCenturyRules() {
        assertEquals(29, CalendarMonth(2028, 2).daysInMonth)
        assertEquals(28, CalendarMonth(2100, 2).daysInMonth)
        assertEquals(29, CalendarMonth(2000, 2).daysInMonth)
        assertEquals(CalendarMonth(2026, 11), CalendarMonth(2026, 11).previous().next())
    }

    @Test
    fun dayOffsetAcrossLeapFebruary() {
        assertEquals(29L, CalendarMonth(2028, 3).daysFrom(2028, 2, 1))
        assertEquals(28L, CalendarMonth(2027, 3).daysFrom(2027, 2, 1))
        assertEquals(3, CalendarMonth(1970, 1).firstWeekday)
    }
}
