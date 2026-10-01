package com.fifokit.app.domain.sharing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class SharingPolicyTest {

    @Test
    fun `free allows one active sharing recipient`() {
        assertEquals(
            1,
            SharingPolicy.recipientLimit(
                isPro = false
            )
        )

        assertTrue(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 0,
                isPro = false
            )
        )

        assertFalse(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 1,
                isPro = false
            )
        )
    }

    @Test
    fun `pro allows up to five active sharing recipients`() {
        assertEquals(
            5,
            SharingPolicy.recipientLimit(
                isPro = true
            )
        )

        assertTrue(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 4,
                isPro = true
            )
        )

        assertFalse(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 5,
                isPro = true
            )
        )
    }

    @Test
    fun `free downgrade keeps oldest recipient active`() {
        val activeIds =
            SharingPolicy.recipientIdsToKeepActive(
                recipientEntries = listOf(
                    "user-b" to 200L,
                    "user-a" to 100L,
                    "user-b" to 250L
                ),
                limit = 1
            )

        assertEquals(
            setOf("user-a"),
            activeIds
        )
    }

    @Test
    fun `pro keeps up to five recipients active`() {
        val activeIds =
            SharingPolicy.recipientIdsToKeepActive(
                recipientEntries =
                    (1..6).map { index ->
                        "user-$index" to index.toLong()
                    },
                limit = 5
            )

        assertEquals(5, activeIds.size)
        assertFalse("user-6" in activeIds)
    }

    @Test
    fun `acceptance blocks a new recipient when limit is full`() {
        assertFalse(
            SharingPolicy.canAcceptRecipient(
                activeRecipientCount = 1,
                recipientAlreadyActive = false,
                recipientLimit = 1
            )
        )

        assertTrue(
            SharingPolicy.canAcceptRecipient(
                activeRecipientCount = 1,
                recipientAlreadyActive = true,
                recipientLimit = 1
            )
        )
    }
}
