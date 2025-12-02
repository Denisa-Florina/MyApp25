package com.example.myapp.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapp.todo.data.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAll(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Item>)

    @Update
    suspend fun update(item: Item): Int

    @Query("DELETE FROM items WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
