@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fifokit.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FifokitBottomNavigation(
    onEdit: () -> Unit,
    onRoster: () -> Unit,
    onCalendar: () -> Unit,
    onTools: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp,
                    vertical = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FifokitNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = onEdit
            )

            FifokitNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.EventNote,
                label = "Roster",
                onClick = onRoster
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(
                            onClick = onCalendar
                        ),
                    shape = CircleShape,
                    color =
                        MaterialTheme.colorScheme.primary,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimary,
                    tonalElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.EventNote,
                            contentDescription =
                                "Roster calendar",
                            modifier =
                                Modifier.size(28.dp)
                        )
                    }
                }
            }

            FifokitNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Build,
                label = "Tools",
                onClick = onTools
            )

            FifokitNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun FifokitNavItem(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun RosterActionsSheet(
    onDismiss: () -> Unit,
    onShareRoster: () -> Unit,
    onSharedRosters: () -> Unit,
    onExportRoster: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        ActionSheetHeader(
            title = "Roster",
            onClose = onDismiss
        )

        ActionSheetItem(
            icon = Icons.Default.Share,
            title = "Share roster",
            description =
                "Invite a partner or family member to view the selected roster.",
            onClick = onShareRoster
        )

        HorizontalDivider()

        ActionSheetItem(
            icon = Icons.Default.People,
            title = "Shared rosters",
            description =
                "View rosters other people have shared with you.",
            onClick = onSharedRosters
        )

        HorizontalDivider()

        ActionSheetItem(
            icon = Icons.Default.FileDownload,
            title = "Export roster",
            description =
                "Save or share the selected roster as an image or PDF.",
            onClick = onExportRoster
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
fun ToolsActionsSheet(
    onDismiss: () -> Unit,
    onPayCalculator: () -> Unit,
    onAnnualEarnings: () -> Unit,
    onFinancialGoal: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        ActionSheetHeader(
            title = "Tools",
            onClose = onDismiss
        )

        ActionSheetItem(
            icon = Icons.Default.Calculate,
            title = "FIFO Pay Calculator",
            description =
                "Calculate pay using your hourly, daily or salary rate.",
            onClick = onPayCalculator
        )

        HorizontalDivider()

        ActionSheetItem(
            icon = Icons.Default.TrendingUp,
            title = "Annual Earnings · PRO",
            description =
                "Estimate yearly work days, hours and gross earnings.",
            onClick = onAnnualEarnings
        )

        HorizontalDivider()

        ActionSheetItem(
            icon = Icons.Default.Flag,
            title = "Financial Goal",
            description =
                "Estimate how long it will take to reach a savings target.",
            onClick = onFinancialGoal
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun ActionSheetHeader(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 12.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        TextButton(
            onClick = onClose
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun ActionSheetItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
