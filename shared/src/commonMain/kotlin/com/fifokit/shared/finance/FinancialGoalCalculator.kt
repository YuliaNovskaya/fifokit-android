package com.fifokit.shared.finance

import kotlin.math.ceil

data class FinancialGoal(
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val contributionPerPay: Double = 0.0,
    val payFrequencyDays: Int = 14
)

data class FinancialGoalResult(
    val amountRemaining: Double,
    val contributionsRequired: Int,
    val daysRequired: Int
)

/** Mirrors Android's financial-goal contribution calculation. */
object FinancialGoalCalculator {
    fun calculate(goal: FinancialGoal): FinancialGoalResult {
        require(goal.targetAmount.isFinite() && goal.targetAmount > 0)
        require(goal.currentAmount.isFinite() && goal.currentAmount >= 0)
        require(goal.contributionPerPay.isFinite() && goal.contributionPerPay >= 0)
        require(goal.payFrequencyDays > 0)

        val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
        if (remaining == 0.0 || goal.contributionPerPay == 0.0) {
            return FinancialGoalResult(remaining, 0, 0)
        }
        val count = ceil(remaining / goal.contributionPerPay)
        require(count <= Int.MAX_VALUE.toDouble() / goal.payFrequencyDays) {
            "Goal projection exceeds the supported range"
        }
        val required = count.toInt()
        return FinancialGoalResult(
            amountRemaining = remaining,
            contributionsRequired = required,
            daysRequired = required * goal.payFrequencyDays
        )
    }
}
