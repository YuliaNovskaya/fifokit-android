package com.fifokit.app.domain.sharing

import java.time.LocalDate

data class SharedOffPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    val days: Long
        get() = java.time.temporal.ChronoUnit.DAYS
            .between(startDate, endDate) + 1
}

object RosterOverlapCalculator {

    fun findSharedOffPeriods(
        fromDate: LocalDate,
        daysToCheck: Int = 90,
        myIsWorkDay: (LocalDate) -> Boolean,
        partnerIsWorkDay: (LocalDate) -> Boolean
    ): List<SharedOffPeriod> {

        val sharedOffDates =
            (0 until daysToCheck)
                .map { fromDate.plusDays(it.toLong()) }
                .filter { date ->
                    !myIsWorkDay(date) &&
                            !partnerIsWorkDay(date)
                }

        if (sharedOffDates.isEmpty()) {
            return emptyList()
        }

        val periods =
            mutableListOf<SharedOffPeriod>()

        var start = sharedOffDates.first()
        var previous = start

        sharedOffDates
            .drop(1)
            .forEach { date ->

                if (date == previous.plusDays(1)) {
                    previous = date
                } else {
                    periods +=
                        SharedOffPeriod(
                            startDate = start,
                            endDate = previous
                        )

                    start = date
                    previous = date
                }
            }

        periods +=
            SharedOffPeriod(
                startDate = start,
                endDate = previous
            )

        return periods
    }
}