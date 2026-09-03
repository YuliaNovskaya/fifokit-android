package com.fifokit.app.domain.model

enum class RosterPattern(
    val label: String,
    val workDays: Int,
    val offDays: Int
) {
    TWO_ONE(
        label = "2/1",
        workDays = 14,
        offDays = 7
    ),
    TWO_TWO(
        label = "2/2",
        workDays = 14,
        offDays = 14
    ),
    EIGHT_SIX(
        label = "8/6",
        workDays = 8,
        offDays = 6
    ),
    SEVEN_SEVEN(
        label = "7/7",
        workDays = 7,
        offDays = 7
    );

    val cycleLength: Int
        get() = workDays + offDays
}