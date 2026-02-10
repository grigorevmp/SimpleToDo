package com.grigorevmp.simpletodo.platform

import androidx.compose.runtime.Composable

@Composable
actual fun NotificationPermissionGate(remindersEnabled: Boolean) {
    // iOS: no-op in shared UI for now.
}
