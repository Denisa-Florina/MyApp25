package com.example.myapp.todo.data

import android.util.Log
import com.example.myapp.core.TAG
import com.example.myapp.core.Result
import com.example.myapp.todo.data.remote.ItemService
import com.example.myapp.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class ItemRepository(private val itemService: ItemService, private val itemWsClient: ItemWsClient) {
    private var cachedItems: MutableList<Item> = listOf<Item>().toMutableList()

    init {
        Log.d(TAG, "init")
    }

    suspend fun loadAll(): Flow<Result<List<Item>>> = flow {
        Log.d(TAG, "loadAll started")
        emit(Result.Loading)
        if (cachedItems.size > 0) {
            Log.d(TAG, "loadAll emit cached items")
            emit(Result.Success(cachedItems as List<Item>))
        }
        while (coroutineContext.isActive) {
            try {
                val items = itemService.find()
                cachedItems = items.toMutableList()
                Log.d(TAG, "loadAll emit remote items")
                emit(Result.Success(cachedItems as List<Item>))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
            delay(5000)
        }
        Log.d(TAG, "loadAll completed")
    }

    suspend fun load(itemId: String?): Item {
        Log.d(TAG, "load $itemId")
        return itemService.read(itemId)
    }

    suspend fun update(item: Item): Item {
        Log.d(TAG, "update $item")
        val updatedItem = itemService.update(item.id, item)
        val index = cachedItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            cachedItems.set(index, updatedItem)
        }
        return updatedItem
    }

    suspend fun save(item: Item): Item {
        Log.d(TAG, "save $item")
        val createdItem = itemService.create(item)
        cachedItems.add(0, createdItem);
        return createdItem
    }

    fun getItem(itemId: String?): Item? = cachedItems?.find { it.id == itemId }

    suspend fun listenSocketEvents() {
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected ${currentCoroutineContext().javaClass} $it")
            }
        }
    }

    fun getItemEvents(): Flow<kotlin.Result<String>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                trySend(kotlin.Result.success(it))
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }
}