package com.fifokit.app.widgets

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RosterWidgetRefreshWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    context,
    workerParameters
) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(
                "FIFOKITWidget",
                "Widget refresh worker started"
            )

            RosterWidgetUpdater.refreshNow(
                applicationContext
            )

            Log.d(
                "FIFOKITWidget",
                "Widget refresh worker finished"
            )

            Result.success()

        } catch (error: Throwable) {
            Log.e(
                "FIFOKITWidget",
                "Widget refresh worker failed",
                error
            )

            Result.retry()
        }
    }
}
