package com.grigorevmp.simpletodo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.Subtask
import com.grigorevmp.simpletodo.model.Tag
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.platform.PlatformDateTimePicker
import com.grigorevmp.simpletodo.ui.components.AddIcon
import com.grigorevmp.simpletodo.ui.components.CloseIcon
import com.grigorevmp.simpletodo.ui.components.DeleteIcon
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.util.newId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskEditorSheet(
    repo: TodoRepository,
    prefsTagList: List<Tag>,
    notes: List<Note>,
    initial: TodoTask?,
    onDismiss: () -> Unit
) {
    val prefs by repo.prefs.collectAsState()
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var plan by remember { mutableStateOf(initial?.plan ?: "") }
    var importance by remember { mutableStateOf(initial?.importance ?: Importance.NORMAL) }
    var tagId by remember { mutableStateOf(initial?.tagId) }
    var noteId by remember { mutableStateOf(initial?.noteId) }
    var deadline by remember { mutableStateOf(initial?.deadline) }
    var plannedAt by remember { mutableStateOf(initial?.plannedAt) }
    var estimateHoursText by remember {
        mutableStateOf(initial?.estimateHours?.let { it.toString() } ?: "")
    }
    var showAdvanced by remember { mutableStateOf(false) }

    var subtasks by remember {
        mutableStateOf(
            initial?.subtasks ?: emptyList()
        )
    }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(
            Modifier.fillMaxWidth().imePadding()
        ) {
            Column(Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().verticalScroll(scrollState)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (initial == null) "New task" else "Edit task",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Icon(CloseIcon, contentDescription = "Close")
                                }
                            }

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Task title") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = plan,
                                onValueChange = { plan = it },
                                label = { Text("Plan / notes") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            TextButton(
                                onClick = { showAdvanced = !showAdvanced },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (showAdvanced) "Hide details" else "Show details",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            AnimatedVisibility(
                                visible = showAdvanced,
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "Planned time to do",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    PlatformDateTimePicker(
                                        current = plannedAt, onPicked = { plannedAt = it })

                                    OutlinedTextField(
                                        value = estimateHoursText,
                                        onValueChange = { estimateHoursText = it },
                                        label = { Text("Estimate time to complete (hours)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Deadline", style = MaterialTheme.typography.titleMedium)
                                    PlatformDateTimePicker(
                                        current = deadline, onPicked = { deadline = it })

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Priority", style = MaterialTheme.typography.titleMedium)
                                    ImportancePicker(
                                        current = importance,
                                        onPick = { importance = it })

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tag", style = MaterialTheme.typography.titleMedium)
                                    TagPicker(
                                        tags = prefsTagList,
                                        currentId = tagId,
                                        onPick = { tagId = it })

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Linked note",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    NotePicker(
                                        notes = notes,
                                        currentId = noteId,
                                        dimScroll = prefs.dimScroll,
                                        onPick = { noteId = it }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Subtasks", style = MaterialTheme.typography.titleMedium)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        subtasks.forEachIndexed { idx, s ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = s.done, onCheckedChange = {
                                                        subtasks = subtasks.map {
                                                            if (it.id == s.id) it.copy(done = !it.done) else it
                                                        }
                                                    })
                                                Spacer(Modifier.width(8.dp))
                                                OutlinedTextField(
                                                    value = s.text,
                                                    onValueChange = { text ->
                                                        subtasks = subtasks.map {
                                                            if (it.id == s.id) it.copy(text = text) else it
                                                        }
                                                    },
                                                    label = { Text("Subtask ${idx + 1}") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        subtasks =
                                                            subtasks.filterNot { it.id == s.id }
                                                    }, enabled = subtasks.isNotEmpty()
                                                ) {
                                                    Icon(
                                                        DeleteIcon,
                                                        contentDescription = "Remove subtask"
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                subtasks = subtasks + Subtask(
                                                    id = newId("sub"),
                                                    text = "New subtask",
                                                    done = false
                                                )
                                            }, modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(AddIcon, contentDescription = "Add")
                                            Spacer(Modifier.width(8.dp))
                                            Text("Add subtask")
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val t = title.trim()
                                    if (t.isEmpty()) return@Button

                                    scope.launch {
                                        val cleanedSubtasks = subtasks.map {
                                            val cleaned = it.text.trim().ifBlank { "Subtask" }
                                            it.copy(text = cleaned)
                                        }
                                        val estimateHours = estimateHoursText.toDoubleOrNull()
                                        if (initial == null) {
                                            repo.addTask(
                                                title = t,
                                                plan = plan,
                                                noteId = noteId,
                                                plannedAt = plannedAt,
                                                estimateHours = estimateHours,
                                                deadline = deadline,
                                                importance = importance,
                                                tagId = tagId,
                                                subtasks = cleanedSubtasks
                                            )
                                        } else {
                                            repo.updateTask(
                                                initial.copy(
                                                    title = t,
                                                    plan = plan,
                                                    noteId = noteId,
                                                    plannedAt = plannedAt,
                                                    estimateHours = estimateHours,
                                                    deadline = deadline,
                                                    importance = importance,
                                                    tagId = tagId,
                                                    subtasks = cleanedSubtasks
                                                )
                                            )
                                        }
                                        onDismiss()
                                    }
                                }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (initial == null) "Create" else "Save")
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        FadingScrollEdges(
                            scrollState = scrollState,
                            modifier = Modifier.matchParentSize(),
                            color = BottomSheetDefaults.ContainerColor,
                            enabled = prefs.dimScroll
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportancePicker(current: Importance, onPick: (Importance) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Importance.entries.forEach { i ->
            FilterChip(selected = current == i, onClick = { onPick(i) }, label = { Text(label(i)) })
        }
    }
}

private fun label(i: Importance): String = when (i) {
    Importance.LOW -> "Low"
    Importance.NORMAL -> "Normal"
    Importance.HIGH -> "High"
    Importance.CRITICAL -> "Critical"
}

@Composable
@ExperimentalMaterial3Api
private fun TagPicker(tags: List<Tag>, currentId: String?, onPick: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = tags.firstOrNull { it.id == currentId }?.name ?: "No tag"

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tag") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("No tag") },
                onClick = { onPick(null); expanded = false })
            tags.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.name) },
                    onClick = { onPick(t.id); expanded = false })
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
private fun NotePicker(
    notes: List<Note>,
    currentId: String?,
    dimScroll: Boolean,
    onPick: (String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val current = notes.firstOrNull { it.id == currentId }?.title ?: "No note"
    val filtered = remember(notes, query) {
        if (query.isBlank()) notes else notes.filter { it.title.contains(query, ignoreCase = true) }
    }
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Note") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDialog) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search...") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                    Box(Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                DropdownMenuItem(
                                    text = { Text("No note") },
                                    onClick = {
                                        onPick(null)
                                        showDialog = false
                                    }
                                )
                            }
                            items(filtered.size) { idx ->
                                val n = filtered[idx]
                                DropdownMenuItem(
                                    text = { Text(n.title) },
                                    onClick = {
                                        onPick(n.id)
                                        showDialog = false
                                    }
                                )
                            }
                        }

                        FadingScrollEdges(
                            listState = listState,
                            modifier = Modifier.matchParentSize(),
                            color = AlertDialogDefaults.containerColor,
                            enabled = dimScroll
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            }
        )
        androidx.compose.runtime.LaunchedEffect(showDialog) {
            if (showDialog) focusRequester.requestFocus()
        }
    }
}
