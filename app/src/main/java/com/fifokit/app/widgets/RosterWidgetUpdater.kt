package com.fifokit.app.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll

object RosterWidgetUpdater {

    suspend fun updateAll(
        context: Context
    ) {
        SwingStatusWidget()
            .updateAll(context.applicationContext)
    }
}
