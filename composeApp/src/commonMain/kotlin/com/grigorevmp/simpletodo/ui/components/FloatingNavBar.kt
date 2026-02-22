package com.grigorevmp.simpletodo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.nav_home
import simpletodo.composeapp.generated.resources.nav_notes
import simpletodo.composeapp.generated.resources.nav_settings

enum class AppTab { HOME, NOTES, SETTINGS }

data class CreateAction(
    val id: String,
    val label: String,
    val contentDescription: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun FloatingNavBar(
    tab: AppTab,
    onTab: (AppTab) -> Unit,
    createActions: List<CreateAction>,
    backdrop: LayerBackdrop,
    enableEffects: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(48.dp)
    val glassColor = MaterialTheme.colorScheme.surfaceVariant
    val glassBrush = Brush.verticalGradient(
        listOf(
            glassColor.copy(alpha = 0.06f),
            glassColor.copy(alpha = 0.18f)
        )
    )
    val fallbackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val isDarkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassOverlay = if (isDarkSurface) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }
    val density = LocalDensity.current
    val blurPx = with(density) { 2.dp.toPx() }
    val lensInnerPx = with(density) { 16.dp.toPx() }
    val lensOuterPx = with(density) { 32.dp.toPx() }

    Surface(
        modifier = modifier
            .wrapContentWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = 520f))
            .padding(bottom = 8.dp)
            .padding(horizontal = 4.dp),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
    ) {
        Box(
            Modifier
                .height(72.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .then(
                        if (enableEffects) {
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
                        } else {
                            Modifier.background(fallbackColor, shape)
                        }
                    )
            )

            Row(
                Modifier
                    .wrapContentWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = 520f)),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavButton(
                    selected = tab == AppTab.HOME,
                    label = stringResource(Res.string.nav_home),
                    onClick = { onTab(AppTab.HOME) },
                    icon = {
                        Icon(
                            HomeIcon,
                            contentDescription = stringResource(Res.string.nav_home),
                            tint = if (tab == AppTab.HOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                )

                NavButton(
                    selected = tab == AppTab.NOTES,
                    label = stringResource(Res.string.nav_notes),
                    onClick = { onTab(AppTab.NOTES) },
                    icon = {
                        Icon(
                            NotesIcon,
                            contentDescription = stringResource(Res.string.nav_notes),
                            tint = if (tab == AppTab.NOTES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                )

                NavButton(
                    selected = tab == AppTab.SETTINGS,
                    label = stringResource(Res.string.nav_settings),
                    onClick = { onTab(AppTab.SETTINGS) },
                    icon = {
                        Icon(
                            SettingsIcon,
                            contentDescription = stringResource(Res.string.nav_settings),
                            tint = if (tab == AppTab.SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                )

                createActions.forEach { action ->
                    AnimatedVisibility(
                        visible = createActions.isNotEmpty(),
                        enter = scaleIn(animationSpec = tween(durationMillis = 200)),
                        exit = scaleOut(animationSpec = tween(durationMillis = 200)),
                    ) {
                        CreateActionButton(action = action)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val active by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "nav-active"
    )
    val bg = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f * active)
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val scale = 0.98f + 0.02f * active

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(48.dp),
        color = bg,
        modifier = Modifier
            .width(72.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
//            Spacer(Modifier.height(4.dp))
//            Text(
//                label,
//                color = fg,
//                style = MaterialTheme.typography.labelLarge,
//                maxLines = 1
//            )
        }
    }
}

@Composable
private fun CreateActionButton(action: CreateAction) {
    Surface(
        onClick = action.onClick,
        shape = RoundedCornerShape(48.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                action.icon,
                contentDescription = action.contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
//            Spacer(Modifier.height(4.dp))
//            Text(
//                action.label,
//                color = MaterialTheme.colorScheme.onPrimary,
//                style = MaterialTheme.typography.labelLarge
//            )
        }
    }
}
