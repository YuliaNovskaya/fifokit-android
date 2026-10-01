package com.fifokit.app.ui.sharing

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.fifokit.app.growth.GrowthEngagementTracker
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
import androidx.compose.material3.Scaffold
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.domain.pro.ProEntitlementManager

@Composable
fun RosterInviteScreen(
    rosterId: String,
    rosterName: String,
    onBack: () -> Unit,
    onProRequested: () -> Unit = {},
    viewModel: RosterInviteViewModel = viewModel(),
    accessViewModel: RosterAccessViewModel = viewModel()
) {
    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)
    val growthTracker = GrowthEngagementTracker(context)
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.inviteId) {
        if (state.inviteId != null) {
            analytics.logEvent(
                AnalyticsEvents.ROSTER_INVITE_CREATED,
                null
            )
        }
    }

    val accessState by accessViewModel.uiState.collectAsState()
    val entitlement by
        ProEntitlementManager.entitlement.collectAsState()

    LaunchedEffect(rosterId) {
        viewModel.clearState()
        accessViewModel.loadShares(rosterId)
    }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "Share roster",
                onBack = onBack
            )
        }
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = rosterName,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text =
                if (entitlement.isPro) {
                    "Pro sharing: ${accessState.activeRecipientCount} of 5 people used."
                } else if (accessState.activeRecipientCount >= 1) {
                    "Your free sharing slot is in use. Upgrade to Pro to share with up to 5 people."
                } else {
                    "Free sharing includes 1 person. Upgrade to Pro to share with up to 5 people."
                },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (
                    state.sharingLimitReached &&
                    !state.isPro
                ) {
                    Button(
                        onClick = onProRequested
                    ) {
                        Text("Upgrade to Pro")
                    }
                } else if (!state.sharingLimitReached) {
                    Button(
                        onClick = {
                            viewModel.createInvite(
                                rosterId = rosterId,
                                rosterName = rosterName
                            )
                        }
                    ) {
                        Text("Try again")
                    }
                }
            }

            state.inviteId != null -> {
                val inviteLink =
                    "https://fifokit.com/invite/${state.inviteId}"

                Text(
                    text = "Invite created",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = inviteLink,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "View my FIFOKIT roster: $inviteLink"
                            )
                        }

                        context.startActivity(
                            Intent.createChooser(
                                intent,
                                "Share FIFOKIT roster"
                            )
                        )

                        growthTracker.recordMeaningfulAction()

                        analytics.logEvent(
                            AnalyticsEvents.ROSTER_INVITE_SHARED
                        ) {
                            param(
                                AnalyticsParams.SURFACE,
                                "invite_screen"
                            )
                        }
                    }
                ) {
                    Text("Share invite")
                }
            }

            else -> {
                Text(
                    text = "Create a private invite for your partner or family member."
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.createInvite(
                            rosterId = rosterId,
                            rosterName = rosterName
                        )
                    }
                ) {
                    Text("Create invite")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (accessState.shares.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Shared access",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            accessState.shares.forEach { access ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            if (access.isActive) {
                                "Viewer"
                            } else {
                                "Viewer · Paused"
                            }
                    )

                    OutlinedButton(
                        onClick = {
                            analytics.logEvent(
                                AnalyticsEvents.ROSTER_SHARE_REVOKED,
                                null
                            )
                            accessViewModel.revokeAccess(
                                rosterId = rosterId,
                                userId = access.userId
                            )
                        }
                    ) {
                        Text("Remove access")
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }

    }
}