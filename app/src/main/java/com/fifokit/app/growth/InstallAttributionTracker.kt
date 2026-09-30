package com.fifokit.app.growth

import android.content.Context
import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.analytics.AnalyticsUserProperties

class InstallAttributionTracker(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val store =
        AcquisitionAttributionStore(
            applicationContext
        )

    private val analytics =
        FirebaseAnalytics.getInstance(
            applicationContext
        )

    fun captureOnce() {
        val existing =
            store.load()

        if (existing != null) {
            applyAnalytics(existing)
            return
        }

        val client =
            InstallReferrerClient
                .newBuilder(
                    applicationContext
                )
                .build()

        client.startConnection(
            object :
                InstallReferrerStateListener {

                override fun onInstallReferrerSetupFinished(
                    responseCode: Int
                ) {
                    if (
                        responseCode !=
                        InstallReferrerClient
                            .InstallReferrerResponse
                            .OK
                    ) {
                        client.endConnection()
                        return
                    }

                    try {
                        val details =
                            client.installReferrer

                        val attribution =
                            AcquisitionAttributionParser
                                .parse(
                                    details.installReferrer
                                )

                        store.save(
                            attribution
                        )

                        applyAnalytics(
                            attribution
                        )

                        logInstallAttribution(
                            attribution =
                                attribution,
                            clickTimestamp =
                                details
                                    .referrerClickTimestampSeconds,
                            installTimestamp =
                                details
                                    .installBeginTimestampSeconds
                        )

                    } catch (_: Exception) {
                        // Retry on a future app launch.
                    } finally {
                        client.endConnection()
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    client.endConnection()
                }
            }
        )
    }

    private fun applyAnalytics(
        attribution:
            AcquisitionAttribution
    ) {
        analytics.setUserProperty(
            AnalyticsUserProperties.ACQ_SOURCE,
            attribution.source
                .take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setUserProperty(
            AnalyticsUserProperties.ACQ_MEDIUM,
            attribution.medium
                ?.take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setUserProperty(
            AnalyticsUserProperties.ACQ_CAMPAIGN,
            attribution.campaign
                ?.take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setUserProperty(
            AnalyticsUserProperties.ACQ_CONTENT,
            attribution.content
                ?.take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setDefaultEventParameters(
            Bundle().apply {
                putString(
                    AnalyticsUserProperties.ACQ_SOURCE,
                    attribution.source
                )

                attribution.medium
                    ?.let {
                        putString(
                            AnalyticsUserProperties.ACQ_MEDIUM,
                            it
                        )
                    }

                attribution.campaign
                    ?.let {
                        putString(
                            AnalyticsUserProperties.ACQ_CAMPAIGN,
                            it
                        )
                    }

                attribution.content
                    ?.let {
                        putString(
                            AnalyticsUserProperties.ACQ_CONTENT,
                            it
                        )
                    }
            }
        )
    }

    private fun logInstallAttribution(
        attribution:
            AcquisitionAttribution,
        clickTimestamp: Long,
        installTimestamp: Long
    ) {
        analytics.logEvent(
            AnalyticsEvents.INSTALL_ATTRIBUTION,
            Bundle().apply {
                putString(
                    AnalyticsParams.SOURCE,
                    attribution.source
                )

                attribution.medium
                    ?.let {
                        putString(
                            AnalyticsParams.MEDIUM,
                            it
                        )
                    }

                attribution.campaign
                    ?.let {
                        putString(
                            AnalyticsParams.CAMPAIGN,
                            it
                        )
                    }

                attribution.content
                    ?.let {
                        putString(
                            AnalyticsParams.CONTENT,
                            it
                        )
                    }

                putLong(
                    AnalyticsParams.REFERRER_CLICK_TS,
                    clickTimestamp
                )

                putLong(
                    AnalyticsParams.INSTALL_BEGIN_TS,
                    installTimestamp
                )
            }
        )
    }

    private companion object {
        const val USER_PROPERTY_VALUE_MAX =
            36
    }
}

private class AcquisitionAttributionStore(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "acquisition_attribution",
            Context.MODE_PRIVATE
        )

    fun load():
            AcquisitionAttribution? {
        if (
            !preferences.getBoolean(
                "captured",
                false
            )
        ) {
            return null
        }

        val source =
            preferences.getString(
                AnalyticsParams.SOURCE,
                null
            )
                ?: return null

        return AcquisitionAttribution(
            source = source,
            medium =
                preferences.getString(
                    AnalyticsParams.MEDIUM,
                    null
                ),
            campaign =
                preferences.getString(
                    AnalyticsParams.CAMPAIGN,
                    null
                ),
            content =
                preferences.getString(
                    AnalyticsParams.CONTENT,
                    null
                )
        )
    }

    fun save(
        attribution:
            AcquisitionAttribution
    ) {
        preferences
            .edit()
            .putBoolean(
                "captured",
                true
            )
            .putString(
                AnalyticsParams.SOURCE,
                attribution.source
            )
            .putString(
                AnalyticsParams.MEDIUM,
                attribution.medium
            )
            .putString(
                AnalyticsParams.CAMPAIGN,
                attribution.campaign
            )
            .putString(
                AnalyticsParams.CONTENT,
                attribution.content
            )
            .apply()
    }
}
