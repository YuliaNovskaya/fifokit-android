package com.fifokit.app.ui.components

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FifokitBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Back"
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(label)
    }
}
