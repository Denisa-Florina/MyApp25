package com.example.myapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.todo.ui.item.ItemViewModel
import com.example.myapp.todo.ui.item.ItemViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModelFactory(itemId))
    val itemUiState = itemViewModel.uiState
    var text by rememberSaveable { mutableStateOf(itemUiState.item?.text ?: "") }
    Log.d("ItemScreen", "recompose, text = $text")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {
                        Log.d("ItemScreen", "save item text = $text");
                        itemViewModel.updateItem(itemId, text)
                        onClose()
                    }) { Text("Save") }
                }
            )
        }
    ) {
        Row {
            TextField(
                value = text,
                onValueChange = { text = it }, label = { Text("Text") },
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            )
        }
    }
}


@Preview
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = "0", onClose = {})
}
