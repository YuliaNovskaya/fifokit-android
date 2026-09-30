package com.fifokit.app.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object RosterWidgetUpdater {

    private const val TAG = "FIFOKITWidget"

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Default
        )

    suspend fun updateAll(
        context: Context
    ) {
        val appContext =
            context.applicationContext

        updateProvider(
            context = appContext,
            receiverClass =
                SwingStatusWidgetReceiver::class.java,
            widget = SwingStatusWidget()
        )

        updateProvider(
            context = appContext,
            receiverClass =
                CompactRosterCalendarWidgetReceiver::class.java,
            widget = CompactRosterCalendarWidget()
        )
    }

    private suspend fun updateProvider(
        context: Context,
        receiverClass: Class<*>,
        widget: GlanceAppWidget
    ) {
        val appWidgetManager =
            AppWidgetManager.getInstance(context)

        val glanceManager =
            GlanceAppWidgetManager(context)

        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    receiverClass
                )
            )

        Log.d(
            TAG,
            "Refreshing " +
                    receiverClass.simpleName +
                    " count=" +
                    appWidgetIds.size
        )

        appWidgetIds.forEach { appWidgetId ->
            runCatching {
                val glanceId =
                    glanceManager.getGlanceIdBy(
                        appWidgetId
                    )

                widget.update(
                    context,
                    glanceId
                )
            }.onFailure { error ->
                Log.e(
                    TAG,
                    "Widget refresh failed for " +
                            receiverClass.simpleName +
                            " id=" +
                            appWidgetId,
                    error
                )
            }
        }
    }

    fun updateAllAsync(
        context: Context
    ) {
        val appContext =
            context.applicationContext

        scope.launch {
            updateAll(appContext)
        }
    }
}
