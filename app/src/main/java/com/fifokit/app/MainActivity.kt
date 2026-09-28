package com.fifokit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fifokit.app.ui.FIFOKITApp
import com.fifokit.app.ui.theme.FIFOKITTheme
import androidx.core.view.WindowCompat
import android.net.Uri
import com.fifokit.app.ui.FIFOKITApp
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = false

        val inviteId = extractInviteId(intent?.data)

        setContent {
            FIFOKITTheme {
                FIFOKITApp(
                    inviteId = inviteId
                )
            }
        }
    }
    private fun extractInviteId(uri: Uri?): String? {
        if (uri == null) return null

        if (
            uri.scheme != "https" ||
            uri.host != "fifokit.com"
        ) {
            return null
        }

        val segments = uri.pathSegments

        if (
            segments.size != 2 ||
            segments[0] != "invite"
        ) {
            return null
        }

        return segments[1]
            .takeIf { it.isNotBlank() }
    }

}