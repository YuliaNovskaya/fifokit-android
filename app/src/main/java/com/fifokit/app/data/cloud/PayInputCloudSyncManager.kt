package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.FinancePreferences
import com.fifokit.app.data.cloud.model.CloudPayInput
import com.fifokit.app.domain.finance.PayInput
import com.fifokit.app.domain.finance.PayRateType
import kotlinx.coroutines.flow.first

data class PayInputSyncResult(
    val uploaded: Boolean,
    val downloaded: Boolean
)

class PayInputCloudSyncManager(
    context: Context,
    private val financePreferences: FinancePreferences,
    private val cloudRepository: CloudRepository = CloudRepository()
) {

    private val deviceIdProvider =
        DeviceIdProvider(context)

    suspend fun sync(
        uid: String
    ): PayInputSyncResult {

        val deviceId =
            deviceIdProvider.getDeviceId()

        val localInput =
            financePreferences.payInput.first()

        val localUpdatedAt =
            financePreferences.payInputUpdatedAt.first()

        val cloudInput =
            cloudRepository.getPayInput(uid)

        if (cloudInput == null) {

            if (localUpdatedAt == 0L) {
                return PayInputSyncResult(
                    uploaded = false,
                    downloaded = false
                )
            }

            cloudRepository.savePayInput(
                uid = uid,
                payInput = CloudPayInput(
                    rateType = localInput.rateType.name,
                    rate = localInput.rate,
                    hoursPerWorkDay =
                        localInput.hoursPerWorkDay,
                    allowancePerWorkDay =
                        localInput.allowancePerWorkDay,
                    updatedAt = localUpdatedAt,
                    deviceId = deviceId,
                    schemaVersion = 1
                )
            )

            return PayInputSyncResult(
                uploaded = true,
                downloaded = false
            )
        }

        if (cloudInput.updatedAt > localUpdatedAt) {

            val rateType =
                runCatching {
                    PayRateType.valueOf(
                        cloudInput.rateType
                    )
                }.getOrDefault(
                    PayRateType.HOURLY
                )

            financePreferences.applyCloudPayInput(
                input = PayInput(
                    rateType = rateType,
                    rate = cloudInput.rate,
                    hoursPerWorkDay =
                        cloudInput.hoursPerWorkDay,
                    allowancePerWorkDay =
                        cloudInput.allowancePerWorkDay
                ),
                updatedAt =
                    cloudInput.updatedAt
            )

            return PayInputSyncResult(
                uploaded = false,
                downloaded = true
            )
        }

        if (localUpdatedAt > cloudInput.updatedAt) {

            cloudRepository.savePayInput(
                uid = uid,
                payInput = CloudPayInput(
                    rateType = localInput.rateType.name,
                    rate = localInput.rate,
                    hoursPerWorkDay =
                        localInput.hoursPerWorkDay,
                    allowancePerWorkDay =
                        localInput.allowancePerWorkDay,
                    updatedAt =
                        localUpdatedAt,
                    deviceId =
                        deviceId,
                    schemaVersion = 1
                )
            )

            return PayInputSyncResult(
                uploaded = true,
                downloaded = false
            )
        }

        return PayInputSyncResult(
            uploaded = false,
            downloaded = false
        )
    }
}