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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {

            if (showSettings) {
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