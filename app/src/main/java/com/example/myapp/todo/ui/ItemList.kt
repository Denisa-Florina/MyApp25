package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ItemList() {
    Log.d("ItemList", "recompose")
    val itemListViewModel = viewModel<ItemsViewModel>()
    Column {
        for (item in itemListViewModel.items) {
            ItemDetail(item = item)
        }
    }
}

@Preview
@Composable
fun PreviewItemList() {
    ItemList()
}
