package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.app.domain.finance.PipType

@Composable
fun PipInputSection(
    pipType: PipType,
    pipValue: String,
    onPipTypeChange: (PipType) -> Unit,
    onPipValueChange: (String) -> Unit
) {
    Text(
        text = "Project Incentive Payment (PIP)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                PipType.NONE to "None",
                PipType.PER_HOUR to "$ / hour",
                PipType.PER_DAY to "$ / day"
            ).forEach { (type, label) ->
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPipTypeChange(type)
                    }
                ) {
                    Text(
                        text =
                            if (pipType == type) {
                                "✓ $label"
                            } else {
                                label
                            }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                PipType.PERCENT_BASE to "% base pay",
                PipType.FIXED_AMOUNT to "Fixed amount"
            ).forEach { (type, label) ->
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPipTypeChange(type)
                    }
                ) {
                    Text(
                        text =
                            if (pipType == type) {
                                "✓ $label"
                            } else {
                                label
                            }
                    )
                }
            }
        }
    }

    if (pipType != PipType.NONE) {
        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = pipValue,
            onValueChange = onPipValueChange,
            label = {
                Text(
                    text =
                        when (pipType) {
                            PipType.PER_HOUR ->
                                "PIP per hour"

                            PipType.PER_DAY ->
                                "PIP per work day"

                            PipType.PERCENT_BASE ->
                                "PIP percentage of base pay"

                            PipType.FIXED_AMOUNT ->
                                "Fixed PIP amount"

                            PipType.NONE ->
                                "PIP"
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
    }
}
