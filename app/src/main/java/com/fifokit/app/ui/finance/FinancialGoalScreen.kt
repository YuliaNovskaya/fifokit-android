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

private enum class FinancialGoalMode {
    LIST,
    EDIT
}

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
            FirebaseAnalytics.getInstance(context)
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

    var mode by remember {
        mutableStateOf(
            FinancialGoalMode.LIST
        )
    }

    var editingGoalId by remember {
        mutableStateOf<String?>(null)
    }

    var draftGoal by remember {
        mutableStateOf<FinancialGoal?>(null)
    }

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var pendingDeleteGoal by remember {
        mutableStateOf<FinancialGoal?>(null)
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

    val storedEditingGoal =
        editingGoalId?.let { id ->
            goals.firstOrNull {
                it.id == id
            }
        }

    val editingGoal =
        draftGoal ?: storedEditingGoal

    val hasFreeGoal =
        goals.any {
            it.id == "primary"
        }

    fun calculationFor(
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

    fun loadEditFields(
        goal: FinancialGoal
    ) {
        goalName =
            goal.name

        targetAmount =
            if (
                goal.targetAmount ==
                0.0
            ) {
                ""
            } else {
                goal.targetAmount
                    .toString()
            }

        currentAmount =
            if (
                goal.currentAmount ==
                0.0
            ) {
                ""
            } else {
                goal.currentAmount
                    .toString()
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

        validationError =
            null
    }

    fun returnToList() {
        keyboardController?.hide()
        focusManager.clearFocus()

        mode =
            FinancialGoalMode.LIST

        editingGoalId =
            null

        draftGoal =
            null

        validationError =
            null
    }

    LaunchedEffect(Unit) {
        financePreferences
            .cleanupLegacyEmptySecondaryGoals()
    }

    LaunchedEffect(
        goals,
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
            draftGoal = null
            editingGoalId = null
            mode =
                FinancialGoalMode.LIST
        }
    }

    BackHandler(
        enabled =
            mode ==
                    FinancialGoalMode.EDIT
    ) {
        returnToList()
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog =
                    false

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
                            "Goal name"
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
                        val draft =
                            FinancialGoal(
                                id =
                                    if (hasFreeGoal) {
                                        UUID
                                            .randomUUID()
                                            .toString()
                                    } else {
                                        "primary"
                                    },
                                name =
                                    newGoalName
                                        .trim()
                            )

                        showCreateDialog =
                            false

                        newGoalName = ""

                        draftGoal =
                            draft

                        editingGoalId =
                            draft.id

                        loadEditFields(
                            draft
                        )

                        mode =
                            FinancialGoalMode.EDIT
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog =
                            false

                        newGoalName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingDeleteGoal?.let { deleteGoal ->
        AlertDialog(
            onDismissRequest = {
                pendingDeleteGoal =
                    null
            },
            title = {
                Text(
                    "Delete financial goal?"
                )
            },
            text = {
                Text(
                    if (
                        deleteGoal.id ==
                        "primary"
                    ) {
                        "Delete \"" +
                                deleteGoal.name +
                                "\"? This cannot be undone."
                    } else {
                        "Delete \"" +
                                deleteGoal.name +
                                "\"? This cannot be undone."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id =
                            deleteGoal.id

                        val isStored =
                            goals.any {
                                it.id == id
                            }

                        pendingDeleteGoal =
                            null

                        if (isStored) {
                            scope.launch {
                                financePreferences
                                    .deleteFinancialGoal(
                                        id
                                    )
                            }
                        }

                        if (
                            editingGoalId ==
                            id
                        ) {
                            returnToList()
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingDeleteGoal =
                            null
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
                title =
                    if (
                        mode ==
                        FinancialGoalMode.LIST
                    ) {
                        "Financial Goals"
                    } else if (
                        draftGoal != null
                    ) {
                        "New Financial Goal"
                    } else {
                        "Edit Financial Goal"
                    },
                onBack = {
                    if (
                        mode ==
                        FinancialGoalMode.EDIT
                    ) {
                        returnToList()
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
            if (
                mode ==
                FinancialGoalMode.LIST
            ) {
                Text(
                    text =
                        "Financial Goals",
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Your first goal is free. Additional saved goals require FIFOKIT Pro.",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick = {
                        if (!hasFreeGoal) {
                            showCreateDialog =
                                true
                        } else if (
                            ProAccess.canUse(
                                ProFeature
                                    .ADVANCED_FINANCIAL_GOALS
                            )
                        ) {
                            showCreateDialog =
                                true
                        } else {
                            onProRequested(
                                "advanced_financial_goals"
                            )
                        }
                    }
                ) {
                    Text(
                        if (hasFreeGoal) {
                            "+ New goal · PRO"
                        } else {
                            "+ New goal"
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                goals.forEach { goal ->
                    FinancialGoalCard(
                        goal = goal,
                        calculation =
                            calculationFor(
                                goal
                            ),
                        isProGoal =
                            goal.id !=
                                    "primary",
                        onEdit = {
                            if (
                                goal.id ==
                                "primary" ||
                                ProAccess.canUse(
                                    ProFeature
                                        .ADVANCED_FINANCIAL_GOALS
                                )
                            ) {
                                draftGoal =
                                    null

                                editingGoalId =
                                    goal.id

                                loadEditFields(
                                    goal
                                )

                                mode =
                                    FinancialGoalMode.EDIT
                            } else {
                                onProRequested(
                                    "advanced_financial_goals"
                                )
                            }
                        },
                        onDelete = {
                            pendingDeleteGoal =
                                goal
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                14.dp
                            )
                    )
                }

            } else {
                val goal =
                    editingGoal

                if (goal == null) {
                    Text(
                        "Goal not found"
                    )
                } else {
                    FinancialGoalEditFields(
                        goalName =
                            goalName,
                        onGoalNameChange = {
                            goalName = it
                            validationError =
                                null
                        },
                        targetAmount =
                            targetAmount,
                        onTargetAmountChange = {
                            targetAmount = it
                            validationError =
                                null
                        },
                        currentAmount =
                            currentAmount,
                        onCurrentAmountChange = {
                            currentAmount = it
                            validationError =
                                null
                        },
                        contributionPerPay =
                            contributionPerPay,
                        onContributionPerPayChange = {
                            contributionPerPay =
                                it
                            validationError =
                                null
                        },
                        payFrequencyDays =
                            payFrequencyDays,
                        onPayFrequencyDaysChange = {
                            payFrequencyDays =
                                it
                            validationError =
                                null
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            keyboardController
                                ?.hide()

                            focusManager
                                .clearFocus()

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
                                val savedGoal =
                                    goal.copy(
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

                                scope.launch {
                                    financePreferences
                                        .saveFinancialGoal(
                                            savedGoal
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

                                returnToList()
                            }
                        }
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
                            returnToList()
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

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    OutlinedButton(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            pendingDeleteGoal =
                                goal
                        }
                    ) {
                        Text(
                            "Delete goal"
                        )
                    }

                    validationError
                        ?.let { error ->

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        12.dp
                                    )
                            )

                            Text(error)
                        }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun FinancialGoalCard(
    goal: FinancialGoal,
    calculation: FinancialGoalResult?,
    isProGoal: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Modifier.padding(16.dp)
        ) {
            Text(
                text =
                    if (isProGoal) {
                        goal.name +
                                " · PRO"
                    } else {
                        goal.name
                    },
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            if (
                goal.targetAmount >
                0.0
            ) {
                Text(
                    text =
                        "Target: " +
                                FinanceFormatter
                                    .money(
                                        goal.targetAmount
                                    ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )

                Text(
                    text =
                        "Current savings: " +
                                FinanceFormatter
                                    .money(
                                        goal.currentAmount
                                    ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )

                Text(
                    text =
                        "Contribution per pay: " +
                                FinanceFormatter
                                    .money(
                                        goal.contributionPerPay
                                    ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )

                Text(
                    text =
                        "Pay every " +
                                goal
                                    .payFrequencyDays +
                                " days",
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )

                calculation
                    ?.let { result ->

                        Spacer(
                            modifier =
                                Modifier.height(
                                    14.dp
                                )
                        )

                        FinancialGoalCalculation(
                            result = result
                        )
                    }
            } else {
                Text(
                    text =
                        "Not configured yet",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onEdit
            ) {
                Text(
                    "Edit goal"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onDelete
            ) {
                Text(
                    "Delete goal"
                )
            }
        }
    }
}

@Composable
private fun FinancialGoalEditFields(
    goalName: String,
    onGoalNameChange: (String) -> Unit,
    targetAmount: String,
    onTargetAmountChange: (String) -> Unit,
    currentAmount: String,
    onCurrentAmountChange: (String) -> Unit,
    contributionPerPay: String,
    onContributionPerPayChange: (String) -> Unit,
    payFrequencyDays: String,
    onPayFrequencyDaysChange: (String) -> Unit
) {
    OutlinedTextField(
        value = goalName,
        onValueChange =
            onGoalNameChange,
        label = {
            Text(
                text =
                    "Goal name",
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
        onValueChange =
            onTargetAmountChange,
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
                    KeyboardType.Decimal
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
        onValueChange =
            onCurrentAmountChange,
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
                    KeyboardType.Decimal
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
        onValueChange =
            onContributionPerPayChange,
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
                    KeyboardType.Decimal
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
        onValueChange =
            onPayFrequencyDaysChange,
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
                    KeyboardType.Number
            ),
        textStyle =
            MaterialTheme
                .typography
                .titleMedium,
        modifier =
            Modifier.fillMaxWidth()
    )
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
                .small,
        color =
            MaterialTheme
                .colorScheme
                .surface
    ) {
        Column(
            modifier =
                Modifier.padding(12.dp)
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
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    "Amount remaining: " +
                            FinanceFormatter
                                .money(
                                    result
                                        .amountRemaining
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
                            "%.1f"
                                .format(
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
                            "%.1f"
                                .format(
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
