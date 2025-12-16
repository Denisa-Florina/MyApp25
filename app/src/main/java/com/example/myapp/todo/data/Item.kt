package com.example.myapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

enum class SyncStatus {
    SYNCED,
    PENDING,
    UPDATED,
    DELETED
}


@JsonClass(generateAdapter = true)
@Entity(tableName = "items")
data class Item(
    @PrimaryKey val _id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val description: String = "",
    val dueDate: Date? = null,
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    @Json(ignore = true) val syncStatus: SyncStatus = SyncStatus.PENDING
)
