package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapp.todo.data.Item

@Composable
fun ItemDetail(item: Item) {
    Log.d("ItemDetail", "recompose")
    Row {
        Checkbox(checked = item.done, onCheckedChange = null)
        Text(text = item.text)
    }
}

@Preview
@Composable
fun PreviewItemDetail() {
    ItemDetail(Item(text = "Learn android", done = true))
}
