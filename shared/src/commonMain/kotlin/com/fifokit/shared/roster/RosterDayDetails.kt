package com.fifokit.shared.roster

data class RosterDayDetails(
    val date: RosterStartDate,
    val daysFromStart: Long,
    val isBeforeStart: Boolean,
    val isWorkDay: Boolean,
    val isToday: Boolean,
    val holidayName: String?
)

/** Details for a selected calendar cell, shared by Android and iOS. */
fun rosterDayDetails(
    date: RosterStartDate,
    rosterStart: RosterStartDate,
    pattern: RosterPattern,
    region: HolidayRegion,
    today: RosterStartDate? = null
): RosterDayDetails {
    val offset = CalendarMonth(date.year, date.month).daysFrom(
        rosterStart.year, rosterStart.month, rosterStart.day
    ) + date.day - 1
    return RosterDayDetails(
        date = date,
        daysFromStart = offset,
        isBeforeStart = offset < 0,
        isWorkDay = offset >= 0 && RosterCalculator.isWorkDay(offset, pattern),
        isToday = date == today,
        holidayName = region.holidayOn(date.year, date.month, date.day)?.name
    )
}
