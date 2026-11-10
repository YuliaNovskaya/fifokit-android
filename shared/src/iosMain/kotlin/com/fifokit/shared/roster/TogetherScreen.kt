package com.fifokit.shared.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSUserDefaults

private const val PARTNER_NAME_KEY = "com.fifokit.together.partnerName"
private const val PARTNER_START_KEY = "com.fifokit.together.partnerStart"
private const val PARTNER_PATTERN_KEY = "com.fifokit.together.partnerPattern"
private val togetherMonths = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TogetherScreen(
    myName: String,
    myStart: RosterStartDate,
    myPattern: RosterPattern
) {
    val prefs = remember { NSUserDefaults.standardUserDefaults }
    val today = remember {
        val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
        RosterStartDate.parseIso(formatter.stringFromDate(NSDate()))
    }
    var partnerName by remember { mutableStateOf(prefs.stringForKey(PARTNER_NAME_KEY) ?: "Partner") }
    var partnerStart by remember {
        mutableStateOf(RosterStartDate.parseIso(prefs.stringForKey(PARTNER_START_KEY) ?: "")
            ?: myStart)
    }
    var partnerPattern by remember {
        mutableStateOf(RosterPattern.entries.firstOrNull {
            it.name == prefs.stringForKey(PARTNER_PATTERN_KEY)
        } ?: RosterPattern.TWO_ONE)
    }
    var showPartnerDatePicker by remember { mutableStateOf(false) }
    var month by remember { mutableStateOf(CalendarMonth(today?.year ?: myStart.year, today?.month ?: myStart.month)) }
    var selectedDay by remember(month) { mutableStateOf<Int?>(null) }

    val summary = remember(today, myStart, myPattern, partnerStart, partnerPattern) {
        RosterOverlapCalculator.calculate(
            fromDate = today ?: myStart,
            myStart = myStart,
            myPattern = myPattern,
            partnerStart = partnerStart,
            partnerPattern = partnerPattern
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Together", style = MaterialTheme.typography.headlineMedium)
        Text("$myName + $partnerName")
        OutlinedTextField(
            value = partnerName,
            onValueChange = {
                partnerName = it
                prefs.setObject(it, forKey = PARTNER_NAME_KEY)
            },
            label = { Text("Partner name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { showPartnerDatePicker = true }) {
            Text("Partner first work day: ${partnerStart.toIsoString()}")
        }
        if (showPartnerDatePicker) {
            val picker = rememberDatePickerState(initialSelectedDateMillis = partnerStart.toEpochMillis())
            DatePickerDialog(
                onDismissRequest = { showPartnerDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        picker.selectedDateMillis?.let {
                            partnerStart = RosterStartDate.fromEpochMillis(it)
                            prefs.setObject(partnerStart.toIsoString(), forKey = PARTNER_START_KEY)
                        }
                        showPartnerDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showPartnerDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = picker) }
        }
        Text("Partner roster pattern", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (pattern in RosterPattern.entries) {
                TextButton(onClick = {
                    partnerPattern = pattern
                    prefs.setObject(pattern.name, forKey = PARTNER_PATTERN_KEY)
                }) {
                    Text(if (pattern == partnerPattern) "[${pattern.label}]" else pattern.label)
                }
            }
        }

        Text("${summary.sharedOffDays} shared days off in the next 90 days",
            style = MaterialTheme.typography.titleMedium)
        summary.nextSharedOffPeriod?.let {
            Text("Next shared break: ${it.startDate.toIsoString()} to ${it.endDate.toIsoString()} (${it.days} days)")
        }
        if (summary.nextSharedOffPeriod == null) {
            Text("No shared days off found in the next 90 days")
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { month = month.previous() }) { Text("Previous") }
            TextButton(onClick = {
                today?.let { month = CalendarMonth(it.year, it.month) }
            }) { Text("Today") }
            Button(onClick = { month = month.next() }) { Text("Next") }
        }
        Text("${togetherMonths[month.month - 1]} ${month.year}",
            style = MaterialTheme.typography.titleLarge)
        TogetherMonthGrid(
            month = month,
            myStart = myStart,
            myPattern = myPattern,
            partnerStart = partnerStart,
            partnerPattern = partnerPattern,
            selectedDay = selectedDay,
            onDaySelected = { selectedDay = it }
        )
        selectedDay?.let { day ->
            val date = RosterStartDate(month.year, month.month, day)
            fun status(value: Boolean?): String = when (value) {
                true -> "Work"
                false -> "Off"
                null -> "Not started"
            }
            Text("${date.toIsoString()}: $myName ${status(RosterOverlapCalculator.isWorkDay(date, myStart, myPattern))}, " +
                "$partnerName ${status(RosterOverlapCalculator.isWorkDay(date, partnerStart, partnerPattern))}")
        }
        Text(
            "This is a local partner overlay. Cross-device invitations and cloud sharing are not yet connected.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
