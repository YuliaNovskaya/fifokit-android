package com.fifokit.app.ui.sharing

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.RosterCalculator
import com.fifokit.app.domain.sharing.SharedRoster
import com.fifokit.app.ui.roster.RosterSetupViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import com.fifokit.app.domain.sharing.RosterOverlapCalculator
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.fifokit.app.domain.roster.PublicHolidayProvider
import com.fifokit.app.domain.roster.AustralianState
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.domain.sharing.FamilyPlanningCalculator
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
import com.fifokit.app.ui.components.calendarHorizontalSwipe
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TogetherRosterCalendarScreen(
    viewModel: RosterSetupViewModel,
    sharedRoster: SharedRoster,
    onMyRoster: () -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }

    val partnerRoster = sharedRoster.roster

    val partnerStates =
        partnerRoster.selectedStates
            .mapNotNull { stateName ->
                runCatching {
                    AustralianState.valueOf(stateName)
                }.getOrNull()
            }
            .toSet()
            .ifEmpty {
                setOf(AustralianState.WA)
            }

    LaunchedEffect(partnerRoster.id) {
        analytics.logEvent(
            AnalyticsEvents.TOGETHER_CALENDAR_VIEWED,
            null
        )

        analytics.logEvent(
            AnalyticsEvents.FAMILY_PLANNING_SUMMARY_VIEWED,
            null
        )
    }


    val partnerStartDate =
        runCatching {
            LocalDate.parse(partnerRoster.startDate)
        }.getOrElse {
            LocalDate.now()
        }

    var month by remember {
        mutableStateOf(YearMonth.now())
    }

    fun partnerIsWorkDay(date: LocalDate): Boolean {
        return if (partnerRoster.isCustomRoster) {

            RosterCalculator.isWorkDay(
                date = date,
                startDate = partnerStartDate,
                workDays = partnerRoster.customWorkDays,
                offDays = partnerRoster.customOffDays
            )

        } else {

            val pattern =
                runCatching {
                    RosterPattern.valueOf(partnerRoster.pattern)
                }.getOrDefault(
                    RosterPattern.TWO_ONE
                )

            RosterCalculator.isWorkDay(
                date = date,
                startDate = partnerStartDate,
                pattern = pattern
            )
        }
    }

    val planningSummary =
        remember(
            partnerRoster.id,
            viewModel.activeRosterId
        ) {
            FamilyPlanningCalculator.calculate(
                myIsWorkDay = viewModel::isWorkDay,
                partnerIsWorkDay = ::partnerIsWorkDay
            )
        }

    val dateFormatter =
        DateTimeFormatter.ofPattern(
            "d MMM",
            Locale.getDefault()
        )

    val nextSharedOff =
        planningSummary.nextSharedOffPeriod


    val firstDayOffset =
        month.atDay(1).dayOfWeek.value - 1

    val cells =
        buildList<LocalDate?> {
            repeat(firstDayOffset) {
                add(null)
            }

            for (day in 1..month.lengthOfMonth()) {
                add(month.atDay(day))
            }
        }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "Together",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .calendarHorizontalSwipe(
                    onPrevious = {
                        month = month.minusMonths(1)
                    },
                    onNext = {
                        month = month.plusMonths(1)
                    }
                )
                .padding(horizontal = 16.dp)
        ) {

            Text(
                text =
                    "${viewModel.rosterName} + ${partnerRoster.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onMyRoster
                ) {
                    Text("My roster")
                }

                androidx.compose.material3.Button(
                    modifier = Modifier.weight(1f),
                    onClick = {}
                ) {
                    Text("Together")
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        text = "Shared time",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "${planningSummary.sharedOffDaysThisMonth} shared days off this month",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text =
                            "${planningSummary.sharedOffDaysNext3Months} shared days off in the next 3 months",
                        style = MaterialTheme.typography.bodySmall
                    )

                    planningSummary.nextSharedWeekend
                        ?.let { weekend ->

                            Text(
                                text =
                                    "Next free weekend: " +
                                            "${weekend.startDate.format(dateFormatter)} - " +
                                            weekend.endDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    planningSummary.longestSharedOffPeriod
                        ?.let { period ->

                            Text(
                                text =
                                    "Longest break: " +
                                            if (
                                                period.startDate ==
                                                period.endDate
                                            ) {
                                                period.startDate
                                                    .format(dateFormatter)
                                            } else {
                                                "${period.startDate.format(dateFormatter)} - " +
                                                        "${period.endDate.format(dateFormatter)} " +
                                                        "(${period.days} days)"
                                            },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            nextSharedOff?.let { period ->

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            month =
                                YearMonth.from(
                                    period.startDate
                                )

                            analytics.logEvent(
                                AnalyticsEvents.SHARED_TIME_CARD_CLICKED,
                                null
                            )
                        },
                    shape = MaterialTheme.shapes.medium,
                    color =
                        MaterialTheme.colorScheme.surfaceVariant
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Upcoming shared time",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                if (
                                    period.startDate ==
                                    period.endDate
                                ) {
                                    "Shared day off: ${
                                        period.startDate.format(
                                            dateFormatter
                                        )
                                    }"
                                } else {
                                    "${
                                        period.startDate.format(
                                            dateFormatter
                                        )
                                    } - ${
                                        period.endDate.format(
                                            dateFormatter
                                        )
                                    } (${period.days} days)"
                                },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "Tap to view in calendar",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = {
                        month = month.minusMonths(1)
                    }
                ) {
                    Text("<")
                }

                Text(
                    text =
                        "${
                            month.month.getDisplayName(
                                TextStyle.FULL,
                                Locale.getDefault()
                            )
                        } ${month.year}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(
                    onClick = {
                        month = month.plusMonths(1)
                    }
                ) {
                    Text(">")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "M", "T", "W", "T", "F", "S", "S"
                ).forEach { day ->

                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                items(cells) { date ->

                    if (date == null) {

                        Spacer(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )

                    } else {

                        val myWork =
                            viewModel.isWorkDay(date)

                        val partnerWork =
                            partnerIsWorkDay(date)

                        val sharedOff =
                            !myWork && !partnerWork

                        val myPublicHoliday =
                            PublicHolidayProvider.isPublicHoliday(
                                date,
                                viewModel.selectedStates
                            )

                        val partnerPublicHoliday =
                            PublicHolidayProvider.isPublicHoliday(
                                date,
                                partnerStates
                            )

                        val isPublicHoliday =
                            myPublicHoliday ||
                                    partnerPublicHoliday

                        val publicHolidayLabel =
                            when {
                                myPublicHoliday &&
                                        partnerPublicHoliday ->
                                    "PH BOTH"

                                myPublicHoliday ->
                                    "PH ME"

                                partnerPublicHoliday ->
                                    "PH THEM"

                                else ->
                                    ""
                            }

                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp),
                            shape = MaterialTheme.shapes.small,
                            color =
                                when {
                                    sharedOff ->
                                        MaterialTheme.colorScheme
                                            .tertiaryContainer

                                    myWork && partnerWork ->
                                        MaterialTheme.colorScheme
                                            .primaryContainer

                                    myWork ->
                                        MaterialTheme.colorScheme
                                            .surfaceVariant

                                    else ->
                                        MaterialTheme.colorScheme
                                            .surfaceContainerHigh
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
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                    )
                                }

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
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                )
                                        )

                                        Text(
                                            text = publicHolidayLabel,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 7.sp
                                            ),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {

                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            text =
                                                when {
                                                    sharedOff ->
                                                        "BOTH\nOFF"

                                                    myWork && partnerWork ->
                                                        "BOTH\nWORK"

                                                    myWork ->
                                                        "ME\nWORK"

                                                    else ->
                                                        "THEM\nWORK"
                                                },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                lineHeight = 9.sp
                                            ),
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    TogetherLegendChip(
                        text = "BOTH OFF",
                        background =
                            MaterialTheme.colorScheme
                                .tertiaryContainer
                    )

                    TogetherLegendChip(
                        text = "BOTH WORK",
                        background =
                            MaterialTheme.colorScheme
                                .primaryContainer
                    )
                }

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    TogetherLegendChip(
                        text = "ME WORK",
                        background =
                            MaterialTheme.colorScheme
                                .surfaceVariant
                    )

                    TogetherLegendChip(
                        text = "THEM WORK",
                        background =
                            MaterialTheme.colorScheme
                                .surfaceContainerHigh
                    )

                    TogetherLegendChip(
                        text = "PH ME/THEM",
                        background =
                            MaterialTheme.colorScheme
                                .secondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TogetherLegendChip(
    text: String,
    background: Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelSmall
        )
    }
}