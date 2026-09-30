package com.fifokit.app.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SwingStatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val roster =
            withContext(Dispatchers.IO) {
                RosterWidgetDataSource(context)
                    .loadActiveRoster()
            }

        val today = LocalDate.now()

        provideContent {
            SwingStatusContent(
                context = context,
                roster = roster,
                today = today
            )
        }
    }
}

private val widgetBackground =
    ColorProvider(Color(0xFF111111))

private val widgetOrange =
    ColorProvider(Color(0xFFF5A623))

private val widgetWhite =
    ColorProvider(Color(0xFFFFFFFF))

private val widgetSecondary =
    ColorProvider(Color(0xFFB3B3B3))

private val widgetDateFormatter =
    DateTimeFormatter.ofPattern("d MMM")

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
            MainActivity.EXTRA_WIDGET_TYPE,
            MainActivity.WIDGET_TYPE_SWING_STATUS
        )

    val openAppAction =
        actionStartActivity(openAppIntent)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(widgetBackground)
            .clickable(openAppAction)
            .padding(16.dp)
    ) {
        Text(
            text = "FIFOKIT",
            style = TextStyle(
                color = widgetOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(
            modifier = GlanceModifier.height(6.dp)
        )

        if (roster == null) {
            Text(
                text = "No active roster",
                style = TextStyle(
                    color = widgetWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(
                modifier = GlanceModifier.height(4.dp)
            )

            Text(
                text = "Open FIFOKIT to create a roster",
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 12.sp
                )
            )
        } else {
            val status =
                SwingStatusCalculator.calculate(
                    date = today,
                    startDate = roster.startDate,
                    workDays = roster.workDays,
                    offDays = roster.offDays
                )

            val statusLabel =
                if (status.isWorkDay) {
                    "ON SWING"
                } else {
                    "OFF SWING"
                }

            val nextLabel =
                if (status.isWorkDay) {
                    "R&R starts"
                } else {
                    "Work starts"
                }

            Text(
                text = statusLabel,
                style = TextStyle(
                    color = widgetWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(
                modifier = GlanceModifier.height(4.dp)
            )

            Text(
                text =
                    "Day " +
                            status.dayInPeriod +
                            " of " +
                            status.periodLength,
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 13.sp
                )
            )

            Text(
                text =
                    status.daysUntilTransition
                        .toString() +
                            " days until change",
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 13.sp
                )
            )

            Spacer(
                modifier = GlanceModifier.height(6.dp)
            )

            Text(
                text =
                    nextLabel +
                            " " +
                            status.nextTransitionDate
                                .format(widgetDateFormatter),
                style = TextStyle(
                    color = widgetOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(
                modifier = GlanceModifier.height(4.dp)
            )

            Text(
                text = roster.name,
                style = TextStyle(
                    color = widgetSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
