package com.grigorevmp.simpletodo.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.grigorevmp.simpletodo.model.TodoTask
import com.russhwolf.settings.Settings
import kotlinx.datetime.Instant

expect fun createPlatformSettings(): Settings

interface NotificationScheduler {
    fun schedule(task: TodoTask, leadMinutes: Int)
    fun cancel(taskId: String)
    fun cancelAll()
}

expect fun createNotificationScheduler(): NotificationScheduler

@Composable
expect fun NotificationPermissionGate(remindersEnabled: Boolean)

@Composable
expect fun PlatformDateTimePicker(
    current: Instant?,
    onPicked: (Instant?) -> Unit
)

@Composable
expect fun PlatformSystemBars(
    isDark: Boolean,
    backgroundColor: Color
)
