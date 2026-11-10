package com.fifokit.shared.roster

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
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
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIViewController

private val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private const val START_DATE_KEY = "com.fifokit.roster.startDate"
private const val PATTERN_KEY = "com.fifokit.roster.pattern"
private const val HOLIDAY_REGION_KEY = "com.fifokit.roster.holidayRegion"

@OptIn(ExperimentalMaterial3Api::class)
fun rosterComposeViewController(): UIViewController {
    val preferences = NSUserDefaults.standardUserDefaults
    val dateFormatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
    val today = RosterStartDate.parseIso(dateFormatter.stringFromDate(NSDate()))
    val savedDate = preferences.stringForKey(START_DATE_KEY)
    val initialStart = RosterStartDate.parseIso(savedDate ?: "")
        ?: RosterStartDate(2026, 11, 1)
    val initialPattern = RosterPattern.entries.firstOrNull {
        it.name == preferences.stringForKey(PATTERN_KEY)
    } ?: RosterPattern.TWO_ONE

    val initialRegion = HolidayRegion.entries.firstOrNull {
        it.name == preferences.stringForKey(HOLIDAY_REGION_KEY)
    } ?: HolidayRegion.WA

    return ComposeUIViewController {
    var displayedMonth by remember { mutableStateOf(CalendarMonth(initialStart.year, initialStart.month)) }
    var rosterStart by remember { mutableStateOf(initialStart) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedPattern by remember { mutableStateOf(initialPattern) }
    var holidayRegion by remember { mutableStateOf(initialRegion) }
    var regionMenuExpanded by remember { mutableStateOf(false) }
    val todayOffset = today?.let {
        CalendarMonth(it.year, it.month).daysFrom(
            rosterStart.year, rosterStart.month, rosterStart.day
        ) + it.day - 1
    }
    val status = todayOffset?.takeIf { it >= 0L }?.let { rosterSwingStatus(it, selectedPattern) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("FIFOKIT roster", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { showDatePicker = true }) {
                    Text("Start date: ${rosterStart.toIsoString()}")
                }
                if (showDatePicker) {
                    val pickerState = rememberDatePickerState(
                        initialSelectedDateMillis = rosterStart.toEpochMillis()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                pickerState.selectedDateMillis?.let { millis ->
                                    val parsed = RosterStartDate.fromEpochMillis(millis)
                                    rosterStart = parsed
                                    displayedMonth = CalendarMonth(parsed.year, parsed.month)
                                    preferences.setObject(parsed.toIsoString(), forKey = START_DATE_KEY)
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = pickerState)
                    }
                }
                if (todayOffset != null && todayOffset < 0L) {
                    Text(
                        "Roster starts in ${-todayOffset} day${if (todayOffset == -1L) "" else "s"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (status != null) {
                    Text(
                        "Today: ${if (status.isWorking) "Work" else "Off"} — " +
                            "day ${status.dayInSwing} of ${status.swingLength}",
                        style = MaterialTheme.typography.bodyMedium
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
                Text("Public holidays", style = MaterialTheme.typography.labelLarge)
                Column {
                    TextButton(onClick = { regionMenuExpanded = true }) {
                        Text("${holidayRegion.displayName} (${holidayRegion.name})")
                    }
                    DropdownMenu(
                        expanded = regionMenuExpanded,
                        onDismissRequest = { regionMenuExpanded = false }
                    ) {
                        for (region in HolidayRegion.entries) {
                            DropdownMenuItem(
                                text = { Text("${region.displayName} (${region.name})") },
                                onClick = {
                                    holidayRegion = region
                                    preferences.setObject(region.name, forKey = HOLIDAY_REGION_KEY)
                                    regionMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                if (!holidayRegion.hasCoverageFor(displayedMonth.year)) {
                    Text(
                        "Holiday dates not available for ${holidayRegion.name} ${displayedMonth.year}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { displayedMonth = displayedMonth.previous() }) {
                        Text("Previous")
                    }
                    TextButton(
                        enabled = today != null,
                        onClick = {
                            today?.let { displayedMonth = CalendarMonth(it.year, it.month) }
                        }
                    ) {
                        Text("Today")
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
                    todayDayOffset = todayOffset,
                    pattern = selectedPattern,
                    isPublicHoliday = { day ->
                        holidayRegion.holidayOn(displayedMonth.year, displayedMonth.month, day) != null
                    }
                )
            }
        }
    }
}

}
