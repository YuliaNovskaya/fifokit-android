package com.fifokit.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object RosterReminderScheduler {

    private const val WORK_NAME = "roster_reminder"

    fun schedule(context: Context) {
        val now = ZonedDateTime.now()

        var nextRun = now
            .withHour(19)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        val initialDelay =
            Duration.between(now, nextRun).toMinutes()

        val request =
            PeriodicWorkRequestBuilder<RosterReminderWorker>(
                24,
                TimeUnit.HOURS
            )
                .setInitialDelay(
                    initialDelay,
                    TimeUnit.MINUTES
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WORK_NAME)
    }
}