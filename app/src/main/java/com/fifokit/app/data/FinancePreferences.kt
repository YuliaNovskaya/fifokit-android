package com.fifokit.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fifokit.app.domain.finance.FinancialGoal
import com.fifokit.app.domain.finance.GoalContributionTiming
import com.fifokit.app.domain.finance.PayInput
import com.fifokit.app.domain.finance.PayRateType
import com.fifokit.app.domain.finance.PipType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

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

        val PIP_TYPE =
            stringPreferencesKey("pip_type")

        val PIP_VALUE =
            doublePreferencesKey("pip_value")

        val LOCAL_RATES_ENABLED =
            booleanPreferencesKey("local_rates_enabled")

        val LOCAL_WEEKDAY_HOURLY_RATE =
            doublePreferencesKey("local_weekday_hourly_rate")

        val LOCAL_WEEKDAY_BASE_HOURS =
            doublePreferencesKey("local_weekday_base_hours")

        val LOCAL_WEEKDAY_OVERTIME_HOURLY_RATE =
            doublePreferencesKey(
                "local_weekday_overtime_hourly_rate"
            )

        val LOCAL_WEEKDAY_OVERTIME_HOURS =
            doublePreferencesKey(
                "local_weekday_overtime_hours"
            )

        val LOCAL_SATURDAY_HOURLY_RATE =
            doublePreferencesKey("local_saturday_hourly_rate")

        val LOCAL_SATURDAY_HOURS =
            doublePreferencesKey("local_saturday_hours")

        val LOCAL_SUNDAY_HOURLY_RATE =
            doublePreferencesKey("local_sunday_hourly_rate")

        val LOCAL_SUNDAY_HOURS =
            doublePreferencesKey("local_sunday_hours")

        val LOCAL_HOURS_PER_DAY =
            doublePreferencesKey("local_hours_per_day")

        // Legacy primary-goal fields retained for migration/backward compatibility.
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

        val FINANCIAL_GOALS_JSON =
            stringPreferencesKey("financial_goals_json")

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

            val pipType =
                preferences[Keys.PIP_TYPE]
                    ?.let { saved ->
                        runCatching {
                            PipType.valueOf(saved)
                        }.getOrNull()
                    }
                    ?: PipType.NONE

            PayInput(
                rateType = rateType,
                rate = preferences[Keys.PAY_RATE] ?: 0.0,
                hoursPerWorkDay =
                    preferences[Keys.HOURS_PER_WORK_DAY] ?: 12.0,
                allowancePerWorkDay =
                    preferences[Keys.ALLOWANCE_PER_WORK_DAY] ?: 0.0,
                pipType = pipType,
                pipValue =
                    preferences[Keys.PIP_VALUE] ?: 0.0,
                localRatesEnabled =
                    preferences[Keys.LOCAL_RATES_ENABLED] ?: false,
                localWeekdayHourlyRate =
                    preferences[Keys.LOCAL_WEEKDAY_HOURLY_RATE] ?: 0.0,
                localWeekdayBaseHours =
                    preferences[Keys.LOCAL_WEEKDAY_BASE_HOURS]
                        ?: preferences[Keys.LOCAL_HOURS_PER_DAY]
                        ?: 8.0,
                localWeekdayOvertimeHourlyRate =
                    preferences[
                        Keys.LOCAL_WEEKDAY_OVERTIME_HOURLY_RATE
                    ] ?: 0.0,
                localWeekdayOvertimeHours =
                    preferences[
                        Keys.LOCAL_WEEKDAY_OVERTIME_HOURS
                    ] ?: 0.0,
                localSaturdayHourlyRate =
                    preferences[Keys.LOCAL_SATURDAY_HOURLY_RATE] ?: 0.0,
                localSaturdayHours =
                    preferences[Keys.LOCAL_SATURDAY_HOURS]
                        ?: preferences[Keys.LOCAL_HOURS_PER_DAY]
                        ?: 8.0,
                localSundayHourlyRate =
                    preferences[Keys.LOCAL_SUNDAY_HOURLY_RATE] ?: 0.0,
                localSundayHours =
                    preferences[Keys.LOCAL_SUNDAY_HOURS] ?: 0.0,
                localHoursPerDay =
                    preferences[Keys.LOCAL_HOURS_PER_DAY] ?: 8.0
            )
        }

    val financialGoalsForSync: Flow<List<FinancialGoal>> =
        context.financeDataStore.data.map { preferences ->
            decodeGoals(preferences)
        }

    val financialGoals: Flow<List<FinancialGoal>> =
        context.financeDataStore.data.map { preferences ->
            decodeGoals(preferences)
                .filterNot { it.isDeleted }
                .sortedWith(
                    compareBy<FinancialGoal> {
                        it.id != "primary"
                    }.thenBy {
                        it.createdAt
                    }
                )
        }

    val financialGoal: Flow<FinancialGoal> =
        context.financeDataStore.data.map { preferences ->
            decodeGoals(preferences)
                .firstOrNull {
                    it.id == "primary" &&
                            !it.isDeleted
                }
                ?: FinancialGoal()
        }

    val financialGoalUpdatedAt: Flow<Long> =
        context.financeDataStore.data.map { preferences ->
            decodeGoals(preferences)
                .firstOrNull {
                    it.id == "primary"
                }
                ?.updatedAt
                ?: 0L
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

            preferences[Keys.PIP_TYPE] =
                input.pipType.name

            preferences[Keys.PIP_VALUE] =
                input.pipValue

            preferences[Keys.LOCAL_RATES_ENABLED] =
                input.localRatesEnabled

            preferences[Keys.LOCAL_WEEKDAY_HOURLY_RATE] =
                input.localWeekdayHourlyRate

            preferences[Keys.LOCAL_WEEKDAY_BASE_HOURS] =
                input.localWeekdayBaseHours

            preferences[
                Keys.LOCAL_WEEKDAY_OVERTIME_HOURLY_RATE
            ] =
                input.localWeekdayOvertimeHourlyRate

            preferences[Keys.LOCAL_WEEKDAY_OVERTIME_HOURS] =
                input.localWeekdayOvertimeHours

            preferences[Keys.LOCAL_SATURDAY_HOURLY_RATE] =
                input.localSaturdayHourlyRate

            preferences[Keys.LOCAL_SATURDAY_HOURS] =
                input.localSaturdayHours

            preferences[Keys.LOCAL_SUNDAY_HOURLY_RATE] =
                input.localSundayHourlyRate

            preferences[Keys.LOCAL_SUNDAY_HOURS] =
                input.localSundayHours

            preferences[Keys.LOCAL_HOURS_PER_DAY] =
                input.localHoursPerDay

            preferences[Keys.PAY_INPUT_UPDATED_AT] =
                System.currentTimeMillis()
        }
    }

    suspend fun createFinancialGoal(
        name: String
    ): FinancialGoal {
        val now =
            System.currentTimeMillis()

        val created =
            FinancialGoal(
                id = UUID.randomUUID().toString(),
                name =
                    name.trim().ifBlank {
                        "New goal"
                    },
                createdAt = now,
                updatedAt = now
            )

        context.financeDataStore.edit { preferences ->
            val goals =
                decodeGoals(preferences)
                    .toMutableList()

            goals += created

            preferences[Keys.FINANCIAL_GOALS_JSON] =
                encodeGoals(goals)
        }

        return created
    }

    suspend fun saveFinancialGoal(
        goal: FinancialGoal
    ) {
        val now =
            System.currentTimeMillis()

        context.financeDataStore.edit { preferences ->
            val goals =
                decodeGoals(preferences)
                    .toMutableList()

            val id =
                goal.id.ifBlank {
                    "primary"
                }

            val existing =
                goals.firstOrNull {
                    it.id == id
                }

            val saved =
                goal.copy(
                    id = id,
                    name =
                        goal.name.trim().ifBlank {
                            if (id == "primary") {
                                "My goal"
                            } else {
                                "Goal"
                            }
                        },
                    createdAt =
                        existing
                            ?.createdAt
                            ?.takeIf {
                                it > 0L
                            }
                            ?: goal.createdAt
                                .takeIf {
                                    it > 0L
                                }
                            ?: now,
                    updatedAt = now,
                    isDeleted = false,
                    deletedAt = 0L
                )

            goals.removeAll {
                it.id == id
            }

            goals += saved

            preferences[Keys.FINANCIAL_GOALS_JSON] =
                encodeGoals(goals)

            if (id == "primary") {
                writeLegacyPrimary(
                    preferences = preferences,
                    goal = saved
                )
            }
        }
    }

    suspend fun cleanupLegacyEmptySecondaryGoals() {
        val now = System.currentTimeMillis()

        context.financeDataStore.edit { preferences ->
            val goals = decodeGoals(preferences).toMutableList()
            var changed = false

            goals.indices.forEach { index ->
                val goal = goals[index]

                val shouldArchive =
                    goal.id != "primary" &&
                    !goal.isDeleted &&
                    goal.targetAmount == 0.0 &&
                    goal.currentAmount == 0.0 &&
                    goal.contributionPerPay == 0.0 &&
                    goal.payFrequencyDays == 14

                if (shouldArchive) {
                    goals[index] = goal.copy(
                        isDeleted = true,
                        deletedAt = now,
                        updatedAt = now
                    )
                    changed = true
                }
            }

            if (changed) {
                preferences[Keys.FINANCIAL_GOALS_JSON] =
                    encodeGoals(goals)
            }
        }
    }

    suspend fun deleteFinancialGoal(
        goalId: String
    ) {
        val now =
            System.currentTimeMillis()

        context.financeDataStore.edit { preferences ->
            val goals =
                decodeGoals(preferences)
                    .toMutableList()

            val index =
                goals.indexOfFirst {
                    it.id == goalId
                }

            if (index < 0) {
                return@edit
            }

            goals[index] =
                goals[index].copy(
                    isDeleted = true,
                    deletedAt = now,
                    updatedAt = now
                )

            preferences[Keys.FINANCIAL_GOALS_JSON] =
                encodeGoals(goals)

            if (goalId == "primary") {
                preferences[Keys.TARGET_AMOUNT] =
                    0.0

                preferences[Keys.CURRENT_AMOUNT] =
                    0.0

                preferences[Keys.CONTRIBUTION_PER_PAY] =
                    0.0

                preferences[Keys.PAY_FREQUENCY_DAYS] =
                    14

                preferences[Keys.FINANCIAL_GOAL_UPDATED_AT] =
                    now
            }
        }
    }

    suspend fun applyCloudFinancialGoal(
        goal: FinancialGoal,
        updatedAt: Long
    ) {
        context.financeDataStore.edit { preferences ->
            val goals =
                decodeGoals(preferences)
                    .toMutableList()

            val normalized =
                goal.copy(
                    id =
                        goal.id.ifBlank {
                            "primary"
                        },
                    name =
                        goal.name.trim().ifBlank {
                            if (
                                goal.id.isBlank() ||
                                goal.id == "primary"
                            ) {
                                "My goal"
                            } else {
                                "Goal"
                            }
                        },
                    updatedAt = updatedAt
                )

            goals.removeAll {
                it.id == normalized.id
            }

            goals += normalized

            preferences[Keys.FINANCIAL_GOALS_JSON] =
                encodeGoals(goals)

            if (normalized.id == "primary") {
                writeLegacyPrimary(
                    preferences = preferences,
                    goal = normalized
                )
            }
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

            preferences[Keys.PIP_TYPE] =
                input.pipType.name

            preferences[Keys.PIP_VALUE] =
                input.pipValue

            preferences[Keys.LOCAL_RATES_ENABLED] =
                input.localRatesEnabled

            preferences[Keys.LOCAL_WEEKDAY_HOURLY_RATE] =
                input.localWeekdayHourlyRate

            preferences[Keys.LOCAL_WEEKDAY_BASE_HOURS] =
                input.localWeekdayBaseHours

            preferences[
                Keys.LOCAL_WEEKDAY_OVERTIME_HOURLY_RATE
            ] =
                input.localWeekdayOvertimeHourlyRate

            preferences[Keys.LOCAL_WEEKDAY_OVERTIME_HOURS] =
                input.localWeekdayOvertimeHours

            preferences[Keys.LOCAL_SATURDAY_HOURLY_RATE] =
                input.localSaturdayHourlyRate

            preferences[Keys.LOCAL_SATURDAY_HOURS] =
                input.localSaturdayHours

            preferences[Keys.LOCAL_SUNDAY_HOURLY_RATE] =
                input.localSundayHourlyRate

            preferences[Keys.LOCAL_SUNDAY_HOURS] =
                input.localSundayHours

            preferences[Keys.LOCAL_HOURS_PER_DAY] =
                input.localHoursPerDay

            preferences[Keys.PAY_INPUT_UPDATED_AT] =
                updatedAt
        }
    }

    private fun decodeGoals(
        preferences: Preferences
    ): List<FinancialGoal> {

        val encoded =
            preferences[Keys.FINANCIAL_GOALS_JSON]

        if (!encoded.isNullOrBlank()) {
            val decoded =
                runCatching {
                    val array =
                        JSONArray(encoded)

                    buildList {
                        for (
                            index in 0 until array.length()
                        ) {
                            val item =
                                array.getJSONObject(index)

                            add(
                                FinancialGoal(
                                    id =
                                        item.optString(
                                            "id",
                                            "primary"
                                        ),
                                    name =
                                        item.optString(
                                            "name",
                                            "My goal"
                                        ),
                                    targetAmount =
                                        item.optDouble(
                                            "targetAmount",
                                            0.0
                                        ),
                                    currentAmount =
                                        item.optDouble(
                                            "currentAmount",
                                            0.0
                                        ),
                                    contributionPerPay =
                                        item.optDouble(
                                            "contributionPerPay",
                                            0.0
                                        ),
                                    payFrequencyDays =
                                        item.optInt(
                                            "payFrequencyDays",
                                            14
                                        ),
                                    contributionTiming =
                                        runCatching {
                                            GoalContributionTiming
                                                .valueOf(
                                                    item.optString(
                                                        "contributionTiming",
                                                        GoalContributionTiming
                                                            .FIXED_DAYS
                                                            .name
                                                    )
                                                )
                                        }.getOrDefault(
                                            GoalContributionTiming
                                                .FIXED_DAYS
                                        ),
                                    createdAt =
                                        item.optLong(
                                            "createdAt",
                                            0L
                                        ),
                                    updatedAt =
                                        item.optLong(
                                            "updatedAt",
                                            0L
                                        ),
                                    isDeleted =
                                        item.optBoolean(
                                            "isDeleted",
                                            false
                                        ),
                                    deletedAt =
                                        item.optLong(
                                            "deletedAt",
                                            0L
                                        )
                                )
                            )
                        }
                    }
                }.getOrNull()

            if (!decoded.isNullOrEmpty()) {
                return decoded
            }
        }

        val updatedAt =
            preferences[
                Keys.FINANCIAL_GOAL_UPDATED_AT
            ] ?: 0L

        return listOf(
            FinancialGoal(
                id = "primary",
                name = "My goal",
                targetAmount =
                    preferences[
                        Keys.TARGET_AMOUNT
                    ] ?: 0.0,
                currentAmount =
                    preferences[
                        Keys.CURRENT_AMOUNT
                    ] ?: 0.0,
                contributionPerPay =
                    preferences[
                        Keys.CONTRIBUTION_PER_PAY
                    ] ?: 0.0,
                payFrequencyDays =
                    preferences[
                        Keys.PAY_FREQUENCY_DAYS
                    ] ?: 14,
                createdAt = updatedAt,
                updatedAt = updatedAt
            )
        )
    }

    private fun encodeGoals(
        goals: List<FinancialGoal>
    ): String {
        val array =
            JSONArray()

        goals.forEach { goal ->
            array.put(
                JSONObject()
                    .put(
                        "id",
                        goal.id
                    )
                    .put(
                        "name",
                        goal.name
                    )
                    .put(
                        "targetAmount",
                        goal.targetAmount
                    )
                    .put(
                        "currentAmount",
                        goal.currentAmount
                    )
                    .put(
                        "contributionPerPay",
                        goal.contributionPerPay
                    )
                    .put(
                        "payFrequencyDays",
                        goal.payFrequencyDays
                    )
                    .put(
                        "contributionTiming",
                        goal.contributionTiming.name
                    )
                    .put(
                        "createdAt",
                        goal.createdAt
                    )
                    .put(
                        "updatedAt",
                        goal.updatedAt
                    )
                    .put(
                        "isDeleted",
                        goal.isDeleted
                    )
                    .put(
                        "deletedAt",
                        goal.deletedAt
                    )
            )
        }

        return array.toString()
    }

    private fun writeLegacyPrimary(
        preferences:
            androidx.datastore.preferences.core.MutablePreferences,
        goal: FinancialGoal
    ) {
        preferences[Keys.TARGET_AMOUNT] =
            goal.targetAmount

        preferences[Keys.CURRENT_AMOUNT] =
            goal.currentAmount

        preferences[Keys.CONTRIBUTION_PER_PAY] =
            goal.contributionPerPay

        preferences[Keys.PAY_FREQUENCY_DAYS] =
            goal.payFrequencyDays

        preferences[Keys.FINANCIAL_GOAL_UPDATED_AT] =
            goal.updatedAt
    }
}
