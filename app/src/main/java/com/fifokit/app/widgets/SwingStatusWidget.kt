package com.fifokit.app.widgets

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.fifokit.app.MainActivity
import com.fifokit.app.domain.roster.shutdownOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SwingStatusWidget :
    GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val dataSource =
            RosterWidgetDataSource(
                context
            )

        withContext(Dispatchers.IO) {
            dataSource.prepare()
        }

        provideContent {
            val roster by
                dataSource
                    .observeActiveRoster()
                    .collectAsState(
                        initial = null
                    )

            val today =
                LocalDate.now()

            Log.d(
                "FIFOKITWidget",
                "Compose Swing roster=" +
                        (roster?.name
                            ?: "none") +
                        " id=" +
                        (roster?.id
                            ?: -1L)
            )

            SwingStatusContent(
                context = context,
                roster = roster,
                today = today
            )
        }
    }
}

private val widgetBackground =
    ColorProvider(
        Color(0xFF111111)
    )

private val widgetOrange =
    ColorProvider(
        Color(0xFFF5A623)
    )

private val widgetWhite =
    ColorProvider(
        Color(0xFFFFFFFF)
    )

private val widgetSecondary =
    ColorProvider(
        Color(0xFFB3B3B3)
    )

private val widgetDateFormatter =
    DateTimeFormatter
        .ofPattern("d MMM")

@Composable
private fun SwingStatusContent(
    context: Context,
    roster: WidgetRoster?,
    today: LocalDate
) {
    val openAppIntent =
        Intent(
            context,
            MainActivity::class.java
        ).putExtra(
            MainActivity
                .EXTRA_WIDGET_TYPE,
            MainActivity
                .WIDGET_TYPE_SWING_STATUS
        )

    val openAppAction =
        actionStartActivity(
            openAppIntent
        )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                widgetBackground
            )
            .clickable(
                openAppAction
            )
            .padding(12.dp)
    ) {
        if (roster == null) {
            Text(
                text = "FIFOKIT",
                style = TextStyle(
                    color = widgetOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text =
                    "No active roster",
                style = TextStyle(
                    color = widgetWhite,
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            )

            Spacer(
                modifier =
                    GlanceModifier
                        .height(4.dp)
            )

            Text(
                text =
                    "Open FIFOKIT to create a roster",
                style = TextStyle(
                    color =
                        widgetSecondary,
                    fontSize = 13.sp
                )
            )
        } else {
            val shutdown =
                roster.shutdowns
                    .shutdownOn(today)

            val status =
                SwingStatusCalculator
                    .calculate(
                        date = today,
                        startDate =
                            roster.startDate,
                        workDays =
                            roster.workDays,
                        offDays =
                            roster.offDays
                    )

            val statusLabel =
                when {
                    shutdown != null ->
                        "SHUTDOWN"

                    status.isWorkDay ->
                        "ON SWING"

                    else ->
                        "OFF SWING"
                }

            val nextLabel =
                when {
                    shutdown != null ->
                        "Ends"

                    status.isWorkDay ->
                        "R&R starts"

                    else ->
                        "Work starts"
                }

            Text(
                text = "FIFOKIT",
                style = TextStyle(
                    color = widgetOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(
                modifier =
                    GlanceModifier
                        .height(2.dp)
            )

            Text(
                text = statusLabel,
                style = TextStyle(
                    color = widgetWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text =
                    "Day " +
                            status.dayInPeriod +
                            " of " +
                            status.periodLength,
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 16.sp
                )
            )

            Text(
                text =
                    status
                        .daysUntilTransition
                        .toString() +
                            " days until change",
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 15.sp
                )
            )

            Text(
                text =
                    nextLabel +
                            " " +
                            (
                                shutdown
                                    ?.endDate
                                    ?: status
                                        .nextTransitionDate
                            )
                                .format(
                                    widgetDateFormatter
                                ),
                style = TextStyle(
                    color = widgetOrange,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = roster.name,
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}
