package com.example.myapp.todo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapp.todo.data.Item

class ItemsViewModel : ViewModel() {
    init {
        Log.d("ItemsViewModel", "init")
    }

    val items = listOf<Item>(
        Item(text = "Learn android", done = false),
        Item(text = "Learn compose", done = false)
    )
}
