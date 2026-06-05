package com.void.shell.data.db

import androidx.room.TypeConverter

class DatabaseConverters {
    @TypeConverter fun longToString(v: Long): String = v.toString()
    @TypeConverter fun stringToLong(v: String): Long = v.toLongOrNull() ?: 0L
}
