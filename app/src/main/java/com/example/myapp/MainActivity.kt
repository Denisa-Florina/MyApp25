package com.example.myapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Log.d(TAG, "setContent")
            ItemDetail("Learn compose")
        }
    }
}

@Composable
fun ItemDetail(text: String) {
    Log.d(TAG, "ItemDetail($text)")
    Text(text = text)
}

@Preview
@Composable
fun PreviewItemDetail() {
    ItemDetail("Learn android")
}
