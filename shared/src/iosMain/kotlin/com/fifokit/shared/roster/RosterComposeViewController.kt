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
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIViewController

private val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private const val START_DATE_KEY = "com.fifokit.roster.startDate"
private const val PATTERN_KEY = "com.fifokit.roster.pattern"

fun rosterComposeViewController(): UIViewController {
    val preferences = NSUserDefaults.standardUserDefaults
    val savedDate = preferences.stringForKey(START_DATE_KEY)
    val initialStart = RosterStartDate.parseIso(savedDate ?: "")
        ?: RosterStartDate(2026, 11, 1)
    val initialDateText = if (savedDate != null &&
        RosterStartDate.parseIso(savedDate) != null) savedDate else "2026-11-01"
    val initialPattern = RosterPattern.entries.firstOrNull {
        it.name == preferences.stringForKey(PATTERN_KEY)
    } ?: RosterPattern.TWO_ONE

    return ComposeUIViewController {
    var displayedMonth by remember { mutableStateOf(CalendarMonth(initialStart.year, initialStart.month)) }
    var startDateText by remember { mutableStateOf(initialDateText) }
    var rosterStart by remember { mutableStateOf(initialStart) }
    var dateError by remember { mutableStateOf(false) }
    var selectedPattern by remember { mutableStateOf(initialPattern) }

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
                        preferences.setObject(startDateText, forKey = START_DATE_KEY)
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
                        TextButton(onClick = {
                            selectedPattern = pattern
                            preferences.setObject(pattern.name, forKey = PATTERN_KEY)
                        }) {
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

}
