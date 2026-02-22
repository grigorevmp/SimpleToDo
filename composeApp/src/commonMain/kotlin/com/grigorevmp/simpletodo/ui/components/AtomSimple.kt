package com.grigorevmp.simpletodo.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AtomSimpleIcon(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface
    val t by rememberInfiniteTransition(label = "atom")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Restart),
            label = "rotate"
        )

    Canvas(modifier = modifier.size(size)) {
        val r = size.toPx() / 2f
        val center = Offset(r, r)
        val orbitRadius = r * 0.72f
        val stroke = r * 0.08f

        repeat(3) { i ->
            val angle = (t + i * 60f) * (PI / 180f)
            val dx = orbitRadius * 0.7f * cos(angle).toFloat()
            val dy = orbitRadius * sin(angle).toFloat()
            drawOval(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(center.x - orbitRadius, center.y - orbitRadius * 0.6f),
                size = androidx.compose.ui.geometry.Size(orbitRadius * 2f, orbitRadius * 1.2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawCircle(
                color = color,
                radius = stroke * 0.9f,
                center = Offset(center.x + dx, center.y + dy)
            )
        }

        drawCircle(color = color, radius = stroke * 1.1f, center = center)
    }
}

@Composable
fun AtomSpinnerIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface
    val transition = rememberInfiniteTransition(label = "atom-spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "atom-spinner-angle"
    )
    Canvas(modifier = modifier.size(size)) {
        val minDim = min(this.size.width, this.size.height)
        val stroke = Stroke(width = minDim * 0.08f)
        val rx = minDim * 0.42f
        val ry = minDim * 0.18f
        rotate(angle) {
            drawOval(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(center.x - rx, center.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = stroke
            )
        }
        rotate(angle + 60f) {
            drawOval(
                color = color.copy(alpha = 0.6f),
                topLeft = Offset(center.x - rx, center.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = stroke
            )
        }
        rotate(angle + 120f) {
            drawOval(
                color = color.copy(alpha = 0.5f),
                topLeft = Offset(center.x - rx, center.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = stroke
            )
        }
        drawCircle(
            color = color,
            radius = minDim * 0.10f
        )
    }
}
