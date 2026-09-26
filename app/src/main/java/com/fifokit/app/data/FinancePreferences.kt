package com.fifokit.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fifokit.app.domain.finance.FinancialGoal
import com.fifokit.app.domain.finance.PayInput
import com.fifokit.app.domain.finance.PayRateType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.longPreferencesKey

private val Context.financeDataStore by preferencesDataStore(
    name = "finance_preferences"
)

class FinancePreferences(
    private val context: Context
) {

    private object Keys {
        val PAY_RATE_TYPE =
            stringPreferencesKey("pay_rate_type")

        val PAY_RATE =
            doublePreferencesKey("pay_rate")

        val HOURS_PER_WORK_DAY =
            doublePreferencesKey("hours_per_work_day")

        val ALLOWANCE_PER_WORK_DAY =
            doublePreferencesKey("allowance_per_work_day")

        val TARGET_AMOUNT =
            doublePreferencesKey("target_amount")

        val CURRENT_AMOUNT =
            doublePreferencesKey("current_amount")

        val CONTRIBUTION_PER_PAY =
            doublePreferencesKey("contribution_per_pay")

        val PAY_FREQUENCY_DAYS =
            intPreferencesKey("pay_frequency_days")

        val FINANCIAL_GOAL_UPDATED_AT =
            longPreferencesKey("financial_goal_updated_at")

        val PAY_INPUT_UPDATED_AT =
            longPreferencesKey("pay_input_updated_at")

    }

    val payInputUpdatedAt: Flow<Long> =
        context.financeDataStore.data.map { preferences ->
            preferences[Keys.PAY_INPUT_UPDATED_AT] ?: 0L
        }

    val payInput: Flow<PayInput> =
        context.financeDataStore.data.map { preferences ->

            val rateType =
                preferences[Keys.PAY_RATE_TYPE]
                    ?.let { saved ->
                        runCatching {
                            PayRateType.valueOf(saved)
                        }.getOrNull()
                    }
                    ?: PayRateType.HOURLY

            PayInput(
                rateType = rateType,
                rate = preferences[Keys.PAY_RATE] ?: 0.0,
                hoursPerWorkDay =
                    preferences[Keys.HOURS_PER_WORK_DAY] ?: 12.0,
                allowancePerWorkDay =
                    preferences[Keys.ALLOWANCE_PER_WORK_DAY] ?: 0.0
            )
        }

    val financialGoal: Flow<FinancialGoal> =
        context.financeDataStore.data.map { preferences ->
            FinancialGoal(
                targetAmount =
                    preferences[Keys.TARGET_AMOUNT] ?: 0.0,
                currentAmount =
                    preferences[Keys.CURRENT_AMOUNT] ?: 0.0,
                contributionPerPay =
                    preferences[Keys.CONTRIBUTION_PER_PAY] ?: 0.0,
                payFrequencyDays =
                    preferences[Keys.PAY_FREQUENCY_DAYS] ?: 14
            )
        }
    val financialGoalUpdatedAt: Flow<Long> =
        context.financeDataStore.data.map { preferences ->
            preferences[Keys.FINANCIAL_GOAL_UPDATED_AT] ?: 0L
        }

    suspend fun savePayInput(
        input: PayInput
    ) {
        context.financeDataStore.edit { preferences ->
            preferences[Keys.PAY_RATE_TYPE] =
                input.rateType.name

            preferences[Keys.PAY_RATE] =
                input.rate

            preferences[Keys.HOURS_PER_WORK_DAY] =
                input.hoursPerWorkDay

            preferences[Keys.ALLOWANCE_PER_WORK_DAY] =
                input.allowancePerWorkDay

            preferences[Keys.PAY_INPUT_UPDATED_AT] =
                System.currentTimeMillis()

        }
    }

    suspend fun saveFinancialGoal(
        goal: FinancialGoal
    ) {
        context.financeDataStore.edit { preferences ->
            preferences[Keys.TARGET_AMOUNT] =
                goal.targetAmount

            preferences[Keys.CURRENT_AMOUNT] =
                goal.currentAmount

            preferences[Keys.CONTRIBUTION_PER_PAY] =
                goal.contributionPerPay

            preferences[Keys.PAY_FREQUENCY_DAYS] =
                goal.payFrequencyDays

            preferences[Keys.FINANCIAL_GOAL_UPDATED_AT] =
                System.currentTimeMillis()

        }
    }

    suspend fun applyCloudFinancialGoal(
        goal: FinancialGoal,
        updatedAt: Long
    ) {
        context.financeDataStore.edit { preferences ->

            preferences[Keys.TARGET_AMOUNT] =
                goal.targetAmount

            preferences[Keys.CURRENT_AMOUNT] =
                goal.currentAmount

            preferences[Keys.CONTRIBUTION_PER_PAY] =
                goal.contributionPerPay

            preferences[Keys.PAY_FREQUENCY_DAYS] =
                goal.payFrequencyDays

            preferences[Keys.FINANCIAL_GOAL_UPDATED_AT] =
                updatedAt
        }
    }
    suspend fun applyCloudPayInput(
        input: PayInput,
        updatedAt: Long
    ) {
        context.financeDataStore.edit { preferences ->

            preferences[Keys.PAY_RATE_TYPE] =
                input.rateType.name

            preferences[Keys.PAY_RATE] =
                input.rate

            preferences[Keys.HOURS_PER_WORK_DAY] =
                input.hoursPerWorkDay

            preferences[Keys.ALLOWANCE_PER_WORK_DAY] =
                input.allowancePerWorkDay

            preferences[Keys.PAY_INPUT_UPDATED_AT] =
                updatedAt
        }
    }

}