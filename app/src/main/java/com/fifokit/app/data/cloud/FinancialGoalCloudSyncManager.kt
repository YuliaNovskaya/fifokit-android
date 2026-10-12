package com.fifokit.app.data.cloud

import android.content.Context
import android.util.Log
import com.fifokit.app.data.FinancePreferences
import com.fifokit.app.data.cloud.model.CloudFinancialGoal
import com.fifokit.app.domain.finance.FinancialGoal
import com.fifokit.app.domain.finance.GoalContributionTiming
import kotlinx.coroutines.flow.first

data class FinancialGoalSyncResult(
    val uploaded: Boolean,
    val downloaded: Boolean
)

class FinancialGoalCloudSyncManager(
    context: Context,
    private val financePreferences: FinancePreferences,
    private val cloudRepository: CloudRepository = CloudRepository()
) {

    private val deviceIdProvider =
        DeviceIdProvider(context)

    suspend fun sync(
        uid: String
    ): FinancialGoalSyncResult {

        val deviceId =
            deviceIdProvider.getDeviceId()

        val localGoals =
            financePreferences
                .financialGoalsForSync
                .first()

        val cloudGoals =
            cloudRepository
                .getFinancialGoals(uid)

        val localById =
            localGoals.associateBy {
                it.id
            }

        val cloudById =
            cloudGoals.associateBy {
                it.id
            }

        val allIds =
            localById.keys + cloudById.keys

        Log.d("GoalSync", "goals local=${localById.size} cloud=${cloudById.size}")

        var uploaded = false
        var downloaded = false

        for (goalId in allIds) {

            val local =
                localById[goalId]

            val cloud =
                cloudById[goalId]

            Log.d("GoalSync", "id=$goalId localDeleted=${local?.isDeleted} cloudDeleted=${cloud?.isDeleted} localAt=${local?.updatedAt} cloudAt=${cloud?.updatedAt}")

            when {
                local != null &&
                        cloud == null -> {

                    if (
                        local.updatedAt > 0L ||
                        local.id != "primary"
                    ) {
                        cloudRepository
                            .saveFinancialGoal(
                                uid = uid,
                                goal =
                                    local.toCloud(
                                        deviceId
                                    )
                            )

                        uploaded = true
                    }
                }

                local == null &&
                        cloud != null -> {

                    financePreferences
                        .applyCloudFinancialGoal(
                            goal =
                                cloud.toLocal(),
                            updatedAt =
                                cloud.updatedAt
                        )

                    downloaded = true
                }

                local != null &&
                        cloud != null -> {

                    // A deletion is terminal for this goal ID. A stale active
                    // copy on another device must never resurrect it.
                    if (local.isDeleted != cloud.isDeleted) {
                        if (local.isDeleted) {
                            val tombstone = local.copy(
                                updatedAt = maxOf(local.updatedAt, cloud.updatedAt) + 1L,
                                deletedAt = maxOf(local.deletedAt, cloud.updatedAt) + 1L
                            )
                            financePreferences.applyCloudFinancialGoal(tombstone, tombstone.updatedAt)
                            cloudRepository.saveFinancialGoal(uid, tombstone.toCloud(deviceId))
                            uploaded = true
                        } else {
                            val tombstone = cloud.copy(
                                updatedAt = maxOf(cloud.updatedAt, local.updatedAt) + 1L,
                                deletedAt = maxOf(cloud.deletedAt, local.updatedAt) + 1L
                            )
                            cloudRepository.saveFinancialGoal(uid, tombstone)
                            financePreferences.applyCloudFinancialGoal(
                                tombstone.toLocal(), tombstone.updatedAt
                            )
                            downloaded = true
                        }
                        continue
                    }

                    when {
                        cloud.updatedAt >
                                local.updatedAt -> {

                            financePreferences
                                .applyCloudFinancialGoal(
                                    goal =
                                        cloud.toLocal(),
                                    updatedAt =
                                        cloud.updatedAt
                                )

                            downloaded = true
                        }

                        local.updatedAt >
                                cloud.updatedAt -> {

                            cloudRepository
                                .saveFinancialGoal(
                                    uid = uid,
                                    goal =
                                        local.toCloud(
                                            deviceId
                                        )
                                )

                            uploaded = true
                        }
                    }
                }
            }
        }

        val after = financePreferences.financialGoalsForSync.first()
        Log.d("GoalSync", "AFTER local=" + after.joinToString { "${it.id}:deleted=${it.isDeleted}:at=${it.updatedAt}" })
        return FinancialGoalSyncResult(
            uploaded = uploaded,
            downloaded = downloaded
        )
    }

    private fun FinancialGoal.toCloud(
        deviceId: String
    ): CloudFinancialGoal {

        return CloudFinancialGoal(
            id = id,
            name = name,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            contributionPerPay =
                contributionPerPay,
            payFrequencyDays =
                payFrequencyDays,
            contributionTiming =
                contributionTiming.name,
            isDeleted = isDeleted,
            deleted = isDeleted,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deviceId = deviceId,
            schemaVersion = 3
        )
    }

    private fun CloudFinancialGoal.toLocal():
            FinancialGoal {

        return FinancialGoal(
            id = id,
            name = name,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            contributionPerPay =
                contributionPerPay,
            payFrequencyDays =
                payFrequencyDays,
            contributionTiming =
                runCatching {
                    GoalContributionTiming.valueOf(
                        contributionTiming
                    )
                }.getOrDefault(
                    GoalContributionTiming.FIXED_DAYS
                ),
            isDeleted = isDeleted,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
