package com.fifokit.app.domain.sharing

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class FamilyPlanningSummary(
    val nextSharedOffPeriod: SharedOffPeriod?,
    val longestSharedOffPeriod: SharedOffPeriod?,
    val nextSharedWeekend: SharedOffPeriod?,
    val upcomingSharedOffPeriods: List<SharedOffPeriod>,
    val sharedOffDaysThisMonth: Int,
    val sharedOffDaysNext3Months: Int
)

object FamilyPlanningCalculator {

    fun calculate(
        fromDate: LocalDate = LocalDate.now(),
        myIsWorkDay: (LocalDate) -> Boolean,
        partnerIsWorkDay: (LocalDate) -> Boolean
    ): FamilyPlanningSummary {

        val threeMonthsEnd =
            fromDate.plusMonths(3)

        val daysToCheck =
            ChronoUnit.DAYS
                .between(
                    fromDate,
                    threeMonthsEnd
                )
                .toInt()

        val periods3Months =
            RosterOverlapCalculator.findSharedOffPeriods(
                fromDate = fromDate,
                daysToCheck = daysToCheck,
                myIsWorkDay = myIsWorkDay,
                partnerIsWorkDay = partnerIsWorkDay
            )

        val monthStart =
            fromDate.withDayOfMonth(1)

        val monthEnd =
            monthStart.plusMonths(1)

        val sharedOffDaysThisMonth =
            generateSequence(monthStart) {
                it.plusDays(1)
            }
                .takeWhile {
                    it.isBefore(monthEnd)
                }
                .count { date ->
                    !myIsWorkDay(date) &&
                            !partnerIsWorkDay(date)
                }

        val sharedOffDaysNext3Months =
            (0 until daysToCheck).count { offset ->

                val date =
                    fromDate.plusDays(
                        offset.toLong()
                    )

                !myIsWorkDay(date) &&
                        !partnerIsWorkDay(date)
            }

        val nextSharedWeekend =
            findNextSharedWeekend(
                fromDate = fromDate,
                myIsWorkDay = myIsWorkDay,
                partnerIsWorkDay = partnerIsWorkDay
            )

        val longestSharedOffPeriod =
            periods3Months.maxByOrNull {
                it.days
            }

        return FamilyPlanningSummary(
            nextSharedOffPeriod =
                periods3Months.firstOrNull(),
            longestSharedOffPeriod =
                longestSharedOffPeriod,
            nextSharedWeekend =
                nextSharedWeekend,
            upcomingSharedOffPeriods =
                periods3Months.take(5),
            sharedOffDaysThisMonth =
                sharedOffDaysThisMonth,
            sharedOffDaysNext3Months =
                sharedOffDaysNext3Months
        )
    }

    private fun findNextSharedWeekend(
        fromDate: LocalDate,
        myIsWorkDay: (LocalDate) -> Boolean,
        partnerIsWorkDay: (LocalDate) -> Boolean
    ): SharedOffPeriod? {

        var date = fromDate

        repeat(100) {

            if (date.dayOfWeek == DayOfWeek.SATURDAY) {

                val sunday =
                    date.plusDays(1)

                val bothOffSaturday =
                    !myIsWorkDay(date) &&
                            !partnerIsWorkDay(date)

                val bothOffSunday =
                    !myIsWorkDay(sunday) &&
                            !partnerIsWorkDay(sunday)

                if (
                    bothOffSaturday &&
                    bothOffSunday
                ) {
                    return SharedOffPeriod(
                        startDate = date,
                        endDate = sunday
                    )
                }
            }

            date = date.plusDays(1)
        }

        return null
    }
}