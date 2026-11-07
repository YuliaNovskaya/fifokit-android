package com.fifokit.shared.roster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RosterCalculatorTest {

    @Test
    fun eachPatternRepeatsAtItsCycleBoundary() {
        for (pattern in RosterPattern.entries) {
            assertTrue(RosterCalculator.isWorkDay(0, pattern), pattern.label)
            assertTrue(RosterCalculator.isWorkDay((pattern.workDays - 1).toLong(), pattern), pattern.label)
            assertFalse(RosterCalculator.isWorkDay(pattern.workDays.toLong(), pattern), pattern.label)
            assertFalse(RosterCalculator.isWorkDay((pattern.cycleLength - 1).toLong(), pattern), pattern.label)
            assertTrue(RosterCalculator.isWorkDay(pattern.cycleLength.toLong(), pattern), pattern.label)
        }
    }

    @Test
    fun datesBeforeStartWrapToPreviousCycle() {
        for (pattern in RosterPattern.entries) {
            assertFalse(RosterCalculator.isWorkDay(-1, pattern), pattern.label)
            assertTrue(RosterCalculator.isWorkDay(-pattern.cycleLength.toLong(), pattern), pattern.label)
            assertEquals(
                RosterCalculator.isWorkDay(-1, pattern),
                RosterCalculator.isWorkDay((pattern.cycleLength - 1).toLong(), pattern)
            )
        }
    }

    @Test
    fun customWorkAndOffDaysAreSupported() {
        assertTrue(RosterCalculator.isWorkDay(0, 5, 2))
        assertTrue(RosterCalculator.isWorkDay(4, 5, 2))
        assertFalse(RosterCalculator.isWorkDay(5, 5, 2))
        assertFalse(RosterCalculator.isWorkDay(6, 5, 2))
        assertTrue(RosterCalculator.isWorkDay(7, 5, 2))
    }

    @Test
    fun invalidLengthsAreRejected() {
        assertFailsWith<IllegalArgumentException> { RosterCalculator.isWorkDay(0, 0, 7) }
        assertFailsWith<IllegalArgumentException> { RosterCalculator.isWorkDay(0, 7, 0) }
        assertFailsWith<IllegalArgumentException> { RosterCalculator.isWorkDay(0, -1, 7) }
    }
}
