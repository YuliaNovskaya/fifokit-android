package com.fifokit.app.growth

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics

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
                "review_flow_triggered",
                null
            )

            manager.launchReviewFlow(
                activity,
                task.result
            )
        }
    }
}
