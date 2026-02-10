package com.grigorevmp.simpletodo.platform

import com.grigorevmp.simpletodo.model.TodoTask

actual fun createNotificationScheduler(): NotificationScheduler {
    return object : NotificationScheduler {
        override fun schedule(task: TodoTask, leadMinutes: Int) {}
        override fun cancel(taskId: String) {}
        override fun cancelAll() {}
    }
}
