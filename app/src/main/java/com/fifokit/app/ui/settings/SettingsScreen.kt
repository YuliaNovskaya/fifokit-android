package com.fifokit.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
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
import com.fifokit.app.growth.GrowthEngagementTracker
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
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
    val analytics = FirebaseAnalytics.getInstance(context)
    val notificationsEnabled =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    val entitlement by ProEntitlementManager.entitlement.collectAsState()

    val isDebugBuild =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FifokitTopBar(
                title = "Settings",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
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

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val storeUrl =
                        "https://play.google.com/store/apps/details" +
                                "?id=com.fifokit.app" +
                                "&referrer=utm_source%3Dfifokit_app" +
                                "%26utm_medium%3Dshare" +
                                "%26utm_campaign%3Dorganic_growth" +
                                "%26utm_content%3Dshare_app"

                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Plan your FIFO roster with FIFOKIT: $storeUrl"
                            )
                        }

                    analytics.logEvent(AnalyticsEvents.APP_SHARED) {
                        param(AnalyticsParams.SURFACE, "settings")
                    }

                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "Share FIFOKIT"
                        )
                    )
                }
            ) {
                Text("Share FIFOKIT")
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        GrowthEngagementTracker(
                            context
                        ).makeReviewEligibleForDebug()
                    }
                ) {
                    Text("Debug: Make review eligible")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Notifications",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =
                    if (notificationsEnabled) {
                        "System notifications: enabled"
                    } else {
                        "System notifications: disabled"
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                Text(
                    if (notificationsEnabled) {
                        "Open notification settings"
                    } else {
                        "Enable notifications"
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Roster reminders",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

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

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                AustralianState.entries.forEach { state ->
                    if (state in selectedStates) {
                        Button(
                            modifier =
                                Modifier.widthIn(
                                    min = 76.dp
                                ),
                            onClick = {
                                onStateToggle(state)
                            }
                        ) {
                            Text(
                                text = state.code,
                                maxLines = 1
                            )
                        }
                    } else {
                        OutlinedButton(
                            modifier =
                                Modifier.widthIn(
                                    min = 76.dp
                                ),
                            onClick = {
                                onStateToggle(state)
                            }
                        ) {
                            Text(
                                text = state.code,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}