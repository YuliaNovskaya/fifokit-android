package com.fifokit.app.growth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AcquisitionAttributionParserTest {

    @Test
    fun `parses website install referrer`() {
        val attribution =
            AcquisitionAttributionParser
                .parse(
                    "utm_source=fifokit_website" +
                            "&utm_medium=app_fallback" +
                            "&utm_campaign=p7_app_integration" +
                            "&utm_content=roster"
                )

        assertEquals(
            "fifokit_website",
            attribution.source
        )

        assertEquals(
            "app_fallback",
            attribution.medium
        )

        assertEquals(
            "p7_app_integration",
            attribution.campaign
        )

        assertEquals(
            "roster",
            attribution.content
        )
    }

    @Test
    fun `parses encoded install referrer`() {
        val attribution =
            AcquisitionAttributionParser
                .parse(
                    "utm_source%3Dfifokit_website" +
                            "%26utm_medium%3Dapp_fallback" +
                            "%26utm_campaign%3Dp7"
                )

        assertEquals(
            "fifokit_website",
            attribution.source
        )

        assertEquals(
            "app_fallback",
            attribution.medium
        )

        assertEquals(
            "p7",
            attribution.campaign
        )
    }

    @Test
    fun `uses neutral source when referrer has no campaign`() {
        val attribution =
            AcquisitionAttributionParser
                .parse("")

        assertEquals(
            "unattributed_play",
            attribution.source
        )

        assertNull(
            attribution.medium
        )

        assertNull(
            attribution.campaign
        )
    }

    @Test
    fun `supports source fallback parameter`() {
        val attribution =
            AcquisitionAttributionParser
                .parse(
                    "source=partner_share"
                )

        assertEquals(
            "partner_share",
            attribution.source
        )
    }
}
