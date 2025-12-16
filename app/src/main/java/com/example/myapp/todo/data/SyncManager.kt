package com.example.myapp.todo.data

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapp.core.TAG

object SyncManager {
    private const val SYNC_WORK_NAME = "item_sync_work"

    fun scheduleSync(context: Context) {
        Log.d(TAG, "SyncManager: Scheduling sync work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    fun cancelSync(context: Context) {
        Log.d(TAG, "SyncManager: Canceling sync work")
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
    }
}