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
import java.time.format.DateTimeFormatter
import com.fifokit.app.ui.components.FifokitTopBar
import com.fifokit.app.domain.finance.FinanceFormatter
import androidx.compose.ui.text.font.FontWeight
import java.time.temporal.ChronoUnit

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
    var annualResult by remember { mutableStateOf<PayBreakdown?>(null) }
    var annualPublicHolidaysWorked by remember { mutableIntStateOf(0) }
    var monthlyPublicHolidaysWorked by remember { mutableIntStateOf(0) }
    var rosterBasedSalary by remember { mutableStateOf(false) }

    Scaffold(topBar = { FifokitTopBar(title = "Advanced pay estimate", onBack = onBack) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Uses the active roster and saved Advanced pay settings.")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { month = month.minusMonths(1); result = null; annualResult = null }) { Text("Previous") }
                Text(month.toString())
                TextButton(onClick = { month = month.plusMonths(1); result = null; annualResult = null }) { Text("Next") }
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
                annualResult = null
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
                        val yearStart = LocalDate.of(month.year, 1, 1)
                        val yearEnd = LocalDate.of(month.year, 12, 31)
                        val annualShifts = RosterPayShiftGenerator.generate(
                            from = yearStart, until = yearEnd,
                            rosterStart = rosterStartDate, pattern = selectedPattern,
                            isCustomRoster = isCustomRoster, isShutdownRoster = isShutdownRoster,
                            segments = scheduleSegments, dayStart = day!!, nightStart = night!!,
                            hoursPerShift = hours!!, defaultShiftType = profile.shiftType
                        )
                        val annualHolidays = (0L until yearStart.lengthOfYear().toLong())
                            .map { yearStart.plusDays(it) }
                            .filter { PublicHolidayProvider.isPublicHoliday(it, selectedStates) }.toSet()
                        annualPublicHolidaysWorked = annualShifts.count { it.start.toLocalDate() in annualHolidays }
                        monthlyPublicHolidaysWorked = shifts.count { it.start.toLocalDate() in holidays }
                        annualResult = PayCalculationEngine.calculate(
                            profile, annualShifts, annualHolidays,
                            annualSalaryPeriodFraction = 1.0
                        )
                        val salaryFraction = if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY && rosterBasedSalary) {
                            if (annualShifts.isEmpty()) 0.0 else shifts.size.toDouble() / annualShifts.size
                        } else {
                            month.lengthOfMonth().toDouble() / month.atDay(1).lengthOfYear()
                        }
                        PayCalculationEngine.calculate(profile, shifts, holidays,
                            annualSalaryPeriodFraction = salaryFraction)
                    }.onSuccess { result = it }.onFailure { error = it.message ?: "Unable to calculate" }
                }
            }) { Text("Calculate advanced estimate") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            annualResult?.let { annual ->
                val rosterLabel = when {
                    isShutdownRoster -> "Shutdown sequence"
                    isCustomRoster -> "Custom sequence"
                    else -> selectedPattern.label
                }
                Text("Annual results — ${month.year}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Annual gross: ${FinanceFormatter.money(annual.totalGross)}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Active roster: $rosterLabel")
                Text("Work days this year: ${annual.shifts.size}")
                Text("Work hours this year: ${"%.1f".format(annual.totalHours)}")
                Text("Base: ${FinanceFormatter.money(advancedBaseGross(annual, profile, 1.0))}")
                Text("PIP: ${FinanceFormatter.money(advancedPipGross(annual, profile, 1.0))}")
                Text("Allowances: ${FinanceFormatter.money(advancedAllowanceGross(annual, profile, 1.0))}")
                Text("Equivalent hourly rate: ${FinanceFormatter.money(annual.effectiveHourlyRate)} / hour")
                Text("Public holidays worked this year: $annualPublicHolidaysWorked")
                HorizontalDivider()
            }
            result?.let { r ->
                Text("Monthly results — ${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY) {
                    Text("Monthly salary calculation", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!rosterBasedSalary) {
                            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Calendar-based") }
                            TextButton(onClick = { rosterBasedSalary = true; result = null; annualResult = null }, modifier = Modifier.weight(1f)) { Text("Roster-based") }
                        } else {
                            TextButton(onClick = { rosterBasedSalary = false; result = null; annualResult = null }, modifier = Modifier.weight(1f)) { Text("Calendar-based") }
                            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Roster-based") }
                        }
                    }
                    Text(if (rosterBasedSalary) "Allocated salary based on rostered work days. This may differ from actual monthly pay." else "Salary prorated by calendar days in the selected month.", style = MaterialTheme.typography.bodySmall)
                }
                Text("Monthly gross: ${FinanceFormatter.money(r.totalGross)}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Work days this month: ${r.shifts.size}")
                Text("Work hours this month: ${"%.1f".format(r.totalHours)}")
                val salaryFraction = if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY && rosterBasedSalary) {
                    val annualDays = annualResult?.shifts?.size ?: 0
                    if (annualDays == 0) 0.0 else r.shifts.size.toDouble() / annualDays
                } else month.lengthOfMonth().toDouble() / month.atDay(1).lengthOfYear()
                Text("Base: ${FinanceFormatter.money(advancedBaseGross(r, profile, salaryFraction))}")
                Text("PIP: ${FinanceFormatter.money(advancedPipGross(r, profile, salaryFraction))}")
                Text("Allowances: ${FinanceFormatter.money(advancedAllowanceGross(r, profile, salaryFraction))}")
                Text("Effective hourly rate: ${FinanceFormatter.money(r.effectiveHourlyRate)} / hour")
                Text("Public holidays worked this month: $monthlyPublicHolidaysWorked")
                HorizontalDivider()
                Text("Breakdown by shift — ${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                val timestampFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                r.shifts.forEach { shift ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${shift.shift.start.format(timestampFormat)} — ${shift.shift.shiftType}",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Shift gross: ${FinanceFormatter.money(shift.totalGross)}",
                                fontWeight = FontWeight.SemiBold)
                            profile.allowances.filter {
                                it.frequency == AllowanceFrequency.PER_HOUR ||
                                it.frequency == AllowanceFrequency.PER_SHIFT
                            }.forEach { allowance ->
                                val amount = if (allowance.frequency == AllowanceFrequency.PER_HOUR)
                                    allowance.amount * java.time.temporal.ChronoUnit.MINUTES.between(
                                        shift.shift.start, shift.shift.end
                                    ) / 60.0 else allowance.amount
                                Text("${allowance.name} (${allowance.frequency.name}): ${FinanceFormatter.money(amount)}")
                            }
                            shift.lines.forEach { line ->
                                Text("${line.start.format(timestampFormat)} – ${line.end.format(timestampFormat)}")
                                Text("${"%.2f".format(line.hours)} h × ${FinanceFormatter.money(line.hourlyRate)} = ${FinanceFormatter.money(line.gross)}")
                            }
                        }
                    }
                }
                if (r.fixedAllowances != 0.0) Text("Period amount and fixed allowances: ${FinanceFormatter.money(r.fixedAllowances)}")
            }
        }
    }
}

private fun advancedBaseGross(result: PayBreakdown, profile: PayProfile, salaryFraction: Double): Double =
    if (profile.basis == AdvancedPayBasis.ANNUAL_SALARY) profile.baseRate * salaryFraction
    else result.shifts.sumOf { it.baseGross }

private fun advancedPipGross(result: PayBreakdown, profile: PayProfile, salaryFraction: Double): Double {
    val base = advancedBaseGross(result, profile, salaryFraction)
    return when (profile.pipType) {
        PipType.NONE -> 0.0
        PipType.PER_HOUR -> result.totalHours * profile.pipValue
        PipType.PER_DAY -> result.shifts.size * profile.pipValue
        PipType.PERCENT_BASE -> base * profile.pipValue / 100.0
        PipType.FIXED_AMOUNT -> profile.pipValue
    }
}

private fun advancedAllowanceGross(result: PayBreakdown, profile: PayProfile, salaryFraction: Double): Double =
    result.totalGross - advancedBaseGross(result, profile, salaryFraction) -
        advancedPipGross(result, profile, salaryFraction)
