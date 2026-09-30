package com.fifokit.app.ui.export

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.YearMonth

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

    fun runExport(
        format: String,
        monthCount: Int,
        exporter: suspend () -> File
    ) {
        if (isExporting) return

        isExporting = true
        errorMessage = null

        scope.launch {
            try {
                val file =
                    withContext(
                        Dispatchers.IO
                    ) {
                        exporter()
                    }

                analytics.logEvent(
                    "roster_export_created"
                ) {
                    param(
                        "format",
                        format
                    )
                    param(
                        "month_count",
                        monthCount.toLong()
                    )
                }

                shareFile(
                    context = context,
                    file = file,
                    mimeType =
                        if (format == "png") {
                            "image/png"
                        } else {
                            "application/pdf"
                        }
                )

                analytics.logEvent(
                    "roster_export_shared"
                ) {
                    param(
                        "format",
                        format
                    )
                    param(
                        "month_count",
                        monthCount.toLong()
                    )
                }

            } catch (_: Exception) {
                errorMessage =
                    "Unable to create roster export."
            } finally {
                isExporting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Export roster")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Roster: ${data.rosterName}"
            )

            Text(
                "Starting month: $startMonth"
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                enabled = !isExporting,
                onClick = {
                    runExport(
                        format = "png",
                        monthCount = 1
                    ) {
                        RosterExportManager
                            .exportMonthImage(
                                context = context,
                                data = data,
                                month =
                                    startMonth
                            )
                    }
                }
            ) {
                Text(
                    "Share this month image"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text("FIFOKIT Pro")

            ProExportButton(
                text =
                    "Share 3-month PDF",
                feature =
                    ProFeature.ROSTER_MULTI_MONTH_EXPORT,
                featureName =
                    "roster_multi_month_export",
                enabled = !isExporting,
                onProRequested =
                    onProRequested,
                onExport = {
                    val months =
                        List(3) { index ->
                            startMonth
                                .plusMonths(
                                    index.toLong()
                                )
                        }

                    runExport(
                        format = "pdf",
                        monthCount = 3
                    ) {
                        RosterExportManager
                            .exportPdf(
                                context =
                                    context,
                                data = data,
                                months =
                                    months,
                                fileName =
                                    "fifokit_roster_3_months.pdf"
                            )
                    }
                }
            )

            ProExportButton(
                text =
                    "Share 6-month PDF",
                feature =
                    ProFeature.ROSTER_MULTI_MONTH_EXPORT,
                featureName =
                    "roster_multi_month_export",
                enabled = !isExporting,
                onProRequested =
                    onProRequested,
                onExport = {
                    val months =
                        List(6) { index ->
                            startMonth
                                .plusMonths(
                                    index.toLong()
                                )
                        }

                    runExport(
                        format = "pdf",
                        monthCount = 6
                    ) {
                        RosterExportManager
                            .exportPdf(
                                context =
                                    context,
                                data = data,
                                months =
                                    months,
                                fileName =
                                    "fifokit_roster_6_months.pdf"
                            )
                    }
                }
            )

            ProExportButton(
                text =
                    "Share annual PDF",
                feature =
                    ProFeature.ROSTER_ANNUAL_EXPORT,
                featureName =
                    "roster_annual_export",
                enabled = !isExporting,
                onProRequested =
                    onProRequested,
                onExport = {
                    val firstMonth =
                        YearMonth.of(
                            startMonth.year,
                            1
                        )

                    val months =
                        List(12) { index ->
                            firstMonth
                                .plusMonths(
                                    index.toLong()
                                )
                        }

                    runExport(
                        format = "pdf",
                        monthCount = 12
                    ) {
                        RosterExportManager
                            .exportPdf(
                                context =
                                    context,
                                data = data,
                                months =
                                    months,
                                fileName =
                                    "fifokit_roster_${startMonth.year}.pdf"
                            )
                    }
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
        modifier = Modifier.fillMaxWidth(),
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
