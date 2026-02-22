package com.grigorevmp.simpletodo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
class FileExportLauncher internal constructor(
    private val launchImpl: (String) -> Unit
) {
    fun launch(text: String) = launchImpl(text)
}

@Stable
class FileImportLauncher internal constructor(
    private val launchImpl: () -> Unit
) {
    fun launch() = launchImpl()
}

@Composable
expect fun rememberFileExportLauncher(
    defaultName: String,
    onResult: (Boolean) -> Unit
): FileExportLauncher

@Composable
expect fun rememberFileImportLauncher(
    onResult: (String?) -> Unit
): FileImportLauncher
