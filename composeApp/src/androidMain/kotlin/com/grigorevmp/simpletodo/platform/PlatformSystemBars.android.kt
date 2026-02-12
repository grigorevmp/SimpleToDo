package com.grigorevmp.simpletodo.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
actual fun PlatformSystemBars(
    isDark: Boolean,
    backgroundColor: ComposeColor
) {
    val view = LocalView.current
    val scrim = backgroundColor.toArgb()
    SideEffect {
        val activity = view.context.findActivity() as? ComponentActivity ?: return@SideEffect
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { _ -> isDark }
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = scrim,
                darkScrim = scrim,
                detectDarkMode = { _ -> isDark }
            )
        )
    }
}

private fun Context.findActivity(): Activity? {
    var cur: Context = this
    while (cur is ContextWrapper) {
        if (cur is Activity) return cur
        cur = cur.baseContext
    }
    return null
}
