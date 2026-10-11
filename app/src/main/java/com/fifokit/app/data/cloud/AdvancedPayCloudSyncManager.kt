package com.fifokit.app.data.cloud

import android.content.Context
import com.fifokit.app.data.AdvancedPayProfileStore

/** Last-write-wins synchronization, matching the existing finance sync strategy. */
class AdvancedPayCloudSyncManager(
    context: Context,
    private val repository: CloudRepository = CloudRepository()
) {
    private val local = AdvancedPayProfileStore(context)
    private val deviceIdProvider = DeviceIdProvider(context)

    suspend fun sync(uid: String) {
        val cloud = repository.getAdvancedPayProfile(uid)
        val localTimestamp = local.updatedAt()
        val localJson = local.exportJson()
        val cloudTimestamp = (cloud?.get("updatedAt") as? Number)?.toLong() ?: 0L
        val cloudJson = cloud?.get("json") as? String

        when {
            cloudJson == null && localJson != null -> {
                val timestamp = if (localTimestamp > 0L) localTimestamp else System.currentTimeMillis()
                if (localTimestamp == 0L) local.applyCloudJson(localJson, timestamp)
                repository.saveAdvancedPayProfile(
                    uid, localJson, timestamp, deviceIdProvider.getDeviceId()
                )
            }
            cloudJson != null && cloudTimestamp > localTimestamp ->
                local.applyCloudJson(cloudJson, cloudTimestamp)
            localJson != null && localTimestamp > cloudTimestamp ->
                repository.saveAdvancedPayProfile(
                    uid, localJson, localTimestamp, deviceIdProvider.getDeviceId()
                )
        }
    }
}
