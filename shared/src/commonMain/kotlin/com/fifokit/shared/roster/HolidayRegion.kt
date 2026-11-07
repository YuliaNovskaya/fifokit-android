package com.fifokit.shared.roster

enum class HolidayRegion(val displayName: String) {
    WA("Western Australia"),
    NSW("New South Wales");

    fun holidayOn(year: Int, month: Int, day: Int): RosterPublicHoliday? =
        when (this) {
            WA -> WesternAustraliaHolidays.onDate(year, month, day)
            NSW -> NewSouthWalesHolidays.onDate(year, month, day)
        }

    fun hasCoverageFor(year: Int): Boolean = when (this) {
        WA -> year in 2026..2029
        NSW -> year in 2026..2027
    }
}
