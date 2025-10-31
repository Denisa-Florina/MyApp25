package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.example.myapp.todo.data.Item

typealias OnItemFn = (id: String) -> Unit

@Composable
fun ItemList(items: List<Item>, onItemClick: OnItemFn) {
    Log.d("ItemList", "recompose")
    Column {
        for (item in items) {
            ItemDetail(item, onItemClick)
        }
    }
}

@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn) {
    Log.d("ItemDetail", "recompose id = ${item.id}")
    Row {
        ClickableText(text = AnnotatedString(item.text), onClick = { onItemClick(item.id) })
    }
}
