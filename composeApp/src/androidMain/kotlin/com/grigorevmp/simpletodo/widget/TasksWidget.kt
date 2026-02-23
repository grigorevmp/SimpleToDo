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
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityWidget
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import android.appwidget.AppWidgetManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.content.edit

private const val PREFS_NAME = "simpletodo_prefs"
private const val TASKS_KEY = "tasks_json_v1"
private const val PREFS_KEY = "prefs_json_v1"
private const val DAYS_AHEAD = 7
private const val EXTRA_TASK_ID = "extra_task_id"

class TasksWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val widgetSize = readWidgetSize(context, id)
        val layout = resolveLayout(widgetSize)
        val spec = layoutSpec(layout)

        provideContent {
            val strings = WidgetStrings.from(context)
            val tasks = loadUpcomingTasks(context, strings)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.card)
                    .cornerRadius(24.dp)
                    .padding(spec.padding)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                when (layout) {
                    WidgetLayout.Small -> SmallContent(tasks, strings, spec)
                    WidgetLayout.Medium -> MediumContent(tasks, strings, spec)
                    WidgetLayout.Large -> LargeContent(tasks, strings, spec)
                }
            }
        }
    }
}

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksWidget()
}

private enum class WidgetLayout { Small, Medium, Large }

@Composable
private fun SmallContent(tasks: List<WidgetTask>, strings: WidgetStrings, spec: WidgetLayoutSpec) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        TaskList(tasks = tasks, maxItems = 2, emptyLabel = strings.empty, spec = spec)
    }
}

@Composable
private fun MediumContent(tasks: List<WidgetTask>, strings: WidgetStrings, spec: WidgetLayoutSpec) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        TaskList(tasks = tasks, maxItems = 4, emptyLabel = strings.empty, spec = spec)
    }
}

@Composable
private fun LargeContent(tasks: List<WidgetTask>, strings: WidgetStrings, spec: WidgetLayoutSpec) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        TaskList(tasks = tasks, maxItems = 7, emptyLabel = strings.empty, spec = spec)
    }
}

@Composable
private fun TaskList(
    tasks: List<WidgetTask>,
    maxItems: Int,
    emptyLabel: String,
    spec: WidgetLayoutSpec
) {
    val visible = tasks.take(maxItems)
    if (visible.isEmpty()) {
        Text(
            text = emptyLabel,
            style = TextStyle(fontSize = spec.bodySize, color = WidgetColors.secondaryText)
        )
        return
    }

    Column {
        visible.forEachIndexed { index, task ->
            if (index > 0) {
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = task.dateLabel,
                    style = TextStyle(fontSize = spec.captionSize, color = WidgetColors.tertiaryText)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = TextStyle(fontSize = spec.bodySize, color = WidgetColors.primaryText),
                        modifier = GlanceModifier
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    DoneButton(task.id)
                }
            }
        }
    }
}

private data class WidgetTask(
    val id: String,
    val title: String,
    val dateLabel: String,
    val instant: Instant,
    val date: LocalDate,
    val overdue: Boolean
)

@Preview
@Composable
private fun TasksWidgetPreview() {
    val spec = layoutSpec(WidgetLayout.Large)
    val tasks = listOf(
        WidgetTask(
            id = "t1",
            title = "Design review",
            dateLabel = "Today",
            instant = Instant.parse("2026-02-22T10:00:00Z"),
            date = LocalDate(2026, 2, 22),
            overdue = false
        ),
        WidgetTask(
            id = "t2",
            title = "Implement calendar",
            dateLabel = "Tomorrow",
            instant = Instant.parse("2026-02-23T12:00:00Z"),
            date = LocalDate(2026, 2, 23),
            overdue = false
        ),
        WidgetTask(
            id = "t3",
            title = "Fix widget layout",
            dateLabel = "Overdue",
            instant = Instant.parse("2026-02-20T09:00:00Z"),
            date = LocalDate(2026, 2, 20),
            overdue = true
        )
    )
    TasksWidgetPreviewContent(tasks = tasks, strings = previewStrings(), spec = spec)
}

@Composable
private fun TasksWidgetPreviewContent(
    tasks: List<WidgetTask>,
    strings: WidgetStrings,
    spec: WidgetLayoutSpec
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.card)
            .cornerRadius(24.dp)
            .padding(spec.padding)
    ) {
        LargeContent(tasks, strings, spec)
    }
}

private fun previewStrings() = WidgetStrings(
    empty = "No upcoming tasks",
    today = "Today",
    tomorrow = "Tomorrow",
    overdue = "Overdue",
    untitled = "(untitled)"
)

private data class WidgetSize(val widthDp: Int, val heightDp: Int)

private fun resolveLayout(size: WidgetSize): WidgetLayout {
    return when {
        size.widthDp < 170 -> WidgetLayout.Small
        size.heightDp < 170 -> WidgetLayout.Medium
        else -> WidgetLayout.Large
    }
}

private data class WidgetLayoutSpec(
    val padding: androidx.compose.ui.unit.Dp,
    val bodySize: androidx.compose.ui.unit.TextUnit,
    val captionSize: androidx.compose.ui.unit.TextUnit
)

private fun layoutSpec(layout: WidgetLayout): WidgetLayoutSpec {
    return when (layout) {
        WidgetLayout.Small -> WidgetLayoutSpec(
            padding = 12.dp,
            bodySize = 11.sp,
            captionSize = 10.sp
        )
        WidgetLayout.Medium -> WidgetLayoutSpec(
            padding = 14.dp,
            bodySize = 12.sp,
            captionSize = 11.sp
        )
        WidgetLayout.Large -> WidgetLayoutSpec(
            padding = 16.dp,
            bodySize = 12.sp,
            captionSize = 11.sp
        )
    }
}

private fun readWidgetSize(context: Context, id: androidx.glance.GlanceId): WidgetSize {
    val manager = GlanceAppWidgetManager(context)
    val appWidgetId = manager.getAppWidgetId(id)
    val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
    val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
    val width = if (maxWidth > 0) maxWidth else minWidth
    val height = if (maxHeight > 0) maxHeight else minHeight
    return WidgetSize(widthDp = width, heightDp = height)
}

private fun loadUpcomingTasks(context: Context, strings: WidgetStrings): List<WidgetTask> {
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

    val tz = TimeZone.currentSystemDefault()
    val today = nowInstant().toLocalDateTime(tz).date
    val lastDay = today.plus(DatePeriod(days = DAYS_AHEAD))
    val nowMs = nowInstant().toEpochMilliseconds()

    val sorted = sortedTasks(tasks, appPrefs)

    val items = sorted.asSequence()
        .filter { !it.done }
        .filter { it.deadline != null || it.plannedAt != null }
        .mapNotNull { task ->
            val target = pickRelevantInstant(task, nowMs) ?: return@mapNotNull null
            val date = target.toLocalDateTime(tz).date
            val overdue = isOverdue(task, nowMs)
            WidgetTask(
                id = task.id,
                title = task.title.ifBlank { strings.untitled },
                dateLabel = dateLabel(date, today, strings, overdue),
                instant = target,
                date = date,
                overdue = overdue
            )
        }
        .toList()

    val inRange = items.filter { it.overdue || it.date <= lastDay }
    val result = if (inRange.isNotEmpty()) inRange else items.filter { !it.overdue }
    return result.sortedBy { it.instant }
}

private fun pickRelevantInstant(task: TodoTask, nowMs: Long): Instant? {
    val planned = task.plannedAt
    val deadline = task.deadline
    return when {
        planned == null && deadline != null -> deadline
        planned == null -> null
        planned.toEpochMilliseconds() < nowMs && deadline != null -> deadline
        else -> planned
    } as Instant?
}

private fun dateLabel(date: LocalDate, today: LocalDate, strings: WidgetStrings, overdue: Boolean): String {
    if (overdue) return strings.overdue
    if (date == today) return strings.today
    if (date == today.plus(DatePeriod(days = 1))) return strings.tomorrow
    val dd = date.dayOfMonth.toString().padStart(2, '0')
    val mm = date.monthNumber.toString().padStart(2, '0')
    return "$dd.$mm"
}

private data class WidgetStrings(
    val empty: String,
    val today: String,
    val tomorrow: String,
    val overdue: String,
    val untitled: String
) {
    companion object {
        fun from(context: Context): WidgetStrings = WidgetStrings(
            empty = context.getString(R.string.widget_tasks_empty),
            today = context.getString(R.string.widget_today),
            tomorrow = context.getString(R.string.widget_tomorrow),
            overdue = context.getString(R.string.widget_overdue),
            untitled = context.getString(R.string.widget_untitled)
        )
    }
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

@Composable
internal fun DoneButton(taskId: String) {
    val intent = Intent()
        .setClassName("com.grigorevmp.simpletodo", "com.grigorevmp.simpletodo.widget.MarkDoneActivity")
        .putExtra(EXTRA_TASK_ID, taskId)

    Box(
        modifier = GlanceModifier
            .background(WidgetColors.doneChip)
            .cornerRadius(10.dp)
            .clickable(actionStartActivityWidget(intent))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "✓",
            style = TextStyle(fontSize = 12.sp, color = WidgetColors.doneText)
        )
    }
}

class MarkDoneActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId = intent?.getStringExtra(EXTRA_TASK_ID)
        if (taskId != null) {
            markDone(taskId)
        }
        finish()
    }

    private fun markDone(taskId: String) {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(TASKS_KEY, null) ?: return
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            serializersModule = SerializersModule {
                contextual(Instant::class, InstantAsStringSerializer)
            }
        }
        val tasks = runCatching { json.decodeFromString(ListSerializer(TodoTask.serializer()), raw) }
            .getOrElse { emptyList() }
        val updated = tasks.map { if (it.id == taskId) it.copy(done = true) else it }
        prefs.edit(commit = true) {
            putString(
                TASKS_KEY,
                json.encodeToString(ListSerializer(TodoTask.serializer()), updated)
            )
        }
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            TasksWidget().updateAll(applicationContext)
            PinnedTasksWidget().updateAll(applicationContext)
            com.grigorevmp.simpletodo.platform.requestPinnedTasksNotificationUpdate()
        }
    }
}

private object WidgetColors {
    val card = ColorProvider(
        day = Color(0xFFF4F6F8),
        night = Color(0xFF111418)
    )
    val accent = ColorProvider(
        day = Color(0xFF3B82F6),
        night = Color(0xFF7AB1FF)
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
    val doneChip = ColorProvider(
        day = Color(0xFFE2E8F0),
        night = Color(0xFF1F2937)
    )
    val doneText = ColorProvider(
        day = Color(0xFF0B0C0F),
        night = Color(0xFFF5F7FB)
    )
}
