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
        return isWorkDay(
            date = date,
            startDate = startDate,
            workDays = pattern.workDays,
            offDays = pattern.offDays
        )
    }

    fun isWorkDay(
        date: LocalDate,
        startDate: LocalDate,
        workDays: Int,
        offDays: Int
    ): Boolean {
        val daysFromStart = ChronoUnit.DAYS.between(startDate, date)
        val cycleLength = workDays + offDays

        val positionInCycle =
            Math.floorMod(daysFromStart, cycleLength.toLong()).toInt()

        return positionInCycle < workDays
    }
}