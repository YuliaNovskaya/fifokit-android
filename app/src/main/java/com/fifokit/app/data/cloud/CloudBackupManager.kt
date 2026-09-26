package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.cloud.model.CloudSettings
import kotlinx.coroutines.flow.first

data class CloudBackupResult(
    val rostersBackedUp: Int,
    val backedUpAt: Long
)

class CloudBackupManager(
    context: Context,
    private val rosterRepository: RosterRepository,
    private val rosterPreferences: RosterPreferences,
    private val cloudRepository: CloudRepository = CloudRepository()
) {

    private val deviceIdProvider =
        DeviceIdProvider(context)

    suspend fun backup(
        uid: String
    ): CloudBackupResult {

        val deviceId =
            deviceIdProvider.getDeviceId()

        val rosters =
            rosterRepository.ensureCloudIds()

        val activeRosterId =
            rosterPreferences.activeRosterId.first()

        val selectedStates =
            rosterPreferences.selectedStates.first()

        val reminderSettings =
            rosterPreferences.reminderSettings.first()

        rosters.forEach { roster ->

            val cloudId =
                requireNotNull(roster.cloudId) {
                    "Roster ${roster.id} has no cloudId"
                }

            cloudRepository.saveRoster(
                uid = uid,
                roster = CloudRoster(
                    id = cloudId,
                    name = roster.name,
                    pattern = roster.pattern,
                    isCustomRoster = roster.isCustomRoster,
                    customWorkDays = roster.customWorkDays,
                    customOffDays = roster.customOffDays,
                    startDate = roster.startDate,
                    isActive = roster.id == activeRosterId,
                    createdAt = roster.createdAt,
                    updatedAt = roster.updatedAt,
                    deviceId = deviceId,
                    schemaVersion = 1
                )
            )
        }

        val backupTime =
            System.currentTimeMillis()

        val activeRosterCloudId =
            rosters
                .firstOrNull { it.id == activeRosterId }
                ?.cloudId
                .orEmpty()

        cloudRepository.saveSettings(
            uid = uid,
            settings = CloudSettings(
                activeRosterCloudId = activeRosterCloudId,
                selectedStates = selectedStates.sorted(),
                remindersEnabled = reminderSettings.enabled,
                workRemindersEnabled =
                    reminderSettings.workRemindersEnabled,
                offRemindersEnabled =
                    reminderSettings.offRemindersEnabled,
                reminderHour = reminderSettings.hour,
                reminderMinute = reminderSettings.minute,
                updatedAt = backupTime,
                deviceId = deviceId,
                schemaVersion = 1
            )
        )

        return CloudBackupResult(
            rostersBackedUp = rosters.size,
            backedUpAt = backupTime
        )
    }
}