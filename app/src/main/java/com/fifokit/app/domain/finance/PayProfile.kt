package com.fifokit.app.domain.finance

import java.time.LocalDate
import java.util.UUID

enum class WorkArrangement { FIFO, LOCAL }
enum class EmploymentType { FULL_TIME, PART_TIME, CASUAL }
enum class AdvancedPayBasis { HOURLY, DAILY, ANNUAL_SALARY }
enum class PayShiftType { DAY, NIGHT, ROTATING, CUSTOM }

data class PayProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My pay",
    val arrangement: WorkArrangement = WorkArrangement.FIFO,
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,
    val basis: AdvancedPayBasis = AdvancedPayBasis.HOURLY,
    val baseRate: Double = 0.0,
    val shiftType: PayShiftType = PayShiftType.DAY,
    val effectiveFrom: LocalDate? = null,
    val effectiveUntil: LocalDate? = null,
    val casualLoadingPercent: Double = 0.0,
    val pipType: PipType = PipType.NONE,
    val pipValue: Double = 0.0,
    val rateRules: List<RateRule> = emptyList(),
    val overtimeRules: List<OvertimeRule> = emptyList(),
    val allowances: List<Allowance> = emptyList()
)

data class PaidShift(
    val start: java.time.LocalDateTime,
    val end: java.time.LocalDateTime,
    val shiftType: PayShiftType = PayShiftType.DAY,
    val swingId: String? = null
) {
    init { require(end.isAfter(start)) { "Shift end must be after start" } }
}
