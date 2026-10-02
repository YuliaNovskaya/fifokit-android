package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.finance.FinancialGoal
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.domain.finance.FinancialGoalResult
import com.fifokit.app.domain.finance.FinanceCalculator
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalScreen(
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
            FifokitTopBar(
                title = "Financial Goal",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = targetAmount,
                onValueChange = {
                    targetAmount = it
                    goalResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text = "Target amount",
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
                value = currentAmount,
                onValueChange = {
                    currentAmount = it
                    goalResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text = "Current savings",
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
                value = contributionPerPay,
                onValueChange = {
                    contributionPerPay = it
                    goalResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text = "Contribution per pay",
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
                value = payFrequencyDays,
                onValueChange = {
                    payFrequencyDays = it
                    goalResult = null
                    validationError = null
                },
                label = {
                    Text(
                        text = "Pay frequency in days",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
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
                    val targetValue = targetAmount.toDoubleOrNull()
                    val currentValue = currentAmount.toDoubleOrNull()
                    val contributionValue = contributionPerPay.toDoubleOrNull()
                    val frequencyValue = payFrequencyDays.toIntOrNull()

                    validationError = when {
                        targetValue == null || targetValue <= 0.0 ->
                            "Enter a valid target amount"

                        currentValue == null || currentValue < 0.0 ->
                            "Enter valid current savings"

                        contributionValue == null || contributionValue < 0.0 ->
                            "Enter a valid contribution"

                        currentValue < targetValue && contributionValue == 0.0 ->
                            "Enter a contribution greater than zero"

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
                            AnalyticsEvents.FINANCIAL_GOAL_CALCULATED,
                            Bundle().apply {
                                putString(
                                    AnalyticsParams.TOOL,
                                    "financial_goal"
                                )
                                putLong(
                                    AnalyticsParams.PAY_FREQUENCY_DAYS,
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
                    text = "Amount remaining: ${FinanceFormatter.money(result.amountRemaining)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
                    text = "Estimated goal date: $estimatedDate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}