package com.fifokit.shared.roster

data class SharedOffPeriod(
    val startDate: RosterStartDate,
    val endDate: RosterStartDate,
    val days: Int
)

data class RosterOverlapSummary(
    val sharedOffDays: Int,
    val nextSharedOffPeriod: SharedOffPeriod?,
    val longestSharedOffPeriod: SharedOffPeriod?,
    val upcomingSharedOffPeriods: List<SharedOffPeriod>
)

/**
 * Finds consecutive days off within the given forward-looking window.
 * Days before either roster starts are excluded, not counted as days off.
 */
object RosterOverlapCalculator {
    fun isWorkDay(date: RosterStartDate, startDate: RosterStartDate, pattern: RosterPattern): Boolean? {
        val difference = (date.toEpochMillis() - startDate.toEpochMillis()) / 86_400_000L
        if (difference < 0L) return null
        return RosterCalculator.isWorkDay(difference, pattern)
    }

    fun calculate(
        fromDate: RosterStartDate,
        myStart: RosterStartDate,
        myPattern: RosterPattern,
        partnerStart: RosterStartDate,
        partnerPattern: RosterPattern,
        daysToCheck: Int = 90
    ): RosterOverlapSummary {
        require(daysToCheck in 1..366)
        val periods = mutableListOf<SharedOffPeriod>()
        var first: RosterStartDate? = null
        var previous: RosterStartDate? = null
        var length = 0
        var sharedOffDays = 0

        fun finishPeriod() {
            val start = first
            val end = previous
            if (start != null && end != null) {
                periods += SharedOffPeriod(start, end, length)
            }
            first = null
            previous = null
            length = 0
        }

        for (index in 0 until daysToCheck) {
            val date = RosterStartDate.fromEpochMillis(fromDate.toEpochMillis() + index * 86_400_000L)
            val myWork = isWorkDay(date, myStart, myPattern)
            val partnerWork = isWorkDay(date, partnerStart, partnerPattern)
            if (myWork == false && partnerWork == false) {
                sharedOffDays++
                if (first == null) first = date
                previous = date
                length++
            } else {
                finishPeriod()
            }
        }
        finishPeriod()

        return RosterOverlapSummary(
            sharedOffDays = sharedOffDays,
            nextSharedOffPeriod = periods.firstOrNull(),
            longestSharedOffPeriod = periods.maxByOrNull { it.days },
            upcomingSharedOffPeriods = periods.take(5)
        )
    }
}
