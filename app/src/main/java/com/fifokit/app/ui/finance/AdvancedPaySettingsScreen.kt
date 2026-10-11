package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.fifokit.app.data.AdvancedPayProfileStore
import com.fifokit.app.domain.finance.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedPaySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { AdvancedPayProfileStore(context) }
    var profile by remember { mutableStateOf(store.load()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    var baseText by remember { mutableStateOf(profile.baseRate.toString()) }
    var loadingText by remember { mutableStateOf(profile.casualLoadingPercent.toString()) }
    val rates = listOf(DayCategory.WEEKDAY, DayCategory.SATURDAY, DayCategory.SUNDAY, DayCategory.PUBLIC_HOLIDAY)

    Scaffold(topBar = { TopAppBar(title = { Text("Advanced pay settings") }, navigationIcon = {
        TextButton(onClick = onBack) { Text("Back") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Employment", style = MaterialTheme.typography.titleMedium)
            ChoiceField("Work arrangement", WorkArrangement.entries, profile.arrangement) { profile = profile.copy(arrangement = it) }
            ChoiceField("Employment type", EmploymentType.entries, profile.employmentType) { profile = profile.copy(employmentType = it) }
            ChoiceField("Pay basis", AdvancedPayBasis.entries, profile.basis) { profile = profile.copy(basis = it) }
            NumericField("Base rate (hour / day / year according to pay basis)", baseText) { baseText = it }
            Text("Shift configuration", style = MaterialTheme.typography.titleMedium)
            ChoiceField("Shift type", PayShiftType.entries, profile.shiftType) { profile = profile.copy(shiftType = it) }
            Text("Shift hours and start times come from the roster in the next integration step.", style = MaterialTheme.typography.bodySmall)
            Text("Rates by day", style = MaterialTheme.typography.titleMedium)
            rates.forEach { category ->
                var expanded by remember(category) { mutableStateOf(false) }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { expanded = !expanded }) { Text(category.name.replace("_", " ") + if (expanded) "  −" else "  +") }
                        if (expanded) {
                            listOf(PayShiftType.DAY, PayShiftType.NIGHT).forEach { shift ->
                                val id = "rate_${category.name}_${shift.name}"
                                val existing = profile.rateRules.find { it.id == id }
                                var entry by remember(id, existing?.value) { mutableStateOf(existing?.value?.toString() ?: "") }
                                Text("${shift.name.lowercase().replaceFirstChar { it.uppercase() }} hourly rate (blank = base)")
                                NumericField("Hourly rate", entry) { value ->
                                    entry = value
                                    val parsed = value.toDoubleOrNull()
                                    profile = profile.copy(rateRules = profile.rateRules.filterNot { it.id == id } +
                                        (if (parsed != null && parsed >= 0) listOf(RateRule(id, category = category, shiftType = shift, value = parsed, priority = 10)) else emptyList()))
                                }
                            }
                        }
                    }
                }
            }
            Text("Overtime tiers", style = MaterialTheme.typography.titleMedium)
            profile.overtimeRules.forEach { rule ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Overtime tier")
                        ChoiceField("Period", OvertimePeriod.entries, rule.period) { v ->
                            profile = profile.copy(overtimeRules = profile.overtimeRules.map { if (it.id == rule.id) it.copy(period = v) else it })
                        }
                        NumericField("After hours", rule.afterHours.toString()) { v ->
                            v.toDoubleOrNull()?.let { n -> profile = profile.copy(overtimeRules = profile.overtimeRules.map { if (it.id == rule.id) it.copy(afterHours = n) else it }) }
                        }
                        NumericField("Until hours (empty = remaining)", rule.untilHours?.toString() ?: "") { v ->
                            if (v.isBlank() || v.toDoubleOrNull() != null) profile = profile.copy(overtimeRules = profile.overtimeRules.map { if (it.id == rule.id) it.copy(untilHours = v.toDoubleOrNull()) else it })
                        }
                        NumericField("Multiplier (e.g. 1.5)", rule.value.toString()) { v ->
                            v.toDoubleOrNull()?.let { n -> profile = profile.copy(overtimeRules = profile.overtimeRules.map { if (it.id == rule.id) it.copy(value = n) else it }) }
                        }
                        TextButton(onClick = { profile = profile.copy(overtimeRules = profile.overtimeRules.filterNot { it.id == rule.id }) }) { Text("Remove tier") }
                    }
                }
            }
            OutlinedButton(onClick = { profile = profile.copy(overtimeRules = profile.overtimeRules + OvertimeRule(UUID.randomUUID().toString(), afterHours = 8.0, value = 1.5)) }) { Text("Add overtime tier") }
            Text("Allowances", style = MaterialTheme.typography.titleMedium)
            profile.allowances.forEach { allowance ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        OutlinedTextField(value = allowance.name, onValueChange = { v ->
                            profile = profile.copy(allowances = profile.allowances.map { if (it.id == allowance.id) it.copy(name = v) else it })
                        }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                        NumericField("Amount", allowance.amount.toString()) { v ->
                            v.toDoubleOrNull()?.let { n -> profile = profile.copy(allowances = profile.allowances.map { if (it.id == allowance.id) it.copy(amount = n) else it }) }
                        }
                        ChoiceField("Frequency", AllowanceFrequency.entries, allowance.frequency) { v ->
                            profile = profile.copy(allowances = profile.allowances.map { if (it.id == allowance.id) it.copy(frequency = v) else it })
                        }
                        TextButton(onClick = { profile = profile.copy(allowances = profile.allowances.filterNot { it.id == allowance.id }) }) { Text("Remove allowance") }
                    }
                }
            }
            OutlinedButton(onClick = { profile = profile.copy(allowances = profile.allowances + Allowance(UUID.randomUUID().toString(), "Allowance", 0.0, AllowanceFrequency.PER_SHIFT)) }) { Text("Add allowance") }
            NumericField("Casual loading %", loadingText) { loadingText = it }
            Text("Rules are contract-specific. Advanced settings are saved locally; calculations are not yet connected to Basic mode.", style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (saved) Text("Advanced pay settings saved")
            Button(onClick = {
                val base = baseText.toDoubleOrNull()
                val loading = loadingText.toDoubleOrNull()
                error = when {
                    base == null || base < 0 -> "Enter a valid base rate"
                    loading == null || loading < 0 -> "Enter valid casual loading"
                    profile.overtimeRules.any { it.afterHours < 0 || it.value < 0 || (it.untilHours != null && it.untilHours <= it.afterHours) } -> "Check overtime thresholds"
                    profile.allowances.any { it.amount < 0 } -> "Check allowance amounts"
                    else -> null
                }
                if (error == null) {
                    runCatching {
                        store.save(profile.copy(baseRate = base!!, casualLoadingPercent = loading!!))
                    }.onSuccess { saved = true }.onFailure { error = it.message ?: "Save failed" }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save advanced settings") }
        }
    }
}

@Composable
private fun NumericField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> ChoiceField(label: String, values: List<T>, selected: T, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Text(label, style = MaterialTheme.typography.labelLarge)
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected.name.replace("_", " "))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(text = { Text(value.name.replace("_", " ")) }, onClick = {
                    onSelect(value); expanded = false
                })
            }
        }
    }
}
