package com.example.myapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.SyncStatus
import com.example.myapp.todo.data.local.ItemDao
import com.example.myapp.todo.ui.date.DateConverter

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}


@Database(entities = [Item::class], version = 5)
@TypeConverters(DateConverter::class, SyncStatusConverter::class)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        fun getDatabase(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyAppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}