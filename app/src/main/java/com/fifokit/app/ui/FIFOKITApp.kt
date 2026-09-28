package com.fifokit.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fifokit.app.ui.roster.RosterCalendarScreen
import com.fifokit.app.ui.roster.RosterSetupRoute
import com.fifokit.app.ui.roster.RosterSetupViewModel
import com.fifokit.app.ui.settings.SettingsScreen
import com.fifokit.app.ui.finance.FinanceToolsScreen
import com.fifokit.app.ui.finance.PayCalculatorScreen
import com.fifokit.app.ui.finance.AnnualEarningsScreen
import com.fifokit.app.ui.finance.FinancialGoalScreen
import androidx.activity.compose.BackHandler
import com.fifokit.app.ui.pro.ProScreen
import com.fifokit.app.domain.pro.ProAccess
import com.fifokit.app.domain.pro.ProFeature
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import androidx.compose.ui.platform.LocalContext
import com.fifokit.app.ui.auth.AccountScreen
import com.fifokit.app.ui.auth.AuthViewModel
import androidx.compose.runtime.LaunchedEffect
import com.fifokit.app.ui.sharing.RosterInviteScreen
import com.fifokit.app.ui.sharing.RosterInviteAcceptScreen
import com.fifokit.app.ui.sharing.SharedRostersScreen
import com.fifokit.app.domain.sharing.SharedRoster
import com.fifokit.app.ui.sharing.SharedRosterCalendarScreen
import com.fifokit.app.ui.sharing.TogetherRosterCalendarScreen

@Composable
fun FIFOKITApp(
    inviteId: String? = null
) {

    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)

    val rosterSetupViewModel: RosterSetupViewModel = viewModel()

    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(authViewModel) {
        authViewModel.syncCompleted.collect {
            rosterSetupViewModel.refreshFromLocalStorage()
        }
    }

    var screenOverride by remember {
        mutableStateOf<Boolean?>(null)
    }

    var showSharedRosters by remember {
        mutableStateOf(false)
    }

    var selectedSharedRoster by remember {
        mutableStateOf<SharedRoster?>(null)
    }

    val showCalendar =
        screenOverride ?: (rosterSetupViewModel.hasSavedRoster == true)

    var showSettings by remember {
        mutableStateOf(false)
    }
    var showFinance by remember {
        mutableStateOf(false)
    }

    var showPayCalculator by remember {
        mutableStateOf(false)
    }
    var showAnnualEarnings by remember {
        mutableStateOf(false)
    }

    var showFinancialGoal by remember {
        mutableStateOf(false)
    }

    var showPro by remember {
        mutableStateOf(false)
    }

    var showAccount by remember {
        mutableStateOf(false)
    }

    var showShareRoster by remember {
        mutableStateOf(false)
    }

    var showAcceptInvite by remember(inviteId) {
        mutableStateOf(inviteId != null)
    }

    var showTogetherRoster by remember {
        mutableStateOf(false)
    }

    BackHandler(
        enabled =
            showTogetherRoster ||
            selectedSharedRoster != null ||
            showSharedRosters ||
            showAcceptInvite ||
            showShareRoster ||
            showAccount ||
                    showFinancialGoal ||
                    showAnnualEarnings ||
                    showPayCalculator ||
                    showFinance ||
                    showSettings
    ) {
        when {
            showTogetherRoster ->
                showTogetherRoster = false

            selectedSharedRoster != null ->
                selectedSharedRoster = null

            showAcceptInvite ->
                showAcceptInvite = false

            showShareRoster ->
                showShareRoster = false

            showSharedRosters -> {
                showSharedRosters = false
                selectedSharedRoster = null
            }

            showAccount ->
                showAccount = false

            showFinancialGoal ->
                showFinancialGoal = false

            showAnnualEarnings ->
                showAnnualEarnings = false

            showPayCalculator ->
                showPayCalculator = false

            showFinance ->
                showFinance = false

            showSettings ->
                showSettings = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {

            val activeCloudRosterId =
                rosterSetupViewModel.rosters
                    .firstOrNull {
                        it.id == rosterSetupViewModel.activeRosterId
                    }
                    ?.cloudId

            if (
                showTogetherRoster &&
                selectedSharedRoster != null
            ) {

                TogetherRosterCalendarScreen(
                    viewModel = rosterSetupViewModel,
                    sharedRoster = selectedSharedRoster!!,
                    onMyRoster = {
                        showTogetherRoster = false
                        showSharedRosters = false
                        selectedSharedRoster = null
                    },
                    onBack = {
                        showTogetherRoster = false
                    }
                )

            } else if (selectedSharedRoster != null) {

                SharedRosterCalendarScreen(
                    sharedRoster = selectedSharedRoster!!,
                    onBack = {
                        selectedSharedRoster = null
                    },
                    onTogether = {
                        showTogetherRoster = true
                    }
                )

            } else if (showSharedRosters) {

                SharedRostersScreen(
                    onBack = {
                        showSharedRosters = false
                        selectedSharedRoster = null
                    },
                    onRosterSelected = { sharedRoster ->

                        analytics.logEvent("shared_roster_selected") {
                            param(
                                "roster_id",
                                sharedRoster.roster.id
                            )
                        }

                        selectedSharedRoster = sharedRoster
                    }
                )

            } else if (
                showAcceptInvite &&
                inviteId != null
            ) {

                RosterInviteAcceptScreen(
                    inviteId = inviteId,
                    onAccepted = {
                        showAcceptInvite = false
                        screenOverride = true
                    }
                )

            } else if (
                showShareRoster &&
                rosterSetupViewModel.activeRosterId != null
            ) {

                RosterInviteScreen(
                    rosterId = activeCloudRosterId!!,
                    rosterName = rosterSetupViewModel.rosterName,
                    onBack = {
                        showShareRoster = false
                    }
                )

            } else if (showPro) {

                ProScreen(
                    onBack = {
                        showPro = false
                    }
                )

            } else if (showFinancialGoal) {

                FinancialGoalScreen(
                    onBack = {
                        showFinancialGoal = false
                    }
                )

            } else if (showAnnualEarnings) {

                AnnualEarningsScreen(
                    selectedPattern = rosterSetupViewModel.selectedPattern,
                    isCustomRoster = rosterSetupViewModel.isCustomRoster,
                    customWorkDays = rosterSetupViewModel.customWorkDays,
                    customOffDays = rosterSetupViewModel.customOffDays,
                    rosterStartDate = rosterSetupViewModel.startDate,
                    onBack = {
                        showAnnualEarnings = false
                    }
                )

            } else if (showPayCalculator) {

                PayCalculatorScreen(
                    selectedPattern = rosterSetupViewModel.selectedPattern,
                    isCustomRoster = rosterSetupViewModel.isCustomRoster,
                    customWorkDays = rosterSetupViewModel.customWorkDays,
                    customOffDays = rosterSetupViewModel.customOffDays,
                    rosterStartDate = rosterSetupViewModel.startDate,
                    onBack = {
                        showPayCalculator = false
                    }
                )

            } else if (showFinance) {

                FinanceToolsScreen(
                    selectedPattern = rosterSetupViewModel.selectedPattern,
                    isCustomRoster = rosterSetupViewModel.isCustomRoster,
                    customWorkDays = rosterSetupViewModel.customWorkDays,
                    customOffDays = rosterSetupViewModel.customOffDays,
                    rosterStartDate = rosterSetupViewModel.startDate,
                    onBack = {
                        showFinance = false
                    },
                    onPayCalculator = {
                        showPayCalculator = true
                    },
                    onAnnualEarnings = {
                        if (
                            ProAccess.canUse(
                                ProFeature.DETAILED_ANNUAL_EARNINGS
                            )
                        ) {
                            showAnnualEarnings = true
                        } else {
                            analytics.logEvent("pro_feature_locked") {
                                param("feature", "annual_earnings")
                            }
                            showPro = true
                        }
                    },
                    onFinancialGoal = {
                        showFinancialGoal = true
                    }
                )

            } else if (showAccount) {

                AccountScreen(
                    authViewModel = authViewModel
                )
            } else if (showSettings) {
                SettingsScreen(
                    onBack = {
                        showSettings = false
                    },
                    onPro = {
                        showPro = true
                    },
                    onAccount = {
                        showAccount = true
                    },
                    selectedStates = rosterSetupViewModel.selectedStates,
                    onStateToggle = rosterSetupViewModel::toggleState,
                    remindersEnabled = rosterSetupViewModel.remindersEnabled,
                    workRemindersEnabled = rosterSetupViewModel.workRemindersEnabled,
                    offRemindersEnabled = rosterSetupViewModel.offRemindersEnabled,
                    reminderHour = rosterSetupViewModel.reminderHour,
                    reminderMinute = rosterSetupViewModel.reminderMinute,
                    onRemindersEnabledChange =
                        rosterSetupViewModel::updateRemindersEnabled,
                    onWorkRemindersEnabledChange =
                        rosterSetupViewModel::updateWorkRemindersEnabled,
                    onOffRemindersEnabledChange =
                        rosterSetupViewModel::updateOffRemindersEnabled,
                    onReminderTimeChange =
                        rosterSetupViewModel::updateReminderTime,
                    sharedTimeRemindersEnabled =
                        rosterSetupViewModel.sharedTimeRemindersEnabled,

                    onSharedTimeRemindersEnabledChange =
                        rosterSetupViewModel::updateSharedTimeRemindersEnabled
                )
            } else if (showCalendar) {
                RosterCalendarScreen(
                    viewModel = rosterSetupViewModel,
                    onBack = {
                        screenOverride = false
                    },
                    onShareRoster = {
                        showShareRoster = true
                    },
                    onSettings = {
                        showSettings = true
                    },
                    onFinance = {
                        showFinance = true
                    },
                    onSharedRosters = {
                        showSharedRosters = true
                    },
                    onProRequested = { feature ->
                        analytics.logEvent("pro_feature_locked") {
                            param("feature", feature)
                        }
                        showPro = true
                    }
                )
            } else {
                RosterSetupRoute(
                    viewModel = rosterSetupViewModel,
                    onGenerateRoster = {
                        screenOverride = true
                    },
                    onSettings = {
                        showSettings = true
                    },
                    onProRequested = { feature ->
                        analytics.logEvent("pro_feature_locked") {
                            param("feature", feature)
                        }
                        showPro = true
                    }
                )
            }
        }
    }
}