package com.grigorevmp.simpletodo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
actual fun PlatformBottomBar(
    tab: AppTab,
    onTab: (AppTab) -> Unit,
    createActions: List<CreateAction>,
    enableEffects: Boolean,
    backdrop: LayerBackdrop,
    modifier: Modifier
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val shape = RoundedCornerShape(26.dp)
    val glassColor = MaterialTheme.colorScheme.surfaceVariant
    val glassBrush = Brush.verticalGradient(
        listOf(
            glassColor.copy(alpha = 0.08f),
            glassColor.copy(alpha = 0.24f)
        )
    )
    val isDarkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassOverlay = if (isDarkSurface) {
        Color.Black.copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }
    val density = LocalDensity.current
    val blurPx = with(density) { 10.dp.toPx() }
    val lensInnerPx = with(density) { 18.dp.toPx() }
    val lensOuterPx = with(density) { 40.dp.toPx() }
    val items = listOf(
        IosTabItem(
            id = "home",
            label = "Home",
            selected = tab == AppTab.HOME,
            icon = AppIconId.Home,
            onClick = { onTab(AppTab.HOME) }
        ),
        IosTabItem(
            id = "notes",
            label = "Notes",
            selected = tab == AppTab.NOTES,
            icon = AppIconId.Notes,
            onClick = { onTab(AppTab.NOTES) }
        ),
        IosTabItem(
            id = "settings",
            label = "Settings",
            selected = tab == AppTab.SETTINGS,
            icon = AppIconId.Settings,
            onClick = { onTab(AppTab.SETTINGS) }
        )
    )

    Surface(
        modifier = modifier,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .then(
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                vibrancy()
                                blur(blurPx)
                                lens(lensInnerPx, lensOuterPx)
                            },
                            onDrawSurface = {
                                drawRect(glassBrush)
                                drawRect(glassOverlay)
                            }
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    IosTabButton(item = item)
                }

                if (createActions.isNotEmpty()) {
                    Spacer(Modifier.width(2.dp))
                    IosActionButton(action = createActions.first())
                }
            }
        }
    }
}

private data class IosTabItem(
    val id: String,
    val label: String,
    val selected: Boolean,
    val icon: AppIconId,
    val onClick: () -> Unit
)

@Composable
private fun RowScope.IosTabButton(item: IosTabItem) {
    val fg = if (item.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressAlpha by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(durationMillis = 140),
        label = "ios-tab-press"
    )
    val isDarkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassBase = if (isDarkSurface) Color.White else Color.Black
    val glassBrush = Brush.verticalGradient(
        listOf(
            glassBase.copy(alpha = 0.18f * pressAlpha),
            glassBase.copy(alpha = 0.06f * pressAlpha)
        )
    )
    val selectedAlpha by animateFloatAsState(
        targetValue = if (item.selected) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "ios-tab-selected"
    )
    val selectedBrush = Brush.verticalGradient(
        listOf(
            glassBase.copy(alpha = 0.16f * selectedAlpha),
            glassBase.copy(alpha = 0.05f * selectedAlpha)
        )
    )
    val shape = RoundedCornerShape(18.dp)
    val border = glassBase.copy(alpha = 0.12f * maxOf(pressAlpha, selectedAlpha))

    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = item.onClick
            )
    ) {
        if (selectedAlpha > 0f) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(selectedBrush)
                    .border(width = 1.dp, color = border, shape = shape)
            )
        }
        if (pressAlpha > 0f) {
            Box(Modifier.matchParentSize().background(glassBrush))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PlatformIcon(
                id = item.icon,
                contentDescription = item.label,
                tint = fg,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = item.label,
                color = fg,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IosActionButton(action: CreateAction) {
    val bg = MaterialTheme.colorScheme.primary
    val fg = MaterialTheme.colorScheme.onPrimary
    Surface(
        onClick = action.onClick,
        color = bg,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .width(72.dp)
            .height(52.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PlatformIcon(
                id = AppIconId.Add,
                contentDescription = action.contentDescription,
                tint = fg,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = action.label,
                color = fg,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
