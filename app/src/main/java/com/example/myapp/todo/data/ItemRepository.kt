package com.example.myapp.todo.data

import android.util.Log
import com.example.myapp.core.TAG
import com.example.myapp.core.data.remote.Api
import com.example.myapp.todo.data.local.ItemDao
import com.example.myapp.todo.data.remote.ItemEvent
import com.example.myapp.todo.data.remote.ItemService
import com.example.myapp.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class ItemRepository(
    private val itemService: ItemService,
    private val itemWsClient: ItemWsClient,
    private val itemDao: ItemDao
) {
    val itemStream by lazy { itemDao.getAll() }

    // Track items being updated to avoid WebSocket race conditions
    private val updatingItems = mutableSetOf<String>()

    init {
        Log.d(TAG, "init")
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun getUnsyncedItems(): List<Item> = itemDao.getUnsyncedItems()

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val items = itemService.find(authorization = getBearerToken())
            itemDao.deleteAll()
            items.forEach {
                itemDao.insert(it.copy(syncStatus = SyncStatus.SYNCED))
            }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it.isSuccess) {
                    val itemEvent = it.getOrNull();
                    when (itemEvent?.type) {
                        "created" -> handleItemCreated(itemEvent.payload)
                        "updated" -> handleItemUpdated(itemEvent.payload)
                        "deleted" -> handleItemDeleted(itemEvent.payload)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            itemWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<kotlin.Result<ItemEvent>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    trySend(kotlin.Result.success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }

    suspend fun update(item: Item): Item {
        Log.d(TAG, "update $item...")

        // Mark this item as being updated (to ignore WebSocket events)
        updatingItems.add(item._id)

        try {
            // Update Local with UPDATED status
            val itemToSave = item.copy(syncStatus = SyncStatus.UPDATED)
            itemDao.update(itemToSave)

            try {
                // Try to update server immediately
                val updatedItem = itemService.update(
                    itemId = item._id,
                    item = itemToSave,
                    authorization = getBearerToken()
                )

                // Mark as synced
                itemDao.updateSyncStatus(item._id, SyncStatus.SYNCED)

                Log.d(TAG, "update succeeded on server")
                return itemToSave.copy(syncStatus = SyncStatus.SYNCED)
            } catch (e: Exception) {
                Log.e(TAG, "Server update failed, will sync later", e)
                // Item remains with UPDATED status for background sync
                return itemToSave
            }
        } finally {
            // Remove from updating set after a delay (allow WebSocket message to arrive and be ignored)
            kotlinx.coroutines.delay(500)
            updatingItems.remove(item._id)
        }
    }

    suspend fun save(item: Item): Item {
        Log.d(TAG, "save $item...")

        // Mark as being created
        updatingItems.add(item._id)

        try {
            // Insert Local with PENDING status
            val itemToSave = item.copy(syncStatus = SyncStatus.PENDING)
            itemDao.insert(itemToSave)

            try {
                // Try to save to server immediately
                val createdItem = itemService.create(
                    item = item,
                    authorization = getBearerToken()
                )

                // Mark as synced
                itemDao.updateSyncStatus(item._id, SyncStatus.SYNCED)

                Log.d(TAG, "save succeeded on server: $createdItem")
                return itemToSave.copy(syncStatus = SyncStatus.SYNCED)
            } catch (e: Exception) {
                Log.e(TAG, "Server save failed, will sync later", e)
                // Item remains with PENDING status for background sync
                return itemToSave
            }
        } finally {
            kotlinx.coroutines.delay(500)
            updatingItems.remove(item._id)
        }
    }

    suspend fun delete(itemId: String) {
        Log.d(TAG, "delete $itemId...")

        // Mark as being deleted
        updatingItems.add(itemId)

        try {
            // Mark as deleted locally (soft delete)
            itemDao.updateSyncStatus(itemId, SyncStatus.DELETED)

            try {
                // Try to delete from server immediately
                itemService.delete(
                    itemId = itemId,
                    authorization = getBearerToken()
                )
                // Permanently delete from local DB
                itemDao.deletePermanently(itemId)
                Log.d(TAG, "delete succeeded on server")
            } catch (e: Exception) {
                Log.e(TAG, "Server delete failed, will sync later", e)
                // Item remains with DELETED status for background sync
            }
        } finally {
            kotlinx.coroutines.delay(500)
            updatingItems.remove(itemId)
        }
    }

    // Background sync methods (called by SyncWorker)
    suspend fun syncCreate(item: Item) {
        Log.d(TAG, "syncCreate for item ${item._id}")
        val createdItem = itemService.create(
            item = item,
            authorization = getBearerToken()
        )
        itemDao.updateSyncStatus(item._id, SyncStatus.SYNCED)
    }

    suspend fun syncUpdate(item: Item) {
        Log.d(TAG, "syncUpdate for item ${item._id}")
        itemService.update(
            itemId = item._id,
            item = item,
            authorization = getBearerToken()
        )
        itemDao.updateSyncStatus(item._id, SyncStatus.SYNCED)
    }

    suspend fun syncDelete(itemId: String) {
        Log.d(TAG, "syncDelete for item $itemId")
        itemService.delete(
            itemId = itemId,
            authorization = getBearerToken()
        )
        itemDao.deletePermanently(itemId)
    }

    private suspend fun handleItemDeleted(item: Item) {
        Log.d(TAG, "handleItemDeleted $item")

        // Don't handle if we're currently deleting this item
        if (updatingItems.contains(item._id)) {
            Log.d(TAG, "Ignoring WebSocket delete for item being deleted locally: ${item._id}")
            return
        }

        itemDao.deleteById(item._id)
    }

    private suspend fun handleItemUpdated(item: Item) {
        Log.d(TAG, "handleItemUpdated for ${item._id}...")

        // CRITICAL: Ignore WebSocket updates for items we're currently updating
        if (updatingItems.contains(item._id)) {
            Log.d(TAG, "Ignoring WebSocket update for item being updated locally: ${item._id}")
            return
        }

        // Check if item exists locally with different data
        val localItems = itemDao.getAll()
        var shouldUpdate = true

        localItems.collect { items ->
            val localItem = items.find { it._id == item._id }
            if (localItem != null && localItem.syncStatus == SyncStatus.UPDATED) {
                // Local item has unsaved changes, don't overwrite
                Log.d(TAG, "Local item has unsaved changes, ignoring WebSocket update")
                shouldUpdate = false
                return@collect
            }
        }

        if (shouldUpdate) {
            Log.d(TAG, "Applying WebSocket update for ${item._id}")
            itemDao.update(item.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    private suspend fun handleItemCreated(item: Item) {
        Log.d(TAG, "handleItemCreated for ${item._id}...")

        // Don't handle if we're currently creating this item
        if (updatingItems.contains(item._id)) {
            Log.d(TAG, "Ignoring WebSocket create for item being created locally: ${item._id}")
            return
        }

        itemDao.insert(item.copy(syncStatus = SyncStatus.SYNCED))
    }

    suspend fun deleteAll() {
        itemDao.deleteAll()
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }
}