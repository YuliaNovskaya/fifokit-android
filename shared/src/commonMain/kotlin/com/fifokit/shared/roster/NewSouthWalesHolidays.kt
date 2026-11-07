package com.fifokit.shared.roster

/** State-wide NSW public holidays; excludes local and bank-only holidays. */
object NewSouthWalesHolidays {
    private fun h(year: Int, month: Int, day: Int, name: String) =
        RosterPublicHoliday(year, month, day, name)

    fun forYear(year: Int): List<RosterPublicHoliday> = when (year) {
        2026 -> listOf(
            h(2026, 1, 1, "New Year's Day"),
            h(2026, 1, 26, "Australia Day"),
            h(2026, 4, 3, "Good Friday"),
            h(2026, 4, 4, "Easter Saturday"),
            h(2026, 4, 5, "Easter Sunday"),
            h(2026, 4, 6, "Easter Monday"),
            h(2026, 4, 25, "Anzac Day"),
            h(2026, 4, 27, "Anzac Day holiday"),
            h(2026, 6, 8, "King's Birthday"),
            h(2026, 10, 5, "Labour Day"),
            h(2026, 12, 25, "Christmas Day"),
            h(2026, 12, 26, "Boxing Day"),
            h(2026, 12, 28, "Boxing Day holiday")
        )
        2027 -> listOf(
            h(2027, 1, 1, "New Year's Day"),
            h(2027, 1, 26, "Australia Day"),
            h(2027, 3, 26, "Good Friday"),
            h(2027, 3, 27, "Easter Saturday"),
            h(2027, 3, 28, "Easter Sunday"),
            h(2027, 3, 29, "Easter Monday"),
            h(2027, 4, 25, "Anzac Day"),
            h(2027, 4, 26, "Anzac Day holiday"),
            h(2027, 6, 14, "King's Birthday"),
            h(2027, 10, 4, "Labour Day"),
            h(2027, 12, 25, "Christmas Day"),
            h(2027, 12, 26, "Boxing Day"),
            h(2027, 12, 27, "Christmas Day holiday"),
            h(2027, 12, 28, "Boxing Day holiday")
        )
        else -> emptyList()
    }

    fun onDate(year: Int, month: Int, day: Int): RosterPublicHoliday? =
        forYear(year).firstOrNull { it.month == month && it.day == day }
}
