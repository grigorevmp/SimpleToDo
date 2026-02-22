package com.grigorevmp.simpletodo.platform

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
actual fun rememberFileExportLauncher(
    defaultName: String,
    onResult: (Boolean) -> Unit
): FileExportLauncher {
    val context = AndroidContextHolder.appContext
    var pendingText by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val text = pendingText
        pendingText = null
        if (uri == null || text == null) {
            onResult(false)
            return@rememberLauncherForActivityResult
        }
        onResult(writeText(context, uri, text))
    }
    return remember(defaultName, launcher) {
        FileExportLauncher { text ->
            pendingText = text
            launcher.launch(defaultName)
        }
    }
}

@Composable
actual fun rememberFileImportLauncher(
    onResult: (String?) -> Unit
): FileImportLauncher {
    val context = AndroidContextHolder.appContext
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        onResult(readText(context, uri))
    }
    return remember(launcher) {
        FileImportLauncher {
            launcher.launch(arrayOf("application/json", "text/plain"))
        }
    }
}

private fun writeText(
    context: Context,
    uri: Uri,
    text: String
): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(text.toByteArray())
            output.flush()
        } ?: return false
        true
    } catch (_: Exception) {
        false
    }
}

private fun readText(
    context: Context,
    uri: Uri
): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
    } catch (_: Exception) {
        null
    }
}
