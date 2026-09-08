package com.fifokit.app.ui

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
import androidx.compose.foundation.layout.Box
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
                    }
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