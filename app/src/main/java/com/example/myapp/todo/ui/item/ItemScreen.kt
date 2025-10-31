package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.todo.ui.item.ItemViewModel
import com.example.myapp.todo.ui.item.ItemViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModelFactory(itemId))
    val itemUiState = itemViewModel.uiState
    var text by rememberSaveable { mutableStateOf(itemUiState.item?.text ?: "") }
    Log.d("ItemScreen", "recompose, text = $text")

    Row {
        TextField(value = text, onValueChange = { text = it }, label = { Text("Text") })
        Button(onClick = {
            Log.d("ItemScreen", "add item with text = $text");
            itemViewModel.updateItem(itemId, text)
            onClose()
        }) { Text("Update") }
    }
}


@Preview
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = "0", onClose = {})
}
