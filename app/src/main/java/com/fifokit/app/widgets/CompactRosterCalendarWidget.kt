package com.fifokit.app.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.fifokit.app.MainActivity
import com.fifokit.app.data.billing.ProEntitlementStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class CompactRosterCalendarWidget :
    GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val roster =
            withContext(Dispatchers.IO) {
                RosterWidgetDataSource(context)
                    .loadActiveRoster()
            }

        val isPro =
            ProEntitlementStore(context)
                .load()
                .isPro

        val today = LocalDate.now()
        val month = YearMonth.from(today)

        provideContent {
            CompactRosterCalendarContent(
                roster = roster,
                isPro = isPro,
                month = month,
                today = today
            )
        }
    }
}

private val compactWidgetTypeKey =
    ActionParameters.Key<String>(
        MainActivity.EXTRA_WIDGET_TYPE
    )

private val compactWidgetDestinationKey =
    ActionParameters.Key<String>(
        MainActivity.EXTRA_WIDGET_DESTINATION
    )

private val compactBackground =
    ColorProvider(Color(0xFF111111))

private val compactSurface =
    ColorProvider(Color(0xFF242424))

private val compactOrange =
    ColorProvider(Color(0xFFF5A623))

private val compactWhite =
    ColorProvider(Color(0xFFFFFFFF))

private val compactDark =
    ColorProvider(Color(0xFF111111))

private val compactSecondary =
    ColorProvider(Color(0xFFB3B3B3))

@Composable
private fun CompactRosterCalendarContent(
    roster: WidgetRoster?,
    isPro: Boolean,
    month: YearMonth,
    today: LocalDate
) {
    val openAction =
        if (isPro) {
            actionStartActivity<MainActivity>(
                actionParametersOf(
                    compactWidgetTypeKey to
                            MainActivity.WIDGET_TYPE_COMPACT_CALENDAR
                )
            )
        } else {
            actionStartActivity<MainActivity>(
                actionParametersOf(
                    compactWidgetTypeKey to
                            MainActivity.WIDGET_TYPE_COMPACT_CALENDAR,
                    compactWidgetDestinationKey to
                            MainActivity.WIDGET_DESTINATION_PRO
                )
            )
        }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(compactBackground)
            .clickable(openAction)
            .padding(12.dp)
    ) {
        Text(
            text = "FIFOKIT",
            style = TextStyle(
                color = compactOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )

        if (!isPro) {
            Text(
                text = "ROSTER CALENDAR",
                style = TextStyle(
                    color = compactWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "FIFOKIT Pro · Tap to unlock",
                style = TextStyle(
                    color = compactOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            return
        }

        if (roster == null) {
            Text(
                text = "No active roster",
                style = TextStyle(
                    color = compactWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Open FIFOKIT to create a roster",
                style = TextStyle(
                    color = compactSecondary,
                    fontSize = 11.sp
                )
            )
            return
        }

        Text(
            text =
                month.month.getDisplayName(
                    JavaTextStyle.FULL,
                    Locale.getDefault()
                ) +
                        " " +
                        month.year,
            style = TextStyle(
                color = compactWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = roster.name,
            style = TextStyle(
                color = compactSecondary,
                fontSize = 10.sp
            ),
            maxLines = 1
        )

        WeekdayRow()

        CompactCalendarRows(
            days =
                CompactRosterCalendarCalculator
                    .monthCells(
                        month = month,
                        roster = roster,
                        today = today
                    )
        )

        Text(
            text = "Orange WORK · • Today · P Public holiday",
            style = TextStyle(
                color = compactSecondary,
                fontSize = 9.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun WeekdayRow() {
    Row(
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S")
            .forEach { day ->
                Text(
                    text = day,
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .padding(1.dp),
                    style = TextStyle(
                        color = compactSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
    }
}

@Composable
private fun CompactCalendarRows(
    days: List<WidgetCalendarDay?>
) {
    Column(
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        days.chunked(7).forEach { week ->
            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                week.forEach { day ->
                    if (day == null) {
                        Text(
                            text = "",
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .padding(2.dp)
                        )
                    } else {
                        Text(
                            text =
                                (if (day.isToday) "•" else "") +
                                        day.date.dayOfMonth +
                                        (if (day.isPublicHoliday) "P" else ""),
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .padding(2.dp)
                                    .background(
                                        if (day.isWorkDay) {
                                            compactOrange
                                        } else {
                                            compactSurface
                                        }
                                    )
                                    .padding(vertical = 3.dp),
                            style = TextStyle(
                                color =
                                    if (day.isWorkDay) {
                                        compactDark
                                    } else {
                                        compactWhite
                                    },
                                fontSize = 10.sp,
                                fontWeight =
                                    if (day.isToday) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
