package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository

data class ItemsUiState(val items: List<Item>)

class ItemsViewModel : ViewModel() {
    companion object {
        val TAG = "ItemsViewModel"
    }

    var uiState: ItemsUiState by mutableStateOf(ItemsUiState(ItemRepository.items))
        private set

    init {
        Log.d(TAG, "init")
    }
}
