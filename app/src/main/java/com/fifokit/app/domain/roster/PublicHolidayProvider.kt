package com.fifokit.app.domain.roster

import java.time.LocalDate

data class PublicHoliday(
    val date: LocalDate,
    val name: String
)

object PublicHolidayProvider {

    fun holidaysFor(
        year: Int,
        state: AustralianState
    ): List<PublicHoliday> {
        return when (year) {
            2026 -> holidays2026(state)
            2027 -> holidays2027(state)
            else -> emptyList()
        }
    }

    fun holidayOn(
        date: LocalDate,
        state: AustralianState
    ): PublicHoliday? {
        return holidaysFor(date.year, state)
            .firstOrNull { it.date == date }
    }

    private fun holiday(
        month: Int,
        day: Int,
        name: String,
        year: Int = 2026
    ) = PublicHoliday(
        date = LocalDate.of(year, month, day),
        name = name
    )

    fun holidaysFor(
        year: Int,
        states: Set<AustralianState>
    ): List<PublicHoliday> {
        return states
            .flatMap { state ->
                holidaysFor(year, state)
            }
            .distinctBy { holiday ->
                holiday.date to holiday.name
            }
            .sortedBy { it.date }
    }

    fun holidaysOn(
        date: LocalDate,
        states: Set<AustralianState>
    ): List<PublicHoliday> {
        return holidaysFor(date.year, states)
            .filter { it.date == date }
    }

    fun isPublicHoliday(
        date: LocalDate,
        states: Set<AustralianState>
    ): Boolean {
        return holidaysOn(date, states).isNotEmpty()
    }

    private fun holidays2026(state: AustralianState): List<PublicHoliday> {

        val common = listOf(
            holiday(1, 1, "New Year's Day"),
            holiday(1, 26, "Australia Day"),
            holiday(4, 3, "Good Friday"),
            holiday(4, 6, "Easter Monday"),
            holiday(4, 25, "Anzac Day"),
            holiday(12, 25, "Christmas Day")
        )

        val stateSpecific = when (state) {

            AustralianState.WA -> listOf(
                holiday(3, 2, "Labour Day"),
                holiday(4, 5, "Easter Sunday"),
                holiday(4, 27, "Anzac Day Holiday"),
                holiday(6, 1, "Western Australia Day"),
                holiday(9, 28, "King's Birthday"),
                holiday(12, 26, "Boxing Day"),
                holiday(12, 28, "Boxing Day Holiday")
            )

            AustralianState.NSW -> listOf(
                holiday(4, 4, "Easter Saturday"),
                holiday(4, 5, "Easter Sunday"),
                holiday(4, 27, "Anzac Day Holiday"),
                holiday(6, 8, "King's Birthday"),
                holiday(10, 5, "Labour Day"),
                holiday(12, 26, "Boxing Day"),
                holiday(12, 28, "Boxing Day Holiday")
            )

            AustralianState.VIC -> listOf(
                holiday(3, 9, "Labour Day"),
                holiday(4, 4, "Saturday before Easter Sunday"),
                holiday(4, 5, "Easter Sunday"),
                holiday(6, 8, "King's Birthday"),
                holiday(9, 25, "Friday before AFL Grand Final"),
                holiday(11, 3, "Melbourne Cup Day"),
                holiday(12, 26, "Boxing Day"),
                holiday(12, 28, "Boxing Day Holiday")
            )

            AustralianState.QLD -> listOf(
                holiday(4, 4, "Day after Good Friday"),
                holiday(4, 5, "Easter Sunday"),
                holiday(5, 4, "Labour Day"),
                holiday(10, 5, "King's Birthday"),
                holiday(12, 26, "Boxing Day"),
                holiday(12, 28, "Boxing Day Holiday")
            )

            AustralianState.SA -> listOf(
                holiday(3, 9, "Adelaide Cup Day"),
                holiday(4, 4, "Easter Saturday"),
                holiday(4, 5, "Easter Sunday"),
                holiday(6, 8, "King's Birthday"),
                holiday(10, 5, "Labour Day"),
                holiday(12, 26, "Proclamation Day"),
                holiday(12, 28, "Proclamation Day Holiday")
            )

            AustralianState.TAS -> listOf(
                holiday(3, 9, "Eight Hours Day"),
                holiday(6, 8, "King's Birthday"),
                holiday(12, 28, "Boxing Day")
            )

            AustralianState.ACT -> listOf(
                holiday(3, 9, "Canberra Day"),
                holiday(4, 4, "Easter Saturday"),
                holiday(4, 5, "Easter Sunday"),
                holiday(4, 27, "Anzac Day Holiday"),
                holiday(6, 1, "Reconciliation Day"),
                holiday(6, 8, "King's Birthday"),
                holiday(10, 5, "Labour Day"),
                holiday(12, 26, "Boxing Day"),
                holiday(12, 28, "Boxing Day Holiday")
            )

            AustralianState.NT -> listOf(
                holiday(4, 4, "Easter Saturday"),
                holiday(4, 5, "Easter Sunday"),
                holiday(5, 4, "May Day"),
                holiday(6, 8, "King's Birthday"),
                holiday(8, 3, "Picnic Day"),
                holiday(12, 26, "Boxing Day"),
                holiday(12, 28, "Boxing Day Holiday")
            )
        }

        return (common + stateSpecific).sortedBy { it.date }
    }

    private fun holidays2027(state: AustralianState): List<PublicHoliday> {

        fun h(month: Int, day: Int, name: String) =
            holiday(month, day, name, year = 2027)

        val common = listOf(
            h(1, 1, "New Year's Day"),
            h(1, 26, "Australia Day"),
            h(3, 26, "Good Friday"),
            h(3, 29, "Easter Monday"),
            h(12, 25, "Christmas Day")
        )

        val stateSpecific = when (state) {

            AustralianState.WA -> listOf(
                h(3, 1, "Labour Day"),
                h(3, 28, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(4, 26, "Anzac Day Holiday"),
                h(6, 7, "Western Australia Day"),
                h(9, 27, "King's Birthday"),
                h(12, 26, "Boxing Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day Holiday")
            )

            AustralianState.NSW -> listOf(
                h(3, 27, "Easter Saturday"),
                h(3, 28, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(4, 26, "Anzac Day Holiday"),
                h(6, 14, "King's Birthday"),
                h(10, 4, "Labour Day"),
                h(12, 26, "Boxing Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day Holiday")
            )

            AustralianState.VIC -> listOf(
                h(3, 8, "Labour Day"),
                h(3, 27, "Saturday before Easter Sunday"),
                h(3, 28, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(6, 14, "King's Birthday"),
                h(11, 2, "Melbourne Cup Day"),
                h(12, 26, "Boxing Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day Holiday")
            )

            AustralianState.QLD -> listOf(
                h(3, 27, "Day after Good Friday"),
                h(3, 28, "Easter Sunday"),
                h(4, 26, "Anzac Day"),
                h(5, 3, "Labour Day"),
                h(10, 4, "King's Birthday"),
                h(12, 26, "Boxing Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day Holiday")
            )

            AustralianState.SA -> listOf(
                h(3, 8, "Adelaide Cup Day"),
                h(3, 27, "Easter Saturday"),
                h(3, 28, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(6, 14, "King's Birthday"),
                h(10, 4, "Labour Day"),
                h(12, 26, "Proclamation Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Proclamation Day Holiday")
            )

            AustralianState.TAS -> listOf(
                h(3, 8, "Eight Hours Day"),
                h(4, 25, "Anzac Day"),
                h(6, 14, "King's Birthday"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day")
            )

            AustralianState.ACT -> listOf(
                h(3, 8, "Canberra Day"),
                h(3, 27, "Easter Saturday"),
                h(3, 28, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(4, 26, "Anzac Day Holiday"),
                h(5, 31, "Reconciliation Day"),
                h(6, 14, "King's Birthday"),
                h(10, 4, "Labour Day"),
                h(12, 26, "Boxing Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day Holiday")
            )

            AustralianState.NT -> listOf(
                h(3, 27, "Easter Saturday"),
                h(3, 28, "Easter Sunday"),
                h(4, 26, "Anzac Day"),
                h(5, 3, "May Day"),
                h(6, 14, "King's Birthday"),
                h(8, 2, "Picnic Day"),
                h(12, 26, "Boxing Day"),
                h(12, 27, "Christmas Day Holiday"),
                h(12, 28, "Boxing Day Holiday")
            )
        }

        return (common + stateSpecific).sortedBy { it.date }
    }
}