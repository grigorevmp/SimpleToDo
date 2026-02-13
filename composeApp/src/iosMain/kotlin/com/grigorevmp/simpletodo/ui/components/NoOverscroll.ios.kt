package com.grigorevmp.simpletodo.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun NoOverscroll(content: @Composable () -> Unit) {
    content()
}
