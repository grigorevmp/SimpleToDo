package com.grigorevmp.simpletodo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

fun Modifier.itemPlacement(): Modifier = composed {
    var previous by remember { mutableStateOf<IntOffset?>(null) }
    val anim = remember { Animatable(IntOffset.Zero, IntOffset.VectorConverter) }
    val scope = rememberCoroutineScope()

    this.onGloballyPositioned { coords ->
        val pos = coords.positionInWindow().toIntOffsetCompat()
        val prev = previous
        if (prev != null) {
            val delta = prev - pos
            if (delta != IntOffset.Zero) {
                scope.launch {
                    anim.snapTo(delta)
                    anim.animateTo(
                        IntOffset.Zero,
                        spring(stiffness = 700f, dampingRatio = 0.85f)
                    )
                }
            }
        }
        previous = pos
    }.graphicsLayer {
        translationX = anim.value.x.toFloat()
        translationY = anim.value.y.toFloat()
    }
}

private fun androidx.compose.ui.geometry.Offset.toIntOffsetCompat(): IntOffset {
    return IntOffset(kotlin.math.round(x).toInt(), kotlin.math.round(y).toInt())
}
