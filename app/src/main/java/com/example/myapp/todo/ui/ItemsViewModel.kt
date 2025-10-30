package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapp.todo.data.Item

data class ItemsUiState(val items: List<Item>)

class ItemsViewModel : ViewModel() {
    companion object {
        val TAG = "ItemsViewModel"
    }

    var uiState = mutableStateOf(
        ItemsUiState(
            items = listOf<Item>(
                Item(id = 0, text = "Learn android", done = false),
                Item(id = 1, text = "Learn compose", done = false)
            )
        )
    )

    init {
        Log.d(TAG, "init")
    }

    fun addItem(text: String) {
        Log.d(TAG, "addItem $text")
        val items = uiState.value.items;
        val item = Item(items.size + 1, text, false)
        uiState.value = ItemsUiState(items.plus(item))
    }

    fun toggleItemDone(id: Int) {
        Log.d(TAG, "toggleItemDone $id")
        val items = uiState.value.items;
        uiState.value =
            ItemsUiState(
                items.map {
                    if (it.id == id) Item(it.id, it.text, !it.done) else it
                }
            )
    }
}
