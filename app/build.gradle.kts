import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// ── Signing config loader ────────────────────────────────────────────
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore/signing.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace   = "com.void.shell"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.void.shell"
        minSdk          = 26
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        buildConfigField("String", "APP_VERSION",  "\"1.0.0\"")
        buildConfigField("long",   "BUILD_TIME",   "${System.currentTimeMillis()}L")
        buildConfigField("String", "BUILD_FLAVOR", "\"standard\"")
    }

    // ── Split APKs per ABI ───────────────────────────────────────────
    splits {
        abi {
            isEnable        = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk  = true  // Universal APK bhi banao
        }
    }

    // ── Signing ──────────────────────────────────────────────────────
    signingConfigs {
        create("release") {
            storeFile       = file("../keystore/voidshell-release.keystore")
            storePassword   = System.getenv("STORE_PASSWORD")
                              ?: keystoreProps["storePassword"] as? String ?: ""
            keyAlias        = System.getenv("KEY_ALIAS")
                              ?: keystoreProps["keyAlias"] as? String ?: ""
            keyPassword     = System.getenv("KEY_PASSWORD")
                              ?: keystoreProps["keyPassword"] as? String ?: ""
            // All signing schemes
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    // ── Build Types ───────────────────────────────────────────────────
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix   = "-DEBUG"
            isDebuggable        = true
            isMinifyEnabled     = false
            buildConfigField("boolean", "IS_DEBUG_BUILD",    "true")
            buildConfigField("boolean", "SECURITY_CHECKS",   "false")
        }
        release {
            isMinifyEnabled     = true
            isShrinkResources   = true
            isDebuggable        = false
            signingConfig       = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG_BUILD",    "false")
            buildConfigField("boolean", "SECURITY_CHECKS",   "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES",
            "META-INF/*.kotlin_module",
            "META-INF/versions/**"
        )
    }
}

dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)

    // Compose BOM — version managed centrally
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.prev)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation)
    implementation(libs.compose.animation.core)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation + Lifecycle
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.service)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    // Room + SQLCipher (encrypted DB)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.sqlcipher)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    // Kotlinx Serialization
    implementation(libs.serialization.json)

    // Security
    implementation(libs.security.crypto)
    implementation(libs.tink.android)

    // DataStore Preferences
    implementation(libs.datastore)

    // Immutable Collections — Compose stability
    implementation(libs.immutable)

    // Splash Screen API
    implementation(libs.splashscreen)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
}
