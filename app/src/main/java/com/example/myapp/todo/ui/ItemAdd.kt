package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview

val TAG = "ItemAdd"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAdd(itemsViewModel: ItemsViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    Log.d("ItemAdd", "recompose, text = $text")

    Row {
        TextField(value = text, onValueChange = { text = it }, label = { Text("Text") })
        Button(onClick = {
            Log.d("ItemAdd", "add item with text = $text");
            itemsViewModel.addItem(text)
            text = ""
        }) { Text("Add") }
    }
}
