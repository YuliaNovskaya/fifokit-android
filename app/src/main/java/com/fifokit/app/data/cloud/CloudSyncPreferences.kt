package com.fifokit.app.data.cloud

import android.content.Context

class CloudSyncPreferences(
    context: Context
) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "cloud_sync",
            Context.MODE_PRIVATE
        )

    fun getLastBackupAt(): Long? {
        val value =
            preferences.getLong(
                "last_backup_at",
                0L
            )

        return value.takeIf { it > 0L }
    }

    fun setLastBackupAt(timestamp: Long) {
        preferences
            .edit()
            .putLong(
                "last_backup_at",
                timestamp
            )
            .apply()
    }
}