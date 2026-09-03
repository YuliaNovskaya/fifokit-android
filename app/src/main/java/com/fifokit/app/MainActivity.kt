package com.fifokit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fifokit.app.ui.FIFOKITApp
import com.fifokit.app.ui.theme.FIFOKITTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            FIFOKITTheme {
                FIFOKITApp()
            }
        }
    }
}