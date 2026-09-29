package com.fifokit.app.ui.sharing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fifokit.app.domain.sharing.SharedRoster
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.fifokit.app.domain.model.RosterPattern

@Composable
fun SharedRostersScreen(
    onBack: () -> Unit,
    onRosterSelected: (SharedRoster) -> Unit,
    viewModel: SharedRostersViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)

    LaunchedEffect(Unit) {
        analytics.logEvent("shared_rosters_viewed") {}
        viewModel.loadSharedRosters()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Shared rosters",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        when {

            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> {

                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = {
                        viewModel.loadSharedRosters()
                    }
                ) {
                    Text("Try again")
                }
            }

            state.rosters.isEmpty() -> {
                Text("No shared rosters")
            }

            else -> {
                state.rosters.forEach { sharedRoster ->
                    val roster = sharedRoster.roster

                    val patternLabel =
                        if (roster.isCustomRoster) {
                            "${roster.customWorkDays}/${roster.customOffDays} custom roster"
                        } else {
                            runCatching {
                                RosterPattern.valueOf(
                                    roster.pattern
                                ).label
                            }.getOrDefault(
                                roster.pattern
                            )
                        }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onRosterSelected(sharedRoster)
                            }
                            .padding(
                                vertical = 16.dp
                            )
                    ) {

                        Text(
                            text = roster.name
                                .trim()
                                .ifBlank {
                                    "Unnamed roster"
                                },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = patternLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider()
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedButton(
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}