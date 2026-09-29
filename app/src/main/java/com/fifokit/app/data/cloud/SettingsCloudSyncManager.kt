package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.ReminderSettings
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.model.CloudSettings
import kotlinx.coroutines.flow.first
import android.util.Log

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

        Log.d("SettingsSync", "1 START")

        val deviceId =
            deviceIdProvider.getDeviceId()

        Log.d("SettingsSync", "2 deviceId done")

        val cloudSettings =
            cloudRepository.getSettings(uid)

        Log.d(
            "SettingsSync",
            "3 getSettings done, exists=${cloudSettings != null}"
        )

        val localUpdatedAt =
            rosterPreferences.settingsUpdatedAt.first()

        Log.d(
            "SettingsSync",
            "4 settingsUpdatedAt done: $localUpdatedAt"
        )

        val selectedStates =
            rosterPreferences.selectedStates.first()

        Log.d("SettingsSync", "5 selectedStates done")

        val reminderSettings =
            rosterPreferences.reminderSettings.first()

        Log.d("SettingsSync", "6 reminderSettings done")

        val activeRosterId =
            rosterPreferences.activeRosterId.first()

        Log.d("SettingsSync", "7 activeRosterId done")

        val activeRosterCloudId =
            activeRosterId
                ?.let {
                    rosterRepository.getRosterById(it)
                }
                ?.cloudId
                .orEmpty()

        Log.d("SettingsSync", "8 roster lookup done")

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

            Log.d("SettingsSync", "9 applying cloud settings")

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
            Log.d("SettingsSync", "10 applyCloudSettings done")
            return SettingsSyncResult(
                uploaded = false,
                downloaded = true
            )
        }

        if (localUpdatedAt > cloudSettings.updatedAt) {
            Log.d("SettingsSync", "9 saving cloud settings")
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
            Log.d("SettingsSync", "10 saveSettings done")
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