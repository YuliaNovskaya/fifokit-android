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
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

@Composable
fun RosterSetupRoute(
    viewModel: RosterSetupViewModel,
    onGenerateRoster: () -> Unit = {}
) {
    RosterSetupScreen(
        selectedRoster = viewModel.selectedPattern,
        isCustomRoster = viewModel.isCustomRoster,
        customWorkDays = viewModel.customWorkDays,
        customOffDays = viewModel.customOffDays,
        startDate = viewModel.startDate,
        onRosterSelected = viewModel::selectPattern,
        onCustomRosterSelected = viewModel::selectCustomRoster,
        onCustomWorkDaysChanged = viewModel::updateCustomWorkDays,
        onCustomOffDaysChanged = viewModel::updateCustomOffDays,
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

        viewModelScope.launch {
            rosterPreferences.saveSelectedStates(
                selectedStates.map { it.name }.toSet()
            )
        }
    }
    private val rosterPreferences = RosterPreferences(application)

    private val analytics = FirebaseAnalytics.getInstance(application)
    var selectedPattern by mutableStateOf(RosterPattern.TWO_ONE)
        private set

    var isCustomRoster by mutableStateOf(false)
        private set

    var customWorkDays by mutableStateOf(14)
        private set

    var customOffDays by mutableStateOf(7)
        private set

    var startDate by mutableStateOf(LocalDate.now())
        private set

    var hasSavedRoster by mutableStateOf<Boolean?>(null)
        private set

    init {
        restoreRoster()
        restoreSelectedStates()
    }

    fun clearRoster() {
        viewModelScope.launch {
            rosterPreferences.clearRoster()
            RosterReminderScheduler.cancel(getApplication())
            selectedPattern = RosterPattern.TWO_ONE
            isCustomRoster = false
            customWorkDays = 14
            customOffDays = 7
            startDate = LocalDate.now()
            hasSavedRoster = false
            analytics.logEvent("roster_reset", null)
        }
    }

    fun selectPattern(pattern: RosterPattern) {
        selectedPattern = pattern
        isCustomRoster = false
    }

    fun selectCustomRoster() {
        isCustomRoster = true
    }

    fun updateCustomWorkDays(days: Int) {
        customWorkDays = days.coerceIn(1, 99)
    }

    fun updateCustomOffDays(days: Int) {
        customOffDays = days.coerceIn(1, 99)
    }

    fun selectStartDate(date: LocalDate) {
        startDate = date
    }

    fun isWorkDay(date: LocalDate): Boolean {
        return if (isCustomRoster) {
            RosterCalculator.isWorkDay(
                date = date,
                startDate = startDate,
                workDays = customWorkDays,
                offDays = customOffDays
            )
        } else {
            RosterCalculator.isWorkDay(
                date = date,
                startDate = startDate,
                pattern = selectedPattern
            )
        }
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

            val isNewRoster = rosterPreferences.savedRoster.first() == null

            rosterPreferences.saveRoster(
                pattern = selectedPattern.name,
                startDate = startDate.toString(),
                isCustomRoster = isCustomRoster,
                customWorkDays = customWorkDays,
                customOffDays = customOffDays
            )

            RosterReminderScheduler.schedule(getApplication())

            hasSavedRoster = true

            val eventName =
                if (isNewRoster) "roster_created"
                else "roster_updated"

            analytics.logEvent(
                eventName,
                Bundle().apply {
                    putString("pattern", selectedPattern.name)
                    putLong("state_count", selectedStates.size.toLong())
                }
            )
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

            isCustomRoster = savedRoster.isCustomRoster
            customWorkDays = savedRoster.customWorkDays
            customOffDays = savedRoster.customOffDays

            runCatching {
                LocalDate.parse(savedRoster.startDate)
            }.getOrNull()?.let {
                startDate = it
            }

            hasSavedRoster = true
        }
    }
    private fun restoreSelectedStates() {
        viewModelScope.launch {
            val savedStates = rosterPreferences.selectedStates.first()

            selectedStates = savedStates
                .mapNotNull { stateName ->
                    runCatching {
                        AustralianState.valueOf(stateName)
                    }.getOrNull()
                }
                .toSet()
        }
    }
}