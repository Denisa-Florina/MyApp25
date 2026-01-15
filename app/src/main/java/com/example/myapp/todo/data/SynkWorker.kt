package com.example.myapp.todo.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapp.MyApplication
import com.example.myapp.core.TAG
import com.example.myapp.todo.data.SyncStatus

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "SyncWorker: Starting sync...")

        val app = applicationContext as MyApplication
        val repository = app.container.itemRepository

        return try {
            // Get all unsynced items
            val unsyncedItems = repository.getUnsyncedItems()
            Log.d(TAG, "SyncWorker: Found ${unsyncedItems.size} unsynced items")

            var successCount = 0
            var failCount = 0

            for (item in unsyncedItems) {
                try {
                    when (item.syncStatus) {
                        SyncStatus.PENDING -> {
                            Log.d(TAG, "SyncWorker: Creating item ${item._id}")
                            repository.syncCreate(item)
                            successCount++
                        }
                        SyncStatus.UPDATED -> {
                            Log.d(TAG, "SyncWorker: Updating item ${item._id}")
                            repository.syncUpdate(item)
                            successCount++
                        }
                        SyncStatus.DELETED -> {
                            Log.d(TAG, "SyncWorker: Deleting item ${item._id}")
                            repository.syncDelete(item._id)
                            successCount++
                        }
                        SyncStatus.SYNCED -> {
                            // Already synced, skip
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "SyncWorker: Failed to sync item ${item._id}", e)
                    failCount++
                }
            }

            Log.d(TAG, "SyncWorker: Sync completed. Success: $successCount, Failed: $failCount")

            if (failCount == 0) {
                Result.success()
            } else if (successCount > 0) {
                // Partial success - retry failed items
                Result.retry()
            } else {
                // All failed
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker: Sync failed", e)
            Result.retry()
        }
    }
}