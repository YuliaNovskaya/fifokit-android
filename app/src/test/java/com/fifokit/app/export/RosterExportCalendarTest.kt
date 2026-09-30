package com.fifokit.app.export

import com.fifokit.app.domain.roster.AustralianState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class RosterExportCalendarTest {

    private val data =
        RosterExportData(
            rosterName = "Site Roster",
            startDate =
                LocalDate.of(
                    2026,
                    9,
                    1
                ),
            workDays = 14,
            offDays = 7,
            selectedStates =
                setOf(
                    AustralianState.WA
                )
        )

    @Test
    fun `month export aligns Monday-first grid`() {
        val cells =
            RosterExportCalendar.monthDays(
                month =
                    YearMonth.of(
                        2026,
                        9
                    ),
                data = data
            )

        assertEquals(
            35,
            cells.size
        )

        assertEquals(
            null,
            cells.first()
        )

        assertEquals(
            LocalDate.of(
                2026,
                9,
                1
            ),
            cells[1]?.date
        )
    }

    @Test
    fun `month export uses roster and holiday logic`() {
        val days =
            RosterExportCalendar.monthDays(
                month =
                    YearMonth.of(
                        2026,
                        9
                    ),
                data = data
            ).filterNotNull()

        val restDay =
            days.first {
                it.date ==
                        LocalDate.of(
                            2026,
                            9,
                            15
                        )
            }

        val nextSwing =
            days.first {
                it.date ==
                        LocalDate.of(
                            2026,
                            9,
                            22
                        )
            }

        val publicHoliday =
            days.first {
                it.date ==
                        LocalDate.of(
                            2026,
                            9,
                            28
                        )
            }

        assertFalse(
            restDay.isWorkDay
        )

        assertTrue(
            nextSwing.isWorkDay
        )

        assertTrue(
            publicHoliday
                .isPublicHoliday
        )
    }
}
