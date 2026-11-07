package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse

class HolidayRegionTest {
    @Test
    fun waAndNswHaveDifferentHolidayDates() {
        assertEquals("Labour Day", HolidayRegion.WA.holidayOn(2026, 3, 2)?.name)
        assertNull(HolidayRegion.NSW.holidayOn(2026, 3, 2))
        assertEquals("Labour Day", HolidayRegion.NSW.holidayOn(2026, 10, 5)?.name)
        assertEquals("Easter Saturday", HolidayRegion.NSW.holidayOn(2027, 3, 27)?.name)
    }

    @Test
    fun coverageIsExplicit() {
        assertFalse(HolidayRegion.NSW.hasCoverageFor(2028))
        assertEquals(true, HolidayRegion.WA.hasCoverageFor(2028))
    }
}
