package com.example.myapp.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE syncStatus != 'DELETED'")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE syncStatus IN ('PENDING', 'UPDATED', 'DELETED')")
    suspend fun getUnsyncedItems(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Item>)

    @Update
    suspend fun update(item: Item): Int

    @Query("UPDATE items SET syncStatus = :status WHERE _id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("DELETE FROM items WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM items WHERE _id = :id AND syncStatus = 'DELETED'")
    suspend fun deletePermanently(id: String): Int

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}