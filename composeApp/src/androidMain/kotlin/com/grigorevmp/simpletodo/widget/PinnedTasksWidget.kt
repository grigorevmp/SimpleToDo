package com.grigorevmp.simpletodo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.grigorevmp.simpletodo.MainActivity
import com.grigorevmp.simpletodo.R
import com.grigorevmp.simpletodo.data.InstantAsStringSerializer
import com.grigorevmp.simpletodo.model.AppPrefs
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.SortDir
import com.grigorevmp.simpletodo.model.SortField
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.util.nowInstant
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

private const val PREFS_NAME = "simpletodo_prefs"
private const val TASKS_KEY = "tasks_json_v1"
private const val PREFS_KEY = "prefs_json_v1"

class PinnedTasksWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        provideContent {
            val strings = PinnedWidgetStrings.from(context)
            val tasks = loadPinnedTasks(context)
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(PinnedWidgetColors.card)
                    .cornerRadius(24.dp)
                    .padding(14.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                PinnedTasksContent(tasks = tasks, strings = strings)
            }
        }
    }
}

class PinnedTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PinnedTasksWidget()
}

@Composable
private fun PinnedTasksContent(tasks: List<TodoTask>, strings: PinnedWidgetStrings) {
    if (tasks.isEmpty()) {
        Text(
            text = strings.empty,
            style = TextStyle(fontSize = 12.sp, color = PinnedWidgetColors.secondaryText)
        )
        return
    }
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = strings.title,
            style = TextStyle(fontSize = 12.sp, color = PinnedWidgetColors.tertiaryText)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title.ifBlank { strings.untitled },
                        style = TextStyle(fontSize = 12.sp, color = PinnedWidgetColors.primaryText)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    DoneButton(task.id)
                }
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}

private data class PinnedWidgetStrings(
    val title: String,
    val empty: String,
    val untitled: String
) {
    companion object {
        fun from(context: Context): PinnedWidgetStrings = PinnedWidgetStrings(
            title = context.getString(R.string.pinned_notification_title),
            empty = context.getString(R.string.widget_pinned_empty),
            untitled = context.getString(R.string.widget_untitled)
        )
    }
}

private fun loadPinnedTasks(context: Context): List<TodoTask> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(TASKS_KEY, null) ?: return emptyList()
    val prefsRaw = prefs.getString(PREFS_KEY, null)
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantAsStringSerializer)
        }
    }
    val tasks = runCatching { json.decodeFromString(ListSerializer(TodoTask.serializer()), raw) }
        .getOrElse { emptyList() }
    val appPrefs = runCatching { prefsRaw?.let { json.decodeFromString(AppPrefs.serializer(), it) } }
        .getOrNull() ?: AppPrefs()
    return sortedTasks(tasks, appPrefs).filter { it.pinned && !it.done }
}

private fun sortedTasks(tasks: List<TodoTask>, prefs: AppPrefs): List<TodoTask> {
    val sort = prefs.sort
    val pinnedCmp = compareBy<TodoTask> { if (it.pinned) 0 else 1 }
    val doneCmp = compareBy<TodoTask> { it.done }
    val now = nowInstant().toEpochMilliseconds()

    if (sort.primary == SortField.PLANNED_AT && sort.secondary == SortField.DEADLINE) {
        val overdueCmp = compareBy<TodoTask> { if (isOverdue(it, now)) 0 else 1 }
        val plannedCmp = compareBy<TodoTask> { plannedSortKey(it, now) }
        val deadlineCmp = compareBy<TodoTask> { it.deadline?.toEpochMilliseconds() ?: Long.MAX_VALUE }

        val primaryCmp = if (sort.primaryDir == SortDir.ASC) plannedCmp else plannedCmp.reversed()
        val secondaryCmp = if (sort.secondaryDir == SortDir.ASC) deadlineCmp else deadlineCmp.reversed()

        return tasks.sortedWith(pinnedCmp.then(doneCmp).then(overdueCmp).then(primaryCmp).then(secondaryCmp))
    }

    val cmp = compareBy<TodoTask> { sortKey(it, sort.primary) }
    val primaryCmp = if (sort.primaryDir == SortDir.ASC) cmp else cmp.reversed()

    val cmp2 = compareBy<TodoTask> { sortKey(it, sort.secondary) }
    val secondaryCmp = if (sort.secondaryDir == SortDir.ASC) cmp2 else cmp2.reversed()

    return tasks.sortedWith(pinnedCmp.then(doneCmp).then(primaryCmp).then(secondaryCmp))
}

private fun plannedSortKey(task: TodoTask, nowMs: Long): Long {
    val planned = task.plannedAt?.toEpochMilliseconds()
    val deadline = task.deadline?.toEpochMilliseconds()

    return when {
        planned == null && deadline != null -> deadline
        planned == null -> Long.MAX_VALUE
        planned < nowMs && deadline != null -> deadline
        else -> planned
    }
}

private fun isOverdue(task: TodoTask, nowMs: Long): Boolean {
    val planned = task.plannedAt?.toEpochMilliseconds()
    val deadline = task.deadline?.toEpochMilliseconds()
    return (deadline != null && deadline < nowMs) || (planned != null && planned < nowMs && deadline == null)
}

private fun sortKey(task: TodoTask, field: SortField): Comparable<*> {
    return when (field) {
        SortField.PLANNED_AT -> task.plannedAt?.toEpochMilliseconds() ?: Long.MAX_VALUE
        SortField.DEADLINE -> task.deadline?.toEpochMilliseconds() ?: Long.MAX_VALUE
        SortField.IMPORTANCE -> importanceRank(task.importance)
        SortField.CREATED_AT -> -task.createdAt.toEpochMilliseconds()
        SortField.TITLE -> task.title.lowercase()
    }
}

private fun importanceRank(i: Importance): Int = when (i) {
    Importance.LOW -> 0
    Importance.NORMAL -> 1
    Importance.HIGH -> 2
    Importance.CRITICAL -> 3
}

private object PinnedWidgetColors {
    val card = ColorProvider(
        day = Color(0xFFF4F6F8),
        night = Color(0xFF111418)
    )
    val primaryText = ColorProvider(
        day = Color(0xFF0B0C0F),
        night = Color(0xFFF5F7FB)
    )
    val secondaryText = ColorProvider(
        day = Color(0xFF4A5568),
        night = Color(0xFFB7C1D1)
    )
    val tertiaryText = ColorProvider(
        day = Color(0xFF7A8797),
        night = Color(0xFF90A0B6)
    )
}

@Preview
@Composable
private fun PinnedTasksWidgetPreview() {
    val tasks = listOf(
        TodoTask(
            id = "1",
            title = "Pinned task",
            plan = "",
            subtasks = emptyList(),
            createdAt = nowInstant(),
            pinned = true
        )
    )
    PinnedTasksContent(tasks = tasks, strings = PinnedWidgetStrings("Pinned tasks", "No pinned tasks", "(untitled)"))
}
