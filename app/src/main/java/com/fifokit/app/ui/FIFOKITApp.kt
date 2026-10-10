package com.fifokit.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fifokit.app.deeplink.AppLinkDestination
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.domain.pro.ProAccess
import com.fifokit.app.domain.pro.ProFeature
import com.fifokit.app.domain.sharing.SharedRoster
import com.fifokit.app.ui.auth.AccountScreen
import com.fifokit.app.ui.auth.AuthViewModel
import com.fifokit.app.ui.finance.AnnualEarningsScreen
import com.fifokit.app.ui.finance.FinancialGoalScreen
import com.fifokit.app.ui.finance.PayCalculatorScreen
import com.fifokit.app.ui.export.RosterExportScreen
import java.time.YearMonth
import com.fifokit.app.ui.pro.ProScreen
import com.fifokit.app.ui.roster.RosterCalendarScreen
import com.fifokit.app.ui.roster.RosterSetupRoute
import com.fifokit.app.ui.roster.RosterSetupViewModel
import com.fifokit.app.ui.settings.SettingsScreen
import com.fifokit.app.ui.sharing.RosterInviteAcceptScreen
import com.fifokit.app.ui.sharing.RosterInviteScreen
import com.fifokit.app.ui.sharing.SharedRosterCalendarScreen
import com.fifokit.app.ui.sharing.SharedRostersScreen
import com.fifokit.app.ui.sharing.TogetherRosterCalendarScreen
import com.fifokit.app.ui.components.FifokitBottomNavigation
import com.fifokit.app.ui.components.RosterActionsSheet
import com.fifokit.app.ui.components.ToolsActionsSheet
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

@Composable
fun FIFOKITApp(
    inviteId: String? = null,
    initialShowPro: Boolean = false,
    appLinkDestination:
        AppLinkDestination? = null
) {
    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)

    val rosterSetupViewModel: RosterSetupViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    var screenOverride by remember {
        mutableStateOf<Boolean?>(null)
    }

    LaunchedEffect(authViewModel) {
        authViewModel.syncCompleted.collect {
            rosterSetupViewModel.refreshFromLocalStorage()
            screenOverride = null
        }
    }

    var showSharedRosters by remember { mutableStateOf(false) }
    var selectedSharedRoster by remember { mutableStateOf<SharedRoster?>(null) }

    val showCalendar =
        screenOverride ?: (
            rosterSetupViewModel.hasSavedRoster == true ||
            (
                currentUser != null &&
                rosterSetupViewModel.hasSavedRoster == false
            )
        )

    var showSettings by remember { mutableStateOf(false) }

    var showRosterActionsSheet by remember {
        mutableStateOf(false)
    }

    var showToolsActionsSheet by remember {
        mutableStateOf(false)
    }
    var showPayCalculator by remember { mutableStateOf(false) }
    var showAnnualEarnings by remember { mutableStateOf(false) }
    var showFinancialGoal by remember { mutableStateOf(false) }
    var showPro by remember(initialShowPro) {
        mutableStateOf(initialShowPro)
    }
    var showAccount by remember { mutableStateOf(false) }
    var showShareRoster by remember { mutableStateOf(false) }

    var showRosterExport by remember {
        mutableStateOf(false)
    }

    var exportStartMonth by remember {
        mutableStateOf(
            YearMonth.now()
        )
    }

    var showAcceptInvite by remember {
        mutableStateOf(false)
    }

    var showTogetherRoster by remember {
        mutableStateOf(false)
    }

    fun clearOpenScreens() {
        showTogetherRoster = false
        selectedSharedRoster = null
        showSharedRosters = false
        showShareRoster = false
        showRosterExport = false
        showPro = false
        showAccount = false
        showFinancialGoal = false
        showAnnualEarnings = false
        showPayCalculator = false
        showSettings = false
        showRosterActionsSheet = false
        showToolsActionsSheet = false
    }

    LaunchedEffect(
        appLinkDestination,
        inviteId
    ) {
        when (appLinkDestination) {

            AppLinkDestination.ROSTER -> {
                showTogetherRoster = false
                selectedSharedRoster = null
                showSharedRosters = false
                showAcceptInvite = false
                showShareRoster = false
                showRosterExport = false
                showPro = false
                showAccount = false
                showFinancialGoal = false
                showAnnualEarnings = false
                showPayCalculator = false
                showSettings = false
                screenOverride = null
            }

            AppLinkDestination.FINANCE -> {
                showTogetherRoster = false
                selectedSharedRoster = null
                showSharedRosters = false
                showAcceptInvite = false
                showShareRoster = false
                showRosterExport = false
                showPro = false
                showAccount = false
                showFinancialGoal = false
                showAnnualEarnings = false
                showPayCalculator = false
                showSettings = false
                screenOverride = true
                showToolsActionsSheet = true
            }

            AppLinkDestination.INVITE -> {
                showTogetherRoster = false
                selectedSharedRoster = null
                showSharedRosters = false
                showShareRoster = false
                showRosterExport = false
                showPro = false
                showAccount = false
                showFinancialGoal = false
                showAnnualEarnings = false
                showPayCalculator = false
                showSettings = false
                showAcceptInvite =
                    inviteId != null
            }

            null -> {
                if (inviteId != null) {
                    showAcceptInvite = true
                }
            }
        }
    }

    BackHandler(
        enabled =
            showTogetherRoster ||
            selectedSharedRoster != null ||
            showSharedRosters ||
            showAcceptInvite ||
            showShareRoster ||
            showRosterExport ||
            showPro ||
            showAccount ||
            showFinancialGoal ||
            showAnnualEarnings ||
            showPayCalculator ||
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

            showRosterExport ->
                showRosterExport = false

            showSharedRosters -> {
                showSharedRosters = false
                selectedSharedRoster = null
            }

            showPro ->
                showPro = false

            showAccount -> {
                showAccount = false
                rosterSetupViewModel.refreshFromLocalStorage()
                screenOverride = null
            }

            showFinancialGoal ->
                showFinancialGoal = false

            showAnnualEarnings ->
                showAnnualEarnings = false

            showPayCalculator ->
                showPayCalculator = false

            showSettings ->
                showSettings = false
        }
    }

    if (showRosterActionsSheet) {
        RosterActionsSheet(
            hasOwnRoster =
                rosterSetupViewModel.rosters.isNotEmpty(),
            onDismiss = {
                showRosterActionsSheet = false
            },
            onCreateRoster = {
                clearOpenScreens()
                rosterSetupViewModel.createNewRoster()
                screenOverride = false
            },
            onShareRoster = {
                clearOpenScreens()
                showShareRoster = true
                screenOverride = true
            },
            onSharedRosters = {
                clearOpenScreens()
                analytics.logEvent(
                    AnalyticsEvents.FEATURE_OPENED
                ) {
                    param(
                        AnalyticsParams.FEATURE,
                        "partner_sharing"
                    )
                }
                showSharedRosters = true
                screenOverride = true
            },
            onExportRoster = {
                clearOpenScreens()
                showRosterExport = true
                screenOverride = true
            }
        )
    }

    if (showToolsActionsSheet) {
        ToolsActionsSheet(
            onDismiss = {
                showToolsActionsSheet = false
            },
            onPayCalculator = {
                clearOpenScreens()
                showPayCalculator = true
                screenOverride = true
            },
            onAnnualEarnings = {
                clearOpenScreens()

                if (
                    ProAccess.canUse(
                        ProFeature.DETAILED_ANNUAL_EARNINGS
                    )
                ) {
                    showAnnualEarnings = true
                    screenOverride = true
                } else {
                    analytics.logEvent(
                        AnalyticsEvents.PRO_FEATURE_LOCKED
                    ) {
                        param(
                            AnalyticsParams.FEATURE,
                            "annual_earnings"
                        )
                    }
                    showPro = true
                    screenOverride = true
                }
            },
            onFinancialGoal = {
                clearOpenScreens()
                showFinancialGoal = true
                screenOverride = true
            }
        )
    }

    val showPersistentNavigation =
        (
            rosterSetupViewModel.rosters.isNotEmpty() ||
            currentUser != null
        ) &&
                !showAcceptInvite

    fun goToCalendar() {
        clearOpenScreens()
        showAcceptInvite = false
        screenOverride = true
    }

    Scaffold(
        bottomBar = {
            if (showPersistentNavigation) {
                FifokitBottomNavigation(
                    onEdit = {
                        clearOpenScreens()

                        if (
                            rosterSetupViewModel
                                .rosters
                                .isEmpty()
                        ) {
                            rosterSetupViewModel
                                .createNewRoster()
                        }

                        screenOverride = false
                    },
                    onRoster = {
                        showRosterActionsSheet = true
                    },
                    onCalendar = {
                        goToCalendar()
                    },
                    onPay = {
                        analytics.logEvent(
                            AnalyticsEvents.FEATURE_OPENED
                        ) {
                            param(
                                AnalyticsParams.FEATURE,
                                "finance_tools"
                            )
                        }
                        showToolsActionsSheet = true
                    },
                    onMore = {
                        clearOpenScreens()
                        showSettings = true
                        screenOverride = true
                    }
                )
            }
        }
    ) { appPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(appPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                        analytics.logEvent(
                            AnalyticsEvents.SHARED_ROSTER_SELECTED,
                            null
                        )

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
                        showSharedRosters = true
                        screenOverride = true
                    }
                )
            } else if (showRosterExport) {
                RosterExportScreen(
                    data =
                        rosterSetupViewModel
                            .rosterExportData(),
                    startMonth =
                        exportStartMonth,
                    onBack = {
                        showRosterExport = false
                    },
                    onProRequested = { feature ->
                        analytics.logEvent(
                            AnalyticsEvents.PRO_FEATURE_LOCKED
                        ) {
                            param(
                                AnalyticsParams.FEATURE,
                                feature
                            )
                        }

                        showRosterExport = false
                        showPro = true
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
                    },
                    onProRequested = {
                        analytics.logEvent(
                            AnalyticsEvents.PRO_FEATURE_LOCKED
                        ) {
                            param(
                                AnalyticsParams.FEATURE,
                                "roster_sharing_limit"
                            )
                        }
                        showShareRoster = false
                        showPro = true
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
                    },
                    onProRequested = { feature ->
                        analytics.logEvent(
                            AnalyticsEvents.PRO_FEATURE_LOCKED
                        ) {
                            param(
                                AnalyticsParams.FEATURE,
                                feature
                            )
                        }

                        showFinancialGoal = false
                        showPro = true
                    }
                )
            } else if (showAnnualEarnings) {
                AnnualEarningsScreen(
                    selectedPattern = rosterSetupViewModel.selectedPattern,
                    isCustomRoster = rosterSetupViewModel.isCustomRoster,
                    customWorkDays = rosterSetupViewModel.customWorkDays,
                    customOffDays = rosterSetupViewModel.customOffDays,
                    rosterStartDate = rosterSetupViewModel.startDate,
                    selectedStates = rosterSetupViewModel.selectedStates,
                    rosterEndDate =
                        if (rosterSetupViewModel.isShutdownRoster) {
                            rosterSetupViewModel.endDate
                        } else {
                            null
                        },
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
                    selectedStates = rosterSetupViewModel.selectedStates,
                    rosterEndDate =
                        if (rosterSetupViewModel.isShutdownRoster) {
                            rosterSetupViewModel.endDate
                        } else {
                            null
                        },
                    onBack = {
                        showPayCalculator = false
                    }
                )
            } else if (showAccount) {
                AccountScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        showAccount = false
                        rosterSetupViewModel.refreshFromLocalStorage()
                        screenOverride = null
                    },
                    onSignedIn = {
                        showAccount = false
                    }
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
                    onMonthChanged = { month ->
                        exportStartMonth = month
                    },
                    onProRequested = { feature ->
                        analytics.logEvent(AnalyticsEvents.PRO_FEATURE_LOCKED) {
                            param(AnalyticsParams.FEATURE, feature)
                        }
                        showPro = true
                    }
                )
            } else if (screenOverride == false) {
                RosterSetupRoute(
                    viewModel = rosterSetupViewModel,
                    onGenerateRoster = {
                        screenOverride = true
                    },
                    onBack = {
                        if (rosterSetupViewModel.isCreatingNewRoster) {
                            rosterSetupViewModel.cancelNewRoster()
                        } else {
                            rosterSetupViewModel.cancelEditRoster()
                        }
                        screenOverride = true
                    },
                    onProRequested = { feature ->
                        analytics.logEvent(AnalyticsEvents.PRO_FEATURE_LOCKED) {
                            param(AnalyticsParams.FEATURE, feature)
                        }
                        showPro = true
                    }
                )
            } else if (rosterSetupViewModel.hasSavedRoster == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AccountScreen(
                    authViewModel = authViewModel,
                    onCreateRoster = {
                        rosterSetupViewModel.createNewRoster()
                        screenOverride = false
                    },
                    onSignedIn = {
                        rosterSetupViewModel.refreshFromLocalStorage()
                        screenOverride = true
                    }
                )
            }

            }
        }
    }
}
