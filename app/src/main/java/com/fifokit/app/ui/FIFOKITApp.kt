package com.fifokit.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fifokit.app.ui.roster.RosterSetupRoute
import com.fifokit.app.ui.roster.RosterSetupViewModel

@Composable
fun FIFOKITApp() {

    val rosterSetupViewModel: RosterSetupViewModel = viewModel()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        RosterSetupRoute(
            viewModel = rosterSetupViewModel
        )
    }
}