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
fun ItemAdd() {
    val mutableState = rememberSaveable { mutableStateOf("") }
    Log.d(TAG, "recompose, text = ${mutableState.value}")
    Row {
        TextField(
            value = mutableState.value,
            onValueChange = { mutableState.value = it },
            label = { Text("Text") }
        )
        Button(onClick = {
            Log.d(TAG, "add todo");
        }) {
            Text("Add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAdd2() {
    val (text, setText) = rememberSaveable { mutableStateOf("") }
    Log.d(TAG, "recompose, text = $text")
    Row {
        TextField(
            value = text,
            onValueChange = { setText(it) },
            label = { Text("Text") }
        )
        Button(onClick = {
            Log.d(TAG, "add todo");
        }) {
            Text("Add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAdd3() {
    var text by rememberSaveable { mutableStateOf("") }
    Log.d(TAG, "recompose, text = $text")
    Row {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text") }
        )
        Button(onClick = {
            Log.d(TAG, "add todo");
        }) {
            Text("Add")
        }
    }
}


@Preview
@Composable
fun PreviewItemAdd() {
    ItemAdd()
}
