package com.grigorevmp.simpletodo.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFileExportLauncher(
    defaultName: String,
    onResult: (Boolean) -> Unit
): FileExportLauncher {
    return FileExportLauncher { onResult(false) }
}

@Composable
actual fun rememberFileImportLauncher(
    onResult: (String?) -> Unit
): FileImportLauncher {
    return FileImportLauncher { onResult(null) }
}
