package com.fifokit.app.domain.roster

import com.fifokit.app.domain.model.RosterPattern
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RosterCalculatorTest {

    private val startDate = LocalDate.of(2026, 9, 1)

    @Test
    fun `2-1 roster calculates work and rest days correctly`() {
        val pattern = RosterPattern.TWO_ONE

        assertTrue(
            RosterCalculator.isWorkDay(
                LocalDate.of(2026, 9, 1),
                startDate,
                pattern
            )
        )

        assertTrue(
            RosterCalculator.isWorkDay(
                LocalDate.of(2026, 9, 14),
                startDate,
                pattern
            )
        )

        assertFalse(
            RosterCalculator.isWorkDay(
                LocalDate.of(2026, 9, 15),
                startDate,
                pattern
            )
        )

        assertFalse(
            RosterCalculator.isWorkDay(
                LocalDate.of(2026, 9, 21),
                startDate,
                pattern
            )
        )

        assertTrue(
            RosterCalculator.isWorkDay(
                LocalDate.of(2026, 9, 22),
                startDate,
                pattern
            )
        )
    }

    @Test
    fun `roster calculates dates before start date correctly`() {
        val pattern = RosterPattern.SEVEN_SEVEN

        assertFalse(
            RosterCalculator.isWorkDay(
                LocalDate.of(2026, 8, 31),
                startDate,
                pattern
            )
        )
    }
}