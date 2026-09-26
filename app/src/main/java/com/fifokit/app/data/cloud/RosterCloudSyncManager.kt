package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.local.RosterEntity
import kotlinx.coroutines.flow.first

data class RosterSyncResult(
    val uploaded: Int,
    val downloaded: Int,
    val unchanged: Int
)

class RosterCloudSyncManager(
    context: Context,
    private val rosterRepository: RosterRepository,
    private val rosterPreferences: RosterPreferences,
    private val cloudRepository: CloudRepository = CloudRepository()
) {

    private val deviceIdProvider =
        DeviceIdProvider(context)

    suspend fun sync(
        uid: String
    ): RosterSyncResult {

        val deviceId =
            deviceIdProvider.getDeviceId()

        val localRosters =
            rosterRepository.ensureCloudIds()

        val cloudRosters =
            cloudRepository.getRosters(uid)

        val activeRosterId =
            rosterPreferences.activeRosterId.first()

        val localByCloudId =
            localRosters.associateBy {
                requireNotNull(it.cloudId)
            }

        val cloudById =
            cloudRosters.associateBy { it.id }

        val allIds =
            localByCloudId.keys + cloudById.keys

        var uploaded = 0
        var downloaded = 0
        var unchanged = 0

        for (cloudId in allIds) {

            val local =
                localByCloudId[cloudId]

            val cloud =
                cloudById[cloudId]

            when {

                local != null && cloud == null -> {

                    cloudRepository.saveRoster(
                        uid = uid,
                        roster = local.toCloudRoster(
                            deviceId = deviceId,
                            isActive =
                                local.id == activeRosterId
                        )
                    )

                    uploaded++
                }

                local == null && cloud != null -> {

                    rosterRepository.upsertRosterFromCloud(
                        cloud.toRosterEntity()
                    )

                    downloaded++
                }

                local != null && cloud != null -> {

                    when {
                        local.updatedAt > cloud.updatedAt -> {

                            cloudRepository.saveRoster(
                                uid = uid,
                                roster = local.toCloudRoster(
                                    deviceId = deviceId,
                                    isActive =
                                        local.id == activeRosterId
                                )
                            )

                            uploaded++
                        }

                        cloud.updatedAt > local.updatedAt -> {

                            rosterRepository.upsertRosterFromCloud(
                                cloud.toRosterEntity()
                            )

                            downloaded++
                        }

                        else -> {
                            unchanged++
                        }
                    }
                }
            }
        }

        return RosterSyncResult(
            uploaded = uploaded,
            downloaded = downloaded,
            unchanged = unchanged
        )
    }

    private fun RosterEntity.toCloudRoster(
        deviceId: String,
        isActive: Boolean
    ): CloudRoster {

        return CloudRoster(
            id = requireNotNull(cloudId),
            name = name,
            pattern = pattern,
            isCustomRoster = isCustomRoster,
            customWorkDays = customWorkDays,
            customOffDays = customOffDays,
            startDate = startDate,
            isActive = isActive && !isDeleted,
            isDeleted = isDeleted,
            deletedAt = deletedAt ?: 0L,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deviceId = deviceId,
            schemaVersion = 1
        )
    }

    private fun CloudRoster.toRosterEntity(): RosterEntity {

        return RosterEntity(
            cloudId = id,
            name = name,
            pattern = pattern,
            startDate = startDate,
            isCustomRoster = isCustomRoster,
            customWorkDays = customWorkDays,
            customOffDays = customOffDays,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted,
            deletedAt = deletedAt.takeIf { it > 0L }
        )
    }
}