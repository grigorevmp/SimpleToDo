package com.grigorevmp.simpletodo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.save

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun platformSavePainter(): Painter {
    return painterResource(Res.drawable.save)
}
