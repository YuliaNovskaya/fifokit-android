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
import com.fifokit.app.data.ReminderSettings
import com.fifokit.app.data.RosterMigration
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.local.RosterDatabase

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

    private val rosterDatabase =
        RosterDatabase.getInstance(application)

    private val rosterRepository =
        RosterRepository(rosterDatabase.rosterDao())

    private val rosterMigration =
        RosterMigration(
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )

    private var activeRosterId: Long? = null

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
    var remindersEnabled by mutableStateOf(true)
        private set

    var workRemindersEnabled by mutableStateOf(true)
        private set

    var offRemindersEnabled by mutableStateOf(true)
        private set

    var reminderHour by mutableStateOf(19)
        private set

    var reminderMinute by mutableStateOf(0)
        private set

    init {
        initialiseRoster()
        restoreSelectedStates()
        restoreReminderSettings()
    }

    fun clearRoster() {
        viewModelScope.launch {

            activeRosterId?.let { id ->
                rosterRepository.deleteRoster(id)
            }

            rosterPreferences.setActiveRosterId(null)

            // Remove the temporary legacy copy too.
            rosterPreferences.clearRoster()

            RosterReminderScheduler.cancel(getApplication())

            activeRosterId = null
            selectedPattern = RosterPattern.TWO_ONE
            isCustomRoster = false
            customWorkDays = 14
            customOffDays = 7
            startDate = LocalDate.now()
            hasSavedRoster = false

            analytics.logEvent("roster_reset", null)
        }
    }

    fun updateRemindersEnabled(enabled: Boolean) {
        remindersEnabled = enabled
        saveReminderSettings()
    }

    fun updateWorkRemindersEnabled(enabled: Boolean) {
        workRemindersEnabled = enabled
        saveReminderSettings()
    }

    fun updateOffRemindersEnabled(enabled: Boolean) {
        offRemindersEnabled = enabled
        saveReminderSettings()
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        reminderHour = hour
        reminderMinute = minute
        saveReminderSettings()
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

            val existingRoster =
                activeRosterId?.let { id ->
                    rosterRepository.getRosterById(id)
                }

            val isNewRoster = existingRoster == null

            if (existingRoster == null) {

                val newRosterId =
                    rosterRepository.createRoster(
                        name = "My Roster",
                        pattern = selectedPattern.name,
                        startDate = startDate.toString(),
                        isCustomRoster = isCustomRoster,
                        customWorkDays = customWorkDays,
                        customOffDays = customOffDays
                    )

                activeRosterId = newRosterId
                rosterPreferences.setActiveRosterId(newRosterId)

            } else {

                rosterRepository.updateRoster(
                    existingRoster.copy(
                        pattern = selectedPattern.name,
                        startDate = startDate.toString(),
                        isCustomRoster = isCustomRoster,
                        customWorkDays = customWorkDays,
                        customOffDays = customOffDays
                    )
                )
            }

            RosterReminderScheduler.schedule(getApplication())

            hasSavedRoster = true

            val eventName =
                if (isNewRoster) {
                    "roster_created"
                } else {
                    "roster_updated"
                }

            analytics.logEvent(
                eventName,
                Bundle().apply {
                    putString("pattern", selectedPattern.name)
                    putLong(
                        "state_count",
                        selectedStates.size.toLong()
                    )
                }
            )
        }
    }

    private fun initialiseRoster() {
        viewModelScope.launch {

            rosterMigration.migrateLegacyRosterIfNeeded()

            var rosterId =
                rosterPreferences.activeRosterId.first()

            var roster =
                rosterId?.let { id ->
                    rosterRepository.getRosterById(id)
                }

            // Safety fallback if an active ID was lost but
            // Room still contains a roster.
            if (roster == null) {
                roster =
                    rosterRepository
                        .getAllRosters()
                        .firstOrNull()

                if (roster != null) {
                    rosterId = roster.id
                    rosterPreferences.setActiveRosterId(roster.id)
                }
            }

            if (roster == null) {
                activeRosterId = null
                hasSavedRoster = false
                return@launch
            }

            activeRosterId = rosterId

            runCatching {
                RosterPattern.valueOf(roster.pattern)
            }.getOrNull()?.let {
                selectedPattern = it
            }

            isCustomRoster = roster.isCustomRoster
            customWorkDays = roster.customWorkDays
            customOffDays = roster.customOffDays

            runCatching {
                LocalDate.parse(roster.startDate)
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
    private fun restoreReminderSettings() {
        viewModelScope.launch {
            val settings = rosterPreferences.reminderSettings.first()

            remindersEnabled = settings.enabled
            workRemindersEnabled = settings.workRemindersEnabled
            offRemindersEnabled = settings.offRemindersEnabled
            reminderHour = settings.hour
            reminderMinute = settings.minute
        }
    }

    private fun saveReminderSettings() {
        viewModelScope.launch {
            val settings = ReminderSettings(
                enabled = remindersEnabled,
                workRemindersEnabled = workRemindersEnabled,
                offRemindersEnabled = offRemindersEnabled,
                hour = reminderHour,
                minute = reminderMinute
            )

            rosterPreferences.saveReminderSettings(settings)

            val rosterId =
                activeRosterId
                    ?: rosterPreferences.activeRosterId.first()

            val rosterExists =
                rosterId != null &&
                        rosterRepository.getRosterById(rosterId) != null

            if (
                settings.enabled &&
                (settings.workRemindersEnabled ||
                        settings.offRemindersEnabled) &&
                rosterExists
            ) {
                RosterReminderScheduler.schedule(getApplication())
            } else {
                RosterReminderScheduler.cancel(getApplication())
            }
        }
    }

}