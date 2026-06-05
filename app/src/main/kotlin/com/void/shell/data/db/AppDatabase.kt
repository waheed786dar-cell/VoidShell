package com.void.shell.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.void.shell.data.db.entities.GameStateEntity

@Database(
    entities  = [GameStateEntity::class],
    version   = 1,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStateDao(): GameStateDao
}
