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
import com.fifokit.app.data.RosterMigration
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.local.RosterDatabase
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

        val preferences =
            RosterPreferences(applicationContext)

        val database =
            RosterDatabase.getInstance(applicationContext)

        val repository =
            RosterRepository(database.rosterDao())

        val migration =
            RosterMigration(
                rosterRepository = repository,
                rosterPreferences = preferences
            )

        migration.migrateLegacyRosterIfNeeded()

        val activeRosterId =
            preferences.activeRosterId.first()
                ?: return Result.success()

        val roster =
            repository.getRosterById(activeRosterId)
                ?: return Result.success()

        val reminderSettings =
            preferences.reminderSettings.first()

        if (!reminderSettings.enabled) {
            return Result.success()
        }

        val pattern = runCatching {
            RosterPattern.valueOf(roster.pattern)
        }.getOrNull() ?: return Result.success()

        val startDate = runCatching {
            LocalDate.parse(roster.startDate)
        }.getOrNull() ?: return Result.success()

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        val todayIsWork =
            if (roster.isCustomRoster) {
                RosterCalculator.isWorkDay(
                    date = today,
                    startDate = startDate,
                    workDays = roster.customWorkDays,
                    offDays = roster.customOffDays
                )
            } else {
                RosterCalculator.isWorkDay(
                    date = today,
                    startDate = startDate,
                    pattern = pattern
                )
            }

        val tomorrowIsWork =
            if (roster.isCustomRoster) {
                RosterCalculator.isWorkDay(
                    date = tomorrow,
                    startDate = startDate,
                    workDays = roster.customWorkDays,
                    offDays = roster.customOffDays
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
            tomorrowIsWork &&
            !reminderSettings.workRemindersEnabled
        ) {
            return Result.success()
        }

        if (
            !tomorrowIsWork &&
            !reminderSettings.offRemindersEnabled
        ) {
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

        RosterNotificationManager.createChannel(
            applicationContext
        )

        val message =
            if (tomorrowIsWork) {
                "Tomorrow is your first WORK day"
            } else {
                "Tomorrow is your first day OFF"
            }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                RosterNotificationManager.CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle("FIFOKIT roster")
                .setContentText(message)
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(1001, notification)

        return Result.success()
    }
}