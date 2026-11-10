package com.fifokit.shared.finance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FinancialGoalCalculatorTest {
    @Test
    fun roundsUpContributions() {
        val result = FinancialGoalCalculator.calculate(
            FinancialGoal(10000.0, 2500.0, 1000.0, 14)
        )
        assertEquals(7500.0, result.amountRemaining)
        assertEquals(8, result.contributionsRequired)
        assertEquals(112, result.daysRequired)
    }

    @Test
    fun completedGoalRequiresNoPayments() {
        val result = FinancialGoalCalculator.calculate(
            FinancialGoal(10000.0, 11000.0, 0.0, 14)
        )
        assertEquals(0.0, result.amountRemaining)
        assertEquals(0, result.contributionsRequired)
    }

    @Test
    fun rejectsInvalidSavings() {
        assertFailsWith<IllegalArgumentException> {
            FinancialGoalCalculator.calculate(FinancialGoal(1000.0, -1.0, 200.0, 14))
        }
    }
}
