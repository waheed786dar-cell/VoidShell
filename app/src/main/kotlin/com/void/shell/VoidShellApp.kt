package com.void.shell

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import com.void.shell.core.engine.GameEngine
import com.void.shell.core.events.EventBus
import com.void.shell.data.crypto.EncryptionManager
import com.void.shell.security.IntegrityGuard
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VoidShellApp : Application() {

    @Inject lateinit var gameEngine       : GameEngine
    @Inject lateinit var eventBus         : EventBus
    @Inject lateinit var encryptionManager: EncryptionManager
    @Inject lateinit var integrityGuard   : IntegrityGuard

    // Application-level supervisor scope
    // SupervisorJob = one child crash doesn't kill others
    val appScope = CoroutineScope(
        SupervisorJob() +
        Dispatchers.Default +
        CoroutineExceptionHandler { _, throwable ->
            InternalCrashLogger.log(throwable)
        }
    )

    companion object {
        lateinit var instance: VoidShellApp
            private set

        const val CH_ENGINE = "void_engine"
        const val CH_SAVE   = "void_save"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.IS_DEBUG_BUILD) setupStrictMode()

        createNotificationChannels()

        // Run in parallel — no blocking
        appScope.launch { integrityGuard.initialize(applicationContext) }
        appScope.launch { encryptionManager.initialize(applicationContext) }
        appScope.launch { gameEngine.warmUp() }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val pressure = when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL -> MemoryPressure.CRITICAL
            TRIM_MEMORY_RUNNING_LOW      -> MemoryPressure.HIGH
            TRIM_MEMORY_UI_HIDDEN        -> MemoryPressure.MEDIUM
            else                         -> MemoryPressure.LOW
        }
        appScope.launch { gameEngine.onMemoryPressure(pressure) }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        appScope.launch { gameEngine.onMemoryPressure(MemoryPressure.CRITICAL) }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CH_ENGINE, "Engine", NotificationManager.IMPORTANCE_LOW)
                    .apply { setShowBadge(false) }
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_SAVE, "Auto Save", NotificationManager.IMPORTANCE_MIN)
                    .apply { setShowBadge(false) }
            )
        }
    }

    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll().penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog().build()
        )
    }
}

enum class MemoryPressure { LOW, MEDIUM, HIGH, CRITICAL }

internal object InternalCrashLogger {
    fun log(t: Throwable) {
        runCatching {
            val f = VoidShellApp.instance.getFileStreamPath("crash.enc")
            if (f.length() < 50_000) {
                f.appendText("[${System.currentTimeMillis()}] ${t.message}\n${t.stackTraceToString().take(1500)}\n\n")
            }
        }
    }
}
