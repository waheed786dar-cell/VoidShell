package com.void.shell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TamperedWarningScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050A0E)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text       = "⚠ INTEGRITY VIOLATION",
                color      = Color(0xFFFF0040),
                fontSize   = 20.sp,
                fontFamily = FontFamily.Monospace,
                textAlign  = TextAlign.Center,
            )
            Text(
                text       = "Is device ya app ke saath kuch gadbad hai.\nApp nahi chalegi.",
                color      = Color(0xFF8899AA),
                fontSize   = 13.sp,
                fontFamily = FontFamily.Monospace,
                textAlign  = TextAlign.Center,
                lineHeight = 20.sp,
            )
            Text(
                text       = "ERROR CODE: VOID_TAMPER_001",
                color      = Color(0xFF445566),
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}
