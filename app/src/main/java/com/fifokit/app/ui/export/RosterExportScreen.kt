package com.fifokit.app.ui.export

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.fifokit.app.export.RosterExportCalendar
import java.time.format.TextStyle
import java.util.Locale
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
import com.fifokit.app.ui.components.calendarHorizontalSwipe
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.fifokit.app.domain.pro.ProAccess
import com.fifokit.app.domain.pro.ProFeature
import com.fifokit.app.export.RosterExportData
import com.fifokit.app.export.RosterExportManager
import com.fifokit.app.growth.GrowthEngagementTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.YearMonth

private enum class ExportKind(
    val label: String,
    val format: String,
    val mimeType: String,
    val monthCount: Int
) {
    MONTH_IMAGE(
        label = "Selected month image",
        format = "png",
        mimeType = "image/png",
        monthCount = 1
    ),
    THREE_MONTH_PDF(
        label = "3-month PDF",
        format = "pdf",
        mimeType = "application/pdf",
        monthCount = 3
    ),
    SIX_MONTH_PDF(
        label = "6-month PDF",
        format = "pdf",
        mimeType = "application/pdf",
        monthCount = 6
    ),
    ANNUAL_PDF(
        label = "Annual PDF",
        format = "pdf",
        mimeType = "application/pdf",
        monthCount = 12
    )
}

private enum class ExportAction {
    SAVE,
    SHARE
}

private data class GeneratedExport(
    val file: File,
    val kind: ExportKind
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterExportScreen(
    data: RosterExportData,
    startMonth: YearMonth,
    onBack: () -> Unit,
    onProRequested: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }

    val growthTracker = remember(context) {
        GrowthEngagementTracker(context)
    }

    var selectedExport by remember {
        mutableStateOf<ExportKind?>(null)
    }

    var selectedMonth by remember(startMonth) {
        mutableStateOf(startMonth)
    }

    var pendingSave by remember {
        mutableStateOf<GeneratedExport?>(null)
    }

    var isExporting by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        analytics.logEvent(
            "roster_export_viewed"
        ) {}
    }

    fun saveToUri(
        uri: Uri
    ) {
        val generated =
            pendingSave
                ?: return

        scope.launch {
            try {
                withContext(
                    Dispatchers.IO
                ) {
                    context
                        .contentResolver
                        .openOutputStream(uri)
                        ?.use { output ->
                            generated
                                .file
                                .inputStream()
                                .use { input ->
                                    input.copyTo(output)
                                }
                        }
                        ?: error(
                            "Unable to open save destination"
                        )
                }

                analytics.logEvent(
                    "roster_export_saved"
                ) {
                    param(
                        "format",
                        generated.kind.format
                    )
                    param(
                        "month_count",
                        generated
                            .kind
                            .monthCount
                            .toLong()
                    )
                }

            } catch (_: Exception) {
                errorMessage =
                    "Unable to save roster export."
            } finally {
                pendingSave = null
            }
        }
    }

    val imageSaveLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .CreateDocument(
                        "image/png"
                    ),
            onResult = { uri ->
                if (uri != null) {
                    saveToUri(uri)
                } else {
                    pendingSave = null
                }
            }
        )

    val pdfSaveLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .CreateDocument(
                        "application/pdf"
                    ),
            onResult = { uri ->
                if (uri != null) {
                    saveToUri(uri)
                } else {
                    pendingSave = null
                }
            }
        )

    suspend fun generateExport(
        kind: ExportKind
    ): File {
        return withContext(
            Dispatchers.IO
        ) {
            when (kind) {
                ExportKind.MONTH_IMAGE ->
                    RosterExportManager
                        .exportMonthImage(
                            context = context,
                            data = data,
                            month = selectedMonth
                        )

                ExportKind.THREE_MONTH_PDF -> {
                    val months =
                        List(3) { index ->
                            selectedMonth
                                .plusMonths(
                                    index.toLong()
                                )
                        }

                    RosterExportManager
                        .exportPdf(
                            context = context,
                            data = data,
                            months = months,
                            fileName =
                                "fifokit_roster_3_months.pdf"
                        )
                }

                ExportKind.SIX_MONTH_PDF -> {
                    val months =
                        List(6) { index ->
                            selectedMonth
                                .plusMonths(
                                    index.toLong()
                                )
                        }

                    RosterExportManager
                        .exportPdf(
                            context = context,
                            data = data,
                            months = months,
                            fileName =
                                "fifokit_roster_6_months.pdf"
                        )
                }

                ExportKind.ANNUAL_PDF -> {
                    val firstMonth =
                        YearMonth.of(
                            selectedMonth.year,
                            1
                        )

                    val months =
                        List(12) { index ->
                            firstMonth
                                .plusMonths(
                                    index.toLong()
                                )
                        }

                    RosterExportManager
                        .exportPdf(
                            context = context,
                            data = data,
                            months = months,
                            fileName =
                                "fifokit_roster_${selectedMonth.year}.pdf"
                        )
                }
            }
        }
    }

    fun runExport(
        kind: ExportKind,
        action: ExportAction
    ) {
        if (isExporting) {
            return
        }

        isExporting = true
        errorMessage = null

        scope.launch {
            try {
                val file =
                    generateExport(kind)

                growthTracker.recordMeaningfulAction()

                analytics.logEvent(
                    "roster_export_created"
                ) {
                    param(
                        "format",
                        kind.format
                    )
                    param(
                        "month_count",
                        kind
                            .monthCount
                            .toLong()
                    )
                }

                when (action) {
                    ExportAction.SHARE -> {
                        shareFile(
                            context = context,
                            file = file,
                            mimeType =
                                kind.mimeType
                        )

                        analytics.logEvent(
                            "roster_export_shared"
                        ) {
                            param(
                                "format",
                                kind.format
                            )
                            param(
                                "month_count",
                                kind
                                    .monthCount
                                    .toLong()
                            )
                        }
                    }

                    ExportAction.SAVE -> {
                        pendingSave =
                            GeneratedExport(
                                file = file,
                                kind = kind
                            )

                        if (
                            kind ==
                            ExportKind.MONTH_IMAGE
                        ) {
                            imageSaveLauncher
                                .launch(file.name)
                        } else {
                            pdfSaveLauncher
                                .launch(file.name)
                        }
                    }
                }

            } catch (_: Exception) {
                errorMessage =
                    "Unable to create roster export."
            } finally {
                isExporting = false
            }
        }
    }

    selectedExport?.let { exportKind ->
        AlertDialog(
            onDismissRequest = {
                selectedExport = null
            },
            title = {
                Text(exportKind.label)
            },
            text = {
                Column {
                    Text(
                        text = exportPeriodLabel(
                            kind = exportKind,
                            selectedMonth = selectedMonth
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        "Would you like to save this file or share it?"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedExport = null

                        runExport(
                            kind = exportKind,
                            action =
                                ExportAction.SHARE
                        )
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedExport = null

                        runExport(
                            kind = exportKind,
                            action =
                                ExportAction.SAVE
                        )
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FifokitTopBar(
                title = "Export roster",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Roster: ${data.rosterName}"
            )

            Text(
                text = "Choose month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        selectedMonth =
                            selectedMonth.minusMonths(1)
                    }
                ) {
                    Text("<")
                }

                Text(
                    text =
                        selectedMonth.month.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        ) +
                                " " +
                                selectedMonth.year,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = {
                        selectedMonth =
                            selectedMonth.plusMonths(1)
                    }
                ) {
                    Text(">")
                }
            }

            ExportMonthPreview(
                data = data,
                month = selectedMonth,
                onPrevious = {
                    selectedMonth =
                        selectedMonth.minusMonths(1)
                },
                onNext = {
                    selectedMonth =
                        selectedMonth.plusMonths(1)
                }
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                enabled = !isExporting,
                onClick = {
                    selectedExport =
                        ExportKind.MONTH_IMAGE
                }
            ) {
                Text(
                    ExportKind
                        .MONTH_IMAGE
                        .label
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text("FIFOKIT Pro")

            ProExportButton(
                text =
                    ExportKind
                        .THREE_MONTH_PDF
                        .label,
                feature =
                    ProFeature.ROSTER_MULTI_MONTH_EXPORT,
                featureName =
                    "roster_multi_month_export",
                enabled = !isExporting,
                onProRequested =
                    onProRequested,
                onExport = {
                    selectedExport =
                        ExportKind
                            .THREE_MONTH_PDF
                }
            )

            ProExportButton(
                text =
                    ExportKind
                        .SIX_MONTH_PDF
                        .label,
                feature =
                    ProFeature.ROSTER_MULTI_MONTH_EXPORT,
                featureName =
                    "roster_multi_month_export",
                enabled = !isExporting,
                onProRequested =
                    onProRequested,
                onExport = {
                    selectedExport =
                        ExportKind
                            .SIX_MONTH_PDF
                }
            )

            ProExportButton(
                text =
                    ExportKind
                        .ANNUAL_PDF
                        .label,
                feature =
                    ProFeature.ROSTER_ANNUAL_EXPORT,
                featureName =
                    "roster_annual_export",
                enabled = !isExporting,
                onProRequested =
                    onProRequested,
                onExport = {
                    selectedExport =
                        ExportKind
                            .ANNUAL_PDF
                }
            )

            if (isExporting) {
                Text("Creating export...")
            }

            errorMessage?.let {
                Text(it)
            }
        }
    }
}

private fun exportPeriodLabel(
    kind: ExportKind,
    selectedMonth: YearMonth
): String {
    return when (kind) {
        ExportKind.MONTH_IMAGE ->
            "Exports " +
                    monthLabel(selectedMonth) +
                    "."

        ExportKind.THREE_MONTH_PDF ->
            "Exports " +
                    monthLabel(selectedMonth) +
                    " to " +
                    monthLabel(
                        selectedMonth.plusMonths(2)
                    ) +
                    "."

        ExportKind.SIX_MONTH_PDF ->
            "Exports " +
                    monthLabel(selectedMonth) +
                    " to " +
                    monthLabel(
                        selectedMonth.plusMonths(5)
                    ) +
                    "."

        ExportKind.ANNUAL_PDF ->
            "Exports January to December " +
                    selectedMonth.year +
                    "."
    }
}

private fun monthLabel(
    month: YearMonth
): String {
    return month.month.getDisplayName(
        TextStyle.FULL,
        Locale.getDefault()
    ) + " " + month.year
}

@Composable
private fun ExportMonthPreview(
    data: RosterExportData,
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val cells =
        RosterExportCalendar.monthDays(
            month = month,
            data = data
        )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .calendarHorizontalSwipe(
                onPrevious = onPrevious,
                onNext = onNext
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

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
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            cells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    week.forEach { day ->
                        if (day == null) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(1.dp),
                                shape =
                                    MaterialTheme.shapes.small,
                                color =
                                    if (day.isWorkDay) {
                                        MaterialTheme.colorScheme
                                            .primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme
                                            .surfaceContainerHigh
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text =
                                                day.date.dayOfMonth
                                                    .toString(),
                                            style =
                                                MaterialTheme.typography
                                                    .labelLarge,
                                            fontWeight =
                                                FontWeight.SemiBold
                                        )

                                        Text(
                                            text =
                                                if (
                                                    day.isPublicHoliday
                                                ) {
                                                    "PH"
                                                } else if (
                                                    day.isWorkDay
                                                ) {
                                                    "W"
                                                } else {
                                                    "O"
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
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "W = WORK · O = OFF · PH = Public holiday",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ProExportButton(
    text: String,
    feature: ProFeature,
    featureName: String,
    enabled: Boolean,
    onProRequested: (String) -> Unit,
    onExport: () -> Unit
) {
    OutlinedButton(
        modifier =
            Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = {
            if (
                ProAccess.canUse(
                    feature
                )
            ) {
                onExport()
            } else {
                onProRequested(
                    featureName
                )
            }
        }
    ) {
        Text("$text · PRO")
    }
}

private fun shareFile(
    context: Context,
    file: File,
    mimeType: String
) {
    val uri =
        FileProvider.getUriForFile(
            context,
            context.packageName +
                    ".fileprovider",
            file
        )

    val shareIntent =
        Intent(
            Intent.ACTION_SEND
        ).apply {
            type = mimeType

            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Share FIFOKIT roster"
        )
    )
}
