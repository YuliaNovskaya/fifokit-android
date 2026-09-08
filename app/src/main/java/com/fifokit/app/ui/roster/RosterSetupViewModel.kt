package com.fifokit.app.ui.roster

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.fifokit.app.notifications.RosterReminderScheduler
import com.fifokit.app.domain.roster.AustralianState

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
        showResetRoster = viewModel.hasSavedRoster == true,
        onResetRoster = viewModel::clearRoster,
        onGenerateRoster = {
            viewModel.saveCurrentRoster()
            onGenerateRoster()
        }
    )
}

class RosterSetupViewModel(
    application: Application
) : AndroidViewModel(application) {

    var selectedStates by mutableStateOf(setOf(AustralianState.WA))
        private set

    fun toggleState(state: AustralianState) {
        selectedStates =
            if (state in selectedStates) {
                selectedStates - state
            } else {
                selectedStates + state
            }
    }
    private val rosterPreferences = RosterPreferences(application)

    var selectedPattern by mutableStateOf(RosterPattern.TWO_ONE)
        private set

    var startDate by mutableStateOf(LocalDate.now())
        private set

    var hasSavedRoster by mutableStateOf<Boolean?>(null)
        private set

    init {
        restoreRoster()
    }

    fun clearRoster() {
        viewModelScope.launch {
            rosterPreferences.clearRoster()
            RosterReminderScheduler.cancel(getApplication())
            selectedPattern = RosterPattern.TWO_ONE
            startDate = LocalDate.now()
            hasSavedRoster = false
        }
    }

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
    fun isTodayWorkDay(): Boolean {
        return isWorkDay(LocalDate.now())
    }

    fun nextRosterChangeDate(
        fromDate: LocalDate = LocalDate.now()
    ): LocalDate {
        val currentStatus = isWorkDay(fromDate)

        var date = fromDate.plusDays(1)

        while (isWorkDay(date) == currentStatus) {
            date = date.plusDays(1)
        }

        return date
    }

    fun daysUntilRosterChange(
        fromDate: LocalDate = LocalDate.now()
    ): Long {
        return ChronoUnit.DAYS.between(
            fromDate,
            nextRosterChangeDate(fromDate)
        )
    }

    fun saveCurrentRoster() {
        viewModelScope.launch {
            rosterPreferences.saveRoster(
                pattern = selectedPattern.name,
                startDate = startDate.toString()
            )

            RosterReminderScheduler.schedule(getApplication())

            hasSavedRoster = true
        }
    }

    private fun restoreRoster() {
        viewModelScope.launch {
            val savedRoster = rosterPreferences.savedRoster.first()

            if (savedRoster == null) {
                hasSavedRoster = false
                return@launch
            }

            runCatching {
                RosterPattern.valueOf(savedRoster.pattern)
            }.getOrNull()?.let {
                selectedPattern = it
            }

            runCatching {
                LocalDate.parse(savedRoster.startDate)
            }.getOrNull()?.let {
                startDate = it
            }

            hasSavedRoster = true
        }
    }
}