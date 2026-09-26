package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.ReminderSettings
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.model.CloudSettings
import kotlinx.coroutines.flow.first

data class SettingsSyncResult(
    val uploaded: Boolean,
    val downloaded: Boolean
)

class SettingsCloudSyncManager(
    context: Context,
    private val rosterRepository: RosterRepository,
    private val rosterPreferences: RosterPreferences,
    private val cloudRepository: CloudRepository = CloudRepository()
) {

    private val deviceIdProvider =
        DeviceIdProvider(context)

    suspend fun sync(
        uid: String
    ): SettingsSyncResult {

        val deviceId =
            deviceIdProvider.getDeviceId()

        val cloudSettings =
            cloudRepository.getSettings(uid)

        val localUpdatedAt =
            rosterPreferences.settingsUpdatedAt.first()

        val selectedStates =
            rosterPreferences.selectedStates.first()

        val reminderSettings =
            rosterPreferences.reminderSettings.first()

        val activeRosterId =
            rosterPreferences.activeRosterId.first()

        val activeRosterCloudId =
            activeRosterId
                ?.let { rosterRepository.getRosterById(it) }
                ?.cloudId
                .orEmpty()

        if (cloudSettings == null) {

            val now =
                if (localUpdatedAt > 0L) {
                    localUpdatedAt
                } else {
                    System.currentTimeMillis()
                }

            cloudRepository.saveSettings(
                uid = uid,
                settings = CloudSettings(
                    activeRosterCloudId =
                        activeRosterCloudId,
                    selectedStates =
                        selectedStates.sorted(),
                    remindersEnabled =
                        reminderSettings.enabled,
                    workRemindersEnabled =
                        reminderSettings.workRemindersEnabled,
                    offRemindersEnabled =
                        reminderSettings.offRemindersEnabled,
                    reminderHour =
                        reminderSettings.hour,
                    reminderMinute =
                        reminderSettings.minute,
                    updatedAt = now,
                    deviceId = deviceId,
                    schemaVersion = 1
                )
            )

            return SettingsSyncResult(
                uploaded = true,
                downloaded = false
            )
        }

        if (cloudSettings.updatedAt > localUpdatedAt) {

            val cloudActiveRosterId =
                cloudSettings
                    .activeRosterCloudId
                    .takeIf { it.isNotBlank() }
                    ?.let { cloudId ->
                        rosterRepository
                            .getRosterByCloudId(cloudId)
                            ?.id
                    }

            rosterPreferences.applyCloudSettings(
                selectedStates =
                    cloudSettings.selectedStates.toSet(),
                reminderSettings =
                    ReminderSettings(
                        enabled =
                            cloudSettings.remindersEnabled,
                        workRemindersEnabled =
                            cloudSettings.workRemindersEnabled,
                        offRemindersEnabled =
                            cloudSettings.offRemindersEnabled,
                        hour =
                            cloudSettings.reminderHour,
                        minute =
                            cloudSettings.reminderMinute
                    ),
                activeRosterId =
                    cloudActiveRosterId,
                updatedAt =
                    cloudSettings.updatedAt
            )

            return SettingsSyncResult(
                uploaded = false,
                downloaded = true
            )
        }

        if (localUpdatedAt > cloudSettings.updatedAt) {

            cloudRepository.saveSettings(
                uid = uid,
                settings = CloudSettings(
                    activeRosterCloudId =
                        activeRosterCloudId,
                    selectedStates =
                        selectedStates.sorted(),
                    remindersEnabled =
                        reminderSettings.enabled,
                    workRemindersEnabled =
                        reminderSettings.workRemindersEnabled,
                    offRemindersEnabled =
                        reminderSettings.offRemindersEnabled,
                    reminderHour =
                        reminderSettings.hour,
                    reminderMinute =
                        reminderSettings.minute,
                    updatedAt =
                        localUpdatedAt,
                    deviceId =
                        deviceId,
                    schemaVersion = 1
                )
            )

            return SettingsSyncResult(
                uploaded = true,
                downloaded = false
            )
        }

        return SettingsSyncResult(
            uploaded = false,
            downloaded = false
        )
    }
}