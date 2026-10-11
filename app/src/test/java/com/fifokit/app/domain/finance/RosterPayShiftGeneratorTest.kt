package com.fifokit.app.domain.finance

import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterScheduleSegment
import com.fifokit.app.domain.roster.RosterSegmentType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RosterPayShiftGeneratorTest {
    @Test fun customDayNightOffSequenceCreatesActualDatedShifts() {
        val start = LocalDate.of(2026, 10, 9)
        val shifts = RosterPayShiftGenerator.generate(
            from = start,
            until = start.plusDays(2),
            rosterStart = start,
            pattern = RosterPattern.entries.first(),
            isCustomRoster = true,
            isShutdownRoster = false,
            segments = listOf(
                RosterScheduleSegment(type = RosterSegmentType.DAY, days = 1),
                RosterScheduleSegment(type = RosterSegmentType.NIGHT, days = 1),
                RosterScheduleSegment(type = RosterSegmentType.OFF, days = 1)
            ),
            dayStart = LocalTime.of(7, 0),
            nightStart = LocalTime.of(19, 0),
            hoursPerShift = 12.0,
            defaultShiftType = PayShiftType.DAY
        )
        assertEquals(2, shifts.size)
        assertEquals(PayShiftType.DAY, shifts[0].shiftType)
        assertEquals(PayShiftType.NIGHT, shifts[1].shiftType)
        assertEquals(LocalDate.of(2026, 10, 11), shifts[1].end.toLocalDate())
    }
}
