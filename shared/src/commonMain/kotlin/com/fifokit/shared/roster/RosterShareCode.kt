package com.fifokit.shared.roster

data class RosterShareData(
    val name: String,
    val startDate: RosterStartDate,
    val pattern: RosterPattern,
    val region: HolidayRegion
)

/**
 * Versioned portable roster snapshot; contains no account identifiers or credentials.
 * A copied code is static and does not auto-update when its owner edits the roster.
 */
object RosterShareCode {
    private const val PREFIX = "FIFOKIT1:"

    fun encode(roster: RosterShareData): String {
        val name = roster.name.trim().ifBlank { "My Roster" }.take(80)
        return "$PREFIX${name.length}:$name|${roster.startDate.toIsoString()}|" +
            "${roster.pattern.name}|${roster.region.name}"
    }

    fun decode(text: String): RosterShareData? {
        val code = text.trim()
        if (!code.startsWith(PREFIX)) return null
        val lengthEnd = code.indexOf(':', PREFIX.length)
        if (lengthEnd < 0) return null
        val length = code.substring(PREFIX.length, lengthEnd).toIntOrNull()
            ?.takeIf { it in 1..80 } ?: return null
        val nameStart = lengthEnd + 1
        val nameEnd = nameStart + length
        if (nameEnd >= code.length || code[nameEnd] != '|') return null
        val name = code.substring(nameStart, nameEnd)
        if (name.isBlank()) return null
        val fields = code.substring(nameEnd + 1).split('|')
        if (fields.size != 3) return null
        val date = RosterStartDate.parseIso(fields[0]) ?: return null
        val pattern = RosterPattern.entries.firstOrNull { it.name == fields[1] } ?: return null
        val region = HolidayRegion.entries.firstOrNull { it.name == fields[2] } ?: return null
        return RosterShareData(name, date, pattern, region)
    }
}
