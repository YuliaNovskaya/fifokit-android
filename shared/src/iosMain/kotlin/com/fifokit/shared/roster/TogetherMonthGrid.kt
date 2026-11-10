package com.fifokit.shared.roster

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
internal fun TogetherMonthGrid(
    month: CalendarMonth,
    myStart: RosterStartDate,
    myPattern: RosterPattern,
    partnerStart: RosterStartDate,
    partnerPattern: RosterPattern,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit
) {
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    val weeks = (month.firstWeekday + month.daysInMonth + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (weekday in weekdays) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(weekday, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekday in 0..6) {
                    val day = week * 7 + weekday - month.firstWeekday + 1
                    if (day !in 1..month.daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = RosterStartDate(month.year, month.month, day)
                        val mine = RosterOverlapCalculator.isWorkDay(date, myStart, myPattern)
                        val partner = RosterOverlapCalculator.isWorkDay(date, partnerStart, partnerPattern)
                        val color = when {
                            mine == null || partner == null -> MaterialTheme.colorScheme.surface
                            !mine && !partner -> MaterialTheme.colorScheme.primaryContainer
                            !mine -> MaterialTheme.colorScheme.secondaryContainer
                            !partner -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                                .background(color, RoundedCornerShape(6.dp))
                                .then(
                                    if (selectedDay == day) Modifier.border(
                                        2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)
                                    ) else Modifier
                                )
                                .clickable { onDaySelected(day) }
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
            "Both off: primary   My off: secondary   Partner off: tertiary   Both work: neutral",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
