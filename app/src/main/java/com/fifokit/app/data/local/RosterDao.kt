package com.fifokit.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RosterDao {

    @Query(
        "SELECT * FROM rosters " +
                "WHERE isDeleted = 0 " +
                "ORDER BY createdAt ASC"
    )
    fun observeAllRosters(): Flow<List<RosterEntity>>

    @Query(
        "SELECT * FROM rosters " +
                "WHERE isDeleted = 0 " +
                "ORDER BY createdAt ASC"
    )
    suspend fun getAllRosters(): List<RosterEntity>

    @Query(
        "SELECT * FROM rosters " +
                "WHERE id = :id AND isDeleted = 0 LIMIT 1"
    )
    suspend fun getRosterById(id: Long): RosterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoster(roster: RosterEntity): Long

    @Update
    suspend fun updateRoster(roster: RosterEntity)


    @Query(
        """
    UPDATE rosters
    SET isDeleted = 1,
        deletedAt = :deletedAt,
        updatedAt = :deletedAt
    WHERE id = :id
    """
    )
    suspend fun softDeleteRosterById(
        id: Long,
        deletedAt: Long
    )

    @Query(
        "SELECT COUNT(*) FROM rosters WHERE isDeleted = 0"
    )
    suspend fun getRosterCount(): Int

    @Query("UPDATE rosters SET cloudId = :cloudId WHERE id = :id")
    suspend fun updateCloudId(
        id: Long,
        cloudId: String
    )

    @Query(
        "SELECT * FROM rosters " +
                "WHERE cloudId = :cloudId " +
                "AND isDeleted = 0 LIMIT 1"
    )
    suspend fun getRosterByCloudId(
        cloudId: String
    ): RosterEntity?

    @Query("SELECT * FROM rosters ORDER BY createdAt ASC")
    suspend fun getAllRostersIncludingDeleted():
            List<RosterEntity>

    @Query(
        "SELECT * FROM rosters " +
                "WHERE cloudId = :cloudId LIMIT 1"
    )
    suspend fun getRosterByCloudIdIncludingDeleted(
        cloudId: String
    ): RosterEntity?

}