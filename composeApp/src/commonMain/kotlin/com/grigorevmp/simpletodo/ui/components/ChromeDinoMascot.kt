package com.grigorevmp.simpletodo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ChromeDinoMascot(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onSurface
    val path1 = remember { PathParser().parsePathString(TREX_DINO_PATH_STR).toPath() }
    val path2 = remember { PathParser().parsePathString(TREX_DINO_PATH_2_STR).toPath() }
    val t by rememberInfiniteTransition(label = "dino")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(360), RepeatMode.Reverse),
            label = "step"
        )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val bounds = path1.getBounds()
        val scaleX = size.toPx() / bounds.width
        val scaleY = size.toPx() / bounds.height
        val scale = kotlin.math.min(scaleX, scaleY)
        val dx = (size.toPx() - bounds.width * scale) / 2f - bounds.left * scale
        val dy = (size.toPx() - bounds.height * scale) / 2f - bounds.top * scale - 16f
        translate(left = dx, top = dy) {
            scale(scale) {
                val a1 = if (t < 0.5f) 1f else 0f
                val a2 = 1f - a1
                drawPath(path1, color.copy(alpha = a1), style = Fill)
                drawPath(path2, color.copy(alpha = a2), style = Fill)
            }
        }
    }
}

private val TREX_DINO_PATH_STR =
    "M93.027,18.996L173.41,18.996L173.41,60.836L93.027,60.836ZM93.027,18.996 M99.27,14.727L167.168,14.727L167.168,56.57L99.27,56.57ZM99.27,14.727 M93.027,37.715L127.531,37.715L127.531,79.555L93.027,79.555ZM93.027,37.715 M93.027,71.113L153.223,71.113L153.223,79.555L93.027,79.555ZM93.027,71.113 M107.113,25.676L115.113,25.676L115.113,33.676L107.113,33.676ZM107.113,25.676 M93.027,37.715L120.188,37.715L120.188,122.5L93.027,122.5ZM93.027,37.715 M93.027,98.848L136.707,98.848L136.707,107.289L93.027,107.289ZM93.027,98.848 M130.098,99.008L136.707,99.008L136.707,114.422L130.098,114.422ZM130.098,99.008 M86.629,84.328L113.789,84.328L113.789,137.914L86.629,137.914ZM86.629,84.328 M73.828,91.793L105.762,91.793L105.762,145.379L73.828,145.379ZM73.828,91.793 M63.16,99.262L90.324,99.262L90.324,152.848L63.16,152.848ZM63.16,99.262 M64.953,106.727L79.656,106.727L79.656,160.313L64.953,160.313ZM64.953,106.727 M86.629,106.727L96.539,106.727L96.539,160.313L86.629,160.313ZM86.629,106.727 M91.031,106.727L96.539,106.727L96.539,185.273L91.031,185.273ZM91.031,106.727 M91.031,179.766L106.082,179.766L106.082,185.273L91.031,185.273ZM91.031,179.766 M63.301,106.727L68.805,106.727L68.805,170.59L63.301,170.59ZM63.301,106.727 M63.301,165.086L78.348,165.086L78.348,170.59L63.301,170.59ZM63.301,165.086 M54.285,106.727L68.988,106.727L68.988,153.566L54.285,153.566ZM54.285,106.727 M45.695,106.727L58.324,106.727L58.324,146.301L45.695,146.301ZM45.695,106.727 M39.293,99.25L49.789,99.25L49.789,137.766L39.293,137.766ZM39.293,99.25 M31.828,91.781L42.324,91.781L42.324,130.301L31.828,130.301ZM31.828,91.781 M26.59,84.316L32.723,84.316L32.723,122.832L26.59,122.832ZM26.59,84.316 "

private val TREX_DINO_PATH_2_STR =
    "M 93.027 18.996 L 173.41 18.996 L 173.41 60.836 L 93.027 60.836 Z M 93.027 18.996 M 99.27 14.727 L 167.168 14.727 L 167.168 56.57 L 99.27 56.57 Z M 99.27 14.727 M 93.027 37.715 L 127.531 37.715 L 127.531 79.555 L 93.027 79.555 Z M 93.027 37.715 M 93.027 71.113 L 153.223 71.113 L 153.223 79.555 L 93.027 79.555 Z M 93.027 71.113 M 107.113 25.676 L 115.113 25.676 L 115.113 33.676 L 107.113 33.676 Z M 107.113 25.676 M 93.027 37.715 L 120.188 37.715 L 120.188 122.5 L 93.027 122.5 Z M 93.027 37.715 M 93.027 98.848 L 136.707 98.848 L 136.707 107.289 L 93.027 107.289 Z M 93.027 98.848 M 130.098 99.008 L 136.707 99.008 L 136.707 114.422 L 130.098 114.422 Z M 130.098 99.008 M 86.629 84.328 L 113.789 84.328 L 113.789 137.914 L 86.629 137.914 Z M 86.629 84.328 M 73.828 91.793 L 105.762 91.793 L 105.762 145.379 L 73.828 145.379 Z M 73.828 91.793 M 63.16 99.262 L 90.324 99.262 L 90.324 152.848 L 63.16 152.848 Z M 63.16 99.262 M 64.953 106.727 L 79.656 106.727 L 79.656 160.313 L 64.953 160.313 Z M 64.953 106.727 M 86.629 106.727 L 96.539 106.727 L 96.539 160.313 L 86.629 160.313 Z M 86.629 106.727 M 91.031 106.727 L 96.539 106.727 L 96.539 165.273 L 91.031 165.273 Z M 91.031 106.727 M 91.031 164.766 L 106.082 164.766 L 106.082 170.273 L 91.031 170.273 Z M 91.031 179.766 M 63.301 106.727 L 68.805 106.727 L 68.805 180.59 L 63.301 180.59 Z M 63.301 106.727 M 63.301 187.086 L 78.348 187.086 L 78.348 180.59 L 63.301 180.59 Z M 63.301 165.086 M 54.285 106.727 L 68.988 106.727 L 68.988 153.566 L 54.285 153.566 Z M 54.285 106.727 M 45.695 106.727 L 58.324 106.727 L 58.324 146.301 L 45.695 146.301 Z M 45.695 106.727 M 39.293 99.25 L 49.789 99.25 L 49.789 137.766 L 39.293 137.766 Z M 39.293 99.25 M 31.828 91.781 L 42.324 91.781 L 42.324 130.301 L 31.828 130.301 Z M 31.828 91.781 M 26.59 84.316 L 32.723 84.316 L 32.723 122.832 L 26.59 122.832 Z M 26.59 84.316 "
