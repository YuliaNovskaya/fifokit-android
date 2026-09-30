package com.fifokit.app.widgets

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

        updateWidget(
            context = appContext,
            widget = SwingStatusWidget()
        )

        updateWidget(
            context = appContext,
            widget = CompactRosterCalendarWidget()
        )
    }

    private suspend fun updateWidget(
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
            runCatching {
                widget.update(
                    context,
                    glanceId
                )
            }.onFailure { error ->
                Log.e(
                    TAG,
                    "Widget refresh failed for " +
                            widget.javaClass.simpleName,
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
