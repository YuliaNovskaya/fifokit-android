package com.fifokit.app.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLinkRouterTest {

    @Test
    fun `routes roster app link`() {
        val request =
            AppLinkRouter.parse(
                scheme = "https",
                host = "fifokit.com",
                pathSegments =
                    listOf(
                        "app",
                        "roster"
                    ),
                query =
                    mapOf(
                        "utm_source" to
                                "website",
                        "utm_campaign" to
                                "roster_cta"
                    )
            )

        assertEquals(
            AppLinkDestination.ROSTER,
            request?.destination
        )

        assertEquals(
            "website",
            request?.source
        )

        assertEquals(
            "roster_cta",
            request?.campaign
        )
    }

    @Test
    fun `routes finance app link`() {
        val request =
            AppLinkRouter.parse(
                scheme = "https",
                host = "fifokit.com",
                pathSegments =
                    listOf(
                        "app",
                        "finance"
                    ),
                query =
                    emptyMap()
            )

        assertEquals(
            AppLinkDestination.FINANCE,
            request?.destination
        )
    }

    @Test
    fun `routes invite app link`() {
        val request =
            AppLinkRouter.parse(
                scheme = "https",
                host = "fifokit.com",
                pathSegments =
                    listOf(
                        "invite",
                        "abc123"
                    ),
                query =
                    mapOf(
                        "source" to
                                "partner"
                    )
            )

        assertEquals(
            AppLinkDestination.INVITE,
            request?.destination
        )

        assertEquals(
            "abc123",
            request?.inviteId
        )

        assertEquals(
            "partner",
            request?.source
        )
    }

    @Test
    fun `rejects unknown FIFOKIT path`() {
        assertNull(
            AppLinkRouter.parse(
                scheme = "https",
                host = "fifokit.com",
                pathSegments =
                    listOf(
                        "app",
                        "unknown"
                    ),
                query =
                    emptyMap()
            )
        )
    }

    @Test
    fun `rejects other hosts`() {
        assertNull(
            AppLinkRouter.parse(
                scheme = "https",
                host = "example.com",
                pathSegments =
                    listOf(
                        "app",
                        "roster"
                    ),
                query =
                    emptyMap()
            )
        )
    }
}
