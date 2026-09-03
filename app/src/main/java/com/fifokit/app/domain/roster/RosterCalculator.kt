package com.fifokit.app.domain.roster

import com.fifokit.app.domain.model.RosterPattern
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RosterCalculator {

    fun isWorkDay(
        date: LocalDate,
        startDate: LocalDate,
        pattern: RosterPattern
    ): Boolean {
        val daysFromStart = ChronoUnit.DAYS.between(startDate, date)

        val positionInCycle =
            Math.floorMod(daysFromStart, pattern.cycleLength.toLong()).toInt()

        return positionInCycle < pattern.workDays
    }
}