package com.fifokit.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RosterDao {

    @Query("SELECT * FROM rosters ORDER BY createdAt ASC")
    fun observeAllRosters(): Flow<List<RosterEntity>>

    @Query("SELECT * FROM rosters ORDER BY createdAt ASC")
    suspend fun getAllRosters(): List<RosterEntity>

    @Query("SELECT * FROM rosters WHERE id = :id LIMIT 1")
    suspend fun getRosterById(id: Long): RosterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoster(roster: RosterEntity): Long

    @Update
    suspend fun updateRoster(roster: RosterEntity)

    @Delete
    suspend fun deleteRoster(roster: RosterEntity)

    @Query("DELETE FROM rosters WHERE id = :id")
    suspend fun deleteRosterById(id: Long)

    @Query("SELECT COUNT(*) FROM rosters")
    suspend fun getRosterCount(): Int
}