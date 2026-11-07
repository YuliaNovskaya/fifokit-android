package com.fifokit.shared.roster

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun rosterComposeViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("FIFOKIT roster", style = MaterialTheme.typography.titleLarge)
                RosterWeekRow(
                    startDayOffset = 12,
                    pattern = RosterPattern.TWO_ONE
                )
            }
        }
    }
}
