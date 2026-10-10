package com.fifokit.app.data

import com.fifokit.app.data.local.RosterDao
import com.fifokit.app.data.local.RosterEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RosterRepository(
    private val rosterDao: RosterDao
) {

    fun observeAllRosters(): Flow<List<RosterEntity>> =
        rosterDao.observeAllRosters()

    suspend fun getAllRosters(): List<RosterEntity> =
        rosterDao.getAllRosters()

    suspend fun getRosterById(id: Long): RosterEntity? =
        rosterDao.getRosterById(id)

    suspend fun createRoster(
        name: String,
        pattern: String,
        startDate: String,
        isCustomRoster: Boolean,
        customWorkDays: Int,
        customOffDays: Int,
        isShutdownRoster: Boolean = false,
        endDate: String? = null,
        scheduleSegmentsJson: String = "[]"
    ): Long {
        return rosterDao.insertRoster(
            RosterEntity(
                cloudId = UUID.randomUUID().toString(),
                name = name,
                pattern = pattern,
                startDate = startDate,
                isCustomRoster = isCustomRoster,
                customWorkDays = customWorkDays,
                customOffDays = customOffDays,
                isShutdownRoster = isShutdownRoster,
                endDate = endDate,
                scheduleSegmentsJson =
                    scheduleSegmentsJson
            )
        )
    }

    suspend fun updateRoster(roster: RosterEntity) {
        rosterDao.updateRoster(
            roster.copy(
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRoster(id: Long) {

        val now = System.currentTimeMillis()

        rosterDao.softDeleteRosterById(
            id = id,
            deletedAt = now
        )
    }

    suspend fun getRosterCount(): Int =
        rosterDao.getRosterCount()

    suspend fun ensureCloudIds(): List<RosterEntity> {

        val rosters =
            rosterDao.getAllRostersIncludingDeleted()

        rosters
            .filter { it.cloudId.isNullOrBlank() }
            .forEach { roster ->

                rosterDao.updateCloudId(
                    id = roster.id,
                    cloudId = UUID.randomUUID().toString()
                )
            }

        return rosterDao.getAllRostersIncludingDeleted()
    }
    suspend fun getRosterByCloudId(
        cloudId: String
    ): RosterEntity? =
        rosterDao.getRosterByCloudId(cloudId)
    suspend fun upsertRosterFromCloud(
        roster: RosterEntity
    ): Long {

        val cloudId = requireNotNull(roster.cloudId)

        val existing =
            rosterDao.getRosterByCloudIdIncludingDeleted(cloudId)

        return if (existing == null) {

            rosterDao.insertRoster(
                roster.copy(id = 0)
            )

        } else {

            rosterDao.updateRoster(
                roster.copy(
                    id = existing.id
                )
            )

            existing.id
        }
    }

    suspend fun getAllRostersIncludingDeleted():
            List<RosterEntity> =
        rosterDao.getAllRostersIncludingDeleted()

    suspend fun getRosterByCloudIdIncludingDeleted(
        cloudId: String
    ): RosterEntity? =
        rosterDao.getRosterByCloudIdIncludingDeleted(cloudId)

}