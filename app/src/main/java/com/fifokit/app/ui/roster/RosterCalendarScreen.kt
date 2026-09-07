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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import java.time.format.DateTimeFormatter
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.fifokit.app.notifications.RosterNotificationManager

@Composable
fun RosterCalendarScreen(
    viewModel: RosterSetupViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { }
        )

    LaunchedEffect(Unit) {
        RosterNotificationManager.createChannel(context)
    }
    var month by remember {
        mutableStateOf(YearMonth.now())
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

        Spacer(modifier = Modifier.height(16.dp))

        RosterTodaySummary(viewModel)

        Spacer(modifier = Modifier.height(12.dp))

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            ) {
                Text("Enable notifications")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(
            month = month,
            startDate = viewModel.startDate,
            isWorkDay = viewModel::isWorkDay,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        CalendarLegend()

        Spacer(modifier = Modifier.height(12.dp))

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
    startDate: LocalDate,
    isWorkDay: (LocalDate) -> Boolean,
    modifier: Modifier = Modifier
)
{
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
                    isWorkDay = isWorkDay(date),
                    isStartDate = date == startDate
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isWorkDay: Boolean,
    isStartDate: Boolean
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
        },
        border = if (isStartDate) {
            BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
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
                text = if (isWorkDay) "WORK" else "OFF",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "WORK",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "OFF",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Border = start date",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun RosterTodaySummary(
    viewModel: RosterSetupViewModel
) {
    val todayIsWork = viewModel.isTodayWorkDay()
    val nextChangeDate = viewModel.nextRosterChangeDate()
    val daysRemaining = viewModel.daysUntilRosterChange()

    val nextStatus = if (todayIsWork) "OFF" else "WORK"

    val formatter = DateTimeFormatter.ofPattern(
        "EEE, d MMM",
        Locale.getDefault()
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today: ${if (todayIsWork) "WORK" else "OFF"}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$nextStatus starts ${
                    nextChangeDate.format(formatter)
                } · $daysRemaining ${
                    if (daysRemaining == 1L) "day" else "days"
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}