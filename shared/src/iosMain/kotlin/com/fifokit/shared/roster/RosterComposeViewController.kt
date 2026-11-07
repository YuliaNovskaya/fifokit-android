package com.fifokit.shared.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    val rosterStartYear = 2026
    val rosterStartMonth = 11
    val rosterStartDay = 1

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("FIFOKIT roster", style = MaterialTheme.typography.titleLarge)
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
                        rosterStartYear, rosterStartMonth, rosterStartDay
                    ),
                    pattern = RosterPattern.TWO_ONE
                )
            }
        }
    }
}
