package com.fifokit.app.growth

import android.content.Context
import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.firebase.analytics.FirebaseAnalytics

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
            "acq_source",
            attribution.source
                .take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setUserProperty(
            "acq_medium",
            attribution.medium
                ?.take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setUserProperty(
            "acq_campaign",
            attribution.campaign
                ?.take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setUserProperty(
            "acq_content",
            attribution.content
                ?.take(
                    USER_PROPERTY_VALUE_MAX
                )
        )

        analytics.setDefaultEventParameters(
            Bundle().apply {
                putString(
                    "acq_source",
                    attribution.source
                )

                attribution.medium
                    ?.let {
                        putString(
                            "acq_medium",
                            it
                        )
                    }

                attribution.campaign
                    ?.let {
                        putString(
                            "acq_campaign",
                            it
                        )
                    }

                attribution.content
                    ?.let {
                        putString(
                            "acq_content",
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
            "install_attribution",
            Bundle().apply {
                putString(
                    "source",
                    attribution.source
                )

                attribution.medium
                    ?.let {
                        putString(
                            "medium",
                            it
                        )
                    }

                attribution.campaign
                    ?.let {
                        putString(
                            "campaign",
                            it
                        )
                    }

                attribution.content
                    ?.let {
                        putString(
                            "content",
                            it
                        )
                    }

                putLong(
                    "referrer_click_ts",
                    clickTimestamp
                )

                putLong(
                    "install_begin_ts",
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
                "source",
                null
            )
                ?: return null

        return AcquisitionAttribution(
            source = source,
            medium =
                preferences.getString(
                    "medium",
                    null
                ),
            campaign =
                preferences.getString(
                    "campaign",
                    null
                ),
            content =
                preferences.getString(
                    "content",
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
                "source",
                attribution.source
            )
            .putString(
                "medium",
                attribution.medium
            )
            .putString(
                "campaign",
                attribution.campaign
            )
            .putString(
                "content",
                attribution.content
            )
            .apply()
    }
}
