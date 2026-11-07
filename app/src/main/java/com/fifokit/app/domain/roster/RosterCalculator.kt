package com.fifokit.app.domain.roster

import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.shared.roster.RosterCalculator as SharedRosterCalculator
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
        return SharedRosterCalculator.isWorkDay(
            daysFromStart = daysFromStart,
            workDays = workDays,
            offDays = offDays
        )
    }
}
