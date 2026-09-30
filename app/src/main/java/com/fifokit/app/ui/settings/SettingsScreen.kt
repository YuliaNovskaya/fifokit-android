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
import android.app.TimePickerDialog
import androidx.compose.material3.Switch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fifokit.app.domain.pro.ProEntitlementManager
import android.content.pm.ApplicationInfo
import com.fifokit.app.data.billing.ProEntitlementStore
import com.fifokit.app.widgets.RosterWidgetUpdater


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPro: () -> Unit = {},
    onAccount: () -> Unit = {},
    selectedStates: Set<AustralianState> = setOf(AustralianState.WA),
    onStateToggle: (AustralianState) -> Unit = {},
    remindersEnabled: Boolean = true,
    workRemindersEnabled: Boolean = true,
    offRemindersEnabled: Boolean = true,
    reminderHour: Int = 19,
    reminderMinute: Int = 0,
    onRemindersEnabledChange: (Boolean) -> Unit = {},
    onWorkRemindersEnabledChange: (Boolean) -> Unit = {},
    onOffRemindersEnabledChange: (Boolean) -> Unit = {},
    sharedTimeRemindersEnabled: Boolean = true,
    onSharedTimeRemindersEnabledChange: (Boolean) -> Unit = {},
    onReminderTimeChange: (Int, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val notificationsEnabled =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    val entitlement by ProEntitlementManager.entitlement.collectAsState()

    val isDebugBuild =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

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
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPro
            ) {
                Text(
                    if (entitlement.isPro) {
                        "FIFOKIT Pro - Active"
                    } else {
                        "FIFOKIT Pro"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAccount
            ) {
                Text("Account & cloud sync")
            }

            if (isDebugBuild) {

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        ProEntitlementManager.setDebugPro(
                            !entitlement.isPro
                        )

                        ProEntitlementStore(
                            context
                        ).save(
                            ProEntitlementManager
                                .entitlement
                                .value
                        )

                        RosterWidgetUpdater
                            .updateAll(
                                context
                            )
                    }
                ) {
                    Text(
                        if (entitlement.isPro) {
                            "Debug: Set Free"
                        } else {
                            "Debug: Set Pro"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Text("Roster reminders")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable roster reminders")

                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersEnabledChange
                )
            }

            if (remindersEnabled) {

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("First WORK day reminder")

                    Switch(
                        checked = workRemindersEnabled,
                        onCheckedChange = onWorkRemindersEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("First OFF day reminder")

                    Switch(
                        checked = offRemindersEnabled,
                        onCheckedChange = onOffRemindersEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Shared time off reminder")

                    Switch(
                        checked = sharedTimeRemindersEnabled,
                        onCheckedChange =
                            onSharedTimeRemindersEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                onReminderTimeChange(hour, minute)
                            },
                            reminderHour,
                            reminderMinute,
                            true
                        ).show()
                    }
                ) {
                    Text(
                        "Reminder time: %02d:%02d".format(
                            reminderHour,
                            reminderMinute
                        )
                    )
                }
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