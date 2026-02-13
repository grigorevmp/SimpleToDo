package com.grigorevmp.simpletodo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

enum class AppIconId {
    Home,
    Notes,
    Settings,
    Filter,
    Tag,
    Add
}

@Composable
expect fun PlatformIcon(
    id: AppIconId,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier
)
