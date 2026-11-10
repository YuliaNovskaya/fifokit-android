package com.fifokit.shared.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.shared.roster.RosterPattern
import com.fifokit.shared.roster.RosterStartDate
import kotlin.math.roundToLong
import platform.Foundation.NSUserDefaults

private const val RATE_TYPE_KEY = "com.fifokit.finance.rateType"
private const val RATE_KEY = "com.fifokit.finance.rate"
private const val HOURS_KEY = "com.fifokit.finance.hours"
private const val ALLOWANCE_KEY = "com.fifokit.finance.allowance"

private fun money(amount: Double): String {
    val cents = (amount * 100.0).roundToLong()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "\$$whole.$fraction"
}

@Composable
internal fun IosPayCalculatorScreen(
    pattern: RosterPattern,
    rosterStart: RosterStartDate,
    year: Int,
    onBack: () -> Unit
) {
    val prefs = remember { NSUserDefaults.standardUserDefaults }
    var rateType by remember {
        mutableStateOf(PayRateType.entries.firstOrNull {
            it.name == prefs.stringForKey(RATE_TYPE_KEY)
        } ?: PayRateType.HOURLY)
    }
    var rate by remember { mutableStateOf(prefs.stringForKey(RATE_KEY) ?: "") }
    var hours by remember { mutableStateOf(prefs.stringForKey(HOURS_KEY) ?: "12") }
    var allowance by remember { mutableStateOf(prefs.stringForKey(ALLOWANCE_KEY) ?: "0") }
    var result by remember { mutableStateOf<EarningsResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back to Tools") }
        Text("FIFO Pay Calculator", style = MaterialTheme.typography.headlineMedium)
        Text("Roster: ${pattern.label}   Year: $year")
        Text("Gross earnings estimate before tax, based on your roster.")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (type in PayRateType.entries) {
                TextButton(onClick = {
                    rateType = type
                    result = null
                    error = null
                }) {
                    val label = when (type) {
                        PayRateType.HOURLY -> "Hourly"
                        PayRateType.DAILY -> "Daily"
                        PayRateType.ANNUAL_SALARY -> "Salary"
                    }
                    Text(if (type == rateType) "[$label]" else label)
                }
            }
        }
        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it; result = null },
            label = { Text(when (rateType) {
                PayRateType.HOURLY -> "Hourly rate (AUD)"
                PayRateType.DAILY -> "Daily rate (AUD)"
                PayRateType.ANNUAL_SALARY -> "Annual salary (AUD)"
            }) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = hours,
            onValueChange = { hours = it; result = null },
            label = { Text("Hours per work day") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = allowance,
            onValueChange = { allowance = it; result = null },
            label = { Text("Allowance per work day (AUD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val rateValue = rate.toDoubleOrNull()
            val hoursValue = hours.toDoubleOrNull()
            val allowanceValue = allowance.toDoubleOrNull() ?: if (allowance.isBlank()) 0.0 else null
            error = when {
                rateValue == null || !rateValue.isFinite() || rateValue <= 0 -> "Enter a valid pay rate"
                hoursValue == null || !hoursValue.isFinite() || hoursValue <= 0 -> "Enter valid work hours"
                allowanceValue == null || !allowanceValue.isFinite() || allowanceValue < 0 ->
                    "Enter a non-negative allowance"
                else -> null
            }
            if (error == null) {
                val input = PayInput(rateType, rateValue!!, hoursValue!!, allowanceValue!!)
                result = FinanceCalculator.calculateAnnualEarnings(input, pattern, rosterStart, year)
                prefs.setObject(rateType.name, forKey = RATE_TYPE_KEY)
                prefs.setObject(rate, forKey = RATE_KEY)
                prefs.setObject(hours, forKey = HOURS_KEY)
                prefs.setObject(allowance, forKey = ALLOWANCE_KEY)
            } else result = null
        }) { Text("Calculate") }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let { earnings ->
            if (rateType == PayRateType.ANNUAL_SALARY) {
                Text("Annual salary: ${money(earnings.baseEarnings)}")
            } else {
                val input = PayInput(rateType, rate.toDouble(), hours.toDouble(), allowance.toDouble())
                Text("Gross per work day: ${money(FinanceCalculator.grossPerWorkDay(input))}")
            }
            Text("Work days this year: ${earnings.workDaysPerYear}")
            Text("Days off this year: ${earnings.restDaysPerYear}")
            Text("Work hours this year: ${earnings.workHoursPerYear}")
            Text("Base gross: ${money(earnings.baseEarnings)}")
            Text("Allowances: ${money(earnings.allowances)}")
            Text("Annual gross: ${money(earnings.totalGrossEarnings)}",
                style = MaterialTheme.typography.titleLarge)
        }
        Text("Estimate only. Tax, superannuation, overtime and award conditions are not included.",
            style = MaterialTheme.typography.bodySmall)
    }
}
