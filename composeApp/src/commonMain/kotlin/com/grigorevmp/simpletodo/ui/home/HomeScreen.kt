package com.grigorevmp.simpletodo.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.isActive
import kotlin.random.Random
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.Tag
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.ChromeDinoMascot
import com.grigorevmp.simpletodo.ui.components.AtomSimpleIcon
import com.grigorevmp.simpletodo.ui.components.FilterIcon
import com.grigorevmp.simpletodo.ui.components.TagIcon
import com.grigorevmp.simpletodo.ui.home.components.SegmentedTabs
import com.grigorevmp.simpletodo.ui.notes.NoteEditorScreen
import com.grigorevmp.simpletodo.util.dateKey
import com.grigorevmp.simpletodo.util.formatDeadline
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class HomeTab { TIMELINE, INBOX }

@Composable
fun HomeScreen(
    repo: TodoRepository,
    createSignal: Int,
    onCreateHandled: () -> Unit
) {
    val tasks by repo.tasks.collectAsState()
    val notes by repo.notes.collectAsState()
    val prefs by repo.prefs.collectAsState() 
    val scope = rememberCoroutineScope()

    val sorted = remember(tasks, prefs) { repo.sortedTasks(tasks, prefs) }

    var showEditor by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TodoTask?>(null) }
    var showNoteEditor by remember { mutableStateOf(false) }
    var editNote by remember { mutableStateOf<Note?>(null) }
    var showSort by remember { mutableStateOf(false) }
    var tagFilter by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(HomeTab.TIMELINE) }
    var detailsTask by remember { mutableStateOf<TodoTask?>(null) }

    val filtered = remember(sorted, tagFilter) {
        when (tagFilter) {
            null -> sorted
            "__no_tag__" -> sorted.filter { it.tagId == null }
            else -> sorted.filter { it.tagId == tagFilter }
        }
    }

    val timelineTasks = filtered.filter { it.deadline != null || it.plannedAt != null }
    val inboxTasks = filtered.filter { it.deadline == null && it.plannedAt == null }
    val backgroundColor = MaterialTheme.colorScheme.background
    val listBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    androidx.compose.runtime.LaunchedEffect(createSignal) {
        if (createSignal > 0) {
            editTask = null
            showEditor = true
            onCreateHandled()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                tagsShown = prefs.showTagFilters,
                onSort = { showSort = true },
                onToggleTags = { scope.launch { repo.setShowTagFilters(!prefs.showTagFilters) } }
            )

            AnimatedVisibility (prefs.showTagFilters) {
                TagFilters(
                    selectedTagId = tagFilter,
                    tags = prefs.tags,
                    onPick = { tagFilter = it }
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .layerBackdrop(listBackdrop)
            ) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "timeline-inbox"
                ) { target ->
                    when (target) {
                        HomeTab.TIMELINE -> {
                            if (timelineTasks.isEmpty()) {
                                EmptyState(
                                    "No timeline tasks.",
                                    "Add a task with a deadline to see it on the timeline.",
                                    showMascot = true
                                )
                            } else {
                                TimelineList(
                                    tasks = timelineTasks,
                                    onToggleDone = { id -> scope.launch { repo.toggleDone(id) } },
                                    onToggleSub = { taskId, subId ->
                                        scope.launch {
                                            repo.toggleSubtask(
                                                taskId,
                                                subId
                                            )
                                        }
                                    },
                                    onEdit = { t -> editTask = t; showEditor = true },
                                    onDelete = { id -> scope.launch { repo.deleteTask(id) } },
                                    onOpenDetails = { t -> detailsTask = t },
                                    tagName = { tagId -> prefs.tags.firstOrNull { it.id == tagId }?.name },
                                    noteTitle = { noteId -> notes.firstOrNull { it.id == noteId }?.title },
                                    onOpenNote = { noteId ->
                                        val note = notes.firstOrNull { it.id == noteId }
                                        if (note != null) {
                                            editNote = note
                                            showEditor = false
                                            showNoteEditor = true
                                        }
                                    }
                                )
                            }
                        }

                        HomeTab.INBOX -> {
                            if (inboxTasks.isEmpty()) {
                                EmptyState(
                                    "Inbox is empty.",
                                    "Tasks without deadlines appear here.",
                                    showMascot = true
                                )
                            } else {
                                FlatList(
                                    tasks = inboxTasks,
                                    onToggleDone = { id -> scope.launch { repo.toggleDone(id) } },
                                    onToggleSub = { taskId, subId ->
                                        scope.launch {
                                            repo.toggleSubtask(
                                                taskId,
                                                subId
                                            )
                                        }
                                    },
                                    onEdit = { t -> editTask = t; showEditor = true },
                                    onDelete = { id -> scope.launch { repo.deleteTask(id) } },
                                    onOpenDetails = { t -> detailsTask = t },
                                    tagName = { tagId -> prefs.tags.firstOrNull { it.id == tagId }?.name },
                                    noteTitle = { noteId -> notes.firstOrNull { it.id == noteId }?.title },
                                    onOpenNote = { noteId ->
                                        val note = notes.firstOrNull { it.id == noteId }
                                        if (note != null) {
                                            editNote = note
                                            showEditor = false
                                            showNoteEditor = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                SegmentedTabs(
                    backdrop = listBackdrop,
                    leftSelected = tab == HomeTab.TIMELINE,
                    onLeft = { tab = HomeTab.TIMELINE },
                    onRight = { tab = HomeTab.INBOX },
                    enableEffects = false,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp)
                )
            }
        }

        if (showEditor) {
            TaskEditorSheet(
                repo = repo,
                prefsTagList = prefs.tags,
                notes = notes,
                initial = editTask,
                onDismiss = { showEditor = false }
            )
        }

        if (showNoteEditor) {
            NoteEditorScreen(
                repo = repo,
                initial = editNote,
                tasks = tasks,
                folderId = editNote?.folderId,
                onDismiss = { showNoteEditor = false }
            )
        }

        detailsTask?.let { t ->
            TaskDetailsDialog(
                task = t,
                tagName = { tagId -> prefs.tags.firstOrNull { it.id == tagId }?.name },
                noteTitle = { noteId -> notes.firstOrNull { it.id == noteId }?.title },
                onClose = { detailsTask = null }
            )
        }

        if (showSort) {
            SortSheet(
                current = prefs.sort,
                onApply = { cfg -> scope.launch { repo.setSort(cfg) } },
                onDismiss = { showSort = false }
            )
        }
    }
}



@Composable
private fun TopBar(
    tagsShown: Boolean,
    onSort: () -> Unit,
    onToggleTags: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AtomSimpleIcon(
                size = 36.dp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )

            Spacer(Modifier.width(24.dp))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 2.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    "Atomic ToDo",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Text(
                    "beta 0.2",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }
        }

        Row {
            IconButton(onClick = onToggleTags) {
                Icon(
                    imageVector = TagIcon,
                    contentDescription = "Tags",
                    tint = if(tagsShown) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = onSort) {
                Icon(
                    imageVector = FilterIcon,
                    contentDescription = "Filter"
                )
            }
        }
    }
}

@Composable
private fun TagFilters(
    selectedTagId: String?,
    tags: List<Tag>,
    onPick: (String?) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 2.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedTagId == null,
            onClick = { onPick(null) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selectedTagId == "__no_tag__",
            onClick = { onPick("__no_tag__") },
            label = { Text("No tag") }
        )
        tags.forEach { tag ->
            FilterChip(
                selected = selectedTagId == tag.id,
                onClick = { onPick(tag.id) },
                label = { Text(tag.name) }
            )
        }
    }
}

@Composable
private fun TaskDetailsDialog(
    task: TodoTask,
    tagName: (String?) -> String?,
    noteTitle: (String?) -> String?,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(task.title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tagName(task.tagId)?.let { name ->
                    InfoRow("Tag", name)
                }
                noteTitle(task.noteId)?.let { title ->
                    InfoRow("Note", title)
                }
                InfoRow("Priority", importanceLabel(task.importance))
                task.plannedAt?.let { InfoRow("Planned", formatDeadline(it)) }
                task.deadline?.let { InfoRow("Due", formatDeadline(it)) }
                task.estimateHours?.let { InfoRow("Estimate", formatHours(it)) }
                if (task.plan.isNotBlank()) {
                    HorizontalDivider()
                    Text(task.plan, style = MaterialTheme.typography.bodyMedium)
                }
                if (task.subtasks.isNotEmpty()) {
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Subtasks", style = MaterialTheme.typography.titleMedium)
                        task.subtasks.forEach { s ->
                            Text("• ${s.text}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Close") }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun importanceLabel(i: Importance): String = when (i) {
    Importance.LOW -> "low"
    Importance.NORMAL -> "normal"
    Importance.HIGH -> "high"
    Importance.CRITICAL -> "critical"
}

private fun formatHours(v: Double): String {
    val scaled = (v * 10).roundToInt()
    val intPart = scaled / 10
    val frac = kotlin.math.abs(scaled % 10)
    val text = if (frac == 0) {
        intPart.toString()
    } else {
        "$intPart.$frac"
    }
    return "$text h"
}

@Composable
private fun EmptyState(title: String, body: String, showMascot: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showMascot) {
            MeteorHeader()
        }
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MeteorHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        MeteorField(modifier = Modifier.matchParentSize())
        ChromeDinoMascot(
            size = 48.dp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun MeteorField(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val particles = remember { mutableStateListOf<MeteorParticle>() }
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var nowNanos by remember { mutableStateOf(0L) }
    val rng = remember { Random(87321) }

    LaunchedEffect(fieldSize) {
        if (fieldSize.width == 0 || fieldSize.height == 0) return@LaunchedEffect
        var lastSpawn = 0L
        while (isActive) {
            val now = withFrameNanos { it }
            nowNanos = now
            val elapsed = now - lastSpawn
            val spawnDelay = (200L + rng.nextInt(400)).toLong() * 1_000_000L
            if (elapsed >= spawnDelay && particles.size < 18) {
                particles.add(
                    createParticle(
                        now = now,
                        size = fieldSize,
                        rng = rng,
                        color = if (rng.nextBoolean()) color else secondary
                    )
                )
                lastSpawn = now
            }
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                if (now - p.startNanos > p.durationNanos) {
                    iterator.remove()
                }
            }
        }
    }

    Canvas(
        modifier = modifier.onSizeChanged { fieldSize = it }
    ) {
        val now = nowNanos
        particles.forEach { p ->
            val t = ((now - p.startNanos).toFloat() / p.durationNanos.toFloat()).coerceIn(0f, 1f)
            val pos = cubicBezier(p.start, p.c1, p.c2, p.end, t)
            val dir = cubicBezierTangent(p.start, p.c1, p.c2, p.end, t)
            val len = p.length
            val tail = Offset(
                pos.x - dir.x * len,
                pos.y - dir.y * len
            )
            val alpha = (1f - t) * 0.9f
            drawLine(
                color = p.color.copy(alpha = alpha),
                start = pos,
                end = tail,
                strokeWidth = p.stroke,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.cornerPathEffect(p.stroke)
            )
        }
    }
}

private data class MeteorParticle(
    val start: Offset,
    val c1: Offset,
    val c2: Offset,
    val end: Offset,
    val startNanos: Long,
    val durationNanos: Long,
    val length: Float,
    val stroke: Float,
    val color: Color
)

private fun createParticle(
    now: Long,
    size: IntSize,
    rng: Random,
    color: Color
): MeteorParticle {
    val w = size.width.toFloat()
    val h = size.height.toFloat()
    val margin = 6f
    val start = Offset(
        x = rng.nextFloat() * w,
        y = rng.nextFloat() * h * 0.25f
    )
    val end = Offset(
        x = rng.nextFloat() * w,
        y = h - margin
    )
    val c1 = Offset(
        x = start.x + (rng.nextFloat() - 0.5f) * w * 0.4f,
        y = start.y + h * (0.25f + rng.nextFloat() * 0.2f)
    )
    val c2 = Offset(
        x = end.x + (rng.nextFloat() - 0.5f) * w * 0.4f,
        y = end.y - h * (0.25f + rng.nextFloat() * 0.2f)
    )
    val duration = (900L + rng.nextInt(900)).toLong() * 1_000_000L
    val length = 12f + rng.nextFloat() * 10f
    val stroke = 1.8f + rng.nextFloat() * 0.8f
    return MeteorParticle(
        start = start,
        c1 = c1,
        c2 = c2,
        end = end,
        startNanos = now,
        durationNanos = duration,
        length = length,
        stroke = stroke,
        color = color
    )
}

private fun cubicBezier(p0: Offset, p1: Offset, p2: Offset, p3: Offset, t: Float): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t
    val x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x
    val y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y
    return Offset(x, y)
}

private fun cubicBezierTangent(p0: Offset, p1: Offset, p2: Offset, p3: Offset, t: Float): Offset {
    val u = 1f - t
    val x = 3f * u * u * (p1.x - p0.x) + 6f * u * t * (p2.x - p1.x) + 3f * t * t * (p3.x - p2.x)
    val y = 3f * u * u * (p1.y - p0.y) + 6f * u * t * (p2.y - p1.y) + 3f * t * t * (p3.y - p2.y)
    val len = kotlin.math.sqrt(x * x + y * y).coerceAtLeast(0.001f)
    return Offset(x / len, y / len)
}

@Composable
private fun TimelineList(
    tasks: List<TodoTask>,
    onToggleDone: (String) -> Unit,
    onToggleSub: (String, String) -> Unit,
    onOpenDetails: (TodoTask) -> Unit,
    onEdit: (TodoTask) -> Unit,
    onDelete: (String) -> Unit,
    tagName: (String?) -> String?,
    noteTitle: (String?) -> String?,
    onOpenNote: (String?) -> Unit
) {
    val grouped = remember(tasks) {
        tasks.groupBy { t ->
            t.plannedAt?.let { dateKey(it) } ?: "No deadline"
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        grouped.forEach { (k, v) ->
            item {
                Text(
                    k,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }
            items(v, key = { it.id }) { t ->
                TaskCard(
                    task = t,
                    tagLabel = tagName(t.tagId),
                    noteTitle = noteTitle(t.noteId),
                    onOpenNote = { onOpenNote(t.noteId) },
                    onToggleDone = { onToggleDone(t.id) },
                    onToggleSub = { subId -> onToggleSub(t.id, subId) },
                    onOpenDetails = { onOpenDetails(t) },
                    onEdit = { onEdit(t) },
                    onDelete = { onDelete(t.id) }
                )
            }
        }
        item { Spacer(Modifier.height(120.dp)) }
    }
}

@Composable
private fun FlatList(
    tasks: List<TodoTask>,
    onToggleDone: (String) -> Unit,
    onToggleSub: (String, String) -> Unit,
    onOpenDetails: (TodoTask) -> Unit,
    onEdit: (TodoTask) -> Unit,
    onDelete: (String) -> Unit,
    tagName: (String?) -> String?,
    noteTitle: (String?) -> String?,
    onOpenNote: (String?) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tasks, key = { it.id }) { t ->
            TaskCard(
                task = t,
                tagLabel = tagName(t.tagId),
                noteTitle = noteTitle(t.noteId),
                onOpenNote = { onOpenNote(t.noteId) },
                onToggleDone = { onToggleDone(t.id) },
                onToggleSub = { subId -> onToggleSub(t.id, subId) },
                onOpenDetails = { onOpenDetails(t) },
                onEdit = { onEdit(t) },
                onDelete = { onDelete(t.id) }
            )
        }
        item { Spacer(Modifier.height(120.dp)) }
    }
}
