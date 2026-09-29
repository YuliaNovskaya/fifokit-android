package com.fifokit.app.widgets

import android.content.Context
import com.fifokit.app.data.RosterMigration
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.local.RosterDatabase
import com.fifokit.app.domain.model.RosterPattern
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class WidgetRoster(
    val id: Long,
    val name: String,
    val startDate: LocalDate,
    val workDays: Int,
    val offDays: Int
)

data class SwingStatus(
    val isWorkDay: Boolean,
    val dayInPeriod: Int,
    val periodLength: Int,
    val daysUntilTransition: Int,
    val nextTransitionDate: LocalDate
)

class RosterWidgetDataSource(
    context: Context
) {
    private val rosterPreferences =
        RosterPreferences(context.applicationContext)

    private val rosterRepository =
        RosterRepository(
            RosterDatabase
                .getInstance(context.applicationContext)
                .rosterDao()
        )

    private val rosterMigration =
        RosterMigration(
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )

    suspend fun loadActiveRoster(): WidgetRoster? {
        rosterMigration.migrateLegacyRosterIfNeeded()

        val activeRosterId =
            rosterPreferences.activeRosterId.first()

        var roster =
            activeRosterId?.let { id ->
                rosterRepository.getRosterById(id)
            }

        if (roster == null) {
            roster =
                rosterRepository
                    .getAllRosters()
                    .firstOrNull()

            if (roster != null) {
                rosterPreferences.setActiveRosterId(
                    roster.id
                )
            }
        }

        roster ?: return null

        val startDate =
            runCatching {
                LocalDate.parse(roster.startDate)
            }.getOrNull()
                ?: return null

        val workDays: Int
        val offDays: Int

        if (roster.isCustomRoster) {
            workDays = roster.customWorkDays
            offDays = roster.customOffDays
        } else {
            val pattern =
                runCatching {
                    RosterPattern.valueOf(roster.pattern)
                }.getOrNull()
                    ?: return null

            workDays = pattern.workDays
            offDays = pattern.offDays
        }

        if (workDays <= 0 || offDays <= 0) {
            return null
        }

        return WidgetRoster(
            id = roster.id,
            name = roster.name,
            startDate = startDate,
            workDays = workDays,
            offDays = offDays
        )
    }
}

object SwingStatusCalculator {

    fun calculate(
        date: LocalDate,
        startDate: LocalDate,
        workDays: Int,
        offDays: Int
    ): SwingStatus {
        require(workDays > 0)
        require(offDays > 0)

        val cycleLength = workDays + offDays
        val daysFromStart =
            ChronoUnit.DAYS.between(
                startDate,
                date
            )

        val positionInCycle =
            Math.floorMod(
                daysFromStart,
                cycleLength.toLong()
            ).toInt()

        val isWorkDay =
            positionInCycle < workDays

        val positionInPeriod =
            if (isWorkDay) {
                positionInCycle
            } else {
                positionInCycle - workDays
            }

        val periodLength =
            if (isWorkDay) {
                workDays
            } else {
                offDays
            }

        val daysUntilTransition =
            periodLength - positionInPeriod

        return SwingStatus(
            isWorkDay = isWorkDay,
            dayInPeriod = positionInPeriod + 1,
            periodLength = periodLength,
            daysUntilTransition = daysUntilTransition,
            nextTransitionDate =
                date.plusDays(
                    daysUntilTransition.toLong()
                )
        )
    }
}
