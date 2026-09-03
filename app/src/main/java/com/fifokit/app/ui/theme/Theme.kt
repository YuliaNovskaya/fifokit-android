package com.fifokit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FifoColorScheme = darkColorScheme(
    primary = FifoOrange,
    onPrimary = FifoBackground,

    background = FifoBackground,
    onBackground = FifoWhite,

    surface = FifoSurface,
    onSurface = FifoWhite,

    surfaceVariant = FifoSurfaceVariant,
    onSurfaceVariant = FifoTextSecondary
)

@Composable
fun FIFOKITTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FifoColorScheme,
        typography = Typography,
        content = content
    )
}