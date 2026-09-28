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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.fifokit.app.domain.sharing.FamilyPlanningCalculator
import androidx.compose.foundation.clickable

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

    LaunchedEffect(partnerRoster.id) {
        analytics.logEvent("together_calendar_viewed") {
            param("partner_roster_id", partnerRoster.id)
        }
        analytics.logEvent(
            "family_planning_summary_viewed"
        ) {
            param(
                "partner_roster_id",
                partnerRoster.id
            )
        }
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

    Spacer(
        modifier = Modifier.height(12.dp)
    )

    Text(
        text = "Shared time off",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    Text(
        text = "${planningSummary.sharedOffDaysThisMonth} shared days off this month",
        style = MaterialTheme.typography.bodyMedium
    )

    Text(
        text = "${planningSummary.sharedOffDaysNext3Months} shared days off in the next 3 months",
        style = MaterialTheme.typography.bodyMedium
    )

    planningSummary.longestSharedOffPeriod?.let { period ->

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Longest shared break in the next 3 months: " +
                        if (period.startDate == period.endDate) {
                            period.startDate.format(dateFormatter)
                        } else {
                            "${period.startDate.format(dateFormatter)} - " +
                                    "${period.endDate.format(dateFormatter)} " +
                                    "(${period.days} days)"
                        },
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (planningSummary.upcomingSharedOffPeriods.isNotEmpty()) {

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        planningSummary.upcomingSharedOffPeriods.forEach { period ->

            Text(
                text =
                    if (period.startDate == period.endDate) {
                        period.startDate.format(dateFormatter)
                    } else {
                        "${period.startDate.format(dateFormatter)} - " +
                                "${period.endDate.format(dateFormatter)} " +
                                "(${period.days} days)"
                    },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    planningSummary.nextSharedWeekend?.let { weekend ->

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Next free weekend: " +
                        "${weekend.startDate.format(dateFormatter)} - " +
                        weekend.endDate.format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Together",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "My roster + ${partnerRoster.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            "shared_time_card_clicked"
                        ) {
                            param(
                                "partner_roster_id",
                                partnerRoster.id
                            )
                        }
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

                    val isPublicHoliday =
                        PublicHolidayProvider.isPublicHoliday(
                            date,
                            viewModel.selectedStates
                        )

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp),
                        shape = MaterialTheme.shapes.small,
                        color =
                            when {
                                sharedOff ->
                                    MaterialTheme.colorScheme.tertiaryContainer

                                myWork && partnerWork ->
                                    MaterialTheme.colorScheme.primaryContainer

                                else ->
                                    MaterialTheme.colorScheme.surfaceVariant
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

                                if (isPublicHoliday) {
                                    Text(
                                        text = "PH",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text =
                                        when {
                                            sharedOff -> "BOTH OFF"
                                            myWork && partnerWork -> "BOTH WORK"
                                            myWork -> "ME WORK"
                                            else -> "THEM WORK"
                                        },
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "BOTH OFF = shared days off",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "PH = public holiday",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("My roster")
        }
    }
}