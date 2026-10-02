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
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.SharedRosterManager
import com.fifokit.app.data.local.RosterDatabase
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import com.fifokit.app.domain.roster.ShutdownPeriodCodec
import com.fifokit.app.domain.roster.shutdownOn
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class SharedTimeReminderWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {

        val preferences =
            RosterPreferences(applicationContext)

        val settings =
            preferences.reminderSettings.first()

        if (
            !settings.enabled ||
            !settings.sharedTimeRemindersEnabled
        ) {
            return Result.success()
        }

        val activeRosterId =
            preferences.activeRosterId.first()
                ?: return Result.success()

        val database =
            RosterDatabase.getInstance(
                applicationContext
            )

        val repository =
            RosterRepository(
                database.rosterDao()
            )

        val myRoster =
            repository.getRosterById(
                activeRosterId
            ) ?: return Result.success()

        val sharedRosters =
            runCatching {
                SharedRosterManager()
                    .loadSharedRosters()
            }.getOrElse {
                return Result.retry()
            }

        if (sharedRosters.isEmpty()) {
            return Result.success()
        }

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        val myShutdowns =
            ShutdownPeriodCodec.decode(
                myRoster.shutdownsJson
            )

        fun myIsWorkDay(
            date: LocalDate
        ): Boolean {

            if (
                myShutdowns.shutdownOn(date) !=
                null
            ) {
                return false
            }

            val startDate =
                runCatching {
                    LocalDate.parse(
                        myRoster.startDate
                    )
                }.getOrNull()
                    ?: return false

            return if (myRoster.isCustomRoster) {

                RosterCalculator.isWorkDay(
                    date = date,
                    startDate = startDate,
                    workDays =
                        myRoster.customWorkDays,
                    offDays =
                        myRoster.customOffDays
                )

            } else {

                val pattern =
                    runCatching {
                        RosterPattern.valueOf(
                            myRoster.pattern
                        )
                    }.getOrNull()
                        ?: return false

                RosterCalculator.isWorkDay(
                    date = date,
                    startDate = startDate,
                    pattern = pattern
                )
            }
        }

        val startingSharedTime =
            sharedRosters.firstOrNull {
                    sharedRoster ->

                val roster =
                    sharedRoster.roster

                val partnerStartDate =
                    runCatching {
                        LocalDate.parse(
                            roster.startDate
                        )
                    }.getOrNull()
                        ?: return@firstOrNull false

                val partnerShutdowns =
                    ShutdownPeriodCodec.decode(
                        roster.shutdownsJson
                    )

                fun partnerIsWorkDay(
                    date: LocalDate
                ): Boolean {

                    if (
                        partnerShutdowns
                            .shutdownOn(date) !=
                        null
                    ) {
                        return false
                    }

                    return if (
                        roster.isCustomRoster
                    ) {

                        RosterCalculator.isWorkDay(
                            date = date,
                            startDate =
                                partnerStartDate,
                            workDays =
                                roster.customWorkDays,
                            offDays =
                                roster.customOffDays
                        )

                    } else {

                        val pattern =
                            runCatching {
                                RosterPattern.valueOf(
                                    roster.pattern
                                )
                            }.getOrNull()
                                ?: return true

                        RosterCalculator.isWorkDay(
                            date = date,
                            startDate =
                                partnerStartDate,
                            pattern = pattern
                        )
                    }
                }

                val bothOffToday =
                    !myIsWorkDay(today) &&
                            !partnerIsWorkDay(today)

                val bothOffTomorrow =
                    !myIsWorkDay(tomorrow) &&
                            !partnerIsWorkDay(tomorrow)

                !bothOffToday &&
                        bothOffTomorrow
            }

        if (startingSharedTime == null) {
            return Result.success()
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        RosterNotificationManager
            .createSharedTimeChannel(
                applicationContext
            )

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                RosterNotificationManager
                    .SHARED_TIME_CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "Shared time off tomorrow"
                )
                .setContentText(
                    "You and ${startingSharedTime.roster.name} are both off tomorrow."
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                SHARED_TIME_NOTIFICATION_ID,
                notification
            )

        return Result.success()
    }

    companion object {
        private const val
                SHARED_TIME_NOTIFICATION_ID = 2201
    }
}