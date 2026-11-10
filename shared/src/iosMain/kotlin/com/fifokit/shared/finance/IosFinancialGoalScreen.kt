package com.fifokit.shared.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.fifokit.shared.roster.RosterStartDate
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSUserDefaults
import kotlin.math.roundToLong

private const val GOAL_TARGET = "com.fifokit.finance.goal.target"
private const val GOAL_CURRENT = "com.fifokit.finance.goal.current"
private const val GOAL_CONTRIBUTION = "com.fifokit.finance.goal.contribution"
private const val GOAL_FREQUENCY = "com.fifokit.finance.goal.frequency"

private fun goalMoney(value: Double): String {
    val cents = (value * 100.0).roundToLong()
    return "\$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}

@Composable
internal fun IosFinancialGoalScreen(onBack: () -> Unit) {
    val prefs = remember { NSUserDefaults.standardUserDefaults }
    var target by remember { mutableStateOf(prefs.stringForKey(GOAL_TARGET) ?: "") }
    var current by remember { mutableStateOf(prefs.stringForKey(GOAL_CURRENT) ?: "0") }
    var contribution by remember { mutableStateOf(prefs.stringForKey(GOAL_CONTRIBUTION) ?: "") }
    var frequency by remember { mutableStateOf(prefs.stringForKey(GOAL_FREQUENCY) ?: "14") }
    var result by remember { mutableStateOf<FinancialGoalResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back to Tools") }
        Text("Financial Goal", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = target, onValueChange = { target = it; result = null },
            label = { Text("Target amount (AUD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = current, onValueChange = { current = it; result = null },
            label = { Text("Current savings (AUD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contribution, onValueChange = { contribution = it; result = null },
            label = { Text("Contribution per pay (AUD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = frequency, onValueChange = { frequency = it; result = null },
            label = { Text("Pay frequency (days)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val t = target.toDoubleOrNull()
            val s = current.toDoubleOrNull()
            val p = contribution.toDoubleOrNull()
            val f = frequency.toIntOrNull()
            error = when {
                t == null || !t.isFinite() || t <= 0 -> "Enter a valid target amount"
                s == null || !s.isFinite() || s < 0 -> "Enter valid current savings"
                p == null || !p.isFinite() || p < 0 -> "Enter a valid contribution"
                s < t && p == 0.0 -> "Contribution must be greater than zero"
                f == null || f <= 0 -> "Enter a valid pay frequency"
                else -> null
            }
            if (error == null) {
                val goal = FinancialGoal(t!!, s!!, p!!, f!!)
                val projection = runCatching { FinancialGoalCalculator.calculate(goal) }
                error = projection.exceptionOrNull()?.message
                result = projection.getOrNull()
                if (result != null) {
                    prefs.setObject(target, forKey = GOAL_TARGET)
                    prefs.setObject(current, forKey = GOAL_CURRENT)
                    prefs.setObject(contribution, forKey = GOAL_CONTRIBUTION)
                    prefs.setObject(frequency, forKey = GOAL_FREQUENCY)
                }
            } else result = null
        }, modifier = Modifier.fillMaxWidth()) { Text("Calculate goal") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let {
            val fmt = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
            val today = RosterStartDate.parseIso(fmt.stringFromDate(NSDate()))
            val projected = today?.let { date ->
                RosterStartDate.fromEpochMillis(date.toEpochMillis() +
                    it.daysRequired.toLong() * 86_400_000L).toIsoString()
            }
            Text("Amount remaining: ${goalMoney(it.amountRemaining)}")
            Text("Contributions required: ${it.contributionsRequired}")
            Text("Time required: ${it.daysRequired} days")
            Text("Approximate weeks: ${it.daysRequired / 7.0}")
            Text("Approximate months: ${it.daysRequired / 30.44}")
            projected?.let { date -> Text("Estimated goal date: $date") }
        }
        Text("Projection assumes equal contributions at each pay interval.",
            style = MaterialTheme.typography.bodySmall)
    }
}
