package com.fifokit.app.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.fifokit.app.MainActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class SwingStatusWidgetReceiver :
    GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget =
        SwingStatusWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(
            "FIFOKITWidget",
            "Swing receiver onUpdate ids=" +
                    appWidgetIds.joinToString()
        )

        super.onUpdate(
            context,
            appWidgetManager,
            appWidgetIds
        )
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        FirebaseAnalytics
            .getInstance(context)
            .logEvent("widget_added") {
                param(
                    "widget_type",
                    MainActivity.WIDGET_TYPE_SWING_STATUS
                )
            }
    }
}
