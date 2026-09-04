package com.fifokit.app.ui.roster

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import java.time.LocalDate

@Composable
fun RosterSetupRoute(
    viewModel: RosterSetupViewModel,
    onGenerateRoster: () -> Unit = {}
) {
    RosterSetupScreen(
        selectedRoster = viewModel.selectedPattern,
        startDate = viewModel.startDate,
        onRosterSelected = viewModel::selectPattern,
        onStartDateSelected = viewModel::selectStartDate,
        onGenerateRoster = onGenerateRoster
    )
}

class RosterSetupViewModel : ViewModel() {

    var selectedPattern by mutableStateOf(RosterPattern.TWO_ONE)
        private set

    var startDate by mutableStateOf(LocalDate.now())
        private set

    fun selectPattern(pattern: RosterPattern) {
        selectedPattern = pattern
    }

    fun selectStartDate(date: LocalDate) {
        startDate = date
    }

    fun isWorkDay(date: LocalDate): Boolean {
        return RosterCalculator.isWorkDay(
            date = date,
            startDate = startDate,
            pattern = selectedPattern
        )
    }
}

