package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtherStateHolidaysTest {
    @Test
    fun eachAdditionalRegionHasAStatewideHoliday() {
        assertEquals("Friday before AFL Grand Final", HolidayRegion.VIC.holidayOn(2026, 9, 25)?.name)
        assertEquals("Anzac Day", HolidayRegion.QLD.holidayOn(2027, 4, 26)?.name)
        assertEquals("Adelaide Cup Day", HolidayRegion.SA.holidayOn(2027, 3, 8)?.name)
        assertEquals("Eight Hours Day", HolidayRegion.TAS.holidayOn(2026, 3, 9)?.name)
        assertEquals("Reconciliation Day", HolidayRegion.ACT.holidayOn(2027, 5, 31)?.name)
        assertEquals("Picnic Day", HolidayRegion.NT.holidayOn(2026, 8, 3)?.name)
    }

    @Test
    fun calendarCoverageAndRegionalExceptionsAreExplicit() {
        for (region in listOf(HolidayRegion.VIC, HolidayRegion.QLD, HolidayRegion.SA,
                HolidayRegion.TAS, HolidayRegion.ACT, HolidayRegion.NT)) {
            assertTrue(region.hasCoverageFor(2026))
            assertTrue(region.hasCoverageFor(2027))
            assertFalse(region.hasCoverageFor(2028))
        }
        assertNull(HolidayRegion.QLD.holidayOn(2026, 8, 12)) // Brisbane-only Ekka
        assertNull(HolidayRegion.TAS.holidayOn(2026, 2, 9)) // Regional Regatta Day
        assertNull(HolidayRegion.VIC.holidayOn(2027, 9, 24)) // AFL date unconfirmed
    }
}
