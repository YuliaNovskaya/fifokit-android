package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.fifokit.app.domain.model.RosterPattern
import java.time.LocalDate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceToolsScreen(
    selectedPattern: RosterPattern,
    isCustomRoster: Boolean,
    customWorkDays: Int,
    customOffDays: Int,
    rosterStartDate: LocalDate,
    onBack: () -> Unit,
    onPayCalculator: () -> Unit = {},
    onAnnualEarnings: () -> Unit = {},
    onFinancialGoal: () -> Unit = {}
) {
    val context = LocalContext.current

    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }

    LaunchedEffect(Unit) {
        analytics.logEvent(
            "finance_tools_viewed",
            null
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Financial tools")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = if (isCustomRoster) {
                    "Active roster: $customWorkDays/$customOffDays"
                } else {
                    "Active roster: ${selectedPattern.label}"
                }
            )

            Text(
                text = "Roster start: $rosterStartDate"
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "FIFO financial calculators"
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    analytics.logEvent(
                        "finance_tool_selected",
                        Bundle().apply {
                            putString("tool", "pay_calculator")
                        }
                    )
                    onPayCalculator()
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("FIFO Pay Calculator")
                    Text("Calculate pay using your active roster.")
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    analytics.logEvent(
                        "finance_tool_selected",
                        Bundle().apply {
                            putString("tool", "annual_earnings")
                        }
                    )
                    onAnnualEarnings()
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Annual Earnings")
                    Text("Estimate yearly work days, hours and gross earnings.")
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    analytics.logEvent(
                        "finance_tool_selected",
                        Bundle().apply {
                            putString("tool", "financial_goal")
                        }
                    )
                    onFinancialGoal()
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Financial Goal")
                    Text("Estimate how long it will take to reach a savings target.")
                }
            }
        }
    }
}