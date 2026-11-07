package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WesternAustraliaHolidaysTest {
    @Test
    fun includesObservedHolidays() {
        assertEquals("Anzac Day holiday",
            WesternAustraliaHolidays.onDate(2026, 4, 27)?.name)
        assertEquals("Christmas Day holiday",
            WesternAustraliaHolidays.onDate(2027, 12, 27)?.name)
        assertEquals("Boxing Day holiday",
            WesternAustraliaHolidays.onDate(2027, 12, 28)?.name)
    }

    @Test
    fun includesRevised2028And2029Dates() {
        assertEquals("Labour Day", WesternAustraliaHolidays.onDate(2028, 3, 13)?.name)
        assertEquals("Easter Saturday", WesternAustraliaHolidays.onDate(2028, 4, 15)?.name)
        assertEquals("Western Australia Day",
            WesternAustraliaHolidays.onDate(2029, 11, 12)?.name)
        assertNull(WesternAustraliaHolidays.onDate(2028, 9, 25))
    }

    @Test
    fun unknownYearsAndOrdinaryDaysAreUnmarked() {
        assertNull(WesternAustraliaHolidays.onDate(2026, 11, 7))
        assertNull(WesternAustraliaHolidays.onDate(2030, 1, 1))
    }
}
