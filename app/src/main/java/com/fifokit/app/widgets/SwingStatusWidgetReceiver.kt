package com.fifokit.app.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.fifokit.app.MainActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class SwingStatusWidgetReceiver :
    GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget =
        SwingStatusWidget()

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
