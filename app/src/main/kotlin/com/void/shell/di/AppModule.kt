package com.void.shell.di

import android.content.Context
import com.void.shell.data.crypto.EncryptionManager
import com.void.shell.security.IntegrityGuard
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideEncryptionManager(
        @ApplicationContext ctx: Context
    ): EncryptionManager = EncryptionManager(ctx)

    @Provides @Singleton
    fun provideIntegrityGuard(): IntegrityGuard = IntegrityGuard()
}
