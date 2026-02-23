package com.grigorevmp.simpletodo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.grigorevmp.simpletodo.ui.components.CircleCheckbox
import com.grigorevmp.simpletodo.ui.components.CloseIcon
import com.grigorevmp.simpletodo.ui.components.DeleteIcon
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.NoOverscroll
import com.grigorevmp.simpletodo.util.newId
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.task_editor_new
import simpletodo.composeapp.generated.resources.task_editor_edit
import simpletodo.composeapp.generated.resources.task_close_cd
import simpletodo.composeapp.generated.resources.task_title_label
import simpletodo.composeapp.generated.resources.task_plan_label
import simpletodo.composeapp.generated.resources.task_show_details
import simpletodo.composeapp.generated.resources.task_hide_details
import simpletodo.composeapp.generated.resources.task_planned_time_label
import simpletodo.composeapp.generated.resources.task_estimate_label
import simpletodo.composeapp.generated.resources.task_deadline_label
import simpletodo.composeapp.generated.resources.task_priority_label
import simpletodo.composeapp.generated.resources.task_tag_label
import simpletodo.composeapp.generated.resources.task_linked_note_label
import simpletodo.composeapp.generated.resources.home_subtasks
import simpletodo.composeapp.generated.resources.task_subtask_label
import simpletodo.composeapp.generated.resources.task_remove_subtask_cd
import simpletodo.composeapp.generated.resources.task_new_subtask_default
import simpletodo.composeapp.generated.resources.task_add_cd
import simpletodo.composeapp.generated.resources.task_add_subtask
import simpletodo.composeapp.generated.resources.task_subtask_default
import simpletodo.composeapp.generated.resources.task_create
import simpletodo.composeapp.generated.resources.task_save
import simpletodo.composeapp.generated.resources.importance_low
import simpletodo.composeapp.generated.resources.importance_normal
import simpletodo.composeapp.generated.resources.importance_high
import simpletodo.composeapp.generated.resources.importance_critical
import simpletodo.composeapp.generated.resources.task_no_tag
import simpletodo.composeapp.generated.resources.task_tag_select
import simpletodo.composeapp.generated.resources.task_no_note
import simpletodo.composeapp.generated.resources.task_note_label
import simpletodo.composeapp.generated.resources.task_select_note
import simpletodo.composeapp.generated.resources.search_placeholder
import simpletodo.composeapp.generated.resources.task_close

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
    val defaultSubtaskLabel = stringResource(Res.string.task_subtask_default)
    val newSubtaskLabel = stringResource(Res.string.task_new_subtask_default)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(
            Modifier.fillMaxWidth().imePadding()
        ) {
            Column(Modifier.fillMaxSize()) {
                NoOverscroll {
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
                                    if (initial == null) stringResource(Res.string.task_editor_new) else stringResource(Res.string.task_editor_edit),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Icon(CloseIcon, contentDescription = stringResource(Res.string.task_close_cd))
                                }
                            }

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text(stringResource(Res.string.task_title_label)) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = plan,
                                onValueChange = { plan = it },
                                label = { Text(stringResource(Res.string.task_plan_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            TextButton(
                                onClick = { showAdvanced = !showAdvanced },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (showAdvanced) stringResource(Res.string.task_hide_details) else stringResource(Res.string.task_show_details),
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
                                        stringResource(Res.string.task_planned_time_label),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    PlatformDateTimePicker(
                                        current = plannedAt, onPicked = { plannedAt = it })

                                    OutlinedTextField(
                                        value = estimateHoursText,
                                        onValueChange = { estimateHoursText = it },
                                        label = { Text(stringResource(Res.string.task_estimate_label)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(Res.string.task_deadline_label), style = MaterialTheme.typography.titleMedium)
                                    PlatformDateTimePicker(
                                        current = deadline, onPicked = { deadline = it })

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(Res.string.task_priority_label), style = MaterialTheme.typography.titleMedium)
                                    ImportancePicker(
                                        current = importance,
                                        onPick = { importance = it })

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(Res.string.task_tag_label), style = MaterialTheme.typography.titleMedium)
                                    TagPicker(
                                        tags = prefsTagList,
                                        currentId = tagId,
                                        onPick = { tagId = it })

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        stringResource(Res.string.task_linked_note_label),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    NotePicker(
                                        notes = notes,
                                        currentId = noteId,
                                        dimScroll = prefs.dimScroll,
                                        onPick = { noteId = it }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(Res.string.home_subtasks), style = MaterialTheme.typography.titleMedium)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 240.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        subtasks.forEachIndexed { idx, s ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                CircleCheckbox(
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
                                                    label = { Text(stringResource(Res.string.task_subtask_label, (idx + 1).toString())) },
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
                                                        contentDescription = stringResource(Res.string.task_remove_subtask_cd)
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                subtasks = subtasks + Subtask(
                                                    id = newId("sub"),
                                                    text = newSubtaskLabel,
                                                    done = false
                                                )
                                            }, modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(AddIcon, contentDescription = stringResource(Res.string.task_add_cd))
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(Res.string.task_add_subtask))
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
                                            val cleaned = it.text.trim().ifBlank { defaultSubtaskLabel }
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
                                Text(if (initial == null) stringResource(Res.string.task_create) else stringResource(Res.string.task_save))
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Importance.entries.toList().chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { i ->
                    FilterChip(
                        selected = current == i,
                        onClick = { onPick(i) },
                        label = { Text(label(i)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun label(i: Importance): String = when (i) {
    Importance.LOW -> stringResource(Res.string.importance_low)
    Importance.NORMAL -> stringResource(Res.string.importance_normal)
    Importance.HIGH -> stringResource(Res.string.importance_high)
    Importance.CRITICAL -> stringResource(Res.string.importance_critical)
}

@Composable
@ExperimentalMaterial3Api
private fun TagPicker(tags: List<Tag>, currentId: String?, onPick: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = tags.firstOrNull { it.id == currentId }?.name ?: stringResource(Res.string.task_no_tag)

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.task_tag_select)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.task_no_tag)) },
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
    val current = notes.firstOrNull { it.id == currentId }?.title ?: stringResource(Res.string.task_no_note)
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
            label = { Text(stringResource(Res.string.task_note_label)) },
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
            title = { Text(stringResource(Res.string.task_select_note)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                    Box(Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.task_no_note)) },
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
                TextButton(onClick = { showDialog = false }) { Text(stringResource(Res.string.task_close)) }
            }
        )
        androidx.compose.runtime.LaunchedEffect(showDialog) {
            if (showDialog) focusRequester.requestFocus()
        }
    }
}
