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

    init {
        Log.d(TAG, "init")
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val items = itemService.find(authorization = getBearerToken())
            itemDao.deleteAll()
            items.forEach { itemDao.insert(it) }
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
        // Update Local immediately (Optimistic UI)
        itemDao.update(item)

        try {
            // Update Server
            val updatedItem = itemService.update(
                itemId = item._id,
                item = item,
                authorization = getBearerToken()
            )
            return updatedItem
        } catch (e: Exception) {
            Log.e(TAG, "Server update failed", e)
            // Optional: Mark item as 'dirty' to sync later
            throw e
        }
    }

    suspend fun save(item: Item): Item {
        Log.d(TAG, "save $item...")
        // Insert Local immediately so the user sees it
        itemDao.insert(item)

        try {
            // Send to Server
            // The server will use the UUID we generated in the Item data class
            val createdItem = itemService.create(
                item = item,
                authorization = getBearerToken()
            )
            Log.d(TAG, "save succeeded on server: $createdItem")
            return createdItem
        } catch (e: Exception) {
            Log.e(TAG, "Server save failed", e)
            // If server fails, we still have it locally.
            // You might want to delete it locally or queue it for retry.
            throw e
        }
    }

    suspend fun delete(itemId: String) {
        Log.d(TAG, "delete $itemId...")
        // Delete Local immediately (Optimistic UI)
        itemDao.deleteById(itemId)

        try {
            // Delete from Server
            itemService.delete(
                itemId = itemId,
                authorization = getBearerToken()
            )
            Log.d(TAG, "delete succeeded on server")
        } catch (e: Exception) {
            Log.e(TAG, "Server delete failed", e)
            // If server fails, the local item is already deleted.
            // You might want to restore it or queue for retry.
            throw e
        }
    }

    private suspend fun handleItemDeleted(item: Item) {
        Log.d(TAG, "handleItemDeleted $item")
        itemDao.deleteById(item._id)
    }

    private suspend fun handleItemUpdated(item: Item) {
        Log.d(TAG, "handleItemUpdated...")
        itemDao.update(item)
    }

    private suspend fun handleItemCreated(item: Item) {
        Log.d(TAG, "handleItemCreated...")
        itemDao.insert(item)
    }

    suspend fun deleteAll() {
        itemDao.deleteAll()
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }
}