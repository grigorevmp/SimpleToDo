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
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.notifications_android_legacy
import simpletodo.composeapp.generated.resources.notifications_allow
import simpletodo.composeapp.generated.resources.notifications_allow_body
import simpletodo.composeapp.generated.resources.notifications_allow_title

@Composable
actual fun NotificationPermissionGate(remindersEnabled: Boolean) {
    if (!remindersEnabled) return

    if (Build.VERSION.SDK_INT < 33) {
        Text(
            stringResource(Res.string.notifications_android_legacy),
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
                Text(stringResource(Res.string.notifications_allow_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(Res.string.notifications_allow_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text(stringResource(Res.string.notifications_allow)) }
            }
        }
    }
}
