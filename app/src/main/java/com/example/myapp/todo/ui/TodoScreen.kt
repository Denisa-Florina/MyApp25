package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TodoScreen() {
    Log.d("TodoScreen", "recompose")
    Column {
        ItemAdd()
        ItemList()
    }
}

@Preview
@Composable
fun PreviewTodoScreen() {
    TodoScreen()
}
