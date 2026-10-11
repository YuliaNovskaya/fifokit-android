package com.fifokit.app.domain.finance

import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import com.fifokit.app.domain.roster.RosterScheduleCalculator
import com.fifokit.app.domain.roster.RosterScheduleSegment
import com.fifokit.app.domain.roster.RosterSegmentType
import java.time.LocalDate
import java.time.LocalTime

/** Converts the existing date-level roster into dated shifts. Times are explicit
 * calculation inputs until times are persisted in the roster model.
 */
object RosterPayShiftGenerator {
    fun generate(
        from: LocalDate,
        until: LocalDate,
        rosterStart: LocalDate,
        pattern: RosterPattern,
        isCustomRoster: Boolean,
        isShutdownRoster: Boolean,
        segments: List<RosterScheduleSegment>,
        dayStart: LocalTime,
        nightStart: LocalTime,
        hoursPerShift: Double,
        defaultShiftType: PayShiftType
    ): List<PaidShift> {
        require(!until.isBefore(from))
        require(hoursPerShift > 0.0 && hoursPerShift <= 24.0)
        val minutes = (hoursPerShift * 60.0).toLong()
        require(minutes > 0)
        return generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(until) }
            .mapNotNull { date ->
                val status = if (isCustomRoster || isShutdownRoster) {
                    RosterScheduleCalculator.statusOn(
                        date = date, startDate = rosterStart,
                        segments = segments, repeat = !isShutdownRoster
                    )
                } else null
                val works = if (isCustomRoster || isShutdownRoster)
                    status?.segment?.isWork == true
                else RosterCalculator.isWorkDay(date, rosterStart, pattern)
                if (!works) return@mapNotNull null
                val type = when (status?.segment?.type) {
                    RosterSegmentType.DAY -> PayShiftType.DAY
                    RosterSegmentType.NIGHT -> PayShiftType.NIGHT
                    else -> if (defaultShiftType == PayShiftType.NIGHT) PayShiftType.NIGHT else PayShiftType.DAY
                }
                val start = date.atTime(if (type == PayShiftType.NIGHT) nightStart else dayStart)
                PaidShift(start, start.plusMinutes(minutes), type)
            }.toList()
    }
}
