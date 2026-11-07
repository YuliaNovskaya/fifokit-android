package com.fifokit.shared.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

private val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

fun rosterComposeViewController(): UIViewController = ComposeUIViewController {
    var displayedMonth by remember { mutableStateOf(CalendarMonth(2026, 11)) }
    var startDateText by remember { mutableStateOf("2026-11-01") }
    var rosterStart by remember { mutableStateOf(RosterStartDate(2026, 11, 1)) }
    var dateError by remember { mutableStateOf(false) }
    var selectedPattern by remember { mutableStateOf(RosterPattern.TWO_ONE) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("FIFOKIT roster", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it; dateError = false },
                    label = { Text("Roster start (YYYY-MM-DD)") },
                    isError = dateError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    val parsed = RosterStartDate.parseIso(startDateText)
                    if (parsed == null) {
                        dateError = true
                    } else {
                        rosterStart = parsed
                        displayedMonth = CalendarMonth(parsed.year, parsed.month)
                    }
                }) {
                    Text("Apply start date")
                }
                if (dateError) {
                    Text(
                        "Enter a valid date, e.g. 2026-11-01",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text("Roster pattern", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (pattern in RosterPattern.entries) {
                        TextButton(onClick = { selectedPattern = pattern }) {
                            Text(
                                if (selectedPattern == pattern) "[${pattern.label}]"
                                else pattern.label
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { displayedMonth = displayedMonth.previous() }) {
                        Text("Previous")
                    }
                    Button(onClick = { displayedMonth = displayedMonth.next() }) {
                        Text("Next")
                    }
                }
                RosterMonthGrid(
                    monthTitle = "${monthNames[displayedMonth.month - 1]} ${displayedMonth.year}",
                    daysInMonth = displayedMonth.daysInMonth,
                    firstWeekday = displayedMonth.firstWeekday,
                    firstDayOffset = displayedMonth.daysFrom(
                        rosterStart.year, rosterStart.month, rosterStart.day
                    ),
                    pattern = selectedPattern
                )
            }
        }
    }
}
