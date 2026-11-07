package com.fifokit.shared.roster

object RosterCalculator {

    fun isWorkDay(
        daysFromStart: Long,
        pattern: RosterPattern
    ): Boolean {
        return isWorkDay(
            daysFromStart = daysFromStart,
            workDays = pattern.workDays,
            offDays = pattern.offDays
        )
    }

    fun isWorkDay(
        daysFromStart: Long,
        workDays: Int,
        offDays: Int
    ): Boolean {
        require(workDays > 0)
        require(offDays > 0)

        val cycleLength = workDays.toLong() + offDays
        val position = ((daysFromStart % cycleLength) + cycleLength) % cycleLength

        return position < workDays
    }
}