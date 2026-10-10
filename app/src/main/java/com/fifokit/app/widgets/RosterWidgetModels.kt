package com.fifokit.app.widgets

import android.content.Context
import com.fifokit.app.data.RosterMigration
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.local.RosterDatabase
import com.fifokit.app.data.local.RosterEntity
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.PublicHolidayProvider
import com.fifokit.app.domain.roster.RosterCalculator
import com.fifokit.app.domain.roster.RosterScheduleCalculator
import com.fifokit.app.domain.roster.RosterScheduleCodec
import com.fifokit.app.domain.roster.RosterScheduleSegment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class WidgetRoster(
    val id: Long,
    val name: String,
    val startDate: LocalDate,
    val workDays: Int,
    val offDays: Int,
    val isCustomRoster: Boolean = false,
    val scheduleSegments:
        List<RosterScheduleSegment> =
        emptyList(),
    val isShutdownRoster: Boolean = false,
    val endDate: LocalDate? = null,
    val selectedStates: Set<AustralianState> =
        setOf(AustralianState.WA)
)

data class SwingStatus(
    val isWorkDay: Boolean,
    val dayInPeriod: Int,
    val periodLength: Int,
    val daysUntilTransition: Int,
    val nextTransitionDate: LocalDate
)

data class WidgetCalendarDay(
    val date: LocalDate,
    val isWorkDay: Boolean,
    val isToday: Boolean,
    val isPublicHoliday: Boolean
)

class RosterWidgetDataSource(
    context: Context
) {
    private val rosterPreferences =
        RosterPreferences(
            context.applicationContext
        )

    private val rosterRepository =
        RosterRepository(
            RosterDatabase
                .getInstance(
                    context.applicationContext
                )
                .rosterDao()
        )

    private val rosterMigration =
        RosterMigration(
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )

    suspend fun prepare() {
        rosterMigration
            .migrateLegacyRosterIfNeeded()
    }

    fun observeActiveRoster():
            Flow<WidgetRoster?> {

        return combine(
            rosterRepository.observeAllRosters(),
            rosterPreferences.activeRosterId,
            rosterPreferences.selectedStates
        ) { rosters, activeRosterId, selectedStateNames ->

            val roster =
                rosters.firstOrNull {
                    it.id == activeRosterId
                } ?: rosters.firstOrNull()

            roster?.toWidgetRoster(
                selectedStateNames
            )
        }.distinctUntilChanged()
    }

    private fun RosterEntity.toWidgetRoster(
        selectedStateNames: Set<String>
    ): WidgetRoster? {

        val parsedStartDate =
            runCatching {
                LocalDate.parse(startDate)
            }.getOrNull()
                ?: return null

        val resolvedWorkDays: Int
        val resolvedOffDays: Int

        if (isCustomRoster) {
            resolvedWorkDays =
                customWorkDays

            resolvedOffDays =
                customOffDays
        } else {
            val rosterPattern =
                runCatching {
                    RosterPattern.valueOf(
                        pattern
                    )
                }.getOrNull()
                    ?: return null

            resolvedWorkDays =
                rosterPattern.workDays

            resolvedOffDays =
                rosterPattern.offDays
        }

        if (
            resolvedWorkDays <= 0 ||
            resolvedOffDays <= 0
        ) {
            return null
        }

        val states =
            selectedStateNames
                .mapNotNull { stateName ->
                    runCatching {
                        AustralianState.valueOf(
                            stateName
                        )
                    }.getOrNull()
                }
                .toSet()
                .ifEmpty {
                    setOf(
                        AustralianState.WA
                    )
                }

        val decodedSegments =
            RosterScheduleCodec.decode(
                scheduleSegmentsJson
            )

        val resolvedSegments =
            if (
                isCustomRoster &&
                decodedSegments.isEmpty()
            ) {
                RosterScheduleCalculator
                    .legacyRepeatingSequence(
                        workDays =
                            customWorkDays,
                        offDays =
                            customOffDays
                    )
            } else {
                decodedSegments
            }

        return WidgetRoster(
            id = id,
            name = name,
            startDate = parsedStartDate,
            workDays = resolvedWorkDays,
            offDays = resolvedOffDays,
            isCustomRoster = isCustomRoster,
            scheduleSegments =
                resolvedSegments,
            isShutdownRoster = isShutdownRoster,
            endDate =
                endDate?.let {
                    runCatching {
                        LocalDate.parse(it)
                    }.getOrNull()
                },
            selectedStates = states
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

        val cycleLength =
            workDays + offDays

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
                positionInCycle -
                        workDays
            }

        val periodLength =
            if (isWorkDay) {
                workDays
            } else {
                offDays
            }

        val daysUntilTransition =
            periodLength -
                    positionInPeriod

        return SwingStatus(
            isWorkDay = isWorkDay,
            dayInPeriod =
                positionInPeriod + 1,
            periodLength =
                periodLength,
            daysUntilTransition =
                daysUntilTransition,
            nextTransitionDate =
                date.plusDays(
                    daysUntilTransition
                        .toLong()
                )
        )
    }

    fun calculate(
        date: LocalDate,
        startDate: LocalDate,
        segments:
            List<RosterScheduleSegment>,
        repeat: Boolean
    ): SwingStatus? {
        val status =
            RosterScheduleCalculator
                .statusOn(
                    date = date,
                    startDate =
                        startDate,
                    segments = segments,
                    repeat = repeat
                )
                ?: return null

        val daysUntilTransition =
            status.segment.days -
                    status.dayInSegment +
                    1

        return SwingStatus(
            isWorkDay =
                status.segment.isWork,
            dayInPeriod =
                status.dayInSegment,
            periodLength =
                status.segment.days,
            daysUntilTransition =
                daysUntilTransition,
            nextTransitionDate =
                date.plusDays(
                    daysUntilTransition
                        .toLong()
                )
        )
    }
}

object CompactRosterCalendarCalculator {

    fun monthCells(
        month: YearMonth,
        roster: WidgetRoster,
        today: LocalDate
    ): List<WidgetCalendarDay?> {

        val cells =
            mutableListOf<
                    WidgetCalendarDay?
                    >()

        repeat(
            month
                .atDay(1)
                .dayOfWeek
                .value - 1
        ) {
            cells += null
        }

        for (
            day in
            1..month.lengthOfMonth()
        ) {
            val date =
                month.atDay(day)

            cells +=
                WidgetCalendarDay(
                    date = date,
                    isWorkDay =
                        if (
                            roster.isCustomRoster &&
                            roster.scheduleSegments
                                .isNotEmpty()
                        ) {
                            RosterScheduleCalculator
                                .isWorkDay(
                                    date = date,
                                    startDate =
                                        roster.startDate,
                                    segments =
                                        roster.scheduleSegments,
                                    repeat =
                                        !roster.isShutdownRoster
                                )
                        } else {
                            RosterCalculator
                                .isWorkDay(
                                    date = date,
                                    startDate =
                                        roster.startDate,
                                    workDays =
                                        roster.workDays,
                                    offDays =
                                        roster.offDays
                                )
                        },
                    isToday =
                        date == today,
                    isPublicHoliday =
                        PublicHolidayProvider
                            .isPublicHoliday(
                                date = date,
                                states =
                                    roster
                                        .selectedStates
                            )
                )
        }

        while (
            cells.size % 7 != 0
        ) {
            cells += null
        }

        return cells
    }
}
