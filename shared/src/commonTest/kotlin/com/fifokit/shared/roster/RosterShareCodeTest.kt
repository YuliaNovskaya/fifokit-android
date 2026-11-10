package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RosterShareCodeTest {
    @Test
    fun roundTripWithPunctuationAndUnicode() {
        val original = RosterShareData(
            "Júlia | Swing: 2/1",
            RosterStartDate(2027, 2, 28),
            RosterPattern.TWO_ONE,
            HolidayRegion.WA
        )
        assertEquals(original, RosterShareCode.decode(RosterShareCode.encode(original)))
    }

    @Test
    fun rejectsMalformedCodes() {
        assertNull(RosterShareCode.decode(""))
        assertNull(RosterShareCode.decode("FIFOKIT0:4:Test|2027-01-01|TWO_ONE|WA"))
        assertNull(RosterShareCode.decode("FIFOKIT1:4:Test|2027-02-30|TWO_ONE|WA"))
        assertNull(RosterShareCode.decode("FIFOKIT1:4:Test|2027-01-01|UNKNOWN|WA"))
        assertNull(RosterShareCode.decode("FIFOKIT1:4:Test|2027-01-01|TWO_ONE|UNKNOWN"))
        assertNull(RosterShareCode.decode("FIFOKIT1:9:Test|2027-01-01|TWO_ONE|WA"))
    }
}
