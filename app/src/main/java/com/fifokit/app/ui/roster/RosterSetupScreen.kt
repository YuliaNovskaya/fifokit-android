@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fifokit.app.ui.roster

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.ui.components.FifokitTopBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun RosterSetupScreen(
    modifier: Modifier = Modifier,
    rosterName: String = "My Roster",
    onRosterNameChanged: (String) -> Unit = {},
    selectedRoster: RosterPattern = RosterPattern.TWO_ONE,
    isCustomRoster: Boolean = false,
    customWorkDays: Int = 14,
    customOffDays: Int = 7,
    startDate: LocalDate = LocalDate.now(),
    isShutdownRoster: Boolean = false,
    endDate: LocalDate? = null,
    onRosterSelected: (RosterPattern) -> Unit = {},
    onCustomRosterSelected: () -> Unit = {},
    onShutdownRosterSelected: () -> Unit = {},
    onCustomWorkDaysChanged: (Int) -> Unit = {},
    onCustomOffDaysChanged: (Int) -> Unit = {},
    onStartDateSelected: (LocalDate) -> Unit = {},
    onEndDateSelected: (LocalDate) -> Unit = {},
    showCancelNewRoster: Boolean = false,
    onCancelNewRoster: () -> Unit = {},
    showCancelExistingRoster: Boolean = false,
    onCancelExistingRoster: () -> Unit = {},
    showResetRoster: Boolean = false,
    onResetRoster: () -> Unit = {},
    onBack: () -> Unit = {},
    onGenerateRoster: () -> Unit = {}
) {
    var showStartDatePicker by remember {
        mutableStateOf(false)
    }

    var showEndDatePicker by remember {
        mutableStateOf(false)
    }

    var showDeleteRosterConfirmation by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            FifokitTopBar(
                title =
                    if (showCancelNewRoster) {
                        "Create roster"
                    } else {
                        "Edit roster"
                    },
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(24.dp)
        ) {
            Text("Roster name")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = rosterName,
                onValueChange = onRosterNameChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        if (isShutdownRoster) {
                            "e.g. KCGM Shutdown"
                        } else {
                            "e.g. My Roster"
                        }
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text("Choose your roster")

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                RosterPattern.entries.forEach { pattern ->

                    if (
                        !isCustomRoster &&
                        !isShutdownRoster &&
                        pattern == selectedRoster
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onRosterSelected(pattern)
                            }
                        ) {
                            Text(pattern.label)
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onRosterSelected(pattern)
                            }
                        ) {
                            Text(pattern.label)
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (
                isCustomRoster &&
                !isShutdownRoster
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCustomRosterSelected
                ) {
                    Text("Custom · PRO")
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCustomRosterSelected
                ) {
                    Text("Custom · PRO")
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (isShutdownRoster) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShutdownRosterSelected
                ) {
                    Text("Shutdown · PRO")
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShutdownRosterSelected
                ) {
                    Text("Shutdown · PRO")
                }
            }

            if (isCustomRoster) {
                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    if (isShutdownRoster) {
                        "Shutdown work pattern"
                    } else {
                        "Custom work pattern"
                    }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text("Work days")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCustomWorkDaysChanged(
                                customWorkDays - 1
                            )
                        }
                    ) {
                        Text("-")
                    }

                    Button(
                        modifier = Modifier.weight(2f),
                        onClick = {}
                    ) {
                        Text(
                            customWorkDays.toString()
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCustomWorkDaysChanged(
                                customWorkDays + 1
                            )
                        }
                    ) {
                        Text("+")
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text("Off days")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCustomOffDaysChanged(
                                customOffDays - 1
                            )
                        }
                    ) {
                        Text("-")
                    }

                    Button(
                        modifier = Modifier.weight(2f),
                        onClick = {}
                    ) {
                        Text(
                            customOffDays.toString()
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCustomOffDaysChanged(
                                customOffDays + 1
                            )
                        }
                    ) {
                        Text("+")
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Text(
                if (isShutdownRoster) {
                    "Shutdown start date"
                } else {
                    "First work day"
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showStartDatePicker = true
                }
            ) {
                Text(
                    startDate.format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM yyyy"
                        )
                    )
                )
            }

            if (isShutdownRoster) {
                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text("Shutdown end date")

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showEndDatePicker = true
                    }
                ) {
                    Text(
                        endDate?.format(
                            DateTimeFormatter.ofPattern(
                                "dd MMM yyyy"
                            )
                        ) ?: "Choose end date"
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    "The roster is active only between these dates. The work/off cycle does not continue after the shutdown ends."
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            if (
                showCancelNewRoster ||
                showCancelExistingRoster
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (showCancelNewRoster) {
                            onCancelNewRoster()
                        } else {
                            onCancelExistingRoster()
                        }
                    }
                ) {
                    Text("Cancel")
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled =
                    !isShutdownRoster ||
                            (
                                endDate != null &&
                                !endDate.isBefore(startDate)
                            ),
                onClick = onGenerateRoster
            ) {
                Text(
                    if (isShutdownRoster) {
                        "Save shutdown roster"
                    } else {
                        "Generate roster"
                    }
                )
            }

            if (showResetRoster) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showDeleteRosterConfirmation =
                            true
                    }
                ) {
                    Text("Delete roster")
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }

    if (showDeleteRosterConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteRosterConfirmation =
                    false
            },
            title = {
                Text("Delete $rosterName?")
            },
            text = {
                Text(
                    "This roster will be deleted. If it is shared, shared access will also stop."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteRosterConfirmation =
                            false
                        onResetRoster()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteRosterConfirmation =
                            false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showStartDatePicker) {
        val state =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    startDate
                        .atStartOfDay(
                            ZoneOffset.UTC
                        )
                        .toInstant()
                        .toEpochMilli()
            )

        DatePickerDialog(
            onDismissRequest = {
                showStartDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis
                            ?.let { millis ->
                                onStartDateSelected(
                                    Instant
                                        .ofEpochMilli(
                                            millis
                                        )
                                        .atZone(
                                            ZoneOffset.UTC
                                        )
                                        .toLocalDate()
                                )
                            }

                        showStartDatePicker =
                            false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePicker =
                            false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = state
            )
        }
    }

    if (
        showEndDatePicker &&
        isShutdownRoster
    ) {
        val initialEnd =
            endDate ?: startDate

        val state =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    initialEnd
                        .atStartOfDay(
                            ZoneOffset.UTC
                        )
                        .toInstant()
                        .toEpochMilli()
            )

        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis
                            ?.let { millis ->
                                val selected =
                                    Instant
                                        .ofEpochMilli(
                                            millis
                                        )
                                        .atZone(
                                            ZoneOffset.UTC
                                        )
                                        .toLocalDate()

                                if (
                                    !selected
                                        .isBefore(
                                            startDate
                                        )
                                ) {
                                    onEndDateSelected(
                                        selected
                                    )
                                }
                            }

                        showEndDatePicker =
                            false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker =
                            false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = state
            )
        }
    }
}
