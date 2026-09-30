package com.fifokit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.fifokit.app.deeplink.AppLinkDestination
import com.fifokit.app.deeplink.AppLinkRequest
import com.fifokit.app.deeplink.AppLinkRouter
import com.fifokit.app.ui.FIFOKITApp
import com.fifokit.app.ui.theme.FIFOKITTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class MainActivity : ComponentActivity() {

    private var appLinkRequest by
        mutableStateOf<AppLinkRequest?>(
            null
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars =
            false

        val widgetType =
            intent
                ?.getStringExtra(
                    EXTRA_WIDGET_TYPE
                )
                ?.takeIf {
                    it.isNotBlank()
                }

        val widgetDestination =
            intent
                ?.getStringExtra(
                    EXTRA_WIDGET_DESTINATION
                )
                ?.takeIf {
                    it.isNotBlank()
                }

        logWidgetOpen(
            widgetType =
                widgetType,
            widgetDestination =
                widgetDestination
        )

        appLinkRequest =
            parseAppLink(
                intent?.data
            )

        appLinkRequest?.let {
            logAppLinkOpen(it)
        }

        setContent {
            FIFOKITTheme {
                val request =
                    appLinkRequest

                FIFOKITApp(
                    inviteId =
                        request
                            ?.takeIf {
                                it.destination ==
                                        AppLinkDestination.INVITE
                            }
                            ?.inviteId,
                    initialShowPro =
                        widgetDestination ==
                                WIDGET_DESTINATION_PRO,
                    appLinkDestination =
                        request
                            ?.destination
                )
            }
        }
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        val request =
            parseAppLink(
                intent.data
            )

        if (request != null) {
            appLinkRequest = request
            logAppLinkOpen(request)
        }
    }

    private fun logWidgetOpen(
        widgetType: String?,
        widgetDestination: String?
    ) {
        if (widgetType != null) {
            FirebaseAnalytics
                .getInstance(this)
                .logEvent(
                    "widget_opened"
                ) {
                    param(
                        "widget_type",
                        widgetType
                    )
                }
        }

        if (
            widgetType ==
            WIDGET_TYPE_COMPACT_CALENDAR &&
            widgetDestination ==
            WIDGET_DESTINATION_PRO
        ) {
            FirebaseAnalytics
                .getInstance(this)
                .logEvent(
                    "widget_pro_locked"
                ) {
                    param(
                        "widget_type",
                        widgetType
                    )
                }
        }
    }

    private fun parseAppLink(
        uri: Uri?
    ): AppLinkRequest? {
        if (uri == null) {
            return null
        }

        val query =
            mapOf(
                "utm_source" to
                        uri.getQueryParameter(
                            "utm_source"
                        ),
                "source" to
                        uri.getQueryParameter(
                            "source"
                        ),
                "utm_medium" to
                        uri.getQueryParameter(
                            "utm_medium"
                        ),
                "utm_campaign" to
                        uri.getQueryParameter(
                            "utm_campaign"
                        ),
                "utm_content" to
                        uri.getQueryParameter(
                            "utm_content"
                        )
            )

        return AppLinkRouter.parse(
            scheme = uri.scheme,
            host = uri.host,
            pathSegments =
                uri.pathSegments,
            query = query
        )
    }

    private fun logAppLinkOpen(
        request: AppLinkRequest
    ) {
        FirebaseAnalytics
            .getInstance(this)
            .logEvent(
                "deep_link_opened"
            ) {
                param(
                    "destination",
                    request.destination
                        .name
                        .lowercase()
                )

                request.source
                    ?.take(100)
                    ?.let {
                        param(
                            "source",
                            it
                        )
                    }

                request.medium
                    ?.take(100)
                    ?.let {
                        param(
                            "medium",
                            it
                        )
                    }

                request.campaign
                    ?.take(100)
                    ?.let {
                        param(
                            "campaign",
                            it
                        )
                    }

                request.content
                    ?.take(100)
                    ?.let {
                        param(
                            "content",
                            it
                        )
                    }
            }
    }

    companion object {
        const val EXTRA_WIDGET_TYPE =
            "widget_type"

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
