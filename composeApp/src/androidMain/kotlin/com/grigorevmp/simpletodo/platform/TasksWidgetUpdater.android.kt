package com.grigorevmp.simpletodo.platform

import androidx.glance.appwidget.updateAll
import com.grigorevmp.simpletodo.widget.TasksWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

actual fun requestTasksWidgetUpdate() {
    val context = AndroidContextHolder.appContext
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        TasksWidget().updateAll(context)
    }
}
