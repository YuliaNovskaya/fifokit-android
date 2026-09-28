package com.fifokit.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
object RosterNotificationManager {

    const val CHANNEL_ID = "roster_changes"

    const val SHARED_TIME_CHANNEL_ID = "shared_time"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Roster reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders before your work and off periods change"
            }

            val notificationManager =
                context.getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createSharedTimeChannel(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            val channel =
                NotificationChannel(
                    SHARED_TIME_CHANNEL_ID,
                    "Shared time reminders",
                    NotificationManager
                        .IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Reminders when shared time off begins"
                }

            val notificationManager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            notificationManager
                .createNotificationChannel(
                    channel
                )
        }
    }

}