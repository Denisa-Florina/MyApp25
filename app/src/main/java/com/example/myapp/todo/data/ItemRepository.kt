package com.example.myapp.todo.data

import android.util.Log
import com.example.myapp.todo.data.remote.ItemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object ItemRepository {
    val items: List<Item> = ItemService.items

    fun update(id: String, text: String): Item? {
        Log.d("ItemRepository", "update $id")
        val index = ItemService.items.indexOfFirst { it -> it.id == id }
        if (index != -1) {
            val item = items[index].copy(text = text)
            ItemService.items.set(index, item)
            return item
        }
        return null
    }

    suspend fun loadItems(): List<Item> {
        Log.d("ItemRepository", "loadItems...")        // Main
        return withContext(Dispatchers.IO) {
            val items = ItemService.getItems();                  // IO
            Log.d("ItemRepository", "loadItems $items") // IO
            return@withContext items;                            // IO
        }
    }

    suspend fun loadOtherItems(): List<Item> {
        Log.d("ItemRepository", "loadOtherItems...")   // Main
        return withContext(Dispatchers.Default) {
            val deferred1 = async { getOtherItems(2) }    // Default
            val deferred2 = async { getOtherItems(3) }    // Default
            val (list1, list2) = awaitAll(deferred1, deferred2) // wait for both
//            val list1 = deferred1.await() // sequentially
//            val list2 = deferred1.await()
            return@withContext list1.plus(list2)
        }
    }

    suspend fun getOtherItems(index: Int): List<Item> { // Default
        Log.d("ItemService", "getOtherItems...")
        delay(10000)
        return items.subList(index, index + 1)
    }
}