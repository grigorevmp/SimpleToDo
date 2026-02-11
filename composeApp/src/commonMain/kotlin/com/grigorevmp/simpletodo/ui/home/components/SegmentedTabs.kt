package com.grigorevmp.simpletodo.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy


@Composable
fun SegmentedTabs(
    leftSelected: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fallbackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val active = MaterialTheme.colorScheme.primary
    val activeText = MaterialTheme.colorScheme.onPrimary
    val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.large)
                    .background(fallbackColor, RoundedCornerShape(64.dp))
            )

            Row {
                SegmentedButton(
                    text = "Timeline",
                    selected = leftSelected,
                    active = active,
                    activeText = activeText,
                    inactiveText = inactiveText,
                    onClick = onLeft
                )

                SegmentedButton(
                    text = "Inbox",
                    selected = !leftSelected,
                    active = active,
                    activeText = activeText,
                    inactiveText = inactiveText,
                    onClick = onRight
                )
            }
        }
    }
}


@Composable
fun SegmentedButton(
    text: String,
    selected: Boolean,
    active: Color,
    activeText: Color,
    inactiveText: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = if (selected) active else Color.Transparent
    ) {
        Box(
            Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = if (selected) activeText else inactiveText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
