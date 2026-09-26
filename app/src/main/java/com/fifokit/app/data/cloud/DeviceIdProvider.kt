package com.fifokit.app.data.cloud

import android.content.Context
import java.util.UUID

class DeviceIdProvider(
    context: Context
) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "cloud_sync",
            Context.MODE_PRIVATE
        )

    fun getDeviceId(): String {

        val existing =
            preferences.getString("device_id", null)

        if (!existing.isNullOrBlank()) {
            return existing
        }

        val deviceId = UUID.randomUUID().toString()

        preferences
            .edit()
            .putString("device_id", deviceId)
            .apply()

        return deviceId
    }
}