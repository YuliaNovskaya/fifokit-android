package com.fifokit.shared.roster

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")

/**
 * Monday-first month grid.
 * firstWeekday: 0=Monday through 6=Sunday.
 * firstDayOffset: days between roster start and day 1 of this month.
 */
@Composable
fun RosterMonthGrid(
    monthTitle: String,
    daysInMonth: Int,
    firstWeekday: Int,
    firstDayOffset: Long,
    todayDayOffset: Long? = null,
    pattern: RosterPattern,
    modifier: Modifier = Modifier
) {
    require(daysInMonth in 28..31)
    require(firstWeekday in 0..6)

    val weekCount = (firstWeekday + daysInMonth + 6) / 7
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(monthTitle, style = MaterialTheme.typography.titleLarge)
        Row(modifier = Modifier.fillMaxWidth()) {
            for (name in weekdays) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        for (week in 0 until weekCount) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekday in 0 until 7) {
                    val day = week * 7 + weekday - firstWeekday + 1
                    if (day !in 1..daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayOffset = firstDayOffset + day - 1
                        val isScheduled = dayOffset >= 0L
                        val isWork = isScheduled && RosterCalculator.isWorkDay(dayOffset, pattern)
                        val isStartDate = dayOffset == 0L
                        val isToday = dayOffset == todayDayOffset
                        val background = when {
                            !isScheduled -> MaterialTheme.colorScheme.surface
                            isWork -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(background, RoundedCornerShape(6.dp))
                                .then(
                                    when {
                                        isToday -> Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        isStartDate -> Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        else -> Modifier
                                    }
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        Text(
            "Work: highlighted   Off: neutral   Before start: blank",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
