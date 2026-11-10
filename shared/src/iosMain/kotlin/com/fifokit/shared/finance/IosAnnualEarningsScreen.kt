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

private fun annualMoney(amount: Double): String {
    val cents = (amount * 100.0).roundToLong()
    return "\$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}

@Composable
internal fun IosAnnualEarningsScreen(
    pattern: RosterPattern,
    rosterStart: RosterStartDate,
    initialYear: Int,
    onBack: () -> Unit
) {
    val prefs = remember { NSUserDefaults.standardUserDefaults }
    var year by remember { mutableStateOf(initialYear) }
    var rateType by remember {
        mutableStateOf(PayRateType.entries.firstOrNull {
            it.name == prefs.stringForKey("com.fifokit.finance.rateType")
        } ?: PayRateType.HOURLY)
    }
    var rate by remember { mutableStateOf(prefs.stringForKey("com.fifokit.finance.rate") ?: "") }
    var hours by remember { mutableStateOf(prefs.stringForKey("com.fifokit.finance.hours") ?: "12") }
    var allowance by remember { mutableStateOf(prefs.stringForKey("com.fifokit.finance.allowance") ?: "0") }
    var earnings by remember { mutableStateOf<EarningsResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back to Tools") }
        Text("Annual Earnings", style = MaterialTheme.typography.headlineMedium)
        Text("Active roster: ${pattern.label}   Start: ${rosterStart.toIsoString()}")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { if (year > 1) { year--; earnings = null } }) { Text("Previous year") }
            Text(year.toString(), style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { if (year < 9999) { year++; earnings = null } }) { Text("Next year") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (type in PayRateType.entries) {
                TextButton(onClick = { rateType = type; earnings = null }) {
                    val label = when (type) {
                        PayRateType.HOURLY -> "Hourly"
                        PayRateType.DAILY -> "Daily"
                        PayRateType.ANNUAL_SALARY -> "Salary"
                    }
                    Text(if (rateType == type) "[$label]" else label)
                }
            }
        }
        OutlinedTextField(
            value = rate, onValueChange = { rate = it; earnings = null },
            label = { Text(when (rateType) {
                PayRateType.HOURLY -> "Hourly rate (AUD)"
                PayRateType.DAILY -> "Daily rate (AUD)"
                PayRateType.ANNUAL_SALARY -> "Annual salary (AUD)"
            }) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = hours, onValueChange = { hours = it; earnings = null },
            label = { Text("Hours per work day") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = allowance, onValueChange = { allowance = it; earnings = null },
            label = { Text("Allowance per work day (AUD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val r = rate.toDoubleOrNull()
            val h = hours.toDoubleOrNull()
            val a = allowance.toDoubleOrNull() ?: if (allowance.isBlank()) 0.0 else null
            error = when {
                r == null || !r.isFinite() || r <= 0 -> "Enter a valid rate"
                h == null || !h.isFinite() || h <= 0 -> "Enter valid work hours"
                a == null || !a.isFinite() || a < 0 -> "Enter a non-negative allowance"
                else -> null
            }
            if (error == null) {
                earnings = FinanceCalculator.calculateAnnualEarnings(
                    PayInput(rateType, r!!, h!!, a!!), pattern, rosterStart, year
                )
                prefs.setObject(rateType.name, forKey = "com.fifokit.finance.rateType")
                prefs.setObject(rate, forKey = "com.fifokit.finance.rate")
                prefs.setObject(hours, forKey = "com.fifokit.finance.hours")
                prefs.setObject(allowance, forKey = "com.fifokit.finance.allowance")
            } else earnings = null
        }) { Text("Calculate annual earnings") }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        earnings?.let {
            Text("Work days: ${it.workDaysPerYear}")
            Text("Days off: ${it.restDaysPerYear}")
            Text("Work hours: ${it.workHoursPerYear}")
            Text("Base gross: ${annualMoney(it.baseEarnings)}")
            Text("Allowances: ${annualMoney(it.allowances)}")
            Text("Annual gross: ${annualMoney(it.totalGrossEarnings)}",
                style = MaterialTheme.typography.titleLarge)
        }
        Text("Gross estimate before tax, superannuation, overtime or award adjustments.",
            style = MaterialTheme.typography.bodySmall)
    }
}
