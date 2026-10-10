package com.fifokit.app.ui.roster

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Scaffold
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
import com.fifokit.app.ui.components.calendarHorizontalSwipe

private enum class CalendarViewMode {
    MONTH,
    YEAR
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterCalendarScreen(
    viewModel: RosterSetupViewModel,
    onBack: () -> Unit,
    onProRequested: (String) -> Unit,
    onMonthChanged: (YearMonth) -> Unit = {}
) {
    val context = LocalContext.current
    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }
    LaunchedEffect(Unit) {
        analytics.logEvent(AnalyticsEvents.CALENDAR_VIEWED, null)
        viewModel.refreshSharingStatus()
    }
    var month by remember {
        mutableStateOf(YearMonth.now())
    }

    LaunchedEffect(month) {
        onMonthChanged(month)
    }

    var calendarViewMode by remember {
        mutableStateOf(
            CalendarViewMode.MONTH
        )
    }

    val monthScrollState =
        rememberScrollState()

    LaunchedEffect(
        month,
        calendarViewMode
    ) {
        if (
            calendarViewMode ==
            CalendarViewMode.MONTH
        ) {
            monthScrollState.scrollTo(0)
        }
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

    var selectedCalendarDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    val hasOwnRoster =
        viewModel.rosters.isNotEmpty()

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

    if (hasOwnRoster) {
        selectedCalendarDate?.let { selectedDate ->
        val workDays =
            if (viewModel.isCustomRoster) {
                viewModel.customWorkDays
            } else {
                viewModel.selectedPattern.workDays
            }

        val offDays =
            if (viewModel.isCustomRoster) {
                viewModel.customOffDays
            } else {
                viewModel.selectedPattern.offDays
            }

        val isRosterActive =
            viewModel.isRosterActive(
                selectedDate
            )

        val holidays =
            PublicHolidayProvider.holidaysOn(
                selectedDate,
                viewModel.selectedStates
            )

        AlertDialog(
            onDismissRequest = {
                selectedCalendarDate = null
            },
            title = {
                Text(
                    selectedDate.format(
                        DateTimeFormatter.ofPattern(
                            "EEEE, d MMMM yyyy",
                            Locale.getDefault()
                        )
                    )
                )
            },
            text = {
                Column {
                    if (!isRosterActive) {
                        if (
                            viewModel.isShutdownRoster &&
                            viewModel.endDate != null &&
                            selectedDate.isAfter(
                                viewModel.endDate
                            )
                        ) {
                            Text(
                                text =
                                    "Shutdown roster has ended.",
                                style =
                                    MaterialTheme.typography
                                        .bodyLarge
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "Ended " +
                                            viewModel.endDate,
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )
                        } else {
                            Text(
                                text =
                                    "Roster has not started yet.",
                                style =
                                    MaterialTheme.typography
                                        .bodyLarge
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "Roster starts " +
                                            viewModel.startDate
                                                .format(
                                                    DateTimeFormatter
                                                        .ofPattern(
                                                            "d MMM yyyy",
                                                            Locale
                                                                .getDefault()
                                                        )
                                                ),
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )
                        }
                    } else {
                        val statusText =
                            if (
                                viewModel.isCustomRoster
                            ) {
                                val status =
                                    viewModel
                                        .scheduleStatusOn(
                                            selectedDate
                                        )

                                if (status != null) {
                                    val label =
                                        status.segment
                                            .type
                                            .name

                                    label +
                                            " day " +
                                            status
                                                .dayInSegment +
                                            " of " +
                                            status
                                                .segment
                                                .days
                                } else {
                                    ""
                                }
                            } else {
                                val daysFromStart =
                                    ChronoUnit.DAYS
                                        .between(
                                            viewModel
                                                .startDate,
                                            selectedDate
                                        )

                                val cycleLength =
                                    workDays +
                                            offDays

                                val cycleDay =
                                    Math.floorMod(
                                        daysFromStart,
                                        cycleLength
                                            .toLong()
                                    ).toInt()

                                if (
                                    cycleDay <
                                    workDays
                                ) {
                                    "WORK day " +
                                            (cycleDay + 1) +
                                            " of " +
                                            workDays
                                } else {
                                    "OFF day " +
                                            (
                                                cycleDay -
                                                    workDays +
                                                    1
                                                ) +
                                            " of " +
                                            offDays
                                }
                            }

                        Text(
                            text = statusText,
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            fontWeight =
                                FontWeight.Bold
                        )

                        if (
                            viewModel.isShutdownRoster
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "Shutdown roster · ends " +
                                            viewModel.endDate,
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )
                        }
                    }

                    if (holidays.isNotEmpty()) {
                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "Public holiday: " +
                                        holidays.joinToString(
                                            separator = ", "
                                        ) {
                                            it.name
                                        },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCalendarDate = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
        }
    }

    Scaffold(
        contentWindowInsets =
            WindowInsets(0, 0, 0, 0),
        topBar = {
            FifokitTopBar(
                title = "Roster Calendar"
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .calendarHorizontalSwipe(
                    onPrevious = {
                        month =
                            if (
                                calendarViewMode ==
                                CalendarViewMode.MONTH
                            ) {
                                month.minusMonths(1)
                            } else {
                                month.minusYears(1)
                            }
                    },
                    onNext = {
                        month =
                            if (
                                calendarViewMode ==
                                CalendarViewMode.MONTH
                            ) {
                                month.plusMonths(1)
                            } else {
                                month.plusYears(1)
                            }
                    }
                )
                .then(
                    if (
                        calendarViewMode ==
                        CalendarViewMode.MONTH
                    ) {
                        Modifier.verticalScroll(
                            monthScrollState
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp)
        ) {
            if (hasOwnRoster) {
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
            } else {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.createNewRoster()
                        onBack()
                    }
                ) {
                    Text("Roster: Tap to add roster")
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

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

            Spacer(modifier = Modifier.height(4.dp))

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

            if (hasOwnRoster) {
                Text(
                    text =
                        when {
                            viewModel.isShutdownRoster ->
                                "Shutdown · ${viewModel.scheduleSegments.size} periods · ends ${viewModel.endDate}"

                            viewModel.isCustomRoster ->
                                "Custom sequence · ${viewModel.scheduleSegments.size} periods"

                            else ->
                                "${viewModel.selectedPattern.label} roster"
                        },
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                RosterTodaySummary(viewModel)

                Spacer(
                    modifier = Modifier.height(4.dp)
                )
            }

            if (calendarViewMode == CalendarViewMode.MONTH) {

                WeekdayHeader()

                Spacer(modifier = Modifier.height(4.dp))

                CalendarGrid(
                    month = month,
                    startDate =
                        if (hasOwnRoster) {
                            viewModel.startDate
                        } else {
                            LocalDate.MIN
                        },
                    showRosterStatus = hasOwnRoster,
                    isWorkDay = { date ->
                        hasOwnRoster &&
                                viewModel.isWorkDay(date)
                    },
                    isPublicHoliday = { date ->
                        hasOwnRoster &&
                                PublicHolidayProvider
                                    .isPublicHoliday(
                                        date,
                                        viewModel.selectedStates
                                    )
                    },
                    isRosterActive = { date ->
                        hasOwnRoster &&
                                viewModel.isRosterActive(date)
                    },
                    rosterStatusLabel = { date ->
                        if (
                            hasOwnRoster &&
                            viewModel.isCustomRoster
                        ) {
                            viewModel
                                .scheduleStatusOn(date)
                                ?.segment
                                ?.type
                                ?.name
                        } else {
                            null
                        }
                    },
                    onDateSelected = {
                        if (hasOwnRoster) {
                            selectedCalendarDate = it
                        }
                    }
                )

                if (hasOwnRoster) {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    TodayPhrase(viewModel)

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    CalendarLegend()
                }

            } else {

                YearCalendarGrid(
                    year = month.year,
                    startDate =
                        if (hasOwnRoster) {
                            viewModel.startDate
                        } else {
                            LocalDate.MIN
                        },
                    isRosterActive = { date ->
                        hasOwnRoster &&
                                viewModel.isRosterActive(date)
                    },
                    isWorkDay = { date ->
                        hasOwnRoster &&
                                viewModel.isWorkDay(date)
                    },
                    isPublicHoliday = { date ->
                        hasOwnRoster &&
                                PublicHolidayProvider
                                    .isPublicHoliday(
                                        date,
                                        viewModel.selectedStates
                                    )
                    },
                    shiftType = { date ->
                        if (hasOwnRoster && viewModel.isCustomRoster)
                            viewModel.scheduleStatusOn(date)?.segment?.type?.name
                        else null
                    },
                    modifier = Modifier.weight(1f)
                )

                if (hasOwnRoster) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    CalendarLegend()
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

        }
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
    showRosterStatus: Boolean,
    isWorkDay: (LocalDate) -> Boolean,
    isPublicHoliday: (LocalDate) -> Boolean,
    isRosterActive: (LocalDate) -> Boolean,
    rosterStatusLabel:
        (LocalDate) -> String? = { null },
    onDateSelected: (LocalDate) -> Unit,
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
                        isRosterActive = isRosterActive(date),
                        isWorkDay = isWorkDay(date),
                        isStartDate =
                            showRosterStatus &&
                                    date == startDate,
                        isPublicHoliday =
                            showRosterStatus &&
                                    isPublicHoliday(date),
                        showRosterStatus =
                            showRosterStatus,
                        rosterStatusLabel =
                            rosterStatusLabel(date),
                        onClick = {
                            onDateSelected(date)
                        }
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
    isPublicHoliday: Boolean,
    showRosterStatus: Boolean,
    rosterStatusLabel: String? = null,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val workOffColor =
        when {
            !showRosterStatus ->
                MaterialTheme.colorScheme.surface

            !isRosterActive ->
                MaterialTheme.colorScheme.surface

            isWorkDay && rosterStatusLabel == "DAY" -> Color(0xFFFFB74D)
            isWorkDay && rosterStatusLabel == "NIGHT" -> Color(0xFF3949AB)
            isWorkDay && shiftType == "DAY" -> Color(0xFFFFB74D)
            isWorkDay && shiftType == "NIGHT" -> Color(0xFF3949AB)
            isWorkDay ->
                MaterialTheme.colorScheme.primaryContainer

            else ->
                MaterialTheme.colorScheme.surfaceContainerHigh
        }
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick),
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
        if (!showRosterStatus) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style =
                        MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                if (isPublicHoliday) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                            .align(Alignment.TopCenter)
                            .background(
                                MaterialTheme.colorScheme
                                    .tertiaryContainer
                            )
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Text(
                                text =
                                    date.dayOfMonth
                                        .toString(),
                                style =
                                    MaterialTheme.typography
                                        .titleSmall,
                                fontWeight =
                                    FontWeight.Medium
                            )

                            if (isPublicHoliday) {
                                Text(
                                    text = "PH",
                                    style =
                                        MaterialTheme.typography
                                            .labelSmall,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                when {
                                    !isRosterActive ->
                                        ""

                                    rosterStatusLabel != null ->
                                        rosterStatusLabel

                                    isWorkDay ->
                                        "WORK"

                                    else ->
                                        "OFF"
                                },
                            style =
                                MaterialTheme.typography
                                    .labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        vertical = 3.dp
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
                        vertical = 3.dp
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
                        vertical = 3.dp
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
    val today = LocalDate.now()
    val shutdownEndDate = viewModel.endDate

    if (
        viewModel.isShutdownRoster &&
        today.isBefore(viewModel.startDate)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text =
                    "Shutdown starts " +
                            viewModel.startDate,
                modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    if (
        viewModel.isShutdownRoster &&
        shutdownEndDate != null &&
        today.isAfter(shutdownEndDate)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text =
                    "Shutdown ended " +
                            shutdownEndDate,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    val todayIsWork =
        viewModel.isTodayWorkDay()

    val nextChangeDate =
        viewModel.nextRosterChangeDate()

    val daysRemaining =
        viewModel.daysUntilRosterChange()

    val formatter =
        DateTimeFormatter.ofPattern(
            "EEE, d MMM",
            Locale.getDefault()
        )

    val nextLine =
        if (
            viewModel.isShutdownRoster &&
            shutdownEndDate != null &&
            nextChangeDate.isAfter(
                shutdownEndDate
            )
        ) {
            "Shutdown ends " +
                    shutdownEndDate
                        .format(formatter)
        } else {
            val nextStatus =
                if (todayIsWork) {
                    "OFF"
                } else {
                    "WORK"
                }

            nextStatus +
                    " starts " +
                    nextChangeDate
                        .format(formatter) +
                    " · " +
                    daysRemaining +
                    " " +
                    if (daysRemaining == 1L) {
                        "day"
                    } else {
                        "days"
                    }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 6.dp
            )
        ) {
            Text(
                text =
                    "Today: " +
                            if (todayIsWork) {
                                "WORK"
                            } else {
                                "OFF"
                            },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = nextLine,
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
                horizontal = 16.dp,
                vertical = 8.dp
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
    isRosterActive: (LocalDate) -> Boolean,
    isWorkDay: (LocalDate) -> Boolean,
    isPublicHoliday: (LocalDate) -> Boolean,
    shiftType: (LocalDate) -> String?,
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
                isRosterActive = isRosterActive,
                isWorkDay = isWorkDay,
                isPublicHoliday = isPublicHoliday,
                shiftType = shiftType
            )
        }
    }
}

@Composable
private fun MiniMonthCalendar(
    month: YearMonth,
    startDate: LocalDate,
    isRosterActive: (LocalDate) -> Boolean,
    isWorkDay: (LocalDate) -> Boolean,
    isPublicHoliday: (LocalDate) -> Boolean,
    shiftType: (LocalDate) -> String?
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
                                isRosterActive = isRosterActive(date),
                                isWorkDay = isWorkDay(date),
                                isStartDate = date == startDate,
                                isPublicHoliday = isPublicHoliday(date),
                                shiftType = shiftType(date)
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
    isPublicHoliday: Boolean,
    shiftType: String?
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
