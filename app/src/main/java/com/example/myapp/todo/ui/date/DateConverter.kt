package com.example.myapp.todo.ui.date

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateConverter {

    // Room needs this annotation
    @TypeConverter
    fun fromString(value: String?): Date? {
        return value?.let {
            // Create formatter locally to ensure thread safety
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(it)
        }
    }

    // Room needs this annotation
    @TypeConverter
    fun toString(date: Date?): String? {
        return date?.let {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(it)
        }
    }
}