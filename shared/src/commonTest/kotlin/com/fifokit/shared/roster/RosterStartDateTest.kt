package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RosterStartDateTest {
    @Test
    fun parsesValidDate() {
        assertEquals(RosterStartDate(2026, 11, 1), RosterStartDate.parseIso("2026-11-01"))
        assertEquals(RosterStartDate(2028, 2, 29), RosterStartDate.parseIso("2028-02-29"))
    }

    @Test
    fun rejectsInvalidDate() {
        for (text in listOf("2026-02-29", "2026-13-01", "2026-11-31", "2026-1-01", "hello", "")) {
            assertNull(RosterStartDate.parseIso(text))
        }
    }
}
