package com.fifokit.shared.roster

data class RosterStartDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(month in 1..12)
        require(day in 1..CalendarMonth(year, month).daysInMonth)
    }

    companion object {
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
