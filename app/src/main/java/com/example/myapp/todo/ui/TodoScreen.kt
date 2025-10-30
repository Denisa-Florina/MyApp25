package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TodoScreen() {
    Log.d("TodoScreen", "recompose")
    val itemsViewModel = viewModel<ItemsViewModel>()
    Column {
        ItemAdd(itemsViewModel)
        ItemList(itemsViewModel)
    }
}

@Preview
@Composable
fun PreviewTodoScreen() {
    TodoScreen()
}
