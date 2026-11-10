package com.fifokit.shared.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class IosTab(val label: String) {
    ROSTER("Roster"), TOOLS("Tools"), SETTINGS("Settings")
}

/** iOS navigation shell. Individual Android tools will be migrated in later batches. */
@Composable
internal fun IosAppShell(
    rosterName: String,
    rosterStart: RosterStartDate,
    pattern: RosterPattern,
    holidayRegion: HolidayRegion,
    rosterContent: @Composable () -> Unit
) {
    var tab by remember { mutableStateOf(IosTab.ROSTER) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                for (item in IosTab.entries) {
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Text(item.label.take(1)) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (tab) {
            IosTab.ROSTER -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    rosterContent()
                }
            }
            IosTab.TOOLS -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Tools", style = MaterialTheme.typography.headlineMedium)
                    Text("Pay calculator", style = MaterialTheme.typography.titleMedium)
                    Text("Migration pending")
                    Text("Annual earnings", style = MaterialTheme.typography.titleMedium)
                    Text("Migration pending")
                    Text("Financial goals", style = MaterialTheme.typography.titleMedium)
                    Text("Migration pending")
                }
            }
            IosTab.SETTINGS -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                    Text("Roster: $rosterName")
                    Text("Start date: ${rosterStart.toIsoString()}")
                    Text("Pattern: ${pattern.label}")
                    Text("Holiday region: ${holidayRegion.displayName}")
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { tab = IosTab.ROSTER }
                    ) {
                        Text("Edit roster settings")
                    }
                }
            }
        }
    }
}
