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

        val localGoal =
            financePreferences.financialGoal.first()

        val localUpdatedAt =
            financePreferences.financialGoalUpdatedAt.first()

        val cloudGoal =
            cloudRepository
                .getFinancialGoals(uid)
                .firstOrNull { it.id == "primary" }

        if (cloudGoal == null) {

            if (localUpdatedAt == 0L) {
                return FinancialGoalSyncResult(
                    uploaded = false,
                    downloaded = false
                )
            }

            cloudRepository.saveFinancialGoal(
                uid = uid,
                goal = CloudFinancialGoal(
                    id = "primary",
                    targetAmount = localGoal.targetAmount,
                    currentAmount = localGoal.currentAmount,
                    contributionPerPay =
                        localGoal.contributionPerPay,
                    payFrequencyDays =
                        localGoal.payFrequencyDays,
                    createdAt = localUpdatedAt,
                    updatedAt = localUpdatedAt,
                    deviceId = deviceId,
                    schemaVersion = 1
                )
            )

            return FinancialGoalSyncResult(
                uploaded = true,
                downloaded = false
            )
        }

        if (cloudGoal.updatedAt > localUpdatedAt) {

            financePreferences.applyCloudFinancialGoal(
                goal = FinancialGoal(
                    targetAmount =
                        cloudGoal.targetAmount,
                    currentAmount =
                        cloudGoal.currentAmount,
                    contributionPerPay =
                        cloudGoal.contributionPerPay,
                    payFrequencyDays =
                        cloudGoal.payFrequencyDays
                ),
                updatedAt =
                    cloudGoal.updatedAt
            )

            return FinancialGoalSyncResult(
                uploaded = false,
                downloaded = true
            )
        }

        if (localUpdatedAt > cloudGoal.updatedAt) {

            cloudRepository.saveFinancialGoal(
                uid = uid,
                goal = CloudFinancialGoal(
                    id = "primary",
                    targetAmount = localGoal.targetAmount,
                    currentAmount = localGoal.currentAmount,
                    contributionPerPay =
                        localGoal.contributionPerPay,
                    payFrequencyDays =
                        localGoal.payFrequencyDays,
                    createdAt =
                        cloudGoal.createdAt,
                    updatedAt =
                        localUpdatedAt,
                    deviceId =
                        deviceId,
                    schemaVersion = 1
                )
            )

            return FinancialGoalSyncResult(
                uploaded = true,
                downloaded = false
            )
        }

        return FinancialGoalSyncResult(
            uploaded = false,
            downloaded = false
        )
    }
}