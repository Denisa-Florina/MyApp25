package com.example.myapp.todo.data.remote

import com.example.myapp.todo.data.Item
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemEvent(val type: String, val payload: Item)
