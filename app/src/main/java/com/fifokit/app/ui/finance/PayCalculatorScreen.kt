package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
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
import com.fifokit.app.domain.finance.PipType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.fifokit.app.domain.finance.EarningsResult
import com.fifokit.app.domain.finance.LocalPayResult
import com.fifokit.app.domain.finance.FinanceCalculator
import com.fifokit.app.domain.finance.PayInput
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.RosterScheduleSegment
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
    isShutdownRoster: Boolean,
    scheduleSegments:
        List<RosterScheduleSegment>,
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
    val scrollState = rememberScrollState()

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

    var localRatesEnabled by remember {
        mutableStateOf(false)
    }

    var localWeekdayRate by remember {
        mutableStateOf("")
    }

    var localWeekdayBaseHours by remember {
        mutableStateOf("8")
    }

    var localWeekdayOvertimeRate by remember {
        mutableStateOf("")
    }

    var localWeekdayOvertimeHours by remember {
        mutableStateOf("0")
    }

    var localSaturdayRate by remember {
        mutableStateOf("")
    }

    var localSaturdayHours by remember {
        mutableStateOf("8")
    }

    var localSundayRate by remember {
        mutableStateOf("")
    }

    var localSundayHours by remember {
        mutableStateOf("0")
    }

    var localResult by remember {
        mutableStateOf<LocalPayResult?>(null)
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

        pipType =
            savedPayInput.pipType

        pipValue =
            if (savedPayInput.pipValue == 0.0) {
                ""
            } else {
                savedPayInput.pipValue.toString()
            }

        localRatesEnabled =
            savedPayInput.localRatesEnabled

        localWeekdayRate =
            if (savedPayInput.localWeekdayHourlyRate == 0.0) {
                ""
            } else {
                savedPayInput.localWeekdayHourlyRate.toString()
            }

        localWeekdayBaseHours =
            savedPayInput.localWeekdayBaseHours
                .toString()

        localWeekdayOvertimeRate =
            if (
                savedPayInput
                    .localWeekdayOvertimeHourlyRate ==
                0.0
            ) {
                ""
            } else {
                savedPayInput
                    .localWeekdayOvertimeHourlyRate
                    .toString()
            }

        localWeekdayOvertimeHours =
            savedPayInput.localWeekdayOvertimeHours
                .toString()

        localSaturdayRate =
            if (savedPayInput.localSaturdayHourlyRate == 0.0) {
                ""
            } else {
                savedPayInput.localSaturdayHourlyRate.toString()
            }

        localSaturdayHours =
            savedPayInput.localSaturdayHours
                .toString()

        localSundayRate =
            if (
                savedPayInput.localSundayHourlyRate ==
                0.0
            ) {
                ""
            } else {
                savedPayInput.localSundayHourlyRate
                    .toString()
            }

        localSundayHours =
            savedPayInput.localSundayHours
                .toString()
    }

    LaunchedEffect(
        annualResult,
        localResult
    ) {
        if (
            annualResult != null ||
            localResult != null
        ) {
            scrollState.animateScrollTo(
                scrollState.maxValue
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = "Work type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!localRatesEnabled) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Text("FIFO")
                    }

                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            localRatesEnabled = true
                            calculatedAmount = null
                            annualResult = null
                            localResult = null
                            validationError = null
                        }
                    ) {
                        Text("Local rates")
                    }
                } else {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            localRatesEnabled = false
                            localResult = null
                            validationError = null
                        }
                    ) {
                        Text("FIFO")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Text("Local rates")
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            if (!localRatesEnabled) {
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
                modifier = Modifier.height(20.dp)
            )

            PipInputSection(
                pipType = pipType,
                pipValue = pipValue,
                onPipTypeChange = {
                    pipType = it
                    pipValue = ""
                    calculatedAmount = null
                    annualResult = null
                    validationError = null
                },
                onPipValueChange = {
                    pipValue = it
                    calculatedAmount = null
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
                                FinanceCalculator
                                    .calculateAnnualEarnings(
                                        input = input,
                                        rosterStartDate =
                                            rosterStartDate,
                                        scheduleSegments =
                                            scheduleSegments,
                                        repeatSchedule =
                                            !isShutdownRoster,
                                        year =
                                            currentYear,
                                        selectedStates =
                                            selectedStates
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
                    text = "Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
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
                    text =
                        if (isShutdownRoster) {
                            "Active roster: shutdown sequence"
                        } else if (isCustomRoster) {
                            "Active roster: custom sequence"
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
                    text = "Base: ${FinanceFormatter.money(result.baseEarnings)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "PIP: ${FinanceFormatter.money(result.pipEarnings)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Allowances: ${FinanceFormatter.money(result.allowances)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Total annual gross: ${FinanceFormatter.money(result.totalGrossEarnings)}",
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

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
            } else {
                OutlinedTextField(
                    value = localWeekdayRate,
                    onValueChange = {
                        localWeekdayRate = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Mon-Fri base hourly rate",
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
                    value = localWeekdayBaseHours,
                    onValueChange = {
                        localWeekdayBaseHours = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Mon-Fri base hours per day",
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
                    value = localWeekdayOvertimeRate,
                    onValueChange = {
                        localWeekdayOvertimeRate = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Mon-Fri overtime hourly rate",
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
                    value = localWeekdayOvertimeHours,
                    onValueChange = {
                        localWeekdayOvertimeHours = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Overtime hours per weekday",
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
                    value = localSaturdayRate,
                    onValueChange = {
                        localSaturdayRate = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Saturday hourly rate",
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
                    value = localSaturdayHours,
                    onValueChange = {
                        localSaturdayHours = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Saturday hours",
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
                    value = localSundayRate,
                    onValueChange = {
                        localSundayRate = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Sunday hourly rate",
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
                    value = localSundayHours,
                    onValueChange = {
                        localSundayHours = it
                        localResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Sunday hours",
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
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Set overtime, Saturday or Sunday hours to 0 when they do not apply.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        val weekday =
                            localWeekdayRate.toDoubleOrNull()
                        val weekdayBaseHours =
                            localWeekdayBaseHours.toDoubleOrNull()
                        val overtimeRate =
                            localWeekdayOvertimeRate
                                .toDoubleOrNull()
                                ?: 0.0
                        val overtimeHours =
                            localWeekdayOvertimeHours
                                .toDoubleOrNull()
                        val saturday =
                            localSaturdayRate
                                .toDoubleOrNull()
                                ?: 0.0
                        val saturdayHours =
                            localSaturdayHours
                                .toDoubleOrNull()
                        val sunday =
                            localSundayRate
                                .toDoubleOrNull()
                                ?: 0.0
                        val sundayHours =
                            localSundayHours
                                .toDoubleOrNull()

                        validationError = when {
                            weekday == null ||
                                    weekday <= 0.0 ->
                                "Enter a valid Mon-Fri base rate"

                            weekdayBaseHours == null ||
                                    weekdayBaseHours <= 0.0 ->
                                "Enter valid Mon-Fri base hours"

                            overtimeHours == null ||
                                    overtimeHours < 0.0 ->
                                "Enter valid overtime hours"

                            overtimeHours > 0.0 &&
                                    overtimeRate <= 0.0 ->
                                "Enter an overtime rate"

                            saturdayHours == null ||
                                    saturdayHours < 0.0 ->
                                "Enter valid Saturday hours"

                            saturdayHours > 0.0 &&
                                    saturday <= 0.0 ->
                                "Enter a Saturday rate"

                            sundayHours == null ||
                                    sundayHours < 0.0 ->
                                "Enter valid Sunday hours"

                            sundayHours > 0.0 &&
                                    sunday <= 0.0 ->
                                "Enter a Sunday rate"

                            else ->
                                null
                        }

                        if (validationError == null) {
                            localResult =
                                FinanceCalculator.calculateLocalPay(
                                    weekdayHourlyRate = weekday!!,
                                    weekdayBaseHours =
                                        weekdayBaseHours!!,
                                    weekdayOvertimeHourlyRate =
                                        overtimeRate,
                                    weekdayOvertimeHours =
                                        overtimeHours!!,
                                    saturdayHourlyRate =
                                        saturday,
                                    saturdayHours =
                                        saturdayHours!!,
                                    sundayHourlyRate =
                                        sunday,
                                    sundayHours =
                                        sundayHours!!
                                )

                            val input =
                                savedPayInput.copy(
                                    localRatesEnabled = true,
                                    localWeekdayHourlyRate =
                                        weekday,
                                    localWeekdayBaseHours =
                                        weekdayBaseHours,
                                    localWeekdayOvertimeHourlyRate =
                                        overtimeRate,
                                    localWeekdayOvertimeHours =
                                        overtimeHours,
                                    localSaturdayHourlyRate =
                                        saturday,
                                    localSaturdayHours =
                                        saturdayHours,
                                    localSundayHourlyRate =
                                        sunday,
                                    localSundayHours =
                                        sundayHours,
                                    localHoursPerDay =
                                        weekdayBaseHours
                                )

                            scope.launch {
                                financePreferences.savePayInput(input)
                            }
                        } else {
                            localResult = null
                        }
                    }
                ) {
                    Text("Calculate local pay")
                }

                validationError?.let { error ->
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(error)
                }

                localResult?.let { result ->
                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        text = "Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Mon-Fri base: " +
                                FinanceFormatter.money(
                                    result.weekdayBaseGrossPerWeek
                                ),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (
                        result.weekdayOvertimeGrossPerWeek >
                        0.0
                    ) {
                        Text(
                            text = "Mon-Fri overtime: " +
                                    FinanceFormatter.money(
                                        result.weekdayOvertimeGrossPerWeek
                                    ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Text(
                        text = "Mon-Fri total: " +
                                FinanceFormatter.money(
                                    result.weekdayGrossPerWeek
                                ),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (result.saturdayGrossPerWeek > 0.0) {
                        Text(
                            text = "Saturday gross: " +
                                    FinanceFormatter.money(
                                        result.saturdayGrossPerWeek
                                    ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (result.sundayGrossPerWeek > 0.0) {
                        Text(
                            text = "Sunday gross: " +
                                    FinanceFormatter.money(
                                        result.sundayGrossPerWeek
                                    ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Text(
                        text = "Weekly gross: " +
                                FinanceFormatter.money(result.weeklyGross),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Annualised gross: " +
                                FinanceFormatter.money(result.annualisedGross),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Effective hourly rate: " +
                                FinanceFormatter.money(result.effectiveHourlyRate) +
                                " / hour",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

        }
    }
}