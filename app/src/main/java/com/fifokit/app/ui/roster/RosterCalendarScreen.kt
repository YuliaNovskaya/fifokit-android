package com.fifokit.app.ui.roster

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import java.time.format.DateTimeFormatter
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.fifokit.app.notifications.RosterNotificationManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.fifokit.app.domain.roster.PublicHolidayProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import com.fifokit.app.domain.roster.RosterPhrases
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.fifokit.app.domain.pro.ProAccess
import com.fifokit.app.domain.pro.ProFeature
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.growth.GrowthEngagementTracker
import com.fifokit.app.growth.InAppReviewLauncher
import com.fifokit.app.ui.components.FifokitTopBar

private enum class CalendarViewMode {
    MONTH,
    YEAR
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterCalendarScreen(
    viewModel: RosterSetupViewModel,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onShareRoster: () -> Unit,
    onExportRoster: (YearMonth) -> Unit,
    onProRequested: (String) -> Unit,
    onSharedRosters: () -> Unit,
    onPayCalculator: () -> Unit,
    onAnnualEarnings: () -> Unit,
    onFinancialGoal: () -> Unit,
    openToolsSheet: Boolean = false,
    onToolsSheetOpened: () -> Unit = {},
) {
    val context = LocalContext.current
    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (granted) {
                    analytics.logEvent(AnalyticsEvents.NOTIFICATION_ENABLED, null)
                } else {
                    analytics.logEvent(AnalyticsEvents.NOTIFICATION_DENIED, null)
                }
            }
        )

    LaunchedEffect(Unit) {
        RosterNotificationManager.createChannel(context)
        analytics.logEvent(AnalyticsEvents.CALENDAR_VIEWED, null)
        viewModel.refreshSharingStatus()
    }
    var month by remember {
        mutableStateOf(YearMonth.now())
    }

    var calendarViewMode by remember {
        mutableStateOf(
            CalendarViewMode.MONTH
        )
    }

    val growthTracker =
        remember(context) {
            GrowthEngagementTracker(context)
        }

    var showReviewPrompt by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (
            growthTracker.shouldShowReviewPrompt()
        ) {
            growthTracker.markPromptShown()
            analytics.logEvent(
                AnalyticsEvents.REVIEW_PROMPT_SHOWN,
                null
            )
            showReviewPrompt = true
        }
    }

    var rosterMenuExpanded by remember {
        mutableStateOf(false)
    }

    var showRosterActionsSheet by remember {
        mutableStateOf(false)
    }

    var showToolsSheet by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(openToolsSheet) {
        if (openToolsSheet) {
            showToolsSheet = true
            onToolsSheetOpened()
        }
    }

    if (showReviewPrompt) {
        AlertDialog(
            onDismissRequest = {
                growthTracker.markReviewDismissed()
                analytics.logEvent(
                    AnalyticsEvents.REVIEW_PROMPT_DISMISSED,
                    null
                )
                showReviewPrompt = false
            },
            title = {
                Text("Enjoying FIFOKIT?")
            },
            text = {
                Text(
                    "If FIFOKIT is useful for your roster, would you like to review it on Google Play?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        growthTracker.markReviewAccepted()
                        analytics.logEvent(
                            AnalyticsEvents.REVIEW_PROMPT_ACCEPTED,
                            null
                        )
                        showReviewPrompt = false

                        context
                            .findActivity()
                            ?.let { activity ->
                                InAppReviewLauncher.launch(
                                    activity = activity,
                                    analytics = analytics
                                )
                            }
                    }
                ) {
                    Text("Review app")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        growthTracker.markReviewDismissed()
                        analytics.logEvent(
                            AnalyticsEvents.REVIEW_PROMPT_DISMISSED,
                            null
                        )
                        showReviewPrompt = false
                    }
                ) {
                    Text("Not now")
                }
            }
        )
    }

    if (showRosterActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showRosterActionsSheet = false
            }
        ) {
            ActionSheetHeader(
                title = "Roster",
                onClose = {
                    showRosterActionsSheet = false
                }
            )

            ActionSheetItem(
                title = "Share roster",
                description =
                    "Invite a partner or family member to view the selected roster.",
                onClick = {
                    showRosterActionsSheet = false
                    onShareRoster()
                }
            )

            HorizontalDivider()

            ActionSheetItem(
                title = "Shared rosters",
                description =
                    "View rosters other people have shared with you.",
                onClick = {
                    showRosterActionsSheet = false
                    analytics.logEvent(
                        AnalyticsEvents.FEATURE_OPENED
                    ) {
                        param(
                            AnalyticsParams.FEATURE,
                            "partner_sharing"
                        )
                    }
                    onSharedRosters()
                }
            )

            HorizontalDivider()

            ActionSheetItem(
                title = "Export roster",
                description =
                    "Save or share the selected roster as an image or PDF.",
                onClick = {
                    showRosterActionsSheet = false
                    onExportRoster(month)
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }

    if (showToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showToolsSheet = false
            }
        ) {
            ActionSheetHeader(
                title = "Tools",
                onClose = {
                    showToolsSheet = false
                }
            )

            ActionSheetItem(
                title = "FIFO Pay Calculator",
                description =
                    "Calculate pay using your hourly, daily or salary rate.",
                onClick = {
                    showToolsSheet = false
                    analytics.logEvent(
                        AnalyticsEvents.FINANCE_TOOL_SELECTED
                    ) {
                        param(
                            AnalyticsParams.TOOL,
                            "pay_calculator"
                        )
                    }
                    onPayCalculator()
                }
            )

            HorizontalDivider()

            ActionSheetItem(
                title = "Annual Earnings · PRO",
                description =
                    "Estimate yearly work days, hours and gross earnings.",
                onClick = {
                    showToolsSheet = false
                    analytics.logEvent(
                        AnalyticsEvents.FINANCE_TOOL_SELECTED
                    ) {
                        param(
                            AnalyticsParams.TOOL,
                            "annual_earnings"
                        )
                    }
                    onAnnualEarnings()
                }
            )

            HorizontalDivider()

            ActionSheetItem(
                title = "Financial Goal",
                description =
                    "Estimate how long it will take to reach a savings target.",
                onClick = {
                    showToolsSheet = false
                    analytics.logEvent(
                        AnalyticsEvents.FINANCE_TOOL_SELECTED
                    ) {
                        param(
                            AnalyticsParams.TOOL,
                            "financial_goal"
                        )
                    }
                    onFinancialGoal()
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "Roster Calendar"
            )
        },
        bottomBar = {
            CalendarBottomNavigation(
                onEdit = onBack,
                onRoster = {
                    showRosterActionsSheet = true
                },
                onTools = {
                    analytics.logEvent(
                        AnalyticsEvents.FEATURE_OPENED
                    ) {
                        param(
                            AnalyticsParams.FEATURE,
                            "finance_tools"
                        )
                    }
                    showToolsSheet = true
                },
                onSettings = onSettings
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        rosterMenuExpanded = true
                    }
                ) {
                    val activeRoster =
                        viewModel.rosters
                            .firstOrNull {
                                it.id ==
                                        viewModel.activeRosterId
                            }

                    val shareCount =
                        activeRoster
                            ?.cloudId
                            ?.let {
                                viewModel
                                    .shareCountByCloudId[it]
                            }
                            ?: 0

                    Text(
                        if (shareCount > 0) {
                            "Roster: ${viewModel.rosterName} · Shared with $shareCount"
                        } else {
                            "Roster: ${viewModel.rosterName}"
                        }
                    )
                }

                DropdownMenu(
                    expanded = rosterMenuExpanded,
                    onDismissRequest = {
                        rosterMenuExpanded = false
                    }
                ) {
                    viewModel.rosters.forEach { roster ->
                        DropdownMenuItem(
                            text = {
                                val shareCount =
                                    roster.cloudId
                                        ?.let {
                                            viewModel
                                                .shareCountByCloudId[it]
                                        }
                                        ?: 0

                                Text(
                                    if (shareCount > 0) {
                                        "${roster.name} · Shared with $shareCount"
                                    } else {
                                        roster.name
                                    }
                                )
                            },
                            onClick = {
                                rosterMenuExpanded = false

                                if (roster.id != viewModel.activeRosterId) {
                                    viewModel.switchRoster(roster.id)
                                }
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text("+ New roster")
                        },
                        onClick = {
                            rosterMenuExpanded = false
                            viewModel.createNewRoster()
                            onBack()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (calendarViewMode == CalendarViewMode.MONTH) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            calendarViewMode = CalendarViewMode.MONTH
                        }
                    ) {
                        Text("Month")
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            calendarViewMode = CalendarViewMode.MONTH
                        }
                    ) {
                        Text("Month")
                    }
                }

                if (calendarViewMode == CalendarViewMode.YEAR) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            analytics.logEvent(
                                AnalyticsEvents.FEATURE_OPENED
                            ) {
                                param(
                                    AnalyticsParams.FEATURE,
                                    "year_view"
                                )
                            }
                            calendarViewMode = CalendarViewMode.YEAR
                        }
                    ) {
                        Text("Year")
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            analytics.logEvent(
                                AnalyticsEvents.FEATURE_OPENED
                            ) {
                                param(
                                    AnalyticsParams.FEATURE,
                                    "year_view"
                                )
                            }

                            if (
                                ProAccess.canUse(
                                    ProFeature.YEARLY_ROSTER
                                )
                            ) {
                                calendarViewMode = CalendarViewMode.YEAR
                            } else {
                                onProRequested("yearly_roster")
                            }
                        }
                    ) {
                        Text("Year · PRO")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        month =
                            if (calendarViewMode == CalendarViewMode.MONTH) {
                                month.minusMonths(1)
                            } else {
                                month.minusYears(1)
                            }
                    }
                ) {
                    Text("<")
                }

                Text(
                    text =
                        if (calendarViewMode == CalendarViewMode.MONTH) {
                            "${
                                month.month.getDisplayName(
                                    TextStyle.FULL,
                                    Locale.getDefault()
                                )
                            } ${month.year}"
                        } else {
                            month.year.toString()
                        },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = {
                        month =
                            if (calendarViewMode == CalendarViewMode.MONTH) {
                                month.plusMonths(1)
                            } else {
                                month.plusYears(1)
                            }
                    }
                ) {
                    Text(">")
                }
            }

            Text(
                text =
                    if (viewModel.isCustomRoster) {
                        "${viewModel.customWorkDays}/${viewModel.customOffDays} custom roster"
                    } else {
                        "${viewModel.selectedPattern.label} roster"
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            RosterTodaySummary(viewModel)

            Spacer(modifier = Modifier.height(12.dp))

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                ) {
                    Text("Enable notifications")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (calendarViewMode == CalendarViewMode.MONTH) {

                WeekdayHeader()

                Spacer(modifier = Modifier.height(8.dp))

                CalendarGrid(
                    month = month,
                    startDate = viewModel.startDate,
                    isWorkDay = viewModel::isWorkDay,
                    isPublicHoliday = { date ->
                        PublicHolidayProvider.isPublicHoliday(
                            date,
                            viewModel.selectedStates
                        )
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                TodayPhrase(viewModel)

                Spacer(modifier = Modifier.weight(1f))

                CalendarLegend()

            } else {

                YearCalendarGrid(
                    year = month.year,
                    startDate = viewModel.startDate,
                    isWorkDay = viewModel::isWorkDay,
                    isPublicHoliday = { date ->
                        PublicHolidayProvider.isPublicHoliday(
                            date,
                            viewModel.selectedStates
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                CalendarLegend()
            }

            Spacer(modifier = Modifier.height(12.dp))

        }
    }
}

@Composable
private fun CalendarBottomNavigation(
    onEdit: () -> Unit,
    onRoster: () -> Unit,
    onTools: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp,
                    vertical = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onEdit
            ) {
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onRoster
            ) {
                Text(
                    text = "Roster",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onTools
            ) {
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onSettings
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            )
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

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    startDate: LocalDate,
    isWorkDay: (LocalDate) -> Boolean,
    isPublicHoliday: (LocalDate) -> Boolean,
    modifier: Modifier = Modifier
)
{
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1

    val cells = buildList<LocalDate?> {
        repeat(firstDayOffset) {
            add(null)
        }

        for (day in 1..month.lengthOfMonth()) {
            add(month.atDay(day))
        }
    }

    val rowCount = (cells.size + 6) / 7

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val cellSize = maxWidth / 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(cellSize * rowCount)
        ) {
            items(cells) { date ->
                if (date == null) {
                    Spacer(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                    )
                } else {
                    CalendarDay(
                        date = date,
                        isRosterActive = !date.isBefore(startDate),
                        isWorkDay = isWorkDay(date),
                        isStartDate = date == startDate,
                        isPublicHoliday = isPublicHoliday(date)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isRosterActive: Boolean,
    isWorkDay: Boolean,
    isStartDate: Boolean,
    isPublicHoliday: Boolean
) {
    val isToday = date == LocalDate.now()
    val workOffColor = when {
        !isRosterActive -> MaterialTheme.colorScheme.surface
        isWorkDay -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        shape = MaterialTheme.shapes.small,
        color = workOffColor,
        border = when {
            isToday -> BorderStroke(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )

            isStartDate -> BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )

            else -> null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            if (isPublicHoliday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        if (isPublicHoliday) {
                            Text(
                                text = "PH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            !isRosterActive -> ""
                            isWorkDay -> "WORK"
                            else -> "OFF"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "WORK",
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = "OFF",
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = "PH",
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Text(
            text = "Thick border = today · Thin border = roster start",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RosterTodaySummary(
    viewModel: RosterSetupViewModel
) {
    val todayIsWork = viewModel.isTodayWorkDay()
    val nextChangeDate = viewModel.nextRosterChangeDate()
    val daysRemaining = viewModel.daysUntilRosterChange()

    val nextStatus = if (todayIsWork) "OFF" else "WORK"

    val formatter = DateTimeFormatter.ofPattern(
        "EEE, d MMM",
        Locale.getDefault()
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today: ${if (todayIsWork) "WORK" else "OFF"}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$nextStatus starts ${
                    nextChangeDate.format(formatter)
                } · $daysRemaining ${
                    if (daysRemaining == 1L) "day" else "days"
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TodayPhrase(
    viewModel: RosterSetupViewModel
) {
    val today = LocalDate.now()

    val phrase = RosterPhrases.phraseFor(
        date = today,
        isWorkDay = viewModel.isWorkDay(today)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = phrase,
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 14.dp
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun YearCalendarGrid(
    year: Int,
    startDate: LocalDate,
    isWorkDay: (LocalDate) -> Boolean,
    isPublicHoliday: (LocalDate) -> Boolean,
    modifier: Modifier = Modifier
) {
    val months = (1..12).map { monthNumber ->
        YearMonth.of(year, monthNumber)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(months) { yearMonth ->
            MiniMonthCalendar(
                month = yearMonth,
                startDate = startDate,
                isWorkDay = isWorkDay,
                isPublicHoliday = isPublicHoliday
            )
        }
    }
}

@Composable
private fun MiniMonthCalendar(
    month: YearMonth,
    startDate: LocalDate,
    isWorkDay: (LocalDate) -> Boolean,
    isPublicHoliday: (LocalDate) -> Boolean
) {
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1

    val cells = buildList<LocalDate?> {
        repeat(firstDayOffset) {
            add(null)
        }

        for (day in 1..month.lengthOfMonth()) {
            add(month.atDay(day))
        }

        while (size < 42) {
            add(null)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = month.month.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S")
                    .forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
            }

            cells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    week.forEach { date ->
                        if (date == null) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {
                            MiniCalendarDay(
                                modifier = Modifier.weight(1f),
                                date = date,
                                isRosterActive = !date.isBefore(startDate),
                                isWorkDay = isWorkDay(date),
                                isStartDate = date == startDate,
                                isPublicHoliday = isPublicHoliday(date)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarDay(
    modifier: Modifier,
    date: LocalDate,
    isRosterActive: Boolean,
    isWorkDay: Boolean,
    isStartDate: Boolean,
    isPublicHoliday: Boolean
) {
    val isToday = date == LocalDate.now()

    val workOffColor =
        when {
            !isRosterActive ->
                MaterialTheme.colorScheme.surface

            isWorkDay ->
                MaterialTheme.colorScheme.primaryContainer

            else ->
                MaterialTheme.colorScheme.surfaceContainerHigh
        }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp),
        shape = MaterialTheme.shapes.small,
        color = workOffColor,
        border = when {
            isToday -> BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )

            isStartDate -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline
            )

            else -> null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isPublicHoliday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                )
            }

            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this

    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }

        current = current.baseContext
    }

    return null
}
