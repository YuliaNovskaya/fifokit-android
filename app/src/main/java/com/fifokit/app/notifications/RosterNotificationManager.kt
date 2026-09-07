package com.fifokit.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object RosterNotificationManager {

    const val CHANNEL_ID = "roster_changes"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Roster reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders before your work and off periods change"
            }

            val notificationManager =
                context.getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }
}