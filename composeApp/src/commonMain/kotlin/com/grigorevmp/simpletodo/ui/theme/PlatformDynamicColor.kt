package com.grigorevmp.simpletodo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun platformDynamicColorScheme(dark: Boolean): ColorScheme?
