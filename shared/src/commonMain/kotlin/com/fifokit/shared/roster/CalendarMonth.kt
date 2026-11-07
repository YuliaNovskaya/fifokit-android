package com.fifokit.shared.roster

data class CalendarMonth(val year: Int, val month: Int) {
    init {
        require(month in 1..12)
    }

    val daysInMonth: Int
        get() = when (month) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

    /** Monday = 0, Sunday = 6. */
    val firstWeekday: Int
        get() = floorMod(daysSinceEpoch(year, month, 1) + 3, 7).toInt()

    fun next(): CalendarMonth =
        if (month == 12) CalendarMonth(year + 1, 1) else CalendarMonth(year, month + 1)

    fun previous(): CalendarMonth =
        if (month == 1) CalendarMonth(year - 1, 12) else CalendarMonth(year, month - 1)

    fun daysFrom(startYear: Int, startMonth: Int, startDay: Int): Long =
        daysSinceEpoch(year, month, 1) - daysSinceEpoch(startYear, startMonth, startDay)

    companion object {
        private fun isLeapYear(year: Int): Boolean =
            year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

        private fun floorMod(value: Long, divisor: Long): Long =
            ((value % divisor) + divisor) % divisor

        /** Proleptic Gregorian date to days since 1970-01-01. */
        private fun daysSinceEpoch(year: Int, month: Int, day: Int): Long {
            require(month in 1..12)
            require(day in 1..CalendarMonth(year, month).daysInMonth)
            val y = year.toLong() - if (month <= 2) 1 else 0
            val era = if (y >= 0) y / 400 else (y - 399) / 400
            val yearOfEra = y - era * 400
            val adjustedMonth = month + if (month > 2) -3 else 9
            val dayOfYear = (153L * adjustedMonth + 2) / 5 + day - 1
            val yearOfEraDays = yearOfEra * 365 + yearOfEra / 4 -
                yearOfEra / 100 + dayOfYear
            return era * 146097 + yearOfEraDays - 719468
        }
    }
}
