# ═══════════════════════════════════════════════════════
# VOIDSHELL — ULTRA PROGUARD / R8 RULES
# ═══════════════════════════════════════════════════════

# Aggressive optimization passes
-optimizationpasses 7
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses 'x'
-flattenpackagehierarchy 'x'
-dontusemixedcaseclassnames

# Strip all debug logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }
-keep @kotlin.Metadata class *

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @javax.inject.Inject class * { *; }
-keep @dagger.Module class * { *; }
-dontwarn dagger.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract !public *;
}
-dontwarn androidx.room.**

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Tink Crypto
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Game engine events — must keep for EventBus reflection-free routing
-keep class com.void.shell.core.events.** { *; }

# Domain models — Room entities + serialization
-keep class com.void.shell.domain.** { *; }
-keep class com.void.shell.data.db.entities.** { *; }

# Security — keep names intact for JNI
-keep class com.void.shell.security.** { *; }
-keepclasseswithmembernames class * { native <methods>; }

# Remove source info from stack traces
-renamesourcefileattribute S
-keepattributes SourceFile,LineNumberTable

# Keep BuildConfig
-keep class com.void.shell.BuildConfig { *; }
