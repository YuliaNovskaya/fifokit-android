package com.fifokit.app.export

import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.PublicHolidayProvider
import com.fifokit.app.domain.roster.RosterCalculator
import java.time.LocalDate
import java.time.YearMonth

data class RosterExportData(
    val rosterName: String,
    val startDate: LocalDate,
    val workDays: Int,
    val offDays: Int,
    val selectedStates: Set<AustralianState>
)

data class RosterExportDay(
    val date: LocalDate,
    val isWorkDay: Boolean,
    val isPublicHoliday: Boolean
)

object RosterExportCalendar {

    fun monthDays(
        month: YearMonth,
        data: RosterExportData
    ): List<RosterExportDay?> {

        val cells =
            mutableListOf<RosterExportDay?>()

        repeat(
            month.atDay(1).dayOfWeek.value - 1
        ) {
            cells += null
        }

        for (day in 1..month.lengthOfMonth()) {
            val date = month.atDay(day)

            cells += RosterExportDay(
                date = date,
                isWorkDay =
                    RosterCalculator.isWorkDay(
                        date = date,
                        startDate = data.startDate,
                        workDays = data.workDays,
                        offDays = data.offDays
                    ),
                isPublicHoliday =
                    PublicHolidayProvider.isPublicHoliday(
                        date = date,
                        states = data.selectedStates
                    )
            )
        }

        while (cells.size % 7 != 0) {
            cells += null
        }

        return cells
    }
}
