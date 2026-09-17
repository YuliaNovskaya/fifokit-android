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

@Composable
fun FIFOKITApp() {

    val rosterSetupViewModel: RosterSetupViewModel = viewModel()

    var screenOverride by remember {
        mutableStateOf<Boolean?>(null)
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

    BackHandler(
        enabled =
            showFinancialGoal ||
                    showAnnualEarnings ||
                    showPayCalculator ||
                    showFinance ||
                    showSettings
    ) {
        when {
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

            if (showFinancialGoal) {

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
                        showAnnualEarnings = true
                    },
                    onFinancialGoal = {
                        showFinancialGoal = true
                    }
                )

            } else if (showSettings) {
                SettingsScreen(
                    onBack = {
                        showSettings = false
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
                        rosterSetupViewModel::updateReminderTime
                )
            } else if (showCalendar) {
                RosterCalendarScreen(
                    viewModel = rosterSetupViewModel,
                    onBack = {
                        screenOverride = false
                    },
                    onSettings = {
                        showSettings = true
                    },
                    onFinance = {
                        showFinance = true
                    }
                )
            } else {
                RosterSetupRoute(
                    viewModel = rosterSetupViewModel,
                    onGenerateRoster = {
                        screenOverride = true
                    }
                )
            }
        }
    }
}