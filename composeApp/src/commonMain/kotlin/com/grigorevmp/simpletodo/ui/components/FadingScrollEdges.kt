package com.grigorevmp.simpletodo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width

@Composable
fun FadingScrollEdges(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    color: Color = MaterialTheme.colorScheme.background,
    maxAlpha: Float = 0.95f,
    enabled: Boolean = true
) {
    if (!enabled) return
    val showTop by remember(scrollState) {
        derivedStateOf { scrollState.value > 0 }
    }
    val showBottom by remember(scrollState) {
        derivedStateOf { scrollState.value < scrollState.maxValue }
    }

    FadingScrollEdges(
        showTop = showTop,
        showBottom = showBottom,
        modifier = modifier,
        height = height,
        color = color,
        maxAlpha = maxAlpha
    )
}

@Composable
fun FadingScrollEdges(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    color: Color = MaterialTheme.colorScheme.background,
    maxAlpha: Float = 0.95f,
    enabled: Boolean = true
) {
    if (!enabled) return
    val showTop by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val showBottom by remember(listState) {
        derivedStateOf {
            val layout = listState.layoutInfo
            val total = layout.totalItemsCount
            if (total == 0) return@derivedStateOf false
            val last = layout.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            val lastIndex = last.index
            if (lastIndex < total - 1) return@derivedStateOf true
            val viewportEnd = layout.viewportEndOffset - layout.beforeContentPadding
            last.offset + last.size > viewportEnd
        }
    }

    FadingScrollEdges(
        showTop = showTop,
        showBottom = showBottom,
        modifier = modifier,
        height = height,
        color = color,
        maxAlpha = maxAlpha
    )
}

@Composable
fun FadingScrollEdgesHorizontal(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    width: Dp = 36.dp,
    color: Color = MaterialTheme.colorScheme.background,
    maxAlpha: Float = 0.95f,
    enabled: Boolean = true
) {
    if (!enabled) return
    val showStart by remember(scrollState) {
        derivedStateOf { scrollState.value > 0 }
    }
    val showEnd by remember(scrollState) {
        derivedStateOf { scrollState.value < scrollState.maxValue }
    }

    FadingScrollEdgesHorizontal(
        showStart = showStart,
        showEnd = showEnd,
        modifier = modifier,
        width = width,
        color = color,
        maxAlpha = maxAlpha
    )
}

@Composable
private fun FadingScrollEdges(
    showTop: Boolean,
    showBottom: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    color: Color = MaterialTheme.colorScheme.background,
    maxAlpha: Float = 0.95f
) {
    val topAlpha by animateFloatAsState(
        targetValue = if (showTop) 1f else 0f,
        label = "fade-top"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (showBottom) 1f else 0f,
        label = "fade-bottom"
    )

    Box(modifier = modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = maxAlpha * topAlpha),
                            color.copy(alpha = maxAlpha * topAlpha * 0.35f),
                            color.copy(alpha = 0f)
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0f),
                            color.copy(alpha = maxAlpha * bottomAlpha * 0.35f),
                            color.copy(alpha = maxAlpha * bottomAlpha)
                        )
                    )
                )
        )
    }
}

@Composable
private fun FadingScrollEdgesHorizontal(
    showStart: Boolean,
    showEnd: Boolean,
    modifier: Modifier = Modifier,
    width: Dp = 36.dp,
    color: Color = MaterialTheme.colorScheme.background,
    maxAlpha: Float = 0.95f
) {
    val startAlpha by animateFloatAsState(
        targetValue = if (showStart) 1f else 0f,
        label = "fade-start"
    )
    val endAlpha by animateFloatAsState(
        targetValue = if (showEnd) 1f else 0f,
        label = "fade-end"
    )

    Box(modifier = modifier) {
        Box(
            Modifier
                .fillMaxHeight()
                .width(width)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            color.copy(alpha = maxAlpha * startAlpha),
                            color.copy(alpha = maxAlpha * startAlpha * 0.35f),
                            color.copy(alpha = 0f)
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxHeight()
                .width(width)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            color.copy(alpha = 0f),
                            color.copy(alpha = maxAlpha * endAlpha * 0.35f),
                            color.copy(alpha = maxAlpha * endAlpha)
                        )
                    )
                )
        )
    }
}
