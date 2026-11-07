package com.fifokit.shared.roster

/**
 * State-wide, full-day public holidays for 2026–2027.
 * Excludes local/regional holidays, restricted-industry holidays and part-day holidays.
 * Victoria's 2027 AFL Grand Final eve date is pending confirmation.
 */
object OtherStateHolidays {
    fun forYear(region: HolidayRegion, year: Int): List<RosterPublicHoliday> {
        if (year !in 2026..2027) return emptyList()
        fun h(month: Int, day: Int, name: String) =
            RosterPublicHoliday(year, month, day, name)
        val easterMonth = if (year == 2026) 4 else 3
        val goodFriday = if (year == 2026) 3 else 26
        val easterMonday = if (year == 2026) 6 else 29
        val easterSaturday = if (year == 2026) 4 else 27
        val easterSunday = if (year == 2026) 5 else 28
        val common = listOf(
            h(1, 1, "New Year's Day"),
            h(1, 26, "Australia Day"),
            h(easterMonth, goodFriday, "Good Friday"),
            h(easterMonth, easterMonday, "Easter Monday"),
            h(12, 25, "Christmas Day")
        )
        val additional = when (region) {
            HolidayRegion.VIC -> listOfNotNull(
                h(3, if (year == 2026) 9 else 8, "Labour Day"),
                h(easterMonth, easterSaturday, "Saturday before Easter Sunday"),
                h(easterMonth, easterSunday, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(6, if (year == 2026) 8 else 14, "King's Birthday"),
                if (year == 2026) h(9, 25, "Friday before AFL Grand Final") else null,
                h(11, if (year == 2026) 3 else 2, "Melbourne Cup Day"),
                h(12, 26, "Boxing Day"),
                if (year == 2027) h(12, 27, "Christmas Day holiday") else null,
                h(12, 28, "Boxing Day holiday")
            )
            HolidayRegion.QLD -> listOfNotNull(
                h(easterMonth, easterSaturday, "Day after Good Friday"),
                h(easterMonth, easterSunday, "Easter Sunday"),
                h(4, if (year == 2026) 25 else 26, "Anzac Day"),
                h(5, if (year == 2026) 4 else 3, "Labour Day"),
                h(10, if (year == 2026) 5 else 4, "King's Birthday"),
                h(12, 26, "Boxing Day"),
                if (year == 2027) h(12, 27, "Christmas Day holiday") else null,
                h(12, 28, "Boxing Day holiday")
            )
            HolidayRegion.SA -> listOfNotNull(
                h(3, if (year == 2026) 9 else 8, "Adelaide Cup Day"),
                h(easterMonth, easterSaturday, "Easter Saturday"),
                h(easterMonth, easterSunday, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(6, if (year == 2026) 8 else 14, "King's Birthday"),
                h(10, if (year == 2026) 5 else 4, "Labour Day"),
                h(12, 26, "Proclamation Day"),
                if (year == 2027) h(12, 27, "Christmas Day holiday") else null,
                h(12, 28, "Proclamation Day holiday")
            )
            HolidayRegion.TAS -> listOfNotNull(
                h(3, if (year == 2026) 9 else 8, "Eight Hours Day"),
                h(4, 25, "Anzac Day"),
                h(6, if (year == 2026) 8 else 14, "King's Birthday"),
                if (year == 2027) h(12, 27, "Christmas Day holiday") else null,
                h(12, 28, "Boxing Day")
            )
            HolidayRegion.ACT -> listOfNotNull(
                h(3, if (year == 2026) 9 else 8, "Canberra Day"),
                h(easterMonth, easterSaturday, "Easter Saturday"),
                h(easterMonth, easterSunday, "Easter Sunday"),
                h(4, 25, "Anzac Day"),
                h(4, if (year == 2026) 27 else 26, "Anzac Day holiday"),
                h(if (year == 2026) 6 else 5, if (year == 2026) 1 else 31, "Reconciliation Day"),
                h(6, if (year == 2026) 8 else 14, "King's Birthday"),
                h(10, if (year == 2026) 5 else 4, "Labour Day"),
                h(12, 26, "Boxing Day"),
                if (year == 2027) h(12, 27, "Christmas Day holiday") else null,
                h(12, 28, "Boxing Day holiday")
            )
            HolidayRegion.NT -> listOfNotNull(
                h(easterMonth, easterSaturday, "Easter Saturday"),
                h(easterMonth, easterSunday, "Easter Sunday"),
                h(4, if (year == 2026) 25 else 26, "Anzac Day"),
                h(5, if (year == 2026) 4 else 3, "May Day"),
                h(6, if (year == 2026) 8 else 14, "King's Birthday"),
                h(8, if (year == 2026) 3 else 2, "Picnic Day"),
                h(12, 26, "Boxing Day"),
                if (year == 2027) h(12, 27, "Christmas Day holiday") else null,
                h(12, 28, "Boxing Day holiday")
            )
            HolidayRegion.WA, HolidayRegion.NSW -> emptyList()
        }
        return (common + additional).sortedWith(compareBy({ it.month }, { it.day }))
    }

    fun onDate(region: HolidayRegion, year: Int, month: Int, day: Int): RosterPublicHoliday? =
        forYear(region, year).firstOrNull { it.month == month && it.day == day }
}
