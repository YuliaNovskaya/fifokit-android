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
import androidx.activity.compose.BackHandler

@Composable
fun FIFOKITApp() {

    val rosterSetupViewModel: RosterSetupViewModel = viewModel()

    var screenOverride by remember {
        mutableStateOf<Boolean?>(null)
    }

    val showCalendar =
        screenOverride ?: rosterSetupViewModel.hasSavedRoster

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (showCalendar) {

            true -> {

                BackHandler {
                    screenOverride = false
                }

                RosterCalendarScreen(
                    viewModel = rosterSetupViewModel,
                    onBack = {
                        screenOverride = false
                    }
                )
            }

            false -> {
                RosterSetupRoute(
                    viewModel = rosterSetupViewModel,
                    onGenerateRoster = {
                        screenOverride = true
                    }
                )
            }

            null -> {
                // Waiting for saved roster state to load
            }
        }
    }
}