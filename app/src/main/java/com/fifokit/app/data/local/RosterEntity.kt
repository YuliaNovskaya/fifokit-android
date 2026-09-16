package com.fifokit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rosters")
data class RosterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val pattern: String,

    val startDate: String,

    val isCustomRoster: Boolean,

    val customWorkDays: Int,

    val customOffDays: Int,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)