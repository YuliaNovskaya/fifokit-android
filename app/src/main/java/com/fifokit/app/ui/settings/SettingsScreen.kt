package com.fifokit.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.layout.Row
import com.fifokit.app.domain.roster.AustralianState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    selectedStates: Set<AustralianState> = setOf(AustralianState.WA),
    onStateToggle: (AustralianState) -> Unit = {}
) {
    val context = LocalContext.current
    val notificationsEnabled =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
                },
                navigationIcon = {
                    androidx.compose.material3.TextButton(
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
                .padding(16.dp)
        ) {
            Text(
                text = if (notificationsEnabled) {
                    "Notifications are enabled"
                } else {
                    "Notifications are disabled"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    ).apply {
                        putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            context.packageName
                        )
                    }

                    context.startActivity(intent)
                }
            ) {
                Text("Open notification settings")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Public holidays")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Show public holidays for:")

            Spacer(modifier = Modifier.height(8.dp))

            AustralianState.entries.chunked(4).forEach { states ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    states.forEach { state ->
                        if (state in selectedStates) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { onStateToggle(state) }
                            ) {
                                Text(state.code)
                            }
                        } else {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onStateToggle(state) }
                            ) {
                                Text(state.code)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}