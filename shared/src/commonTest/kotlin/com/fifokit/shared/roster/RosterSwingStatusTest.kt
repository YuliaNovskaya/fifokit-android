package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterSwingStatusTest {
    @Test
    fun tracksWorkAndOffSwingBoundaries() {
        val pattern = RosterPattern.TWO_ONE
        assertEquals(RosterSwingStatus(true, 1, 14), rosterSwingStatus(0, pattern))
        assertEquals(RosterSwingStatus(true, 14, 14), rosterSwingStatus(13, pattern))
        assertEquals(RosterSwingStatus(false, 1, 7), rosterSwingStatus(14, pattern))
        assertEquals(RosterSwingStatus(false, 7, 7), rosterSwingStatus(20, pattern))
        assertEquals(RosterSwingStatus(true, 1, 14), rosterSwingStatus(21, pattern))
        assertEquals(RosterSwingStatus(false, 7, 7), rosterSwingStatus(-1, pattern))
    }
}
