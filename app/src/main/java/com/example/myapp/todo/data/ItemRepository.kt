package com.example.myapp.todo.data

import android.util.Log
import com.example.myapp.core.TAG
import com.example.myapp.todo.data.remote.ItemService

class ItemRepository(private val itemService: ItemService) {
    private var cachedItems: MutableList<Item> = listOf<Item>().toMutableList()

    init {
        Log.d(TAG, "init")
    }

    suspend fun loadAll(): List<Item> {
        Log.d(TAG, "loadAll")
        val items = itemService.find();
        Log.d(TAG, "loadAll succeeded ${items.size}")
        cachedItems = items.toMutableList()
        return cachedItems as List<Item>
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
}