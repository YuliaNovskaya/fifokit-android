package com.fifokit.app.growth

import android.content.Context

object ReviewEligibility {

    const val MIN_MEANINGFUL_ACTIONS = 4
    const val MIN_AGE_MS =
        3L * 24L * 60L * 60L * 1000L

    fun shouldShow(
        meaningfulActionCount: Int,
        firstSeenAtMs: Long,
        nowMs: Long,
        nextEligibleAtMs: Long,
        reviewAccepted: Boolean
    ): Boolean {
        if (reviewAccepted) {
            return false
        }

        if (
            meaningfulActionCount <
            MIN_MEANINGFUL_ACTIONS
        ) {
            return false
        }

        if (
            firstSeenAtMs <= 0L ||
            nowMs - firstSeenAtMs <
            MIN_AGE_MS
        ) {
            return false
        }

        return nowMs >= nextEligibleAtMs
    }
}

class GrowthEngagementTracker(
    context: Context
) {

    private val preferences =
        context
            .applicationContext
            .getSharedPreferences(
                "growth_engagement",
                Context.MODE_PRIVATE
            )

    init {
        if (
            preferences.getLong(
                KEY_FIRST_SEEN_AT,
                0L
            ) == 0L
        ) {
            preferences
                .edit()
                .putLong(
                    KEY_FIRST_SEEN_AT,
                    System.currentTimeMillis()
                )
                .apply()
        }
    }

    fun recordMeaningfulAction() {
        val nextCount =
            preferences.getInt(
                KEY_MEANINGFUL_ACTIONS,
                0
            ) + 1

        preferences
            .edit()
            .putInt(
                KEY_MEANINGFUL_ACTIONS,
                nextCount
            )
            .apply()
    }

    fun shouldShowReviewPrompt(): Boolean {
        val now =
            System.currentTimeMillis()

        return ReviewEligibility.shouldShow(
            meaningfulActionCount =
                preferences.getInt(
                    KEY_MEANINGFUL_ACTIONS,
                    0
                ),
            firstSeenAtMs =
                preferences.getLong(
                    KEY_FIRST_SEEN_AT,
                    0L
                ),
            nowMs = now,
            nextEligibleAtMs =
                preferences.getLong(
                    KEY_NEXT_ELIGIBLE_AT,
                    0L
                ),
            reviewAccepted =
                preferences.getBoolean(
                    KEY_REVIEW_ACCEPTED,
                    false
                )
        )
    }

    fun markPromptShown() {
        preferences
            .edit()
            .putLong(
                KEY_NEXT_ELIGIBLE_AT,
                System.currentTimeMillis() +
                        SHOWN_COOLDOWN_MS
            )
            .apply()
    }

    fun markReviewAccepted() {
        preferences
            .edit()
            .putBoolean(
                KEY_REVIEW_ACCEPTED,
                true
            )
            .apply()
    }

    fun markReviewDismissed() {
        preferences
            .edit()
            .putLong(
                KEY_NEXT_ELIGIBLE_AT,
                System.currentTimeMillis() +
                        DISMISSED_COOLDOWN_MS
            )
            .apply()
    }

    fun makeReviewEligibleForDebug() {
        val now =
            System.currentTimeMillis()

        preferences
            .edit()
            .putInt(
                KEY_MEANINGFUL_ACTIONS,
                ReviewEligibility
                    .MIN_MEANINGFUL_ACTIONS
            )
            .putLong(
                KEY_FIRST_SEEN_AT,
                now -
                        ReviewEligibility
                            .MIN_AGE_MS -
                        1L
            )
            .putLong(
                KEY_NEXT_ELIGIBLE_AT,
                0L
            )
            .putBoolean(
                KEY_REVIEW_ACCEPTED,
                false
            )
            .apply()
    }

    private companion object {

        const val KEY_FIRST_SEEN_AT =
            "first_seen_at"

        const val KEY_MEANINGFUL_ACTIONS =
            "meaningful_action_count"

        const val KEY_NEXT_ELIGIBLE_AT =
            "next_review_eligible_at"

        const val KEY_REVIEW_ACCEPTED =
            "review_accepted"

        const val SHOWN_COOLDOWN_MS =
            7L * 24L * 60L * 60L * 1000L

        const val DISMISSED_COOLDOWN_MS =
            60L * 24L * 60L * 60L * 1000L
    }
}
