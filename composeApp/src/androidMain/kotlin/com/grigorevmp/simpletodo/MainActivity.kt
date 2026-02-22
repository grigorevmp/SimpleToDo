package com.grigorevmp.simpletodo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.grigorevmp.simpletodo.platform.AndroidContextHolder
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import kotlinx.coroutines.delay
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidContextHolder.appContext = applicationContext
        AndroidContextHolder.currentActivity = this

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(650)
                showSplash = false
            }
            if (showSplash) {
                AppSplash()
            } else {
                App()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AndroidContextHolder.currentActivity = this
    }

    override fun onPause() {
        super.onPause()
        if (AndroidContextHolder.currentActivity === this) {
            AndroidContextHolder.currentActivity = null
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppSplash() {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) {
        androidx.compose.ui.graphics.Color(0xFF070A12)
    } else {
        androidx.compose.ui.graphics.Color(0xFFF8FAFC)
    }
    val fg = if (isDark) Color.White else Color(0xFF0B1220)
    val badge = if (isDark) Color(0xFF111827) else Color(0xFFE2E8F0)
    Surface(color = bg, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = badge,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(fg),
                        modifier = Modifier.size(96.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AtomSpinner(
                        color = fg,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = fg,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AtomSpinner(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "atom")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "atom-angle"
    )
    Canvas(modifier) {
        val stroke = Stroke(width = size.minDimension * 0.08f)
        val rx = size.minDimension * 0.42f
        val ry = size.minDimension * 0.18f
        rotate(angle) {
            drawOval(
                color = color.copy(alpha = 0.7f),
                topLeft = androidx.compose.ui.geometry.Offset(center.x - rx, center.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = stroke
            )
        }
        rotate(angle + 60f) {
            drawOval(
                color = color.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(center.x - rx, center.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = stroke
            )
        }
        rotate(angle + 120f) {
            drawOval(
                color = color.copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(center.x - rx, center.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = stroke
            )
        }
        drawCircle(
            color = color,
            radius = size.minDimension * 0.10f
        )
    }
}
