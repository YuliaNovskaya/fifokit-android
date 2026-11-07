package com.fifokit.shared.roster

data class RosterSwingStatus(
    val isWorking: Boolean,
    val dayInSwing: Int,
    val swingLength: Int
)

fun rosterSwingStatus(daysFromStart: Long, pattern: RosterPattern): RosterSwingStatus {
    val cycleLength = pattern.cycleLength.toLong()
    val position = ((daysFromStart % cycleLength) + cycleLength) % cycleLength
    return if (position < pattern.workDays) {
        RosterSwingStatus(true, position.toInt() + 1, pattern.workDays)
    } else {
        RosterSwingStatus(false, (position - pattern.workDays).toInt() + 1, pattern.offDays)
    }
}
