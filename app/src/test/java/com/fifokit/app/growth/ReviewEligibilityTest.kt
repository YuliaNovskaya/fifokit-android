package com.fifokit.app.growth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewEligibilityTest {

    private val day =
        24L * 60L * 60L * 1000L

    @Test
    fun `review waits for meaningful usage and app age`() {
        val now = 10L * day

        assertFalse(
            ReviewEligibility.shouldShow(
                meaningfulActionCount = 3,
                firstSeenAtMs = now - 5L * day,
                nowMs = now,
                nextEligibleAtMs = 0L,
                reviewAccepted = false
            )
        )

        assertFalse(
            ReviewEligibility.shouldShow(
                meaningfulActionCount = 4,
                firstSeenAtMs = now - 2L * day,
                nowMs = now,
                nextEligibleAtMs = 0L,
                reviewAccepted = false
            )
        )

        assertTrue(
            ReviewEligibility.shouldShow(
                meaningfulActionCount = 4,
                firstSeenAtMs = now - 4L * day,
                nowMs = now,
                nextEligibleAtMs = 0L,
                reviewAccepted = false
            )
        )
    }

    @Test
    fun `review respects cooldown and accepted state`() {
        val now = 10L * day

        assertFalse(
            ReviewEligibility.shouldShow(
                meaningfulActionCount = 10,
                firstSeenAtMs = now - 5L * day,
                nowMs = now,
                nextEligibleAtMs =
                    now + day,
                reviewAccepted = false
            )
        )

        assertFalse(
            ReviewEligibility.shouldShow(
                meaningfulActionCount = 10,
                firstSeenAtMs = now - 5L * day,
                nowMs = now,
                nextEligibleAtMs = 0L,
                reviewAccepted = true
            )
        )
    }
}
