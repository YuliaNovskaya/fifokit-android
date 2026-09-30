package com.fifokit.app.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
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

        requestProviderUpdate(
            context = appContext,
            receiverClass =
                SwingStatusWidgetReceiver::class.java
        )

        requestProviderUpdate(
            context = appContext,
            receiverClass =
                CompactRosterCalendarWidgetReceiver::class.java
        )
    }

    private fun requestProviderUpdate(
        context: Context,
        receiverClass: Class<*>
    ) {
        val component =
            ComponentName(
                context,
                receiverClass
            )

        val appWidgetIds =
            AppWidgetManager
                .getInstance(context)
                .getAppWidgetIds(component)

        Log.d(
            TAG,
            "Requesting update " +
                    receiverClass.simpleName +
                    " count=" +
                    appWidgetIds.size
        )

        if (appWidgetIds.isEmpty()) {
            return
        }

        context.sendBroadcast(
            Intent(
                AppWidgetManager.ACTION_APPWIDGET_UPDATE
            )
                .setComponent(component)
                .putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    appWidgetIds
                )
        )
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
