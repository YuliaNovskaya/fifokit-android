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
import androidx.compose.material3.OutlinedTextField
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
private const val ROSTER_NAME_KEY = "com.fifokit.roster.name"
private const val PATTERN_KEY = "com.fifokit.roster.pattern"
private const val HOLIDAY_REGION_KEY = "com.fifokit.roster.holidayRegion"

@OptIn(ExperimentalMaterial3Api::class)
fun rosterComposeViewController(): UIViewController {
    val preferences = NSUserDefaults.standardUserDefaults
    val dateFormatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
    val today = RosterStartDate.parseIso(dateFormatter.stringFromDate(NSDate()))
    val defaultStart = today ?: RosterStartDate(2026, 11, 1)
    val rosterStore = IosRosterStore(preferences).apply { ensureMigrated(defaultStart) }
    val initialRoster = rosterStore.load(rosterStore.activeId())
        ?: error("No active roster after migration")

    return ComposeUIViewController {
    var displayedMonth by remember { mutableStateOf(CalendarMonth(initialRoster.startDate.year, initialRoster.startDate.month)) }
    var rosterStart by remember { mutableStateOf(initialRoster.startDate) }
    var rosterName by remember { mutableStateOf(initialRoster.name) }
    var activeRosterId by remember { mutableStateOf(initialRoster.id) }
    var rosterIds by remember { mutableStateOf(rosterStore.ids()) }
    var rosterMenuExpanded by remember { mutableStateOf(false) }
    var selectedDay by remember(displayedMonth, activeRosterId) { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedPattern by remember { mutableStateOf(initialRoster.pattern) }
    var holidayRegion by remember { mutableStateOf(initialRoster.holidayRegion) }
    var regionMenuExpanded by remember { mutableStateOf(false) }

    fun useRoster(roster: IosRosterSnapshot) {
        rosterStore.activate(roster.id)
        activeRosterId = roster.id
        rosterName = roster.name
        rosterStart = roster.startDate
        selectedPattern = roster.pattern
        holidayRegion = roster.holidayRegion
        displayedMonth = CalendarMonth(roster.startDate.year, roster.startDate.month)
        showDatePicker = false
    }
    val todayOffset = today?.let {
        CalendarMonth(it.year, it.month).daysFrom(
            rosterStart.year, rosterStart.month, rosterStart.day
        ) + it.day - 1
    }
    val status = todayOffset?.takeIf { it >= 0L }?.let { rosterSwingStatus(it, selectedPattern) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            IosAppShell(
                rosterName = rosterName,
                rosterStart = rosterStart,
                pattern = selectedPattern,
                holidayRegion = holidayRegion,
                currentYear = today?.year ?: rosterStart.year
            ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("FIFOKIT roster", style = MaterialTheme.typography.titleLarge)
                Column {
                    TextButton(onClick = { rosterMenuExpanded = true }) {
                        Text("Roster: $rosterName")
                    }
                    DropdownMenu(
                        expanded = rosterMenuExpanded,
                        onDismissRequest = { rosterMenuExpanded = false }
                    ) {
                        for (id in rosterIds) {
                            val saved = rosterStore.load(id)
                            if (saved != null) {
                                DropdownMenuItem(
                                    text = { Text(if (id == activeRosterId) "[${saved.name}]" else saved.name) },
                                    onClick = {
                                        rosterMenuExpanded = false
                                        useRoster(saved)
                                    }
                                )
                            }
                        }
                        DropdownMenuItem(
                            text = { Text("+ New roster") },
                            onClick = {
                                rosterMenuExpanded = false
                                val created = rosterStore.create(today ?: rosterStart)
                                rosterIds = rosterStore.ids()
                                useRoster(created)
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = rosterName,
                    onValueChange = { value ->
                        rosterName = value
                        rosterStore.update(activeRosterId, name = value)
                    },
                    label = { Text("Roster name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                                    rosterStore.update(activeRosterId, startDate = parsed)
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
                            rosterStore.update(activeRosterId, pattern = pattern)
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
                                    rosterStore.update(activeRosterId, holidayRegion = region)
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
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it },
                    isPublicHoliday = { day ->
                        holidayRegion.holidayOn(displayedMonth.year, displayedMonth.month, day) != null
                    }
                )
                selectedDay?.let { day ->
                    val details = rosterDayDetails(
                        date = RosterStartDate(displayedMonth.year, displayedMonth.month, day),
                        rosterStart = rosterStart,
                        pattern = selectedPattern,
                        region = holidayRegion,
                        today = today
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Selected: ${monthNames[displayedMonth.month - 1]} $day",
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                details.isBeforeStart -> "Before roster starts"
                                details.isWorkDay -> "Work day"
                                else -> "Day off"
                            }
                        )
                        details.holidayName?.let { Text("Public holiday: $it") }
                    }
                }
                val monthHolidays = (1..displayedMonth.daysInMonth).mapNotNull { day ->
                    holidayRegion.holidayOn(displayedMonth.year, displayedMonth.month, day)
                }
                if (monthHolidays.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Public holidays this month", style = MaterialTheme.typography.titleMedium)
                        for (holiday in monthHolidays) {
                            Text("${holiday.day} ${monthNames[holiday.month - 1]}: ${holiday.name}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            }
        }
    }
}

}
