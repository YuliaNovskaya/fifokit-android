package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.finance.PayRateType
import com.fifokit.app.domain.finance.PipType
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.ShutdownPeriod
import java.time.LocalDate
import com.fifokit.app.domain.finance.EarningsResult
import com.fifokit.app.domain.finance.FinanceCalculator
import com.fifokit.app.domain.finance.PayInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import com.fifokit.app.data.FinancePreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.fifokit.app.domain.finance.FinanceFormatter
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualEarningsScreen(
    selectedPattern: RosterPattern,
    isCustomRoster: Boolean,
    customWorkDays: Int,
    customOffDays: Int,
    rosterStartDate: LocalDate,
    selectedStates: Set<AustralianState>,
    shutdowns: List<ShutdownPeriod>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController =
        LocalSoftwareKeyboardController.current
    val focusManager =
        LocalFocusManager.current

    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }

    val financePreferences = remember(context) {
        FinancePreferences(context)
    }

    val savedPayInput by financePreferences.payInput.collectAsState(
        initial = PayInput()
    )

    var rateType by remember {
        mutableStateOf(PayRateType.HOURLY)
    }

    var rate by remember {
        mutableStateOf("")
    }

    var hoursPerDay by remember {
        mutableStateOf("12")
    }

    var allowancePerDay by remember {
        mutableStateOf("")
    }

    var pipType by remember {
        mutableStateOf(PipType.NONE)
    }

    var pipValue by remember {
        mutableStateOf("")
    }

    var annualResult by remember {
        mutableStateOf<EarningsResult?>(null)
    }

    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(savedPayInput) {
        rateType = savedPayInput.rateType

        rate =
            if (savedPayInput.rate == 0.0) {
                ""
            } else {
                savedPayInput.rate.toString()
            }

        hoursPerDay =
            savedPayInput.hoursPerWorkDay.toString()

        allowancePerDay =
            if (savedPayInput.allowancePerWorkDay == 0.0) {
                ""
            } else {
                savedPayInput.allowancePerWorkDay.toString()
            }

        pipType =
            savedPayInput.pipType

        pipValue =
            if (savedPayInput.pipValue == 0.0) {
                ""
            } else {
                savedPayInput.pipValue.toString()
            }
    }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "Annual Earnings",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (isCustomRoster) {
                    "Active roster: $customWorkDays/$customOffDays"
                } else {
                    "Active roster: ${selectedPattern.label}"
                }
            )

            Text(
                text = "Roster start: $rosterStartDate"
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
            Text("Pay type")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PayRateType.entries.forEach { type ->
                    TextButton(
                        onClick = {
                            rateType = type
                            annualResult = null
                            validationError = null
                        }
                    ) {
                        Text(
                            when (type) {
                                PayRateType.HOURLY -> "Hourly"
                                PayRateType.DAILY -> "Daily"
                                PayRateType.ANNUAL_SALARY -> "Salary"
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = rate,
                onValueChange = {
                    rate = it
                    annualResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text =
                            when (rateType) {
                                PayRateType.HOURLY -> "Hourly rate"
                                PayRateType.DAILY -> "Daily rate"
                                PayRateType.ANNUAL_SALARY -> "Annual salary"
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = hoursPerDay,
                onValueChange = {
                    hoursPerDay = it
                    annualResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text =
                            when (rateType) {
                                PayRateType.HOURLY ->
                                    "Hours per work day"

                                PayRateType.DAILY,
                                PayRateType.ANNUAL_SALARY ->
                                    "Hours per work day (for annual hours)"
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = allowancePerDay,
                onValueChange = {
                    allowancePerDay = it
                    annualResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text = "Allowance per work day",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            PipInputSection(
                pipType = pipType,
                pipValue = pipValue,
                onPipTypeChange = {
                    pipType = it
                    pipValue = ""
                    annualResult = null
                    validationError = null
                },
                onPipValueChange = {
                    pipValue = it
                    annualResult = null
                    validationError = null
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val rateValue = rate.toDoubleOrNull()
                    val hoursValue = hoursPerDay.toDoubleOrNull()
                    val allowanceValue =
                        allowancePerDay.toDoubleOrNull() ?: 0.0

                    val pipNumericValue =
                        if (pipType == PipType.NONE) {
                            0.0
                        } else {
                            pipValue.toDoubleOrNull()
                        }

                    validationError = when {
                        rateValue == null || rateValue <= 0.0 ->
                            "Enter a valid pay rate"

                        hoursValue == null || hoursValue <= 0.0 ->
                            "Enter valid work hours"

                        allowanceValue < 0.0 ->
                            "Allowance cannot be negative"

                        pipNumericValue == null ||
                                pipNumericValue < 0.0 ->
                            "Enter a valid PIP value"

                        else ->
                            null
                    }

                    if (validationError == null) {

                        val input = PayInput(
                            rateType = rateType,
                            rate = rateValue!!,
                            hoursPerWorkDay = hoursValue!!,
                            allowancePerWorkDay = allowanceValue,
                            pipType = pipType,
                            pipValue = pipNumericValue!!
                        )

                        scope.launch {
                            financePreferences.savePayInput(input)
                        }

                        val year = LocalDate.now().year

                        annualResult =
                            if (isCustomRoster) {
                                FinanceCalculator.calculateAnnualEarnings(
                                    input = input,
                                    rosterStartDate = rosterStartDate,
                                    workDays = customWorkDays,
                                    offDays = customOffDays,
                                    year = year,
                                    selectedStates = selectedStates,
                                    shutdowns = shutdowns
                                )
                            } else {
                                FinanceCalculator.calculateAnnualEarnings(
                                    input = input,
                                    pattern = selectedPattern,
                                    rosterStartDate = rosterStartDate,
                                    year = year,
                                    selectedStates = selectedStates,
                                    shutdowns = shutdowns
                                )
                            }
                        analytics.logEvent(
                            AnalyticsEvents.ANNUAL_EARNINGS_CALCULATED,
                            Bundle().apply {
                                putString(
                                    AnalyticsParams.TOOL,
                                    "annual_earnings"
                                )
                                putString(
                                    AnalyticsParams.PAY_TYPE,
                                    rateType.name.lowercase()
                                )
                                putString(
                                    AnalyticsParams.ROSTER_TYPE,
                                    if (isCustomRoster) {
                                        "custom"
                                    } else {
                                        selectedPattern.name.lowercase()
                                    }
                                )
                            }
                        )
                    } else {
                        annualResult = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate annual earnings")
            }
            validationError?.let { error ->

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(error)
            }

            annualResult?.let { result ->

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text("Work days: ${result.workDaysPerYear}")

                Text("Off days: ${result.restDaysPerYear}")

                Text(
                    "Work hours: ${"%.1f".format(result.workHoursPerYear)}"
                )

                Text(
                    text =
                        "Public holidays worked: " +
                                result.publicHolidaysWorked,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = when (rateType) {
                        PayRateType.ANNUAL_SALARY ->
                            "Annual salary: ${FinanceFormatter.money(result.baseEarnings)}"

                        PayRateType.HOURLY,
                        PayRateType.DAILY ->
                            "Base earnings: ${FinanceFormatter.money(result.baseEarnings)}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                result.equivalentHourlyRate?.let { hourly ->
                    Text(
                        text =
                            "Equivalent hourly rate: " +
                                    FinanceFormatter.money(hourly) +
                                    " / hour",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "PIP: ${FinanceFormatter.money(result.pipEarnings)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Allowances: ${FinanceFormatter.money(result.allowances)}"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Total gross earnings: ${FinanceFormatter.money(result.totalGrossEarnings)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )


            }

        }
    }
}