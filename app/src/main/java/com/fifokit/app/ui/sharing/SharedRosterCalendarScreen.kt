package com.fifokit.app.ui.sharing

import androidx.compose.foundation.BorderStroke
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
import com.fifokit.app.domain.roster.PublicHolidayProvider
import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.ShutdownPeriodCodec
import com.fifokit.app.domain.roster.shutdownOn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import com.fifokit.app.domain.sharing.SharedRoster
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
import com.fifokit.app.ui.components.calendarHorizontalSwipe
import androidx.compose.material3.Scaffold

@Composable
fun SharedRosterCalendarScreen(
    sharedRoster: SharedRoster,
    onBack: () -> Unit,
    onTogether: () -> Unit
) {
    val context = LocalContext.current

    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }

    val roster = sharedRoster.roster
    val patternLabel =
        if (roster.isCustomRoster) {
            "${roster.customWorkDays}/${roster.customOffDays} custom roster"
        } else {
            runCatching {
                RosterPattern.valueOf(
                    roster.pattern
                ).label
            }.getOrDefault(
                roster.pattern
            )
        }
    LaunchedEffect(roster.id) {
        analytics.logEvent("partner_calendar_viewed") {
            param("partner_roster_id", roster.id)
        }
    }


    val ownerStates =
        roster.selectedStates
            .mapNotNull { stateName ->
                runCatching {
                    AustralianState.valueOf(stateName)
                }.getOrNull()
            }
            .toSet()
            .ifEmpty {
                setOf(AustralianState.WA)
            }

    val startDate =
        runCatching {
            LocalDate.parse(roster.startDate)
        }.getOrElse {
            LocalDate.now()
        }

    var month by remember {
        mutableStateOf(YearMonth.now())
    }

    val shutdowns =
        remember(roster.shutdownsJson) {
            ShutdownPeriodCodec.decode(
                roster.shutdownsJson
            )
        }

    fun isWorkDay(date: LocalDate): Boolean {
        if (
            shutdowns.shutdownOn(date) !=
            null
        ) {
            return false
        }

        return if (roster.isCustomRoster) {

            RosterCalculator.isWorkDay(
                date = date,
                startDate = startDate,
                workDays = roster.customWorkDays,
                offDays = roster.customOffDays
            )

        } else {

            val pattern =
                runCatching {
                    RosterPattern.valueOf(roster.pattern)
                }.getOrDefault(
                    RosterPattern.TWO_ONE
                )

            RosterCalculator.isWorkDay(
                date = date,
                startDate = startDate,
                pattern = pattern
            )
        }
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

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "Shared roster",
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
            .padding(16.dp)
    ) {

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = roster.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = patternLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            TextButton(
                onClick = {
                    month = month.plusMonths(1)
                }
            ) {
                Text(">")
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "M",
                "T",
                "W",
                "T",
                "F",
                "S",
                "S"
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

                    val workDay =
                        isWorkDay(date)

                    val isPublicHoliday =
                        PublicHolidayProvider.isPublicHoliday(
                            date,
                            ownerStates
                        )

                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp),
                        shape = MaterialTheme.shapes.small,
                        color =
                            if (workDay) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        border =
                            if (date == startDate) {
                                BorderStroke(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                null
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
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                )

                                Text(
                                    text = "PH",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text =
                                        if (workDay) {
                                            "WORK"
                                        } else {
                                            "OFF"
                                        },
                                    style = MaterialTheme.typography.labelSmall
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

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTogether
        ) {
            Text("Together")
        }

    }
}
}