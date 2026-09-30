package com.fifokit.app.growth

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.fifokit.app.analytics.AnalyticsEvents

object InAppReviewLauncher {

    fun launch(
        activity: Activity,
        analytics: FirebaseAnalytics
    ) {
        val manager =
            ReviewManagerFactory.create(
                activity
            )

        val request =
            manager.requestReviewFlow()

        request.addOnCompleteListener {
            task ->

            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            analytics.logEvent(
                AnalyticsEvents.REVIEW_FLOW_TRIGGERED,
                null
            )

            manager.launchReviewFlow(
                activity,
                task.result
            )
        }
    }
}
