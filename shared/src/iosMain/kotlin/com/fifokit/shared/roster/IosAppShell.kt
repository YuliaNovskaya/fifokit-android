package com.fifokit.shared.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.fifokit.shared.finance.IosPayCalculatorScreen
import com.fifokit.shared.finance.IosAnnualEarningsScreen
import com.fifokit.shared.finance.IosFinancialGoalScreen
import platform.UIKit.UIPasteboard

private enum class IosToolsPage { MENU, PAY, ANNUAL, GOAL }

private enum class IosTab(val label: String) {
    ROSTER("Roster"), TOGETHER("Together"), TOOLS("Tools"), SETTINGS("Settings")
}

/** iOS navigation shell. Individual Android tools will be migrated in later batches. */
@Composable
internal fun IosAppShell(
    rosterName: String,
    rosterStart: RosterStartDate,
    pattern: RosterPattern,
    holidayRegion: HolidayRegion,
    currentYear: Int,
    rosterContent: @Composable () -> Unit
) {
    var tab by remember { mutableStateOf(IosTab.ROSTER) }
    var toolsPage by remember { mutableStateOf(IosToolsPage.MENU) }
    var rosterCodeCopied by remember { mutableStateOf(false) }

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
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    rosterContent()
                }
            }
            IosTab.TOGETHER -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    TogetherScreen(myName = rosterName, myStart = rosterStart, myPattern = pattern)
                }
            }
            IosTab.TOOLS -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (toolsPage) {
                        IosToolsPage.PAY -> IosPayCalculatorScreen(
                            pattern = pattern,
                            rosterStart = rosterStart,
                            year = currentYear,
                            onBack = { toolsPage = IosToolsPage.MENU }
                        )
                        IosToolsPage.ANNUAL -> IosAnnualEarningsScreen(
                            pattern = pattern,
                            rosterStart = rosterStart,
                            initialYear = currentYear,
                            onBack = { toolsPage = IosToolsPage.MENU }
                        )
                        IosToolsPage.GOAL -> IosFinancialGoalScreen(
                            onBack = { toolsPage = IosToolsPage.MENU }
                        )
                        IosToolsPage.MENU -> Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Tools", style = MaterialTheme.typography.headlineMedium)
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { toolsPage = IosToolsPage.PAY }
                            ) { Text("Pay calculator") }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { toolsPage = IosToolsPage.ANNUAL }
                            ) { Text("Annual earnings") }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { toolsPage = IosToolsPage.GOAL }
                            ) { Text("Financial goals") }
                        }
                    }
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
                        onClick = {
                            val code = RosterShareCode.encode(
                                RosterShareData(rosterName, rosterStart, pattern, holidayRegion)
                            )
                            UIPasteboard.generalPasteboard.string = code
                            rosterCodeCopied = true
                        }
                    ) {
                        Text("Copy roster sharing code")
                    }
                    if (rosterCodeCopied) {
                        Text("Code copied. Paste it into a message for your partner.")
                    }
                    Text("Sharing codes are snapshots; later edits do not update the recipient's roster.",
                        style = MaterialTheme.typography.bodySmall)
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
