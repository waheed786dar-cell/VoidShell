package com.void.shell.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.void.shell.core.engine.EngineState

val CyberScheme = darkColorScheme(
    primary          = C.Green,
    onPrimary        = C.Bg,
    primaryContainer = C.GreenDark,
    secondary        = C.Blue,
    onSecondary      = C.Bg,
    tertiary         = C.Purple,
    background       = C.Bg,
    onBackground     = C.TextPrimary,
    surface          = C.Surface,
    onSurface        = C.TextPrimary,
    surfaceVariant   = C.SurfaceVar,
    error            = C.Red,
    onError          = C.Bg,
    outline          = C.GreenDark,
)

val LocalEngineState = compositionLocalOf<EngineState> { EngineState.Idle }

@Composable
fun VoidShellTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberScheme,
        typography  = VoidTypography,
        content     = content,
    )
}
