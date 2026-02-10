package com.grigorevmp.simpletodo.platform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
actual fun NotificationPermissionGate(remindersEnabled: Boolean) {
    if (!remindersEnabled) return

    if (Build.VERSION.SDK_INT < 33) {
        Text(
            "Android < 13 does not require permission.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val ctx = AndroidContextHolder.appContext
    val granted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        granted.value = ok
    }

    if (!granted.value) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Text("Notification permission needed", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Allow notifications so reminders arrive on time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Allow") }
            }
        }
    } else {
        Text(
            "Notification permission granted.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
