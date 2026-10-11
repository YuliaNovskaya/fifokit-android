package com.fifokit.app.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fifokit.app.data.AdvancedPayProfileStore
import com.fifokit.app.domain.finance.*
import com.fifokit.app.domain.model.RosterPattern
import com.fifokit.app.domain.roster.AustralianState
import com.fifokit.app.domain.roster.PublicHolidayProvider
import com.fifokit.app.domain.roster.RosterScheduleSegment
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import com.fifokit.app.ui.components.FifokitTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedPayPreviewScreen(
    selectedPattern: RosterPattern,
    isCustomRoster: Boolean,
    isShutdownRoster: Boolean,
    scheduleSegments: List<RosterScheduleSegment>,
    rosterStartDate: LocalDate,
    selectedStates: Set<AustralianState>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val store = remember(context) { AdvancedPayProfileStore(context) }
    var profile by remember { mutableStateOf(store.load()) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var dayStart by remember { mutableStateOf("07:00") }
    var nightStart by remember { mutableStateOf("19:00") }
    var shiftHours by remember { mutableStateOf("12") }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<PayBreakdown?>(null) }

    Scaffold(topBar = { FifokitTopBar(title = "Advanced pay estimate", onBack = onBack) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Uses the active roster and saved Advanced pay settings.")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { month = month.minusMonths(1); result = null }) { Text("Previous") }
                Text(month.toString())
                TextButton(onClick = { month = month.plusMonths(1); result = null }) { Text("Next") }
            }
            OutlinedTextField(value = dayStart, onValueChange = { dayStart = it; result = null },
                label = { Text("Day shift start (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nightStart, onValueChange = { nightStart = it; result = null },
                label = { Text("Night shift start (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = shiftHours, onValueChange = { shiftHours = it; result = null },
                label = { Text("Hours per shift") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth())
            Text("These shift times are inputs for this estimate, not saved roster times. WORK segments use the selected shift type; ROTATING/CUSTOM without DAY/NIGHT segments currently default to DAY.")
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                result = null
                val hours = shiftHours.toDoubleOrNull()
                val day = runCatching { LocalTime.parse(dayStart) }.getOrNull()
                val night = runCatching { LocalTime.parse(nightStart) }.getOrNull()
                error = when {
                    profile.baseRate <= 0 -> "Save a positive base rate in Advanced pay settings"
                    day == null || night == null -> "Enter valid shift start times (HH:mm)"
                    hours == null || hours <= 0 || hours > 24 -> "Enter hours per shift between 0 and 24"
                    month.year !in 2026..2027 && selectedStates.isNotEmpty() ->
                        "Public holiday data is only available for 2026 and 2027. Clear selected states to calculate other years."
                    else -> null
                }
                if (error == null) {
                    runCatching {
                        profile = store.load()
                        val shifts = RosterPayShiftGenerator.generate(
                            from = month.atDay(1), until = month.atEndOfMonth(),
                            rosterStart = rosterStartDate, pattern = selectedPattern,
                            isCustomRoster = isCustomRoster, isShutdownRoster = isShutdownRoster,
                            segments = scheduleSegments, dayStart = day!!, nightStart = night!!,
                            hoursPerShift = hours!!, defaultShiftType = profile.shiftType
                        )
                        val holidayStart = month.atDay(1).minusDays(1)
                        val holidays = (0L..(month.lengthOfMonth() + 1).toLong())
                            .map { holidayStart.plusDays(it) }
                            .filter { PublicHolidayProvider.isPublicHoliday(it, selectedStates) }.toSet()
                        PayCalculationEngine.calculate(profile, shifts, holidays,
                            annualSalaryPeriodFraction = month.lengthOfMonth().toDouble() / month.atDay(1).lengthOfYear())
                    }.onSuccess { result = it }.onFailure { error = it.message ?: "Unable to calculate" }
                }
            }) { Text("Calculate advanced estimate") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            result?.let { r ->
                Text("Shifts: ${r.shifts.size}")
                Text("Hours: ${"%.2f".format(r.totalHours)}")
                Text("Gross estimate: ${"%.2f".format(r.totalGross)}",
                    style = MaterialTheme.typography.titleMedium)
                Text("Effective hourly: ${"%.2f".format(r.effectiveHourlyRate)}")
                Text("Breakdown by shift", style = MaterialTheme.typography.titleMedium)
                r.shifts.forEach { shift ->
                    Text("${shift.shift.start} — ${shift.shift.shiftType}: ${"%.2f".format(shift.totalGross)}")
                    shift.lines.forEach { line ->
                        Text("  ${line.start.toLocalTime()}–${line.end.toLocalTime()}  ${"%.2f".format(line.hours)} h × ${"%.2f".format(line.hourlyRate)} = ${"%.2f".format(line.gross)}")
                    }
                }
                if (r.fixedAllowances != 0.0) Text("Period amount and fixed allowances: ${"%.2f".format(r.fixedAllowances)}")
            }
        }
    }
}
