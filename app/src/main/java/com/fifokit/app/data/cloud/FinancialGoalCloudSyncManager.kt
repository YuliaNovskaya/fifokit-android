package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.FinancePreferences
import com.fifokit.app.data.cloud.model.CloudFinancialGoal
import com.fifokit.app.domain.finance.FinancialGoal
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

        var uploaded = false
        var downloaded = false

        for (goalId in allIds) {

            val local =
                localById[goalId]

            val cloud =
                cloudById[goalId]

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
            isDeleted = isDeleted,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deviceId = deviceId,
            schemaVersion = 2
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
            isDeleted = isDeleted,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
