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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.finance.PayRateType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.fifokit.app.domain.finance.EarningsResult
import com.fifokit.app.domain.finance.FinanceCalculator
import com.fifokit.app.domain.finance.PayInput
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.AustralianState
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import com.fifokit.app.data.FinancePreferences
import kotlinx.coroutines.launch
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.fifokit.app.domain.finance.FinanceFormatter
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayCalculatorScreen(
    selectedPattern: RosterPattern,
    isCustomRoster: Boolean,
    customWorkDays: Int,
    customOffDays: Int,
    rosterStartDate: LocalDate,
    selectedStates: Set<AustralianState>,
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

    val scope = rememberCoroutineScope()

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

    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    var calculatedAmount by remember {
        mutableStateOf<Double?>(null)
    }

    var annualResult by remember {
        mutableStateOf<EarningsResult?>(null)
    }

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
    }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "FIFO Pay Calculator",
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
                            calculatedAmount = null
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
                    calculatedAmount = null
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
                    calculatedAmount = null
                    annualResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text = "Hours per work day",
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
                    calculatedAmount = null
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

                    validationError = when {
                        rateValue == null || rateValue <= 0.0 ->
                            "Enter a valid pay rate"

                        hoursValue == null || hoursValue <= 0.0 ->
                            "Enter valid work hours"

                        allowanceValue < 0.0 ->
                            "Allowance cannot be negative"

                        else ->
                            null
                    }

                    if (validationError == null) {

                        val input = PayInput(
                            rateType = rateType,
                            rate = rateValue!!,
                            hoursPerWorkDay = hoursValue!!,
                            allowancePerWorkDay = allowanceValue
                        )
                        scope.launch {
                            financePreferences.savePayInput(input)
                        }

                        calculatedAmount = when (rateType) {
                            PayRateType.HOURLY ->
                                rateValue * hoursValue + allowanceValue

                            PayRateType.DAILY ->
                                rateValue + allowanceValue

                            PayRateType.ANNUAL_SALARY ->
                                rateValue
                        }

                        val currentYear = LocalDate.now().year

                        annualResult =
                            if (isCustomRoster) {
                                FinanceCalculator.calculateAnnualEarnings(
                                    input = input,
                                    rosterStartDate = rosterStartDate,
                                    workDays = customWorkDays,
                                    offDays = customOffDays,
                                    year = currentYear,
                                    selectedStates = selectedStates
                                )
                            } else {
                                FinanceCalculator.calculateAnnualEarnings(
                                    input = input,
                                    pattern = selectedPattern,
                                    rosterStartDate = rosterStartDate,
                                    year = currentYear,
                                    selectedStates = selectedStates
                                )
                            }
                        analytics.logEvent(
                            AnalyticsEvents.PAY_CALCULATION_COMPLETED,
                            Bundle().apply {
                                putString(
                                    AnalyticsParams.TOOL,
                                    "pay_calculator"
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
                        calculatedAmount = null
                        annualResult = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate")
            }
            validationError?.let { error ->

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = error
                )
            }
            calculatedAmount?.let { amount ->

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = when (rateType) {
                        PayRateType.HOURLY ->
                            "Gross per work day: ${FinanceFormatter.money(amount)}"

                        PayRateType.DAILY ->
                            "Gross per work day: ${FinanceFormatter.money(amount)}"

                        PayRateType.ANNUAL_SALARY ->
                            "Annual gross: ${FinanceFormatter.money(amount)}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            annualResult?.let { result ->

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = if (isCustomRoster) {
                        "Active roster: $customWorkDays/$customOffDays"
                    } else {
                        "Active roster: ${selectedPattern.label}"
                    }
                )

                Text(
                    text = "Work days this year: ${result.workDaysPerYear}"
                )

                Text(
                    text = "Work hours this year: ${"%.1f".format(result.workHoursPerYear)}"
                )

                Text(
                    text = "Annual gross: ${FinanceFormatter.money(result.totalGrossEarnings)}",
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
                    text =
                        "Public holidays worked this year: " +
                                result.publicHolidaysWorked,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
}