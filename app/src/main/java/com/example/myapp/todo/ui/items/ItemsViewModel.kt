package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository
import kotlinx.coroutines.launch

data class ItemsUiState(val items: List<Item>)

class ItemsViewModel : ViewModel() {
    companion object {
        val TAG = "ItemsViewModel"
    }

    var uiState: ItemsUiState by mutableStateOf(ItemsUiState(listOf()))
        private set

    init {
        Log.d(TAG, "init")
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            Log.d(TAG, "loadData...")                         // Main
            val items = ItemRepository.loadItems();                // IO
            Log.d(TAG, "loadData items")                      // Main
            uiState = ItemsUiState(items)                          // Main
            val otherItems = ItemRepository.loadOtherItems()       // Default
            Log.d(TAG, "loadData other items")                // Main
            uiState = ItemsUiState(uiState.items.plus(otherItems)) // Main
        }
    }
}
