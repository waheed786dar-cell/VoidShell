package com.void.shell.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.void.shell.R

// Fonts must exist at: app/src/main/res/font/
// Download: fonts.google.com/specimen/Share+Tech+Mono
val ShareTechMono = FontFamily(Font(R.font.share_tech_mono))
val CourierPrime  = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold,    FontWeight.Bold),
)

val VoidTypography = Typography(
    displayLarge  = TextStyle(fontFamily = ShareTechMono, fontSize = 32.sp, color = C.Green,  letterSpacing = 2.sp),
    displayMedium = TextStyle(fontFamily = ShareTechMono, fontSize = 24.sp, color = C.Green,  letterSpacing = 1.5.sp),
    headlineMedium= TextStyle(fontFamily = ShareTechMono, fontSize = 18.sp, color = C.TextPrimary, letterSpacing = 1.sp),
    bodyLarge     = TextStyle(fontFamily = CourierPrime,  fontSize = 14.sp, color = C.TextPrimary, lineHeight = 22.sp),
    bodyMedium    = TextStyle(fontFamily = CourierPrime,  fontSize = 12.sp, color = C.TextSecond,  lineHeight = 18.sp),
    labelLarge    = TextStyle(fontFamily = ShareTechMono, fontSize = 12.sp, color = C.Green,  letterSpacing = 1.sp),
    labelSmall    = TextStyle(fontFamily = ShareTechMono, fontSize = 10.sp, color = C.TextSecond, letterSpacing = 0.8.sp),
)
