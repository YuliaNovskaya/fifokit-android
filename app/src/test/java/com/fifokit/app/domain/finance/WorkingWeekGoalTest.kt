package com.fifokit.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WorkingWeekGoalTest {
    @Test fun skipsWeeksWithoutWork() {
        val first = LocalDate.of(2026, 10, 12) // Monday
        val goal = FinancialGoal(
            targetAmount = 200.0,
            currentAmount = 0.0,
            contributionPerPay = 100.0,
            contributionTiming = GoalContributionTiming.WORKING_WEEK
        )
        val result = FinanceCalculator.calculateFinancialGoal(goal, first) { date ->
            // Only the first and third weeks have work.
            val elapsed = java.time.temporal.ChronoUnit.DAYS.between(first, date)
            elapsed in 0L..4L || elapsed in 14L..18L
        }
        assertEquals(2, result?.contributionsRequired)
        assertEquals(18, result?.daysRequired)
    }
}
