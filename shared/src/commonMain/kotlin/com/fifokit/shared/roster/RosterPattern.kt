package com.fifokit.shared.roster

enum class RosterPattern(
    val label: String,
    val workDays: Int,
    val offDays: Int
) {
    TWO_ONE("2/1", 14, 7),
    TWO_TWO("2/2", 14, 14),
    EIGHT_SIX("8/6", 8, 6),
    SEVEN_SEVEN("7/7", 7, 7);

    val cycleLength: Int
        get() = workDays + offDays
}