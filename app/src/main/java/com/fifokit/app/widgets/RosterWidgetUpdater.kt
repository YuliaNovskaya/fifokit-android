package com.fifokit.app.widgets

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object RosterWidgetUpdater {

    private const val TAG = "FIFOKITWidget"
    private const val WORK_NAME =
        "fifokit_widget_refresh"

    fun updateAll(
        context: Context
    ) {
        val request =
            OneTimeWorkRequestBuilder<
                    RosterWidgetRefreshWorker
                    >()
                .build()

        WorkManager
            .getInstance(
                context.applicationContext
            )
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    internal suspend fun refreshNow(
        context: Context
    ) {
        val appContext =
            context.applicationContext

        refreshWidget(
            context = appContext,
            widget = SwingStatusWidget()
        )

        refreshWidget(
            context = appContext,
            widget =
                CompactRosterCalendarWidget()
        )
    }

    private suspend fun refreshWidget(
        context: Context,
        widget: GlanceAppWidget
    ) {
        val manager =
            GlanceAppWidgetManager(context)

        val glanceIds =
            manager.getGlanceIds(
                widget.javaClass
            )

        Log.d(
            TAG,
            "Refreshing " +
                    widget.javaClass.simpleName +
                    " count=" +
                    glanceIds.size
        )

        glanceIds.forEach { glanceId ->
            widget.update(
                context,
                glanceId
            )
        }
    }
}
