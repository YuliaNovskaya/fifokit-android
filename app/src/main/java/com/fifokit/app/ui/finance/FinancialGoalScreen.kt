package com.fifokit.app.ui.finance

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import com.fifokit.app.data.FinancePreferences
import com.fifokit.app.domain.finance.FinanceCalculator
import com.fifokit.app.domain.finance.FinanceFormatter
import com.fifokit.app.domain.finance.FinancialGoal
import com.fifokit.app.domain.finance.FinancialGoalResult
import com.fifokit.app.domain.pro.ProAccess
import com.fifokit.app.domain.pro.ProFeature
import com.fifokit.app.ui.components.FifokitTopBar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalScreen(
    onBack: () -> Unit,
    onProRequested: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController =
        LocalSoftwareKeyboardController.current
    val focusManager =
        LocalFocusManager.current

    val analytics =
        remember(context) {
            FirebaseAnalytics.getInstance(
                context
            )
        }

    val financePreferences =
        remember(context) {
            FinancePreferences(context)
        }

    val goals by
        financePreferences
            .financialGoals
            .collectAsState(
                initial =
                    listOf(
                        FinancialGoal()
                    )
            )

    val scope =
        rememberCoroutineScope()

    var selectedGoalId by remember {
        mutableStateOf("primary")
    }

    var draftGoal by remember {
        mutableStateOf<FinancialGoal?>(null)
    }

    var isEditing by remember {
        mutableStateOf(false)
    }

    var goalMenuExpanded by remember {
        mutableStateOf(false)
    }

    var showCreateGoalDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteGoalDialog by remember {
        mutableStateOf(false)
    }

    var newGoalName by remember {
        mutableStateOf("")
    }

    var goalName by remember {
        mutableStateOf("")
    }

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

    val storedSelectedGoal =
        goals.firstOrNull {
            it.id == selectedGoalId
        } ?: goals.firstOrNull {
            it.id == "primary"
        } ?: FinancialGoal()

    val selectedGoal =
        draftGoal
            ?: storedSelectedGoal

    fun loadGoalFields(
        goal: FinancialGoal
    ) {
        goalName =
            goal.name

        targetAmount =
            if (goal.targetAmount == 0.0) {
                ""
            } else {
                goal.targetAmount.toString()
            }

        currentAmount =
            if (goal.currentAmount == 0.0) {
                ""
            } else {
                goal.currentAmount.toString()
            }

        contributionPerPay =
            if (
                goal.contributionPerPay ==
                0.0
            ) {
                ""
            } else {
                goal.contributionPerPay
                    .toString()
            }

        payFrequencyDays =
            goal.payFrequencyDays
                .toString()

        validationError = null
    }

    fun calculateSavedGoal(
        goal: FinancialGoal
    ): FinancialGoalResult? {
        return if (
            goal.targetAmount > 0.0 &&
            goal.currentAmount >= 0.0 &&
            goal.contributionPerPay >= 0.0 &&
            goal.payFrequencyDays > 0 &&
            (
                goal.currentAmount >=
                    goal.targetAmount ||
                goal.contributionPerPay > 0.0
            )
        ) {
            FinanceCalculator
                .calculateFinancialGoal(
                    goal
                )
        } else {
            null
        }
    }

    fun cancelEditing() {
        keyboardController?.hide()
        focusManager.clearFocus()

        val wasDraft =
            draftGoal != null

        if (wasDraft) {
            val fallbackGoal =
                goals.firstOrNull {
                    it.id == "primary"
                } ?: goals.firstOrNull()
                ?: FinancialGoal()

            draftGoal = null
            selectedGoalId =
                fallbackGoal.id

            loadGoalFields(
                fallbackGoal
            )

            goalResult =
                calculateSavedGoal(
                    fallbackGoal
                )
        } else {
            loadGoalFields(
                storedSelectedGoal
            )

            goalResult =
                calculateSavedGoal(
                    storedSelectedGoal
                )
        }

        isEditing = false
        validationError = null
    }

    BackHandler(
        enabled = isEditing
    ) {
        cancelEditing()
    }

    LaunchedEffect(
        goals,
        selectedGoalId,
        draftGoal?.id
    ) {
        val draft =
            draftGoal

        if (
            draft != null &&
            goals.any {
                it.id == draft.id
            }
        ) {
            selectedGoalId =
                draft.id
            draftGoal = null
        } else if (
            draft == null &&
            goals.none {
                it.id == selectedGoalId
            }
        ) {
            selectedGoalId =
                goals
                    .firstOrNull()
                    ?.id
                    ?: "primary"
        }
    }

    LaunchedEffect(
        selectedGoal.id,
        selectedGoal.updatedAt,
        draftGoal?.id
    ) {
        loadGoalFields(
            selectedGoal
        )

        if (draftGoal != null) {
            goalResult =
                calculateSavedGoal(
                    selectedGoal
                )
        } else {
            goalResult =
                calculateSavedGoal(
                    selectedGoal
                )

            isEditing =
                selectedGoal.targetAmount <=
                        0.0
        }
    }

    if (showCreateGoalDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateGoalDialog = false
                newGoalName = ""
            },
            title = {
                Text(
                    "New financial goal"
                )
            },
            text = {
                OutlinedTextField(
                    value = newGoalName,
                    onValueChange = {
                        newGoalName = it
                    },
                    label = {
                        Text(
                            text = "Goal name",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )
                    },
                    singleLine = true,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled =
                        newGoalName
                            .trim()
                            .isNotEmpty(),
                    onClick = {
                        val name =
                            newGoalName.trim()

                        showCreateGoalDialog =
                            false
                        newGoalName = ""

                        val draft =
                            FinancialGoal(
                                id =
                                    UUID
                                        .randomUUID()
                                        .toString(),
                                name = name
                            )

                        draftGoal =
                            draft
                        selectedGoalId =
                            draft.id
                        isEditing = true
                        goalResult = null
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateGoalDialog =
                            false
                        newGoalName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (
        showDeleteGoalDialog &&
        selectedGoal.id != "primary" &&
        draftGoal == null
    ) {
        AlertDialog(
            onDismissRequest = {
                showDeleteGoalDialog =
                    false
            },
            title = {
                Text(
                    "Delete financial goal?"
                )
            },
            text = {
                Text(
                    "Delete \"" +
                            selectedGoal.name +
                            "\"? This will also be removed from cloud sync."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val goalId =
                            selectedGoal.id

                        showDeleteGoalDialog =
                            false
                        selectedGoalId =
                            "primary"
                        isEditing = false

                        scope.launch {
                            financePreferences
                                .deleteFinancialGoal(
                                    goalId
                                )
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteGoalDialog =
                            false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title = "Financial Goal",
                onBack = {
                    if (isEditing) {
                        cancelEditing()
                    } else {
                        onBack()
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Saved goals",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick = {
                    goalMenuExpanded =
                        true
                }
            ) {
                Text(
                    if (
                        selectedGoal.id ==
                        "primary"
                    ) {
                        selectedGoal.name
                    } else {
                        selectedGoal.name +
                                " · PRO"
                    }
                )
            }

            DropdownMenu(
                expanded =
                    goalMenuExpanded,
                onDismissRequest = {
                    goalMenuExpanded =
                        false
                }
            ) {
                goals.forEach { goal ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (
                                    goal.id ==
                                    "primary"
                                ) {
                                    goal.name
                                } else {
                                    goal.name +
                                            " · PRO"
                                }
                            )
                        },
                        onClick = {
                            goalMenuExpanded =
                                false

                            if (
                                goal.id ==
                                "primary" ||
                                ProAccess.canUse(
                                    ProFeature
                                        .ADVANCED_FINANCIAL_GOALS
                                )
                            ) {
                                draftGoal = null
                                selectedGoalId =
                                    goal.id
                                isEditing =
                                    false
                            } else {
                                onProRequested(
                                    "advanced_financial_goals"
                                )
                            }
                        }
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            "+ New goal · PRO"
                        )
                    },
                    onClick = {
                        goalMenuExpanded =
                            false

                        if (
                            ProAccess.canUse(
                                ProFeature
                                    .ADVANCED_FINANCIAL_GOALS
                            )
                        ) {
                            showCreateGoalDialog =
                                true
                        } else {
                            onProRequested(
                                "advanced_financial_goals"
                            )
                        }
                    }
                )
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            if (isEditing) {
                Text(
                    text =
                        if (
                            draftGoal != null
                        ) {
                            "New goal"
                        } else {
                            "Edit goal"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = goalName,
                    onValueChange = {
                        goalName = it
                        goalResult = null
                    },
                    label = {
                        Text(
                            text = "Goal name",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            fontWeight =
                                FontWeight.Medium
                        )
                    },
                    textStyle =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    singleLine = true,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = {
                        targetAmount = it
                        goalResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text =
                                "Target amount",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            fontWeight =
                                FontWeight.Medium
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType
                                    .Decimal
                        ),
                    textStyle =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
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
                            text =
                                "Current savings",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            fontWeight =
                                FontWeight.Medium
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType
                                    .Decimal
                        ),
                    textStyle =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value =
                        contributionPerPay,
                    onValueChange = {
                        contributionPerPay = it
                        goalResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text =
                                "Contribution per pay",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            fontWeight =
                                FontWeight.Medium
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType
                                    .Decimal
                        ),
                    textStyle =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value =
                        payFrequencyDays,
                    onValueChange = {
                        payFrequencyDays = it
                        goalResult = null
                        validationError = null
                    },
                    label = {
                        Text(
                            text =
                                "Pay frequency in days",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            fontWeight =
                                FontWeight.Medium
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType
                                    .Number
                        ),
                    textStyle =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Button(
                    onClick = {
                        keyboardController
                            ?.hide()

                        focusManager.clearFocus()

                        val targetValue =
                            targetAmount
                                .toDoubleOrNull()

                        val currentValue =
                            currentAmount
                                .toDoubleOrNull()

                        val contributionValue =
                            contributionPerPay
                                .toDoubleOrNull()

                        val frequencyValue =
                            payFrequencyDays
                                .toIntOrNull()

                        validationError =
                            when {
                                goalName
                                    .trim()
                                    .isEmpty() ->
                                    "Enter a goal name"

                                targetValue == null ||
                                        targetValue <=
                                        0.0 ->
                                    "Enter a valid target amount"

                                currentValue == null ||
                                        currentValue <
                                        0.0 ->
                                    "Enter valid current savings"

                                contributionValue ==
                                        null ||
                                        contributionValue <
                                        0.0 ->
                                    "Enter a valid contribution"

                                currentValue <
                                        targetValue &&
                                        contributionValue ==
                                        0.0 ->
                                    "Enter a contribution greater than zero"

                                frequencyValue ==
                                        null ||
                                        frequencyValue <=
                                        0 ->
                                    "Enter a valid pay frequency"

                                else ->
                                    null
                            }

                        if (
                            validationError ==
                            null
                        ) {
                            val goal =
                                selectedGoal.copy(
                                    name =
                                        goalName
                                            .trim(),
                                    targetAmount =
                                        targetValue!!,
                                    currentAmount =
                                        currentValue!!,
                                    contributionPerPay =
                                        contributionValue!!,
                                    payFrequencyDays =
                                        frequencyValue!!
                                )

                            val result =
                                FinanceCalculator
                                    .calculateFinancialGoal(
                                        goal
                                    )

                            if (
                                draftGoal !=
                                null
                            ) {
                                draftGoal =
                                    goal
                            }

                            goalResult =
                                result
                            isEditing =
                                false

                            scope.launch {
                                financePreferences
                                    .saveFinancialGoal(
                                        goal
                                    )
                            }

                            analytics.logEvent(
                                AnalyticsEvents
                                    .FINANCIAL_GOAL_CALCULATED,
                                Bundle().apply {
                                    putString(
                                        AnalyticsParams
                                            .TOOL,
                                        "financial_goal"
                                    )
                                    putLong(
                                        AnalyticsParams
                                            .PAY_FREQUENCY_DAYS,
                                        frequencyValue
                                            .toLong()
                                    )
                                }
                            )
                        }
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Save & calculate"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick = {
                        cancelEditing()
                    }
                ) {
                    Text(
                        if (
                            draftGoal != null
                        ) {
                            "Cancel new goal"
                        } else {
                            "Cancel edit"
                        }
                    )
                }

                if (
                    draftGoal == null &&
                    selectedGoal.id != "primary"
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    OutlinedButton(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            showDeleteGoalDialog =
                                true
                        }
                    ) {
                        Text("Delete goal")
                    }
                }
            } else {
                Text(
                    text =
                        selectedGoal.name,
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        MaterialTheme
                            .shapes
                            .medium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                16.dp
                            )
                    ) {
                        Text(
                            text =
                                "Target: " +
                                        FinanceFormatter.money(
                                            selectedGoal
                                                .targetAmount
                                        ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )

                        Text(
                            text =
                                "Current savings: " +
                                        FinanceFormatter.money(
                                            selectedGoal
                                                .currentAmount
                                        ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )

                        Text(
                            text =
                                "Contribution per pay: " +
                                        FinanceFormatter.money(
                                            selectedGoal
                                                .contributionPerPay
                                        ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )

                        Text(
                            text =
                                "Pay every " +
                                        selectedGoal
                                            .payFrequencyDays +
                                        " days",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    OutlinedButton(
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            loadGoalFields(
                                selectedGoal
                            )
                            isEditing =
                                true
                        }
                    ) {
                        Text("Edit goal")
                    }

                    if (
                        selectedGoal.id !=
                        "primary"
                    ) {
                        OutlinedButton(
                            modifier =
                                Modifier.weight(
                                    1f
                                ),
                            onClick = {
                                showDeleteGoalDialog =
                                    true
                            }
                        ) {
                            Text("Delete goal")
                        }
                    }
                }

                goalResult?.let { result ->
                    Spacer(
                        modifier =
                            Modifier.height(
                                20.dp
                            )
                    )

                    FinancialGoalCalculation(
                        result = result
                    )
                }
            }

            validationError?.let { error ->
                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(error)
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun FinancialGoalCalculation(
    result: FinancialGoalResult
) {
    val weeksRequired =
        result.daysRequired / 7.0

    val monthsRequired =
        result.daysRequired / 30.44

    val estimatedDate =
        LocalDate.now()
            .plusDays(
                result.daysRequired
                    .toLong()
            )

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            MaterialTheme
                .shapes
                .medium,
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {
            Text(
                text =
                    "Calculation",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Amount remaining: " +
                            FinanceFormatter.money(
                                result.amountRemaining
                            ),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Contributions required: " +
                            result
                                .contributionsRequired,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Text(
                text =
                    "Time required: " +
                            "%.1f".format(
                                weeksRequired
                            ) +
                            " weeks",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Text(
                text =
                    "Approx. months: " +
                            "%.1f".format(
                                monthsRequired
                            ),
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Text(
                text =
                    "Estimated goal date: " +
                            estimatedDate,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}
