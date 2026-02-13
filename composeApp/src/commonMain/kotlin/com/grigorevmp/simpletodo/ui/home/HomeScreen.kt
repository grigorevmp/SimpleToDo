package com.grigorevmp.simpletodo.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
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
import com.grigorevmp.simpletodo.ui.components.AppIconId
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.PlatformIcon
import com.grigorevmp.simpletodo.ui.components.NoteIcon
import com.grigorevmp.simpletodo.ui.components.FlameIcon
import com.grigorevmp.simpletodo.ui.components.SimpleIcons
import com.grigorevmp.simpletodo.ui.components.itemPlacement
import com.grigorevmp.simpletodo.ui.components.CircleCheckbox
import com.grigorevmp.simpletodo.ui.home.components.SegmentedTabs
import com.grigorevmp.simpletodo.ui.notes.MarkdownText
import com.grigorevmp.simpletodo.util.dateKey
import com.grigorevmp.simpletodo.util.formatDeadline
import com.grigorevmp.simpletodo.util.nowInstant
import com.grigorevmp.simpletodo.platform.isIos
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.datetime.Instant
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class HomeTab { TIMELINE, INBOX }

@Composable
fun HomeScreen(
    repo: TodoRepository,
    createSignal: Int,
    onCreateHandled: () -> Unit,
    onEditNote: (String) -> Unit
) {
    val tasks by repo.tasks.collectAsState()
    val notes by repo.notes.collectAsState()
    val links by repo.taskNoteLinks.collectAsState()
    val prefs by repo.prefs.collectAsState() 
    val scope = rememberCoroutineScope()

    val sorted = remember(tasks, prefs) { repo.sortedTasks(tasks, prefs) }
    val notesById = remember(notes) { notes.associateBy { it.id } }

    var showEditor by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TodoTask?>(null) }
    var previewNote by remember { mutableStateOf<Note?>(null) }
    var taskNotesSheet by remember { mutableStateOf<Pair<TodoTask, List<Note>>?>(null) }
    var showSort by remember { mutableStateOf(false) }
    var tagFilter by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(HomeTab.TIMELINE) }
    var detailsTaskId by remember { mutableStateOf<String?>(null) }
    val celebrationTrigger = remember { mutableIntStateOf(0) }

    val filtered = remember(sorted, tagFilter) {
        val tagged = when (tagFilter) {
            null -> sorted
            "__no_tag__" -> sorted.filter { it.tagId == null }
            else -> sorted.filter { it.tagId == tagFilter }
        }
        tagged
    }

    val timelineTasks = filtered.filter { it.deadline != null || it.plannedAt != null }
    val inboxTasks = filtered.filter { it.deadline == null && it.plannedAt == null }
    val backgroundColor = MaterialTheme.colorScheme.background
    val listBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    fun notesForTask(task: TodoTask): List<Note> {
        val list = links
            .asSequence()
            .filter { it.taskId == task.id }
            .mapNotNull { notesById[it.noteId] }
            .distinct()
            .sortedByDescending { it.updatedAt }
            .toList()
        return list
    }

    androidx.compose.runtime.LaunchedEffect(createSignal) {
        if (createSignal > 0) {
            editTask = null
            showEditor = true
            onCreateHandled()
        }
    }

    Box(Modifier.fillMaxSize()) {
        CelebrationVolley(
            trigger = celebrationTrigger.intValue,
            modifier = Modifier.matchParentSize()
        )
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
                                        onToggleDone = { id ->
                                            val t = tasks.firstOrNull { it.id == id }
                                            if (t != null && !t.done) {
                                                celebrationTrigger.intValue += 1
                                            }
                                            scope.launch { repo.toggleDone(id) }
                                        },
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
                                        onClearCompleted = { scope.launch { repo.clearCompletedTasks() } },
                                        showCompleted = prefs.showCompletedTasks,
                                        onOpenDetails = { t -> detailsTaskId = t.id },
                                        tagName = { tagId -> prefs.tags.firstOrNull { it.id == tagId }?.name },
                                        noteCount = { t -> notesForTask(t).size },
                                        onOpenNotes = { t ->
                                        val linked = notesForTask(t)
                                        if (linked.isNotEmpty()) {
                                            taskNotesSheet = t to linked
                                        }
                                    },
                                    dimScroll = prefs.dimScroll,
                                    backdrop = listBackdrop
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
                                        onToggleDone = { id ->
                                            val t = tasks.firstOrNull { it.id == id }
                                            if (t != null && !t.done) {
                                                celebrationTrigger.intValue += 1
                                            }
                                            scope.launch { repo.toggleDone(id) }
                                        },
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
                                        onClearCompleted = { scope.launch { repo.clearCompletedTasks() } },
                                        showCompleted = prefs.showCompletedTasks,
                                        onOpenDetails = { t -> detailsTaskId = t.id },
                                        tagName = { tagId -> prefs.tags.firstOrNull { it.id == tagId }?.name },
                                        noteCount = { t -> notesForTask(t).size },
                                        onOpenNotes = { t ->
                                        val linked = notesForTask(t)
                                        if (linked.isNotEmpty()) {
                                            taskNotesSheet = t to linked
                                        }
                                    },
                                    dimScroll = prefs.dimScroll,
                                    backdrop = listBackdrop
                                )
                            }
                        }
                    }
                }

                SegmentedTabs(
                    leftSelected = tab == HomeTab.TIMELINE,
                    onLeft = { tab = HomeTab.TIMELINE },
                    onRight = { tab = HomeTab.INBOX },
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

        detailsTaskId?.let { id ->
            val current = tasks.firstOrNull { it.id == id }
            if (current == null) {
                detailsTaskId = null
            } else {
                TaskDetailsSheet(
                    task = current,
                    tagName = { tagId -> prefs.tags.firstOrNull { it.id == tagId }?.name },
                    notes = notesForTask(current),
                    onOpenNotes = {
                        val linked = notesForTask(current)
                        if (linked.isNotEmpty()) {
                            taskNotesSheet = current to linked
                        }
                    },
                    onToggleSub = { subId ->
                        scope.launch { repo.toggleSubtask(current.id, subId) }
                    },
                    onToggleDone = {
                        if (!current.done) {
                            celebrationTrigger.intValue += 1
                        }
                        scope.launch { repo.toggleDone(current.id) }
                    },
                    onEdit = {
                        editTask = current
                        showEditor = true
                    },
                    onClose = { detailsTaskId = null }
                )
            }
        }

        previewNote?.let { note ->
            NotePreviewDialog(
                note = note,
                onEdit = { onEditNote(note.id); previewNote = null },
                onClose = { previewNote = null },
                dimScroll = prefs.dimScroll
            )
        }

        taskNotesSheet?.let { (task, list) ->
            TaskNotesSheet(
                taskTitle = task.title,
                notes = list,
                onOpenNote = { note ->
                    taskNotesSheet = null
                    previewNote = note
                },
                onDismiss = { taskNotesSheet = null }
            )
        }

        if (showSort) {
            SortSheet(
                current = prefs.sort,
                showCompleted = prefs.showCompletedTasks,
                onShowCompleted = { show -> scope.launch { repo.setShowCompletedTasks(show) } },
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
                    "beta 0.7.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = if (isIos) FontStyle.Normal else FontStyle.Italic,
                )
            }
        }

        Row {
            IconButton(onClick = onToggleTags) {
                PlatformIcon(
                    id = AppIconId.Tag,
                    contentDescription = "Tags",
                    tint = if (tagsShown) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onSort) {
                PlatformIcon(
                    id = AppIconId.Filter,
                    contentDescription = "Filter",
                    tint = LocalContentColor.current,
                    modifier = Modifier.size(22.dp)
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
@OptIn(ExperimentalMaterial3Api::class)
private fun TaskDetailsSheet(
    task: TodoTask,
    tagName: (String?) -> String?,
    notes: List<Note>,
    onOpenNotes: () -> Unit,
    onToggleSub: (String) -> Unit,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Box(Modifier.fillMaxWidth()) {
            ImportanceFlameBackdrop(
                importance = task.importance,
                modifier = Modifier
                    .matchParentSize()
                    .padding(8.dp)
            )
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) { Text("Редактировать") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onToggleDone) {
                        Text(if (task.done) "Снять отметку" else "Отметить выполненным")
                    }
                }

                Box(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(task.title, style = MaterialTheme.typography.titleLarge)
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tagName(task.tagId)?.let { name ->
                                TagChipLabel(name)
                            }
                            if (notes.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                NoteChipLabel(
                                    count = notes.size,
                                    onOpen = onOpenNotes
                                )
                            }
                        }
                    }
                }

                DateInfoSection(task)

                task.estimateHours?.let {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Estimate", style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatHours(it),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (task.plan.isNotBlank()) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Details", style = MaterialTheme.typography.titleMedium)
                            Text(task.plan, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (task.subtasks.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Subtasks", style = MaterialTheme.typography.titleMedium)
                            SubtasksInteractive(task, onToggleSub)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NotePreviewDialog(
    note: Note,
    onEdit: () -> Unit,
    onClose: () -> Unit,
    dimScroll: Boolean
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(note.title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Box(Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (note.content.isBlank()) {
                        Text(
                            "No content",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        MarkdownText(note.content)
                    }
                }
                FadingScrollEdges(
                    scrollState = scrollState,
                    modifier = Modifier.matchParentSize(),
                    color = MaterialTheme.colorScheme.surface,
                    enabled = dimScroll
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) { Text("Edit") }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskNotesSheet(
    taskTitle: String,
    notes: List<Note>,
    onOpenNote: (Note) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Notes", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${notes.size} linked to \"$taskTitle\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(note) }
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                note.title.ifBlank { "Untitled" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                formatDeadline(note.updatedAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                notePreview(note),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
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
    Importance.LOW -> "Low"
    Importance.NORMAL -> "Normal"
    Importance.HIGH -> "High"
    Importance.CRITICAL -> "Critical"
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

private fun notePreview(note: Note): String {
    val text = note.content.trim()
    if (text.isBlank()) return "No content"
    return text.replace("\n", " ").take(140)
}

@Composable
private fun TagChipLabel(name: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlatformIcon(
                id = AppIconId.Tag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NoteChipLabel(
    count: Int,
    onOpen: () -> Unit
) {
    val label = if (count == 1) "Note" else "Notes"
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier.clickable { onOpen() }
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(NoteIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(
                if (count == 1) label else "$label $count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DateInfoSection(task: TodoTask) {
    val planned = task.plannedAt
    val deadline = task.deadline
    if (planned == null && deadline == null) return

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Dates", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                planned?.let {
                    DateInfoCard(
                        title = "Planned",
                        instant = it
                    )
                }
                deadline?.let {
                    DateInfoCard(
                        title = "Due",
                        instant = it
                    )
                }
            }
        }
    }
}

@Composable
private fun DateInfoCard(
    title: String,
    instant: Instant
) {
    val days = daysUntil(instant)
    val isOverdue = instant.toEpochMilliseconds() < nowInstant().toEpochMilliseconds()
    val badge = if (isOverdue) {
        "Overdue"
    } else {
        "In $days days"
    }
    val badgeColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val badgeOn = if (isOverdue) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(
                    formatDeadline(instant),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(shape = MaterialTheme.shapes.small, color = badgeColor) {
                Text(
                    badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeOn,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SubtasksInteractive(
    task: TodoTask,
    onToggleSub: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        task.subtasks.forEach { s ->
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleCheckbox(
                        checked = s.done,
                        onCheckedChange = { onToggleSub(s.id) }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(s.text, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ImportanceFlameBackdrop(importance: Importance, modifier: Modifier = Modifier) {
    val count = when (importance) {
        Importance.LOW -> 0
        Importance.NORMAL -> 1
        Importance.HIGH -> 3
        Importance.CRITICAL -> 6
    }
    if (count == 0) return
    val sizes = listOf(18.dp, 22.dp, 26.dp, 30.dp, 34.dp, 38.dp)
    val positions = flamePositions(count)
    BoxWithConstraints(modifier = modifier.alpha(0.85f)) {
        val w = maxWidth
        val h = maxHeight
        positions.forEachIndexed { index, (x, y) ->
            val size = sizes[index.coerceAtMost(sizes.lastIndex)]
            Icon(
                imageVector = FlameIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .offset(x = w * x, y = h * y)
            )
        }
    }
}

private fun flamePositions(count: Int): List<Pair<Float, Float>> {
    val all = listOf(
        0.08f to 0.12f,
        0.68f to 0.10f,
        0.18f to 0.52f,
        0.62f to 0.48f,
        0.10f to 0.70f,
        0.72f to 0.68f
    )
    return all.take(count)
}

private fun daysUntil(deadline: Instant): Int {
    val now = nowInstant()
    val diffMs = deadline.toEpochMilliseconds() - now.toEpochMilliseconds()
    val dayMs = 24 * 60 * 60 * 1000L
    val days = ((diffMs + dayMs - 1) / dayMs).toInt()
    return if (days < 0) 0 else days
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

@Composable
private fun CelebrationVolley(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    val particles = remember { mutableStateListOf<VolleyParticle>() }
    val meteors = remember { mutableStateListOf<MeteorParticle>() }
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var nowNanos by remember { mutableStateOf(0L) }
    val rng = remember { Random(45677) }
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error
    )

    LaunchedEffect(trigger, fieldSize) {
        if (trigger == 0 || fieldSize.width == 0 || fieldSize.height == 0) return@LaunchedEffect
        val now = withFrameNanos { it }
        nowNanos = now
        repeat(18) {
            particles.add(createVolleyParticle(now, fieldSize, rng, palette))
        }
        repeat(8) {
            meteors.add(createBottomMeteor(now, fieldSize, rng, palette))
        }
    }

    LaunchedEffect(fieldSize) {
        if (fieldSize.width == 0 || fieldSize.height == 0) return@LaunchedEffect
        while (isActive) {
            val now = withFrameNanos { it }
            nowNanos = now
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                if (now - p.startNanos > p.durationNanos) {
                    iterator.remove()
                }
            }
            val meteorIterator = meteors.iterator()
            while (meteorIterator.hasNext()) {
                val p = meteorIterator.next()
                if (now - p.startNanos > p.durationNanos) {
                    meteorIterator.remove()
                }
            }
        }
    }

    Canvas(modifier = modifier.onSizeChanged { fieldSize = it }) {
        val now = nowNanos
        particles.forEach { p ->
            val t = ((now - p.startNanos).toFloat() / p.durationNanos.toFloat()).coerceIn(0f, 1f)
            drawVolleyParticle(p, t)
        }
        meteors.forEach { p ->
            val t = ((now - p.startNanos).toFloat() / p.durationNanos.toFloat()).coerceIn(0f, 1f)
            drawMeteorParticle(p, t)
        }
    }
}

private fun createBottomMeteor(
    now: Long,
    size: IntSize,
    rng: Random,
    palette: List<Color>
): MeteorParticle {
    val w = size.width.toFloat()
    val h = size.height.toFloat()
    val start = Offset(
        x = w * (0.1f + rng.nextFloat() * 0.8f),
        y = h + 18f
    )
    val end = Offset(
        x = w * (0.15f + rng.nextFloat() * 0.7f),
        y = h * (0.2f + rng.nextFloat() * 0.35f)
    )
    val c1 = Offset(
        x = start.x + (rng.nextFloat() - 0.5f) * w * 0.35f,
        y = start.y - h * (0.2f + rng.nextFloat() * 0.25f)
    )
    val c2 = Offset(
        x = end.x + (rng.nextFloat() - 0.5f) * w * 0.35f,
        y = end.y + h * (0.05f + rng.nextFloat() * 0.15f)
    )
    val duration = (700L + rng.nextInt(700)).toLong() * 1_000_000L
    val length = 12f + rng.nextFloat() * 10f
    val stroke = 1.6f + rng.nextFloat() * 1.0f
    return MeteorParticle(
        start = start,
        c1 = c1,
        c2 = c2,
        end = end,
        startNanos = now,
        durationNanos = duration,
        length = length,
        stroke = stroke,
        color = palette[rng.nextInt(palette.size)].copy(alpha = 0.65f)
    )
}

private fun DrawScope.drawMeteorParticle(p: MeteorParticle, t: Float) {
    val pos = cubicBezier(p.start, p.c1, p.c2, p.end, t)
    val dir = cubicBezierTangent(p.start, p.c1, p.c2, p.end, t)
    val tail = Offset(
        pos.x - dir.x * p.length,
        pos.y - dir.y * p.length
    )
    val alpha = (1f - t).coerceIn(0f, 1f) * 0.9f
    drawLine(
        color = p.color.copy(alpha = alpha),
        start = pos,
        end = tail,
        strokeWidth = p.stroke,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(p.stroke)
    )
}

private data class VolleyParticle(
    val start: Offset,
    val velocity: Offset,
    val color: Color,
    val length: Float,
    val stroke: Float,
    val startNanos: Long,
    val durationNanos: Long,
    val kind: VolleyKind,
    val swayAmp: Float,
    val swayFreq: Float,
    val swayPhase: Float
)

private enum class VolleyKind { STREAK, DOT }

private fun createVolleyParticle(
    now: Long,
    size: IntSize,
    rng: Random,
    palette: List<Color>
): VolleyParticle {
    val w = size.width.toFloat()
    val h = size.height.toFloat()
    val start = Offset(
        x = w * (0.15f + rng.nextFloat() * 0.7f),
        y = h + 24f
    )
    val speed = 780f + rng.nextFloat() * 520f
    val angle = (-105f + rng.nextFloat() * 50f)
    val rad = angle * (kotlin.math.PI / 180.0)
    val vx = kotlin.math.cos(rad).toFloat() * speed
    val vy = kotlin.math.sin(rad).toFloat() * speed
    val streak = rng.nextFloat() > 0.35f
    val kind = if (streak) VolleyKind.STREAK else VolleyKind.DOT
    val length = if (streak) 18f + rng.nextFloat() * 12f else 8f + rng.nextFloat() * 6f
    val stroke = if (streak) 2.2f + rng.nextFloat() * 1.2f else 3.0f + rng.nextFloat() * 1.4f
    val duration = (700L + rng.nextInt(600)).toLong() * 1_000_000L

    return VolleyParticle(
        start = start,
        velocity = Offset(vx, vy),
        color = palette[rng.nextInt(palette.size)],
        length = length,
        stroke = stroke,
        startNanos = now,
        durationNanos = duration,
        kind = kind,
        swayAmp = 6f + rng.nextFloat() * 10f,
        swayFreq = 6f + rng.nextFloat() * 8f,
        swayPhase = rng.nextFloat() * 6.28f
    )
}

private fun DrawScope.drawVolleyParticle(p: VolleyParticle, t: Float) {
    val elapsedSec = (t * p.durationNanos) / 1_000_000_000f
    val gravity = Offset(0f, 900f)
    val drift = p.velocity * elapsedSec
    val fall = gravity * (0.5f * elapsedSec * elapsedSec)
    val sway = kotlin.math.sin(t * p.swayFreq + p.swayPhase) * p.swayAmp
    val pos = p.start + drift + fall + Offset(sway, 0f)
    val alpha = (1f - t).coerceIn(0f, 1f)
    val color = p.color.copy(alpha = alpha)

    when (p.kind) {
        VolleyKind.STREAK -> {
            val dir = p.velocity
            val len = kotlin.math.sqrt(dir.x * dir.x + dir.y * dir.y).coerceAtLeast(0.001f)
            val nx = dir.x / len
            val ny = dir.y / len
            val tail = Offset(pos.x - nx * p.length, pos.y - ny * p.length)
            drawLine(
                color = color,
                start = pos,
                end = tail,
                strokeWidth = p.stroke,
                cap = StrokeCap.Round
            )
        }
        VolleyKind.DOT -> {
            drawCircle(
                color = color,
                radius = p.stroke * 1.2f,
                center = pos
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
    onClearCompleted: () -> Unit,
    showCompleted: Boolean,
    tagName: (String?) -> String?,
    noteCount: (TodoTask) -> Int,
    onOpenNotes: (TodoTask) -> Unit,
    dimScroll: Boolean,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop
) {
    val grouped = remember(tasks) {
        tasks.groupBy { t ->
            val now = nowInstant()
            val planned = t.plannedAt
            val deadline = t.deadline
            if (planned != null && planned < now) {
                "Запланированы ранее"
            } else if (deadline != null) {
                dateKey(deadline)
            } else if (planned != null) {
                dateKey(planned)
            } else {
                "No deadline"
            }
        }
    }
    val listState = rememberLazyListState()

    Box(Modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
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
                    Box(Modifier.itemPlacement()) {
                        AnimatedVisibility(
                            visible = showCompleted || !t.done,
                            enter = fadeIn(tween(160)),
                            exit = shrinkVertically(tween(320)) + fadeOut(tween(260))
                        ) {
                            TaskCard(
                                task = t,
                                tagLabel = tagName(t.tagId),
                                noteCount = noteCount(t),
                                backdrop = backdrop,
                                onOpenNotes = { onOpenNotes(t) },
                                onToggleDone = { onToggleDone(t.id) },
                                onToggleSub = { subId -> onToggleSub(t.id, subId) },
                                onOpenDetails = { onOpenDetails(t) },
                                onEdit = { onEdit(t) },
                                onDelete = { onDelete(t.id) },
                                onClearCompleted = onClearCompleted
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(120.dp)) }
        }
        FadingScrollEdges(
            listState = listState,
            modifier = Modifier.matchParentSize(),
            enabled = dimScroll
        )
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
    onClearCompleted: () -> Unit,
    showCompleted: Boolean,
    tagName: (String?) -> String?,
    noteCount: (TodoTask) -> Int,
    onOpenNotes: (TodoTask) -> Unit,
    dimScroll: Boolean,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop
) {
    val listState = rememberLazyListState()
    Box(Modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tasks, key = { it.id }) { t ->
                Box(Modifier.itemPlacement()) {
                    AnimatedVisibility(
                        visible = showCompleted || !t.done,
                        enter = fadeIn(tween(160)),
                        exit = shrinkVertically(tween(320)) + fadeOut(tween(260))
                    ) {
                        TaskCard(
                            task = t,
                            tagLabel = tagName(t.tagId),
                            noteCount = noteCount(t),
                            backdrop = backdrop,
                            onOpenNotes = { onOpenNotes(t) },
                            onToggleDone = { onToggleDone(t.id) },
                            onToggleSub = { subId -> onToggleSub(t.id, subId) },
                            onOpenDetails = { onOpenDetails(t) },
                            onEdit = { onEdit(t) },
                            onDelete = { onDelete(t.id) },
                            onClearCompleted = onClearCompleted
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(120.dp)) }
        }
        FadingScrollEdges(
            listState = listState,
            modifier = Modifier.matchParentSize(),
            enabled = dimScroll
        )
    }
}
