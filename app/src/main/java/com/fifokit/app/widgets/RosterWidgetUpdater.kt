package com.fifokit.app.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object RosterWidgetUpdater {

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

        SwingStatusWidget()
            .updateAll(appContext)

        CompactRosterCalendarWidget()
            .updateAll(appContext)
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
