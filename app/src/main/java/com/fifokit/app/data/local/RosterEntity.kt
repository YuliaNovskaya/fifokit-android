package com.fifokit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
@Entity(
    tableName = "rosters",
    indices = [
        Index(
            value = ["cloudId"],
            unique = true
        )
    ]
)
data class RosterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cloudId: String? = null,
    val name: String,

    val pattern: String,

    val startDate: String,

    val isCustomRoster: Boolean,

    val customWorkDays: Int,

    val customOffDays: Int,

    val isShutdownRoster: Boolean = false,

    val endDate: String? = null,

    // Retained only for Room v4 compatibility. No longer used.
    val shutdownsJson: String = "[]",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,

    val deletedAt: Long? = null
)