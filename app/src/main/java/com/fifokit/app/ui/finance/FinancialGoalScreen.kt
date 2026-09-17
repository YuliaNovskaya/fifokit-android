package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Column
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
import com.fifokit.app.domain.finance.FinancialGoal
import com.fifokit.app.domain.finance.FinancialGoalResult
import com.fifokit.app.domain.finance.FinanceCalculator
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.fifokit.app.data.FinancePreferences
import kotlinx.coroutines.launch
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val analytics = remember(context) {
        FirebaseAnalytics.getInstance(context)
    }

    val financePreferences = remember(context) {
        FinancePreferences(context)
    }

    val savedGoal by financePreferences.financialGoal.collectAsState(
        initial = FinancialGoal()
    )

    val scope = rememberCoroutineScope()

    var targetAmount by remember {
        mutableStateOf("")
    }

    var currentAmount by remember {
        mutableStateOf("")
    }

    var contributionPerPay by remember {
        mutableStateOf("")
    }

    var payFrequencyDays by remember {
        mutableStateOf("14")
    }

    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    var goalResult by remember {
        mutableStateOf<FinancialGoalResult?>(null)
    }

    LaunchedEffect(savedGoal) {
        targetAmount =
            if (savedGoal.targetAmount == 0.0) "" else savedGoal.targetAmount.toString()

        currentAmount =
            if (savedGoal.currentAmount == 0.0) "" else savedGoal.currentAmount.toString()

        contributionPerPay =
            if (savedGoal.contributionPerPay == 0.0) "" else savedGoal.contributionPerPay.toString()

        payFrequencyDays =
            savedGoal.payFrequencyDays.toString()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Financial Goal")
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
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = targetAmount,
                onValueChange = {
                    targetAmount = it
                },
                label = {
                    Text("Target amount")
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
                value = currentAmount,
                onValueChange = {
                    currentAmount = it
                },
                label = {
                    Text("Current savings")
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
                value = contributionPerPay,
                onValueChange = {
                    contributionPerPay = it
                },
                label = {
                    Text("Contribution per pay")
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
                value = payFrequencyDays,
                onValueChange = {
                    payFrequencyDays = it
                },
                label = {
                    Text("Pay frequency in days")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
                    val targetValue = targetAmount.toDoubleOrNull()
                    val currentValue = currentAmount.toDoubleOrNull()
                    val contributionValue = contributionPerPay.toDoubleOrNull()
                    val frequencyValue = payFrequencyDays.toIntOrNull()

                    validationError = when {
                        targetValue == null || targetValue <= 0.0 ->
                            "Enter a valid target amount"

                        currentValue == null || currentValue < 0.0 ->
                            "Enter valid current savings"

                        contributionValue == null || contributionValue <= 0.0 ->
                            "Enter a valid contribution"

                        frequencyValue == null || frequencyValue <= 0 ->
                            "Enter a valid pay frequency"

                        else ->
                            null
                    }

                    if (validationError == null) {
                        val goal = FinancialGoal(
                            targetAmount = targetValue!!,
                            currentAmount = currentValue!!,
                            contributionPerPay = contributionValue!!,
                            payFrequencyDays = frequencyValue!!
                        )
                        scope.launch {
                            financePreferences.saveFinancialGoal(goal)
                        }

                        goalResult =
                            FinanceCalculator.calculateFinancialGoal(goal)

                        analytics.logEvent(
                            "financial_goal_calculated",
                            Bundle().apply {
                                putLong(
                                    "pay_frequency_days",
                                    frequencyValue!!.toLong()
                                )
                            }
                        )

                    } else {
                        goalResult = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate goal")
            }

            validationError?.let { error ->

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(error)
            }
            goalResult?.let { result ->

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                val weeksRequired =
                    result.daysRequired / 7.0

                val monthsRequired =
                    result.daysRequired / 30.44

                val estimatedDate =
                    LocalDate.now().plusDays(result.daysRequired.toLong())

                Text(
                    "Amount remaining: $${"%.2f".format(result.amountRemaining)}"
                )

                Text(
                    "Contributions required: ${result.contributionsRequired}"
                )

                Text(
                    "Time required: ${"%.1f".format(weeksRequired)} weeks"
                )

                Text(
                    "Approx. months: ${"%.1f".format(monthsRequired)}"
                )

                Text(
                    "Estimated goal date: $estimatedDate"
                )
            }

        }
    }
}