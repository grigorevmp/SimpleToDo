package com.grigorevmp.simpletodo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
actual fun platformSavePainter(): Painter {
    return rememberVectorPainter(Icons.Default.Save)
}
