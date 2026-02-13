package com.grigorevmp.simpletodo.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.add
import simpletodo.composeapp.generated.resources.filter
import simpletodo.composeapp.generated.resources.home
import simpletodo.composeapp.generated.resources.notes
import simpletodo.composeapp.generated.resources.settings
import simpletodo.composeapp.generated.resources.tag

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun PlatformIcon(
    id: AppIconId,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier
) {
    val painter = when (id) {
        AppIconId.Home -> painterResource(Res.drawable.home)
        AppIconId.Notes -> painterResource(Res.drawable.notes)
        AppIconId.Settings -> painterResource(Res.drawable.settings)
        AppIconId.Filter -> painterResource(Res.drawable.filter)
        AppIconId.Tag -> painterResource(Res.drawable.tag)
        AppIconId.Add -> painterResource(Res.drawable.add)
    }
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
