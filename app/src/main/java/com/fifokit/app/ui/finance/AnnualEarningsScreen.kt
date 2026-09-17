package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.finance.PayRateType
import com.fifokit.app.domain.model.RosterPattern
import java.time.LocalDate
import com.fifokit.app.domain.finance.EarningsResult
import com.fifokit.app.domain.finance.FinanceCalculator
import com.fifokit.app.domain.finance.PayInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.fifokit.app.data.FinancePreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualEarningsScreen(
    selectedPattern: RosterPattern,
    isCustomRoster: Boolean,
    customWorkDays: Int,
    customOffDays: Int,
    rosterStartDate: LocalDate,
    onBack: () -> Unit
) {
    val context = LocalContext.current

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

    var annualResult by remember {
        mutableStateOf<EarningsResult?>(null)
    }

    var validationError by remember {
        mutableStateOf<String?>(null)
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
            TopAppBar(
                title = {
                    Text("Annual Earnings")
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
                },
                label = {
                    Text(
                        when (rateType) {
                            PayRateType.HOURLY -> "Hourly rate"
                            PayRateType.DAILY -> "Daily rate"
                            PayRateType.ANNUAL_SALARY -> "Annual salary"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = hoursPerDay,
                onValueChange = {
                    hoursPerDay = it
                },
                label = {
                    Text(
                        when (rateType) {
                            PayRateType.HOURLY ->
                                "Hours per work day"

                            PayRateType.DAILY,
                            PayRateType.ANNUAL_SALARY ->
                                "Hours per work day (for annual hours)"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = allowancePerDay,
                onValueChange = {
                    allowancePerDay = it
                },
                label = {
                    Text("Allowance per work day")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
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

                        val year = LocalDate.now().year

                        annualResult =
                            if (isCustomRoster) {
                                FinanceCalculator.calculateAnnualEarnings(
                                    input = input,
                                    rosterStartDate = rosterStartDate,
                                    workDays = customWorkDays,
                                    offDays = customOffDays,
                                    year = year
                                )
                            } else {
                                FinanceCalculator.calculateAnnualEarnings(
                                    input = input,
                                    pattern = selectedPattern,
                                    rosterStartDate = rosterStartDate,
                                    year = year
                                )
                            }

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

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = when (rateType) {
                        PayRateType.ANNUAL_SALARY ->
                            "Annual salary: $${"%.2f".format(result.baseEarnings)}"

                        PayRateType.HOURLY,
                        PayRateType.DAILY ->
                            "Base earnings: $${"%.2f".format(result.baseEarnings)}"
                    }
                )

                Text(
                    "Allowances: $${"%.2f".format(result.allowances)}"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    "Total gross earnings: $${"%.2f".format(result.totalGrossEarnings)}"
                )
            }
            analytics.logEvent(
                "annual_earnings_calculated",
                Bundle().apply {
                    putString(
                        "pay_type",
                        rateType.name.lowercase()
                    )

                    putString(
                        "roster_type",
                        if (isCustomRoster) {
                            "custom"
                        } else {
                            selectedPattern.name.lowercase()
                        }
                    )
                }
            )

        }
    }
}