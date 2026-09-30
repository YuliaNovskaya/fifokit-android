package com.fifokit.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.fifokit.app.ui.FIFOKITApp
import com.fifokit.app.ui.theme.FIFOKITTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = false

        val widgetType =
            intent
                ?.getStringExtra(EXTRA_WIDGET_TYPE)
                ?.takeIf { it.isNotBlank() }

        val widgetDestination =
            intent
                ?.getStringExtra(EXTRA_WIDGET_DESTINATION)
                ?.takeIf { it.isNotBlank() }

        if (widgetType != null) {
            FirebaseAnalytics
                .getInstance(this)
                .logEvent("widget_opened") {
                    param("widget_type", widgetType)
                }
        }

        if (
            widgetType == WIDGET_TYPE_COMPACT_CALENDAR &&
            widgetDestination == WIDGET_DESTINATION_PRO
        ) {
            FirebaseAnalytics
                .getInstance(this)
                .logEvent("widget_pro_locked") {
                    param("widget_type", widgetType)
                }
        }

        val inviteId = extractInviteId(intent?.data)

        setContent {
            FIFOKITTheme {
                FIFOKITApp(
                    inviteId = inviteId,
                    initialShowPro =
                        widgetDestination ==
                                WIDGET_DESTINATION_PRO
                )
            }
        }
    }

    private fun extractInviteId(uri: Uri?): String? {
        if (uri == null) return null

        if (
            uri.scheme != "https" ||
            uri.host != "fifokit.com"
        ) {
            return null
        }

        val segments = uri.pathSegments

        if (
            segments.size != 2 ||
            segments[0] != "invite"
        ) {
            return null
        }

        return segments[1]
            .takeIf { it.isNotBlank() }
    }

    companion object {
        const val EXTRA_WIDGET_TYPE = "widget_type"
        const val EXTRA_WIDGET_DESTINATION =
            "widget_destination"

        const val WIDGET_TYPE_SWING_STATUS =
            "swing_status"

        const val WIDGET_TYPE_COMPACT_CALENDAR =
            "compact_calendar"

        const val WIDGET_DESTINATION_PRO =
            "pro"
    }
}
