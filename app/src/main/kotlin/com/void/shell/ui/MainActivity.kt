package com.void.shell.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.void.shell.BuildConfig
import com.void.shell.core.engine.GameEngine
import com.void.shell.security.IntegrityGuard
import com.void.shell.ui.navigation.VoidNavGraph
import com.void.shell.ui.theme.LocalEngineState
import com.void.shell.ui.theme.VoidShellTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var gameEngine    : GameEngine
    @Inject lateinit var integrityGuard: IntegrityGuard

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold splash until engine warm
        splash.setKeepOnScreenCondition {
            !gameEngine.isWarmedUp.value
        }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Immersive fullscreen
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Prevent screenshots in release
        if (!BuildConfig.IS_DEBUG_BUILD) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }

        setContent {
            val engineState by gameEngine.engineState.collectAsStateWithLifecycle()
            val integrityOk by integrityGuard.integrityState.collectAsStateWithLifecycle()

            VoidShellTheme {
                CompositionLocalProvider(LocalEngineState provides engineState) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color    = Color(0xFF050A0E),
                    ) {
                        if (integrityOk) VoidNavGraph()
                        else TamperedWarningScreen()
                    }
                }
            }
        }
    }

    override fun onResume()  { super.onResume();  gameEngine.onAppForeground() }
    override fun onPause()   { super.onPause();   gameEngine.onAppBackground() }
    override fun onDestroy() { super.onDestroy(); gameEngine.onAppDestroy()    }
}
