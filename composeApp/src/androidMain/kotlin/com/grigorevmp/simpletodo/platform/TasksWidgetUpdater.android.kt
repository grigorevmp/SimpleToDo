package com.grigorevmp.simpletodo.platform

import androidx.glance.appwidget.updateAll
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.grigorevmp.simpletodo.widget.PinnedTasksWidget
import com.grigorevmp.simpletodo.widget.PinnedTasksWidgetReceiver
import com.grigorevmp.simpletodo.widget.TasksWidget
import com.grigorevmp.simpletodo.widget.TasksWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual fun requestTasksWidgetUpdate() {
    val context = AndroidContextHolder.appContext
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        delay(150)
        TasksWidget().updateAll(context)
        PinnedTasksWidget().updateAll(context)
        forceGlanceUpdate(context, TasksWidget())
        forceGlanceUpdate(context, PinnedTasksWidget())
        forceAppWidgetUpdate(context, TasksWidgetReceiver::class.java)
        forceAppWidgetUpdate(context, PinnedTasksWidgetReceiver::class.java)
        delay(600)
        TasksWidget().updateAll(context)
        PinnedTasksWidget().updateAll(context)
        forceGlanceUpdate(context, TasksWidget())
        forceGlanceUpdate(context, PinnedTasksWidget())
        forceAppWidgetUpdate(context, TasksWidgetReceiver::class.java)
        forceAppWidgetUpdate(context, PinnedTasksWidgetReceiver::class.java)
    }
}

private suspend fun forceGlanceUpdate(context: Context, widget: GlanceAppWidget) {
    val manager = GlanceAppWidgetManager(context)
    val ids = manager.getGlanceIds(widget::class.java)
    ids.forEach { widget.update(context, it) }
}

private fun forceAppWidgetUpdate(context: android.content.Context, receiver: Class<*>) {
    val manager = AppWidgetManager.getInstance(context)
    val component = ComponentName(context, receiver)
    val ids = manager.getAppWidgetIds(component)
    if (ids.isEmpty()) return
    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
        this.component = component
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    }
    context.sendBroadcast(intent)
}
