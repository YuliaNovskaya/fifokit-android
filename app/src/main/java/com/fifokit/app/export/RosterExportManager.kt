package com.fifokit.app.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

object RosterExportManager {

    private const val IMAGE_WIDTH = 1400
    private const val IMAGE_HEIGHT = 1600

    private const val PDF_WIDTH = 1240
    private const val PDF_HEIGHT = 1754

    private const val COLOR_BACKGROUND = 0xFF111111.toInt()
    private const val COLOR_SURFACE = 0xFF242424.toInt()
    private const val COLOR_ORANGE = 0xFFF5A623.toInt()
    private const val COLOR_WHITE = 0xFFFFFFFF.toInt()
    private const val COLOR_SECONDARY = 0xFFB3B3B3.toInt()
    private const val COLOR_HOLIDAY = 0xFF6A5329.toInt()

    fun exportMonthImage(
        context: Context,
        data: RosterExportData,
        month: YearMonth
    ): File {
        val bitmap =
            Bitmap.createBitmap(
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                Bitmap.Config.ARGB_8888
            )

        val canvas = Canvas(bitmap)

        drawMonthPage(
            canvas = canvas,
            width = IMAGE_WIDTH,
            height = IMAGE_HEIGHT,
            data = data,
            month = month
        )

        val file =
            exportFile(
                context = context,
                fileName =
                    "fifokit_" +
                            safeFilePart(data.rosterName) +
                            "_" +
                            month +
                            ".png"
            )

        FileOutputStream(file).use { output ->
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                output
            )
        }

        bitmap.recycle()

        return file
    }

    fun exportPdf(
        context: Context,
        data: RosterExportData,
        months: List<YearMonth>,
        fileName: String
    ): File {
        require(months.isNotEmpty())

        val document = PdfDocument()

        try {
            months.forEachIndexed { index, month ->

                val pageInfo =
                    PdfDocument.PageInfo.Builder(
                        PDF_WIDTH,
                        PDF_HEIGHT,
                        index + 1
                    ).create()

                val page =
                    document.startPage(pageInfo)

                drawMonthPage(
                    canvas = page.canvas,
                    width = PDF_WIDTH,
                    height = PDF_HEIGHT,
                    data = data,
                    month = month
                )

                document.finishPage(page)
            }

            val file =
                exportFile(
                    context = context,
                    fileName = fileName
                )

            FileOutputStream(file).use {
                document.writeTo(it)
            }

            return file

        } finally {
            document.close()
        }
    }

    private fun drawMonthPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        data: RosterExportData,
        month: YearMonth
    ) {
        canvas.drawColor(COLOR_BACKGROUND)

        val margin =
            width * 0.07f

        val contentWidth =
            width - margin * 2f

        val titlePaint =
            paint(
                color = COLOR_ORANGE,
                textSize = width * 0.055f,
                bold = true
            )

        val monthPaint =
            paint(
                color = COLOR_WHITE,
                textSize = width * 0.046f,
                bold = true
            )

        val secondaryPaint =
            paint(
                color = COLOR_SECONDARY,
                textSize = width * 0.024f
            )

        val weekdayPaint =
            paint(
                color = COLOR_SECONDARY,
                textSize = width * 0.024f,
                bold = true,
                center = true
            )

        val dayPaint =
            paint(
                color = COLOR_WHITE,
                textSize = width * 0.030f,
                bold = true,
                center = true
            )

        val workDayPaint =
            paint(
                color = COLOR_BACKGROUND,
                textSize = width * 0.030f,
                bold = true,
                center = true
            )

        val labelPaint =
            paint(
                color = COLOR_SECONDARY,
                textSize = width * 0.018f,
                center = true
            )

        val workLabelPaint =
            paint(
                color = COLOR_BACKGROUND,
                textSize = width * 0.018f,
                center = true
            )

        var y =
            margin + titlePaint.textSize

        canvas.drawText(
            "FIFOKIT",
            margin,
            y,
            titlePaint
        )

        y += monthPaint.textSize * 1.45f

        val monthTitle =
            month.month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            ) + " " + month.year

        canvas.drawText(
            monthTitle,
            margin,
            y,
            monthPaint
        )

        y += secondaryPaint.textSize * 1.8f

        canvas.drawText(
            data.rosterName,
            margin,
            y,
            secondaryPaint
        )

        y += secondaryPaint.textSize * 1.6f

        canvas.drawText(
            "WORK / OFF roster · Public holidays marked PH",
            margin,
            y,
            secondaryPaint
        )

        y += width * 0.055f

        val columnWidth =
            contentWidth / 7f

        val headerHeight =
            width * 0.045f

        listOf(
            "M",
            "T",
            "W",
            "T",
            "F",
            "S",
            "S"
        ).forEachIndexed { index, label ->

            val x =
                margin +
                        columnWidth *
                        index +
                        columnWidth / 2f

            canvas.drawText(
                label,
                x,
                y + headerHeight * 0.75f,
                weekdayPaint
            )
        }

        y += headerHeight

        val cells =
            RosterExportCalendar.monthDays(
                month = month,
                data = data
            )

        val rows =
            cells.chunked(7)

        val availableHeight =
            height -
                    y -
                    margin -
                    width * 0.10f

        val rowHeight =
            min(
                availableHeight /
                        rows.size,
                columnWidth * 0.95f
            )

        rows.forEachIndexed { rowIndex, week ->

            week.forEachIndexed { columnIndex, day ->

                if (day == null) {
                    return@forEachIndexed
                }

                val left =
                    margin +
                            columnIndex *
                            columnWidth

                val top =
                    y +
                            rowIndex *
                            rowHeight

                val right =
                    left +
                            columnWidth -
                            width * 0.008f

                val bottom =
                    top +
                            rowHeight -
                            width * 0.008f

                val cellPaint =
                    Paint(
                        Paint.ANTI_ALIAS_FLAG
                    ).apply {
                        color =
                            if (day.isWorkDay) {
                                COLOR_ORANGE
                            } else {
                                COLOR_SURFACE
                            }
                    }

                canvas.drawRoundRect(
                    left,
                    top,
                    right,
                    bottom,
                    width * 0.012f,
                    width * 0.012f,
                    cellPaint
                )

                if (day.isPublicHoliday) {
                    val holidayPaint =
                        Paint(
                            Paint.ANTI_ALIAS_FLAG
                        ).apply {
                            color =
                                COLOR_HOLIDAY
                        }

                    canvas.drawRoundRect(
                        left,
                        top,
                        right,
                        top + rowHeight * 0.24f,
                        width * 0.012f,
                        width * 0.012f,
                        holidayPaint
                    )
                }

                val centerX =
                    (left + right) / 2f

                val numberY =
                    top +
                            rowHeight *
                            0.49f

                canvas.drawText(
                    day.date.dayOfMonth
                        .toString(),
                    centerX,
                    numberY,
                    if (day.isWorkDay) {
                        workDayPaint
                    } else {
                        dayPaint
                    }
                )

                canvas.drawText(
                    if (day.isWorkDay) {
                        "WORK"
                    } else {
                        "OFF"
                    },
                    centerX,
                    top + rowHeight * 0.76f,
                    if (day.isWorkDay) {
                        workLabelPaint
                    } else {
                        labelPaint
                    }
                )

                if (day.isPublicHoliday) {
                    val phPaint =
                        paint(
                            color = COLOR_WHITE,
                            textSize =
                                width * 0.014f,
                            bold = true,
                            center = true
                        )

                    canvas.drawText(
                        "PH",
                        centerX,
                        top +
                                rowHeight *
                                0.18f,
                        phPaint
                    )
                }
            }
        }

        val footerY =
            height - margin * 0.6f

        canvas.drawText(
            "fifokit.com",
            margin,
            footerY,
            secondaryPaint
        )
    }

    private fun exportFile(
        context: Context,
        fileName: String
    ): File {
        val directory =
            File(
                context.cacheDir,
                "exports"
            )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return File(
            directory,
            fileName
        )
    }

    private fun safeFilePart(
        value: String
    ): String {
        return value
            .trim()
            .lowercase(Locale.US)
            .replace(
                Regex("[^a-z0-9]+"),
                "_"
            )
            .trim('_')
            .ifBlank {
                "roster"
            }
            .take(40)
    }

    private fun paint(
        color: Int,
        textSize: Float,
        bold: Boolean = false,
        center: Boolean = false
    ): Paint {
        return Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            this.color = color
            this.textSize = textSize
            textAlign =
                if (center) {
                    Paint.Align.CENTER
                } else {
                    Paint.Align.LEFT
                }

            typeface =
                if (bold) {
                    android.graphics.Typeface
                        .DEFAULT_BOLD
                } else {
                    android.graphics.Typeface
                        .DEFAULT
                }
        }
    }
}
