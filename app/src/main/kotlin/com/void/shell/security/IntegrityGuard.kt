package com.void.shell.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.void.shell.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrityGuard @Inject constructor() {

    private val _ok = MutableStateFlow(true)
    val integrityState: StateFlow<Boolean> = _ok.asStateFlow()

    // Replace after first signed build:
    // keytool -printcert -jarfile app-release.apk | grep SHA256
    private val CERT_SHA256 = "REPLACE_AFTER_FIRST_SIGNED_BUILD"

    suspend fun initialize(ctx: Context) = withContext(Dispatchers.Default) {
        if (BuildConfig.IS_DEBUG_BUILD) { _ok.value = true; return@withContext }

        val result = IntegrityResult(
            sig   = checkSignature(ctx),
            root  = !isRooted(),
            debug = !isDebugged(),
            emu   = !isEmulator(),
            tracer= checkTracerPid(),
            apk   = checkApkExists(ctx),
        )
        _ok.value = result.passed()
    }

    private fun checkSignature(ctx: Context): Boolean = runCatching {
        val pm = ctx.packageManager
        val sigs = if (Build.VERSION.SDK_INT >= 28) {
            pm.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
              .signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNATURES).signatures
        }
        sigs?.any { s ->
            val h = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
            h.joinToString("") { "%02x".format(it) } == CERT_SHA256
        } ?: false
    }.getOrDefault(false)

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/sbin/su","/system/bin/su","/system/xbin/su",
            "/data/local/xbin/su","/su/bin/su","/system/app/Superuser.apk",
            "/data/local/bin/su","/system/sd/xbin/su"
        )
        if (paths.any { File(it).exists() }) return true
        if (Build.TAGS?.contains("test-keys") == true) return true
        return runCatching {
            val p = Runtime.getRuntime().exec(arrayOf("which","su"))
            p.inputStream.bufferedReader().readLine()?.isNotEmpty() == true
        }.getOrDefault(false)
    }

    private fun isDebugged(): Boolean {
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) return true
        // Timing attack detection
        val t = System.nanoTime()
        var s = 0L; repeat(500_000) { s += it }
        return (System.nanoTime() - t) > 40_000_000L
    }

    private fun checkTracerPid(): Boolean = runCatching {
        File("/proc/self/status").readLines().none { line ->
            line.startsWith("TracerPid:") &&
            line.substringAfter(":").trim().toLongOrNull()?.let { it != 0L } == true
        }
    }.getOrDefault(true)

    private fun isEmulator() =
        Build.FINGERPRINT.run { startsWith("generic") || startsWith("unknown") } ||
        Build.MODEL.run { contains("google_sdk") || contains("Emulator") || contains("Android SDK built for x86") } ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.run { contains("goldfish") || contains("ranchu") } ||
        "google_sdk" == Build.PRODUCT

    private fun checkApkExists(ctx: Context) = runCatching {
        File(ctx.applicationInfo.sourceDir).run { exists() && length() > 0 }
    }.getOrDefault(false)
}

data class IntegrityResult(
    val sig: Boolean, val root: Boolean,
    val debug: Boolean, val emu: Boolean,
    val tracer: Boolean, val apk: Boolean,
) { fun passed() = sig && root && debug && emu && tracer && apk }
