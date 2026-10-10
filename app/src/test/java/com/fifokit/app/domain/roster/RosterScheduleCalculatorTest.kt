package com.fifokit.app.domain.roster

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RosterScheduleCalculatorTest {

    private val start =
        LocalDate.of(
            2026,
            1,
            1
        )

    @Test
    fun customSequenceRepeatsWholePattern() {
        val segments =
            listOf(
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.WORK,
                    days = 3
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.OFF,
                    days = 4
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.WORK,
                    days = 5
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.OFF,
                    days = 2
                )
            )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start,
                    start,
                    segments,
                    repeat = true
                )
        )

        assertFalse(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(3),
                    start,
                    segments,
                    repeat = true
                )
        )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(7),
                    start,
                    segments,
                    repeat = true
                )
        )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(14),
                    start,
                    segments,
                    repeat = true
                )
        )
    }

    @Test
    fun shutdownSequenceRunsOnceAndStops() {
        val segments =
            listOf(
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.WORK,
                    days = 7
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.OFF,
                    days = 2
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.WORK,
                    days = 4
                )
            )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(6),
                    start,
                    segments,
                    repeat = false
                )
        )

        assertFalse(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(7),
                    start,
                    segments,
                    repeat = false
                )
        )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(9),
                    start,
                    segments,
                    repeat = false
                )
        )

        assertNull(
            RosterScheduleCalculator
                .statusOn(
                    start.plusDays(13),
                    start,
                    segments,
                    repeat = false
                )
        )

        assertEquals(
            start.plusDays(12),
            RosterScheduleCalculator
                .endDate(
                    start,
                    segments
                )
        )
    }    @Test
    fun dayAndNightSegmentsAreBothWorkDays() {
        val segments =
            listOf(
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.DAY,
                    days = 7
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.NIGHT,
                    days = 7
                ),
                RosterScheduleSegment(
                    type =
                        RosterSegmentType.OFF,
                    days = 7
                )
            )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(2),
                    start,
                    segments,
                    repeat = true
                )
        )

        assertTrue(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(9),
                    start,
                    segments,
                    repeat = true
                )
        )

        assertFalse(
            RosterScheduleCalculator
                .isWorkDay(
                    start.plusDays(16),
                    start,
                    segments,
                    repeat = true
                )
        )
    }


}
