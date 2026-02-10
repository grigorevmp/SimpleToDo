package com.grigorevmp.simpletodo.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.grigorevmp.simpletodo.model.TodoTask
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit
import kotlin.math.max

private const val CHANNEL_ID = "simpletodo_reminders"
private const val CHANNEL_NAME = "Simple TODO reminders"
private const val TAG_ALL = "simpletodo_all"
private const val TYPE_PLANNED = "planned"
private const val TYPE_DEADLINE = "deadline"

actual fun createNotificationScheduler(): NotificationScheduler {
    return AndroidNotificationScheduler(AndroidContextHolder.appContext)
}

private class AndroidNotificationScheduler(
    private val ctx: Context
) : NotificationScheduler {

    private val wm by lazy { WorkManager.getInstance(ctx) }

    override fun schedule(task: TodoTask, leadMinutes: Int) {
        ensureChannel(ctx)

        val now = Clock.System.now().toEpochMilliseconds()

        // Planned time notification (at planned time)
        task.plannedAt?.let { planned ->
            val delayMs = max(0L, planned.toEpochMilliseconds() - now)
            val data = workDataOf(
                ReminderWorker.KEY_TASK_ID to task.id,
                ReminderWorker.KEY_TITLE to task.title,
                ReminderWorker.KEY_PLAN to task.plan,
                ReminderWorker.KEY_TYPE to TYPE_PLANNED
            )
            val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tagFor(task.id))
                .addTag(TAG_ALL)
                .build()
            wm.enqueueUniqueWork(uniqueName(task.id, TYPE_PLANNED), ExistingWorkPolicy.REPLACE, req)
        }

        // Deadline notification (lead time before deadline)
        task.deadline?.let { deadline ->
            val remindAt = deadline.toEpochMilliseconds() - leadMinutes.toLong() * 60_000L
            val delayMs = max(0L, remindAt - now)
            val data = workDataOf(
                ReminderWorker.KEY_TASK_ID to task.id,
                ReminderWorker.KEY_TITLE to task.title,
                ReminderWorker.KEY_PLAN to task.plan,
                ReminderWorker.KEY_TYPE to TYPE_DEADLINE
            )
            val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tagFor(task.id))
                .addTag(TAG_ALL)
                .build()
            wm.enqueueUniqueWork(uniqueName(task.id, TYPE_DEADLINE), ExistingWorkPolicy.REPLACE, req)
        }
    }

    override fun cancel(taskId: String) {
        wm.cancelUniqueWork(uniqueName(taskId, TYPE_PLANNED))
        wm.cancelUniqueWork(uniqueName(taskId, TYPE_DEADLINE))
    }

    override fun cancelAll() {
        wm.cancelAllWorkByTag(TAG_ALL)
    }

    private fun uniqueName(taskId: String, type: String) = "simpletodo_reminder_${type}_$taskId"
    private fun tagFor(taskId: String) = "simpletodo_tag_$taskId"
}

private fun ensureChannel(ctx: Context) {
    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val existing = nm.getNotificationChannel(CHANNEL_ID)
    if (existing != null) return

    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Task reminders"
        }
    )
}

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        ensureChannel(applicationContext)

        val id = inputData.getString(KEY_TASK_ID) ?: return Result.success()
        val title = inputData.getString(KEY_TITLE) ?: "Task"
        val plan = inputData.getString(KEY_PLAN) ?: ""
        val type = inputData.getString(KEY_TYPE) ?: TYPE_DEADLINE

        val prefix = if (type == TYPE_PLANNED) "Planned time" else "Deadline"
        val text = if (plan.isBlank()) "$prefix — check this task" else "$prefix — $plan"

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notif)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TITLE = "title"
        const val KEY_PLAN = "plan"
        const val KEY_TYPE = "type"
    }
}
