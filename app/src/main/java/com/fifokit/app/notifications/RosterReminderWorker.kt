package com.fifokit.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class RosterReminderWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {

        val preferences = RosterPreferences(applicationContext)

        val savedRoster = preferences.savedRoster.first()
            ?: return Result.success()

        val pattern = runCatching {
            RosterPattern.valueOf(savedRoster.pattern)
        }.getOrNull() ?: return Result.success()

        val startDate = runCatching {
            LocalDate.parse(savedRoster.startDate)
        }.getOrNull() ?: return Result.success()

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        val todayIsWork =
            if (savedRoster.isCustomRoster) {
                RosterCalculator.isWorkDay(
                    date = today,
                    startDate = startDate,
                    workDays = savedRoster.customWorkDays,
                    offDays = savedRoster.customOffDays
                )
            } else {
                RosterCalculator.isWorkDay(
                    date = today,
                    startDate = startDate,
                    pattern = pattern
                )
            }

        val tomorrowIsWork =
            if (savedRoster.isCustomRoster) {
                RosterCalculator.isWorkDay(
                    date = tomorrow,
                    startDate = startDate,
                    workDays = savedRoster.customWorkDays,
                    offDays = savedRoster.customOffDays
                )
            } else {
                RosterCalculator.isWorkDay(
                    date = tomorrow,
                    startDate = startDate,
                    pattern = pattern
                )
            }

        if (todayIsWork == tomorrowIsWork) {
            return Result.success()
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        RosterNotificationManager.createChannel(applicationContext)

        val message = if (tomorrowIsWork) {
            "Tomorrow is your first WORK day"
        } else {
            "Tomorrow is your first day OFF"
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            RosterNotificationManager.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("FIFOKIT roster")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(1001, notification)

        return Result.success()
    }
}