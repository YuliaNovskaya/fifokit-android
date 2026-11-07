package com.fifokit.shared.roster

data class RosterStartDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(month in 1..12)
        require(day in 1..CalendarMonth(year, month).daysInMonth)
    }

    fun toEpochMillis(): Long =
        (CalendarMonth(year, month).daysFrom(1970, 1, 1) + day - 1) * 86_400_000L

    fun toIsoString(): String =
        year.toString().padStart(4, '0') + "-" +
            month.toString().padStart(2, '0') + "-" +
            day.toString().padStart(2, '0')

    companion object {
        fun fromEpochMillis(millis: Long): RosterStartDate {
            val days = millis / 86_400_000L -
                if (millis < 0 && millis % 86_400_000L != 0L) 1 else 0

            var low = 1
            var high = 9999
            var foundYear = 1970
            while (low <= high) {
                val middle = (low + high) / 2
                val firstDay = CalendarMonth(middle, 1).daysFrom(1970, 1, 1)
                if (firstDay <= days) {
                    foundYear = middle
                    low = middle + 1
                } else {
                    high = middle - 1
                }
            }

            var foundMonth = 1
            for (candidate in 1..12) {
                if (CalendarMonth(foundYear, candidate).daysFrom(1970, 1, 1) <= days) {
                    foundMonth = candidate
                } else break
            }
            val firstOfMonth = CalendarMonth(foundYear, foundMonth).daysFrom(1970, 1, 1)
            return RosterStartDate(foundYear, foundMonth, (days - firstOfMonth + 1).toInt())
        }

        fun parseIso(text: String): RosterStartDate? {
            val parts = text.split("-")
            if (parts.size != 3 || parts[0].length != 4 ||
                parts[1].length != 2 || parts[2].length != 2 ||
                parts.any { part -> part.any { !it.isDigit() } }
            ) return null

            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            if (month !in 1..12 || day !in 1..CalendarMonth(year, month).daysInMonth) {
                return null
            }
            return RosterStartDate(year, month, day)
        }
    }
}
