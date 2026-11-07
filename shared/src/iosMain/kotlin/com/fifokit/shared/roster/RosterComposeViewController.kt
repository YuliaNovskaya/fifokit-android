package com.fifokit.shared.roster

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun rosterComposeViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("FIFOKIT roster", style = MaterialTheme.typography.titleLarge)
                RosterMonthGrid(
                    monthTitle = "November 2026",
                    daysInMonth = 30,
                    firstWeekday = 6,
                    firstDayOffset = 0L,
                    pattern = RosterPattern.TWO_ONE
                )
            }
        }
    }
}
