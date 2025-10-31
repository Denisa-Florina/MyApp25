package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository

data class ItemUiState(val item: Item?)

class ItemViewModelFactory(private val itemId: String?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ItemViewModel(itemId) as T
}

class ItemViewModel(itemId: String?) : ViewModel() {
    companion object {
        val TAG = "ItemEditViewModel"
    }

    var uiState: ItemUiState by mutableStateOf(ItemUiState(ItemRepository.items.filter { it.id == itemId }[0]))
        private set

    init {
        Log.d(TAG, "init")
    }

    fun updateItem(id: String, text: String) {
        Log.d(TAG, "addItem $text")
        val item = ItemRepository.update(id, text)
        uiState = ItemUiState(item)
    }
}
