package com.fifokit.app.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsSchemaTest {

    private val analyticsNamePattern =
        Regex("^[A-Za-z][A-Za-z0-9_]{0,39}$")

    @Test
    fun `custom event names follow GA4 limits`() {
        AnalyticsSchema.customEventNames.forEach { name ->
            assertTrue(
                "Invalid event name: $name",
                analyticsNamePattern.matches(name)
            )
        }
    }

    @Test
    fun `parameter names follow GA4 limits`() {
        AnalyticsSchema.parameterNames.forEach { name ->
            assertTrue(
                "Invalid parameter name: $name",
                analyticsNamePattern.matches(name)
            )
        }
    }

    @Test
    fun `user property names stay within Firebase limit`() {
        AnalyticsSchema.userPropertyNames.forEach { name ->
            assertTrue(
                "Invalid user property name: $name",
                name.length <= 24
            )
            assertTrue(
                analyticsNamePattern.matches(name)
            )
        }
    }

    @Test
    fun `custom event names are unique`() {
        val names = AnalyticsSchema.customEventNames.toList()

        assertEquals(
            names.size,
            names.toSet().size
        )
    }

    @Test
    fun `all funnel events are known or automatic`() {
        val allowed =
            AnalyticsSchema.customEventNames +
                    AnalyticsEvents.FIRST_OPEN

        AnalyticsFunnels.all
            .flatMap { it.steps }
            .flatMap { it.eventNames }
            .forEach { eventName ->
                assertTrue(
                    "Unknown funnel event: $eventName",
                    eventName in allowed
                )
            }
    }
}
