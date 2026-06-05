package com.void.shell.di

import android.content.Context
import androidx.room.Room
import com.void.shell.data.db.AppDatabase
import com.void.shell.data.db.GameStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext ctx: Context
    ): AppDatabase {
        // SQLCipher encrypted database
        val passphrase   = SQLiteDatabase.getBytes("VoidShellDbKey2025".toCharArray())
        val factory      = SupportFactory(passphrase)

        return Room.databaseBuilder(ctx, AppDatabase::class.java, "voidshell.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGameStateDao(db: AppDatabase): GameStateDao = db.gameStateDao()
}
