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
        customOffDays: Int
    ): Long {
        return rosterDao.insertRoster(
            RosterEntity(
                cloudId = UUID.randomUUID().toString(),
                name = name,
                pattern = pattern,
                startDate = startDate,
                isCustomRoster = isCustomRoster,
                customWorkDays = customWorkDays,
                customOffDays = customOffDays
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
        rosterDao.deleteRosterById(id)
    }

    suspend fun getRosterCount(): Int =
        rosterDao.getRosterCount()

    suspend fun ensureCloudIds(): List<RosterEntity> {

        val rosters = rosterDao.getAllRosters()

        rosters
            .filter { it.cloudId.isNullOrBlank() }
            .forEach { roster ->

                rosterDao.updateCloudId(
                    id = roster.id,
                    cloudId = UUID.randomUUID().toString()
                )
            }

        return rosterDao.getAllRosters()
    }

}