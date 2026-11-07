package com.fifokit.shared.roster

enum class HolidayRegion(val displayName: String) {
    WA("Western Australia"),
    NSW("New South Wales"),
    VIC("Victoria"),
    QLD("Queensland"),
    SA("South Australia"),
    TAS("Tasmania"),
    ACT("Australian Capital Territory"),
    NT("Northern Territory");

    fun holidayOn(year: Int, month: Int, day: Int): RosterPublicHoliday? =
        when (this) {
            WA -> WesternAustraliaHolidays.onDate(year, month, day)
            NSW -> NewSouthWalesHolidays.onDate(year, month, day)
            else -> OtherStateHolidays.onDate(this, year, month, day)
        }

    fun hasCoverageFor(year: Int): Boolean = when (this) {
        WA -> year in 2026..2029
        else -> year in 2026..2027
    }
}
