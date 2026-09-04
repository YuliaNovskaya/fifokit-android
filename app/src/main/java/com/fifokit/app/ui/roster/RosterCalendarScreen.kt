package com.fifokit.app.ui.roster

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun RosterCalendarScreen(
    viewModel: RosterSetupViewModel,
    onBack: () -> Unit
) {
    var month by remember {
        mutableStateOf(YearMonth.from(viewModel.startDate))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    month = month.minusMonths(1)
                }
            ) {
                Text("<")
            }

            Text(
                text = "${
                    month.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    )
                } ${month.year}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = {
                    month = month.plusMonths(1)
                }
            ) {
                Text(">")
            }
        }

        Text(
            text = "${viewModel.selectedPattern.label} roster",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(
            month = month,
            isWorkDay = viewModel::isWorkDay,
            modifier = Modifier.weight(1f)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Edit roster")
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    isWorkDay: (LocalDate) -> Boolean,
    modifier: Modifier = Modifier
) {
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1

    val cells = buildList<LocalDate?> {
        repeat(firstDayOffset) {
            add(null)
        }

        for (day in 1..month.lengthOfMonth()) {
            add(month.atDay(day))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.fillMaxWidth()
    ) {
        items(cells) { date ->
            if (date == null) {
                Spacer(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                )
            } else {
                CalendarDay(
                    date = date,
                    isWorkDay = isWorkDay(date)
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isWorkDay: Boolean
) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isWorkDay) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (isWorkDay) "W" else "O",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}