package com.grigorevmp.simpletodo.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun PlatformIcon(
    id: AppIconId,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier
) {
    val imageVector = when (id) {
        AppIconId.Home -> HomeIcon
        AppIconId.Notes -> NotesIcon
        AppIconId.Settings -> SettingsIcon
        AppIconId.Filter -> FilterIcon
        AppIconId.Tag -> TagIcon
        AppIconId.Add -> AddIcon
    }
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
