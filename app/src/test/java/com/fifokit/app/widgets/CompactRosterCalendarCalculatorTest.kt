package com.fifokit.app.widgets

import com.fifokit.app.domain.roster.AustralianState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CompactRosterCalendarCalculatorTest {

    private val roster =
        WidgetRoster(
            id = 1L,
            name = "Site Roster",
            startDate = LocalDate.of(2026, 9, 1),
            workDays = 14,
            offDays = 7,
            selectedStates =
                setOf(AustralianState.WA)
        )

    @Test
    fun `month cells align to Monday-first grid`() {
        val cells =
            CompactRosterCalendarCalculator
                .monthCells(
                    month = YearMonth.of(2026, 9),
                    roster = roster,
                    today = LocalDate.of(2026, 9, 30)
                )

        assertEquals(35, cells.size)
        assertEquals(null, cells.first())
        assertEquals(
            LocalDate.of(2026, 9, 1),
            cells[1]?.date
        )
    }

    @Test
    fun `month cells mark roster today and holiday`() {
        val cells =
            CompactRosterCalendarCalculator
                .monthCells(
                    month = YearMonth.of(2026, 9),
                    roster = roster,
                    today = LocalDate.of(2026, 9, 30)
                )
                .filterNotNull()

        val restDay =
            cells.first {
                it.date == LocalDate.of(2026, 9, 15)
            }

        val workDay =
            cells.first {
                it.date == LocalDate.of(2026, 9, 22)
            }

        val holiday =
            cells.first {
                it.date == LocalDate.of(2026, 9, 28)
            }

        val today =
            cells.first {
                it.date == LocalDate.of(2026, 9, 30)
            }

        assertFalse(restDay.isWorkDay)
        assertTrue(workDay.isWorkDay)
        assertTrue(holiday.isPublicHoliday)
        assertTrue(today.isToday)
    }
}
