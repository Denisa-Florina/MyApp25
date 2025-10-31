package com.example.myapp.todo.data.remote

import android.util.Log
import com.example.myapp.todo.data.Item
import kotlinx.coroutines.delay

object ItemService {
    val items: MutableList<Item> = List(4) { index ->
        Item(id = "$index", text = "Item $index")
    }.toMutableList()

    suspend fun getItems(): List<Item> {
        Log.d("ItemService", "getItems...")
        delay(5000)                              // should be IO or Default
        return items.subList(0, 2);
    }
}