package com.grigorevmp.simpletodo.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun PlatformSystemBars(
    isDark: Boolean,
    backgroundColor: Color
) {
    // No-op on iOS.
}
