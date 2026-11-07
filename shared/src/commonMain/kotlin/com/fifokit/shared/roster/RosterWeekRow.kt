package com.fifokit.shared.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Displays seven consecutive roster days.
 * startDayOffset is the number of days since the roster's start date.
 */
@Composable
fun RosterWeekRow(
    startDayOffset: Long,
    pattern: RosterPattern,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(androidx.compose.ui.unit.Dp(4f))) {
        for (index in 0 until 7) {
            val offset = startDayOffset + index
            val isWorking = RosterCalculator.isWorkDay(offset, pattern)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Day ${index + 1}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = if (isWorking) "Work" else "Off",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isWorking) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
