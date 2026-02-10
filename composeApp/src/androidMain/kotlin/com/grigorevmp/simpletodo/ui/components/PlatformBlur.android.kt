package com.grigorevmp.simpletodo.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import android.graphics.RenderEffect
import android.graphics.Shader

@Composable
actual fun Modifier.platformBlur(radius: Dp): Modifier {
    if (Build.VERSION.SDK_INT < 31) return this
    val px = with(LocalDensity.current) { radius.toPx() }
    return this.graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
        renderEffect = RenderEffect.createBlurEffect(px, px, Shader.TileMode.CLAMP).asComposeRenderEffect()
    }
}
