package com.grigorevmp.simpletodo.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.grigorevmp.simpletodo.R
import com.grigorevmp.simpletodo.data.InstantAsStringSerializer
import com.grigorevmp.simpletodo.model.AppPrefs
import com.grigorevmp.simpletodo.model.TodoTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

private const val PINNED_CHANNEL_ID = "simpletodo_pinned"
private const val PINNED_CHANNEL_NAME = "Simple TODO pinned tasks"
private const val PINNED_NOTIFICATION_ID = 2201
private const val PREFS_NAME = "simpletodo_prefs"
private const val TASKS_KEY = "tasks_json_v1"
private const val PREFS_KEY = "prefs_json_v1"
private const val ACTION_REPOST_PINNED = "com.grigorevmp.simpletodo.action.REPOST_PINNED"

actual fun requestPinnedTasksNotificationUpdate() {
    val context = AndroidContextHolder.appContext
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        updatePinnedNotification(context)
    }
}

internal fun updatePinnedNotification(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val prefsRaw = prefs.getString(PREFS_KEY, null)
    val tasksRaw = prefs.getString(TASKS_KEY, null)
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantAsStringSerializer)
        }
    }
    val appPrefs = runCatching { prefsRaw?.let { json.decodeFromString(AppPrefs.serializer(), it) } }
        .getOrNull() ?: AppPrefs()
    if (!appPrefs.pinPinnedInNotifications) {
        NotificationManagerCompat.from(context).cancel(PINNED_NOTIFICATION_ID)
        return
    }
    if (tasksRaw == null) {
        NotificationManagerCompat.from(context).cancel(PINNED_NOTIFICATION_ID)
        return
    }
    val tasks = runCatching { json.decodeFromString(ListSerializer(TodoTask.serializer()), tasksRaw) }
        .getOrElse { emptyList() }
    val pinned = tasks.filter { it.pinned && !it.done }
    if (pinned.isEmpty()) {
        NotificationManagerCompat.from(context).cancel(PINNED_NOTIFICATION_ID)
        return
    }

    ensurePinnedChannel(context)

    val title = context.getString(R.string.pinned_notification_title)
    val untitled = context.getString(R.string.widget_untitled)
    val style = NotificationCompat.InboxStyle()
    pinned.take(6).forEach { task ->
        style.addLine(task.title.ifBlank { untitled })
    }
    if (pinned.size > 6) {
        style.addLine(context.getString(R.string.pinned_notification_more, pinned.size - 6))
    }

    val deleteIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, PinnedTasksNotificationReceiver::class.java).setAction(ACTION_REPOST_PINNED),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notif = NotificationCompat.Builder(context, PINNED_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_atom)
        .setContentTitle(title)
        .setStyle(style)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setAutoCancel(false)
        .setShowWhen(false)
        .setSilent(true)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setDeleteIntent(deleteIntent)
        .build()

    NotificationManagerCompat.from(context).notify(PINNED_NOTIFICATION_ID, notif)
}

private fun ensurePinnedChannel(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val existing = nm.getNotificationChannel(PINNED_CHANNEL_ID)
    if (existing != null) return
    nm.createNotificationChannel(
        NotificationChannel(PINNED_CHANNEL_ID, PINNED_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
            description = "Pinned tasks list"
            setSound(null, null)
            enableVibration(false)
        }
    )
}

class PinnedTasksNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_REPOST_PINNED) {
            updatePinnedNotification(context.applicationContext)
        }
    }
}
