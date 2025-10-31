package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.MyApplication
import com.example.myapp.core.Result
import com.example.myapp.core.TAG
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository
import kotlinx.coroutines.launch

class ItemsViewModel(private val itemRepository: ItemRepository) : ViewModel() {
    var uiState: Result<List<Item>> by mutableStateOf(Result.Loading)
        private set

    init {
        Log.d(TAG, "init")
        loadItems()
        listenItemEvents()
    }

    fun loadItems() {
        Log.d(TAG, "loadItems...")
        viewModelScope.launch {
            itemRepository.loadAll().collect { result ->
                Log.d(TAG, "loadItems collect")
                uiState = result
            }
        }
    }

    fun listenItemEvents() {
        viewModelScope.launch {
            itemRepository.listenSocketEvents()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemsViewModel(app.container.itemRepository)
            }
        }
    }
}
