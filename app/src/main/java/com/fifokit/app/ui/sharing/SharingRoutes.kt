package com.fifokit.app.ui.sharing

object SharingRoutes {

    const val SHARE_ROSTER = "share_roster/{rosterId}/{rosterName}"
    const val ACCEPT_INVITE = "accept_invite/{inviteId}"

    fun shareRoster(
        rosterId: String,
        rosterName: String
    ): String {
        return "share_roster/${android.net.Uri.encode(rosterId)}/${android.net.Uri.encode(rosterName)}"
    }

    fun acceptInvite(
        inviteId: String
    ): String {
        return "accept_invite/$inviteId"
    }
}