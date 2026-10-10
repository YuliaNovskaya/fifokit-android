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
import com.fifokit.app.domain.roster.RosterSegmentType
import com.fifokit.app.domain.roster.RosterScheduleStatus
import com.fifokit.app.domain.roster.RosterScheduleSegment
import com.fifokit.app.domain.roster.RosterScheduleCodec
import com.fifokit.app.domain.roster.RosterScheduleCalculator
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
import com.fifokit.app.data.local.RosterEntity
import kotlinx.coroutines.flow.collectLatest
import com.fifokit.app.domain.pro.ProAccess
import com.fifokit.app.domain.pro.ProFeature
import com.fifokit.app.notifications.SharedTimeReminderScheduler
import com.fifokit.app.widgets.RosterWidgetUpdater
import com.fifokit.app.export.RosterExportData
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.growth.GrowthEngagementTracker
import com.fifokit.app.data.cloud.RosterSharingManager

@Composable
fun RosterSetupRoute(
    viewModel: RosterSetupViewModel,
    onGenerateRoster: () -> Unit = {},
    onBack: () -> Unit = {},
    onProRequested: (String) -> Unit = { _ -> }
) {
    RosterSetupScreen(
        rosterName = viewModel.rosterName,
        onRosterNameChanged = viewModel::updateRosterName,
        selectedRoster = viewModel.selectedPattern,
        isCustomRoster = viewModel.isCustomRoster,
        scheduleSegments = viewModel.scheduleSegments,
        startDate = viewModel.startDate,
        isShutdownRoster = viewModel.isShutdownRoster,
        endDate = viewModel.endDate,
        onRosterSelected = viewModel::selectPattern,
        onBack = onBack,
        onCustomRosterSelected = {
            if (
                ProAccess.canUse(
                    ProFeature.CUSTOM_ROSTER
                )
            ) {
                viewModel.selectCustomRoster()
            } else {
                onProRequested("custom_roster")
            }
        },
        onShutdownRosterSelected = {
            if (
                ProAccess.canUse(
                    ProFeature.CUSTOM_ROSTER
                )
            ) {
                viewModel.selectShutdownRoster()
            } else {
                onProRequested("shutdown_roster")
            }
        },
        onAddScheduleSegment =
            viewModel::addScheduleSegment,
        onUpdateScheduleSegmentType =
            viewModel::updateScheduleSegmentType,
        onUpdateScheduleSegmentDays =
            viewModel::updateScheduleSegmentDays,
        onRemoveScheduleSegment =
            viewModel::removeScheduleSegment,
        onStartDateSelected = viewModel::selectStartDate,
        showCancelNewRoster = viewModel.isCreatingNewRoster,
        onCancelNewRoster = {
            viewModel.cancelNewRoster()
            onGenerateRoster()
        },
        showCancelExistingRoster =
            !viewModel.isCreatingNewRoster &&
                    viewModel.hasSavedRoster == true,

        onCancelExistingRoster = {
            viewModel.cancelEditRoster()
            onGenerateRoster()
        },
        showResetRoster = viewModel.hasSavedRoster == true,
        onResetRoster = {
            viewModel.clearRoster { hasRemainingRoster ->
                if (hasRemainingRoster) {
                    onGenerateRoster()
                }
            }
        },
        onGenerateRoster = {
            if (
                viewModel.isCustomRoster &&
                !ProAccess.canUse(ProFeature.CUSTOM_ROSTER)
            ) {
                onProRequested("custom_roster")
            } else {
                viewModel.saveCurrentRoster()
                onGenerateRoster()
            }
        }
    )
}

class RosterSetupViewModel(
    application: Application
) : AndroidViewModel(application) {

    var selectedStates by mutableStateOf(setOf(AustralianState.WA))
        private set
    var isCreatingNewRoster by mutableStateOf(false)
        private set

    var sharedTimeRemindersEnabled by mutableStateOf(true)
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

            RosterWidgetUpdater.updateAll(
                getApplication()
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

    var activeRosterId by mutableStateOf<Long?>(null)
        private set

    private var editingRosterId: Long? = null

    var rosters by mutableStateOf<List<RosterEntity>>(emptyList())
        private set

    var shareCountByCloudId by
        mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    private val rosterSharingManager =
        RosterSharingManager()

    private val analytics = FirebaseAnalytics.getInstance(application)

    private val growthTracker =
        GrowthEngagementTracker(application)
    var selectedPattern by mutableStateOf(RosterPattern.TWO_ONE)
        private set

    var isCustomRoster by mutableStateOf(false)
        private set

    var customWorkDays by mutableStateOf(14)
        private set

    var customOffDays by mutableStateOf(7)
        private set

    var scheduleSegments by
        mutableStateOf(
            RosterScheduleCalculator
                .legacyRepeatingSequence(
                    workDays = 14,
                    offDays = 7
                )
        )
        private set

    var startDate by mutableStateOf(LocalDate.now())
        private set

    var isShutdownRoster by mutableStateOf(false)
        private set

    var endDate by mutableStateOf<LocalDate?>(null)
        private set

    var rosterName by mutableStateOf("My Roster")
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
        observeRosters()
        initialiseRoster()
        restoreSelectedStates()
        restoreReminderSettings()
    }

    fun clearRoster(
        onFinished: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {

            val rosterId = activeRosterId

            if (rosterId != null) {
                rosterRepository.deleteRoster(rosterId)
            }

            val remainingRosters =
                rosterRepository.getAllRosters()

            val nextRoster =
                remainingRosters.firstOrNull()

            if (nextRoster != null) {
                activeRosterId = nextRoster.id
                editingRosterId = nextRoster.id

                rosterPreferences.setActiveRosterId(
                    nextRoster.id
                )

                applyRoster(nextRoster)

                if (
                    remindersEnabled &&
                    (workRemindersEnabled ||
                            offRemindersEnabled)
                ) {
                    RosterReminderScheduler.schedule(
                        getApplication()
                    )
                }

            } else {
                rosterPreferences.setActiveRosterId(null)
                RosterReminderScheduler.cancel(
                    getApplication()
                )

                activeRosterId = null
                editingRosterId = null
                rosterName = "My Roster"
                selectedPattern = RosterPattern.TWO_ONE
                isCustomRoster = false
                customWorkDays = 14
                customOffDays = 7
                scheduleSegments =
                    RosterScheduleCalculator
                        .legacyRepeatingSequence(
                            workDays = 14,
                            offDays = 7
                        )
                startDate = LocalDate.now()
                isShutdownRoster = false
                endDate = null
                hasSavedRoster = false
            }

            RosterWidgetUpdater.updateAll(
                getApplication()
            )

            analytics.logEvent(
                AnalyticsEvents.ROSTER_DELETED,
                null
            )
            onFinished(nextRoster != null)
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
        isShutdownRoster = false
        endDate = null
    }

    fun selectCustomRoster() {
        if (
            !isCustomRoster ||
            scheduleSegments.isEmpty()
        ) {
            scheduleSegments =
                RosterScheduleCalculator
                    .legacyRepeatingSequence(
                        workDays =
                            customWorkDays,
                        offDays =
                            customOffDays
                    )
        }

        isCustomRoster = true
        isShutdownRoster = false
        endDate = null
        syncLegacyCustomFields()
    }

    fun selectShutdownRoster() {
        val wasCustom =
            isCustomRoster

        isCustomRoster = true
        isShutdownRoster = true

        if (
            !wasCustom ||
            scheduleSegments.isEmpty()
        ) {
            scheduleSegments =
                listOf(
                    RosterScheduleSegment(
                        type =
                            RosterSegmentType.WORK,
                        days = 7
                    )
                )
        }

        syncLegacyCustomFields()
        refreshShutdownEndDate()
    }

    fun addScheduleSegment(
        type: RosterSegmentType
    ) {
        scheduleSegments =
            scheduleSegments +
                    RosterScheduleSegment(
                        type = type,
                        days = 1
                    )

        syncLegacyCustomFields()
        refreshShutdownEndDate()
    }

    fun updateScheduleSegmentType(
        id: String,
        type: RosterSegmentType
    ) {
        scheduleSegments =
            scheduleSegments.map {
                if (it.id == id) {
                    it.copy(type = type)
                } else {
                    it
                }
            }

        syncLegacyCustomFields()
        refreshShutdownEndDate()
    }

    fun updateScheduleSegmentDays(
        id: String,
        days: Int
    ) {
        scheduleSegments =
            scheduleSegments.map {
                if (it.id == id) {
                    it.copy(
                        days =
                            days.coerceIn(
                                1,
                                99
                            )
                    )
                } else {
                    it
                }
            }

        syncLegacyCustomFields()
        refreshShutdownEndDate()
    }

    fun removeScheduleSegment(
        id: String
    ) {
        scheduleSegments =
            scheduleSegments.filterNot {
                it.id == id
            }

        syncLegacyCustomFields()
        refreshShutdownEndDate()
    }

    fun updateCustomWorkDays(days: Int) {
        customWorkDays =
            days.coerceIn(1, 99)
    }

    fun updateCustomOffDays(days: Int) {
        customOffDays =
            days.coerceIn(1, 99)
    }

    fun selectStartDate(date: LocalDate) {
        startDate = date
        refreshShutdownEndDate()
    }

    fun selectEndDate(date: LocalDate) {
        endDate = date
    }

    private fun syncLegacyCustomFields() {
        customWorkDays =
            scheduleSegments
                .firstOrNull {
                    it.isWork
                }
                ?.days
                ?: 1

        customOffDays =
            scheduleSegments
                .firstOrNull {
                    it.type ==
                            RosterSegmentType.OFF
                }
                ?.days
                ?: 1
    }

    private fun refreshShutdownEndDate() {
        endDate =
            if (isShutdownRoster) {
                RosterScheduleCalculator
                    .endDate(
                        startDate =
                            startDate,
                        segments =
                            scheduleSegments
                    )
            } else {
                null
            }
    }

    fun isRosterActive(date: LocalDate): Boolean {
        if (date.isBefore(startDate)) {
            return false
        }

        return if (isCustomRoster) {
            if (isShutdownRoster) {
                RosterScheduleCalculator
                    .statusOn(
                        date = date,
                        startDate = startDate,
                        segments =
                            scheduleSegments,
                        repeat = false
                    ) != null
            } else {
                scheduleSegments
                    .isNotEmpty()
            }
        } else {
            true
        }
    }

    fun scheduleStatusOn(
        date: LocalDate
    ): RosterScheduleStatus? {
        if (!isCustomRoster) {
            return null
        }

        return RosterScheduleCalculator
            .statusOn(
                date = date,
                startDate = startDate,
                segments =
                    scheduleSegments,
                repeat =
                    !isShutdownRoster
            )
    }

    fun isWorkDay(date: LocalDate): Boolean {
        if (!isRosterActive(date)) {
            return false
        }

        return if (isCustomRoster) {
            RosterScheduleCalculator
                .isWorkDay(
                    date = date,
                    startDate = startDate,
                    segments =
                        scheduleSegments,
                    repeat =
                        !isShutdownRoster
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
        if (
            isShutdownRoster &&
            endDate != null &&
            !fromDate.isBefore(endDate)
        ) {
            return endDate!!.plusDays(1)
        }

        val currentStatus = isWorkDay(fromDate)
        var date = fromDate.plusDays(1)

        while (isWorkDay(date) == currentStatus) {
            if (
                isShutdownRoster &&
                endDate != null &&
                date.isAfter(endDate)
            ) {
                return endDate!!.plusDays(1)
            }

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
                editingRosterId?.let { id ->
                    rosterRepository.getRosterById(id)
                }

            val isNewRoster = existingRoster == null

            val nameToSave =
                rosterName.trim().ifBlank {
                    "My Roster"
                }

            if (isCustomRoster) {
                syncLegacyCustomFields()
                refreshShutdownEndDate()
            }

            val scheduleJson =
                if (isCustomRoster) {
                    RosterScheduleCodec.encode(
                        scheduleSegments
                    )
                } else {
                    "[]"
                }

            if (existingRoster == null) {

                val newRosterId =
                    rosterRepository.createRoster(
                        name = nameToSave,
                        pattern = selectedPattern.name,
                        startDate = startDate.toString(),
                        isCustomRoster = isCustomRoster,
                        customWorkDays = customWorkDays,
                        customOffDays = customOffDays,
                        isShutdownRoster = isShutdownRoster,
                        endDate =
                            endDate?.toString(),
                        scheduleSegmentsJson =
                            scheduleJson
                    )

                activeRosterId = newRosterId
                editingRosterId = newRosterId
                rosterPreferences.setActiveRosterId(newRosterId)

            } else {

                rosterRepository.updateRoster(
                    existingRoster.copy(
                        name = nameToSave,
                        pattern = selectedPattern.name,
                        startDate = startDate.toString(),
                        isCustomRoster = isCustomRoster,
                        customWorkDays = customWorkDays,
                        customOffDays = customOffDays,
                        isShutdownRoster = isShutdownRoster,
                        endDate =
                            endDate?.toString(),
                        scheduleSegmentsJson =
                            scheduleJson,
                        shutdownsJson = "[]"
                    )
                )
            }

            isCreatingNewRoster = false

            RosterReminderScheduler.schedule(getApplication())

            RosterWidgetUpdater.updateAll(
                getApplication()
            )

            hasSavedRoster = true

            val eventName =
                if (isNewRoster) {
                    AnalyticsEvents.ROSTER_CREATED
                } else {
                    AnalyticsEvents.ROSTER_UPDATED
                }

            growthTracker.recordMeaningfulAction()

            analytics.logEvent(
                eventName,
                Bundle().apply {
                    putString(AnalyticsParams.PATTERN, selectedPattern.name)
                    putLong(
                        AnalyticsParams.STATE_COUNT,
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
                activeRosterId = null
                hasSavedRoster = false
                return@launch
            }

            activeRosterId = roster.id
            editingRosterId = roster.id
            applyRoster(roster)
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
            sharedTimeRemindersEnabled = settings.sharedTimeRemindersEnabled
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
                sharedTimeRemindersEnabled = sharedTimeRemindersEnabled,
                hour = reminderHour,
                minute = reminderMinute
            )

            rosterPreferences.saveReminderSettings(settings)

            if (
                settings.enabled &&
                settings.sharedTimeRemindersEnabled
            ) {
                SharedTimeReminderScheduler.schedule(
                    context = getApplication(),
                    hour = settings.hour,
                    minute = settings.minute
                )
            } else {
                SharedTimeReminderScheduler.cancel(
                    getApplication()
                )
            }

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
    private fun observeRosters() {
        viewModelScope.launch {
            rosterRepository
                .observeAllRosters()
                .collectLatest { rosterList ->
                    rosters = rosterList
                }
        }
    }

    fun refreshSharingStatus() {
        viewModelScope.launch {
            runCatching {
                rosterSharingManager
                    .reconcileCurrentEntitlement()

                rosterSharingManager
                    .getOwnerShares()
                    .filter { it.isActive }
                    .groupBy { it.rosterId }
                    .mapValues { (_, shares) ->
                        shares
                            .map { it.userId }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .size
                    }
            }.onSuccess { counts ->
                shareCountByCloudId = counts
            }
        }
    }

    fun switchRoster(id: Long) {
        viewModelScope.launch {
            val roster = rosterRepository.getRosterById(id)
                ?: return@launch

            activeRosterId = roster.id
            editingRosterId = roster.id
            rosterPreferences.setActiveRosterId(roster.id)

            applyRoster(roster)

            if (
                remindersEnabled &&
                (workRemindersEnabled || offRemindersEnabled)
            ) {
                RosterReminderScheduler.schedule(getApplication())
            }

            RosterWidgetUpdater.updateAll(
                getApplication()
            )

            isCreatingNewRoster = false
        }
    }

    private fun applyRoster(
        roster: RosterEntity
    ) {
        runCatching {
            RosterPattern.valueOf(roster.pattern)
        }.getOrNull()?.let {
            selectedPattern = it
        }
        rosterName = roster.name
        isCustomRoster = roster.isCustomRoster
        isShutdownRoster = roster.isShutdownRoster

        val parsedStartDate =
            runCatching {
                LocalDate.parse(
                    roster.startDate
                )
            }.getOrNull()
                ?: LocalDate.now()

        startDate =
            parsedStartDate

        val parsedEndDate =
            roster.endDate?.let {
                runCatching {
                    LocalDate.parse(it)
                }.getOrNull()
            }

        val decodedSegments =
            RosterScheduleCodec.decode(
                roster.scheduleSegmentsJson
            )

        scheduleSegments =
            when {
                decodedSegments.isNotEmpty() ->
                    decodedSegments

                roster.isCustomRoster &&
                        roster.isShutdownRoster &&
                        parsedEndDate != null ->
                    RosterScheduleCalculator
                        .legacyFiniteSequence(
                            startDate =
                                parsedStartDate,
                            endDate =
                                parsedEndDate,
                            workDays =
                                roster.customWorkDays,
                            offDays =
                                roster.customOffDays
                        )

                roster.isCustomRoster ->
                    RosterScheduleCalculator
                        .legacyRepeatingSequence(
                            workDays =
                                roster.customWorkDays,
                            offDays =
                                roster.customOffDays
                        )

                else ->
                    RosterScheduleCalculator
                        .legacyRepeatingSequence(
                            workDays = 14,
                            offDays = 7
                        )
            }

        customWorkDays =
            roster.customWorkDays
                .coerceAtLeast(1)

        customOffDays =
            roster.customOffDays
                .coerceAtLeast(1)

        if (roster.isCustomRoster) {
            syncLegacyCustomFields()
        }

        endDate =
            if (roster.isShutdownRoster) {
                RosterScheduleCalculator
                    .endDate(
                        startDate =
                            parsedStartDate,
                        segments =
                            scheduleSegments
                    )
            } else {
                null
            }

        hasSavedRoster = true
    }

    fun createNewRoster() {
        editingRosterId = null
        isCreatingNewRoster = true

        rosterName = "New Roster"
        selectedPattern = RosterPattern.TWO_ONE
        isCustomRoster = false
        customWorkDays = 14
        customOffDays = 7
        scheduleSegments =
            RosterScheduleCalculator
                .legacyRepeatingSequence(
                    workDays = 14,
                    offDays = 7
                )
        startDate = LocalDate.now()
        isShutdownRoster = false
        endDate = null

        hasSavedRoster = false
    }

    fun updateRosterName(name: String) {
        rosterName = name
    }

    fun cancelNewRoster() {
        viewModelScope.launch {
            val rosterId = activeRosterId

            if (rosterId != null) {
                val roster = rosterRepository.getRosterById(rosterId)

                if (roster != null) {
                    editingRosterId = roster.id
                    isCreatingNewRoster = false
                    applyRoster(roster)
                    return@launch
                }
            }

            isCreatingNewRoster = false
            hasSavedRoster = false
        }
    }
    fun cancelEditRoster() {
        viewModelScope.launch {
            val rosterId = activeRosterId
                ?: return@launch

            val roster = rosterRepository.getRosterById(rosterId)
                ?: return@launch

            editingRosterId = roster.id
            isCreatingNewRoster = false

            applyRoster(roster)
        }
    }
    fun refreshFromLocalStorage() {
        viewModelScope.launch {

            val savedStates =
                rosterPreferences.selectedStates.first()

            selectedStates =
                savedStates
                    .mapNotNull { stateName ->
                        runCatching {
                            AustralianState.valueOf(stateName)
                        }.getOrNull()
                    }
                    .toSet()

            val settings =
                rosterPreferences.reminderSettings.first()

            remindersEnabled = settings.enabled
            workRemindersEnabled =
                settings.workRemindersEnabled
            offRemindersEnabled =
                settings.offRemindersEnabled
            reminderHour = settings.hour
            reminderMinute = settings.minute

            var rosterId =
                rosterPreferences.activeRosterId.first()

            var roster =
                rosterId?.let {
                    rosterRepository.getRosterById(it)
                }

            if (roster == null) {

                roster =
                    rosterRepository
                        .getAllRosters()
                        .firstOrNull()

                rosterId = roster?.id

                rosterPreferences.setActiveRosterId(
                    rosterId
                )
            }

            if (roster != null) {

                activeRosterId = roster.id
                editingRosterId = roster.id
                applyRoster(roster)

            } else {

                activeRosterId = null
                editingRosterId = null

                rosterName = "My Roster"
                selectedPattern = RosterPattern.TWO_ONE
                isCustomRoster = false
                customWorkDays = 14
                customOffDays = 7
                scheduleSegments =
                    RosterScheduleCalculator
                        .legacyRepeatingSequence(
                            workDays = 14,
                            offDays = 7
                        )
                startDate = LocalDate.now()
                isShutdownRoster = false
                endDate = null
                hasSavedRoster = false
            }

            RosterWidgetUpdater.updateAll(
                getApplication()
            )
        }
    }

    fun rosterExportData(): RosterExportData {
        val workDays =
            if (isCustomRoster) {
                customWorkDays
            } else {
                selectedPattern.workDays
            }

        val offDays =
            if (isCustomRoster) {
                customOffDays
            } else {
                selectedPattern.offDays
            }

        return RosterExportData(
            rosterName = rosterName,
            startDate = startDate,
            workDays = workDays,
            offDays = offDays,
            selectedStates = selectedStates,
            isShutdownRoster = isShutdownRoster,
            endDate = endDate
        )
    }

    fun updateSharedTimeRemindersEnabled(
        enabled: Boolean
    ) {
        sharedTimeRemindersEnabled = enabled
        saveReminderSettings()
    }

}