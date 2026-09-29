package com.fifokit.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SwingStatusCalculatorTest {

    private val startDate =
        LocalDate.of(2026, 9, 1)

    @Test
    fun `work swing reports current day and next transition`() {
        val status =
            SwingStatusCalculator.calculate(
                date = LocalDate.of(2026, 9, 14),
                startDate = startDate,
                workDays = 14,
                offDays = 7
            )

        assertTrue(status.isWorkDay)
        assertEquals(14, status.dayInPeriod)
        assertEquals(14, status.periodLength)
        assertEquals(1, status.daysUntilTransition)
        assertEquals(
            LocalDate.of(2026, 9, 15),
            status.nextTransitionDate
        )
    }

    @Test
    fun `rest period reports current day and next work date`() {
        val status =
            SwingStatusCalculator.calculate(
                date = LocalDate.of(2026, 9, 16),
                startDate = startDate,
                workDays = 14,
                offDays = 7
            )

        assertFalse(status.isWorkDay)
        assertEquals(2, status.dayInPeriod)
        assertEquals(7, status.periodLength)
        assertEquals(6, status.daysUntilTransition)
        assertEquals(
            LocalDate.of(2026, 9, 22),
            status.nextTransitionDate
        )
    }

    @Test
    fun `dates before roster start follow repeating cycle`() {
        val status =
            SwingStatusCalculator.calculate(
                date = LocalDate.of(2026, 8, 31),
                startDate = startDate,
                workDays = 7,
                offDays = 7
            )

        assertFalse(status.isWorkDay)
        assertEquals(7, status.dayInPeriod)
        assertEquals(1, status.daysUntilTransition)
        assertEquals(
            LocalDate.of(2026, 9, 1),
            status.nextTransitionDate
        )
    }
}
