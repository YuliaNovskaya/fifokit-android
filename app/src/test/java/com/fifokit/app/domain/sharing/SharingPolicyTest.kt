package com.fifokit.app.domain.sharing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class SharingPolicyTest {

    @Test
    fun `free allows one sharing recipient`() {
        assertEquals(
            1,
            SharingPolicy.recipientLimit(
                isPro = false
            )
        )

        assertTrue(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 0,
                pendingInviteCount = 0,
                isPro = false
            )
        )

        assertFalse(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 1,
                pendingInviteCount = 0,
                isPro = false
            )
        )
    }

    @Test
    fun `pro allows up to five sharing recipients`() {
        assertEquals(
            5,
            SharingPolicy.recipientLimit(
                isPro = true
            )
        )

        assertTrue(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 4,
                pendingInviteCount = 0,
                isPro = true
            )
        )

        assertFalse(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 5,
                pendingInviteCount = 0,
                isPro = true
            )
        )
    }

    @Test
    fun `pending invites reserve recipient slots`() {
        assertFalse(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 0,
                pendingInviteCount = 1,
                isPro = false
            )
        )

        assertFalse(
            SharingPolicy.canCreateInvite(
                activeRecipientCount = 3,
                pendingInviteCount = 2,
                isPro = true
            )
        )
    }
}
