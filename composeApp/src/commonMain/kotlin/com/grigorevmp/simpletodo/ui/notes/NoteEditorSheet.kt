package com.grigorevmp.simpletodo.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.CloseIcon
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteEditorScreen(
    repo: TodoRepository,
    initial: Note?,
    tasks: List<TodoTask>,
    folderId: String?,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var content by remember { mutableStateOf(TextFieldValue(initial?.content ?: "")) }
    var taskId by remember { mutableStateOf(initial?.taskId) }
    var preview by remember { mutableStateOf(false) }

    val linkedTaskTitle = remember(taskId, tasks) {
        tasks.firstOrNull { it.id == taskId }?.title
    }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val imeVisible = imeBottom > 0

    val saveNote: () -> Unit = saveNote@{
        val t = title.trim()
        if (t.isEmpty()) return@saveNote

        scope.launch {
            if (initial == null) {
                repo.addNote(
                    title = t,
                    content = content.text,
                    taskId = taskId,
                    folderId = folderId
                )
            } else {
                repo.updateNote(
                    initial.copy(
                        title = t,
                        content = content.text,
                        taskId = taskId
                    )
                )
            }
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (initial == null) "New note" else "Edit note",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { preview = !preview }) {
                            Icon(
                                if (preview) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (preview) "Edit mode" else "Preview mode"
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(CloseIcon, contentDescription = "Close")
                        }
                    }
                }

                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Crossfade(targetState = preview, label = "note-preview") { isPreview ->
                                if (!isPreview) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = title,
                                            onValueChange = { title = it },
                                            label = { Text("Title") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        TaskLinkPicker(
                                            tasks = tasks,
                                            currentId = taskId,
                                            onPick = { taskId = it }
                                        )

                                        OutlinedTextField(
                                            value = content,
                                            onValueChange = { content = it },
                                            label = { Text("Markdown") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 10
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            title.ifBlank { "Untitled note" },
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            "Linked task: ${linkedTaskTitle ?: "No task"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (content.text.isBlank()) {
                                            Text(
                                                "No content",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            MarkdownText(content.text)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                        }

                        FadingScrollEdges(
                            scrollState = scrollState,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }

                if (!preview) {
                    MarkdownToolbar(
                        onWrapBold = { content = wrapSelection(content, "**", "**") },
                        onWrapItalic = { content = wrapSelection(content, "*", "*") },
                        onWrapCode = { content = wrapSelection(content, "`", "`") },
                        onH1 = { content = prefixLines(content, "# ") },
                        onH2 = { content = prefixLines(content, "## ") },
                        onBullet = { content = prefixLines(content, "- ") },
                        onTodo = { content = prefixLines(content, "- [ ] ") },
                        onOrdered = { content = prefixLines(content, "1. ") },
                        onQuote = { content = prefixLines(content, "> ") },
                        onCodeBlock = { content = wrapSelection(content, "```\n", "\n```") },
                        onLink = { content = wrapSelection(content, "[", "](url)") }
                    )
                }

                if (!imeVisible) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                            .padding(bottom = 80.dp)
                    ) {
                        Button(
                            onClick = saveNote,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (initial == null) "Create" else "Save") }
                    }
                } else {
                    Spacer(Modifier.height(88.dp))
                }
            }

            if (imeVisible) {
                FloatingActionButton(
                    onClick = saveNote,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(18.dp)
                        .imePadding()
                ) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskLinkPicker(
    tasks: List<TodoTask>,
    currentId: String?,
    onPick: (String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val current = tasks.firstOrNull { it.id == currentId }?.title ?: "No task"
    val filtered = remember(tasks, query) {
        if (query.isBlank()) tasks else tasks.filter { it.title.contains(query, ignoreCase = true) }
    }
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Linked task") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDialog) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select task") },
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
                                    text = { Text("No task") },
                                    onClick = {
                                        onPick(null)
                                        showDialog = false
                                    }
                                )
                            }
                            items(filtered.size) { idx ->
                                val t = filtered[idx]
                                DropdownMenuItem(
                                    text = { Text(t.title) },
                                    onClick = {
                                        onPick(t.id)
                                        showDialog = false
                                    }
                                )
                            }
                        }
                        FadingScrollEdges(
                            listState = listState,
                            modifier = Modifier.matchParentSize(),
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            }
        )
        LaunchedEffect(showDialog) {
            if (showDialog) focusRequester.requestFocus()
        }
    }
}

@Composable
private fun MarkdownToolbar(
    onWrapBold: () -> Unit,
    onWrapItalic: () -> Unit,
    onWrapCode: () -> Unit,
    onH1: () -> Unit,
    onH2: () -> Unit,
    onBullet: () -> Unit,
    onTodo: () -> Unit,
    onOrdered: () -> Unit,
    onQuote: () -> Unit,
    onCodeBlock: () -> Unit,
    onLink: () -> Unit
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(onClick = onWrapBold) { Text("B") }
        TextButton(onClick = onWrapItalic) { Text("I") }
        TextButton(onClick = onWrapCode) { Text("`") }
        TextButton(onClick = onCodeBlock) { Text("```") }
        TextButton(onClick = onLink) { Text("Link") }
        TextButton(onClick = onH1) { Text("H1") }
        TextButton(onClick = onH2) { Text("H2") }
        TextButton(onClick = onBullet) { Text("-") }
        TextButton(onClick = onTodo) { Text("[ ]") }
        TextButton(onClick = onOrdered) { Text("1.") }
        TextButton(onClick = onQuote) { Text(">") }
    }
}

private fun wrapSelection(
    value: TextFieldValue,
    prefix: String,
    suffix: String
): TextFieldValue {
    val sel = value.selection
    val start = sel.start.coerceIn(0, value.text.length)
    val end = sel.end.coerceIn(0, value.text.length)
    val before = value.text.substring(0, start)
    val selected = value.text.substring(start, end)
    val after = value.text.substring(end)

    return if (start == end) {
        val newText = before + prefix + suffix + after
        val cursor = start + prefix.length
        value.copy(text = newText, selection = TextRange(cursor, cursor))
    } else {
        val newText = before + prefix + selected + suffix + after
        value.copy(
            text = newText,
            selection = TextRange(start + prefix.length, end + prefix.length)
        )
    }
}

private fun prefixLines(value: TextFieldValue, prefix: String): TextFieldValue {
    val text = value.text
    val selStart = value.selection.start.coerceIn(0, text.length)
    val selEnd = value.selection.end.coerceIn(0, text.length)
    val lineStart = text.lastIndexOf('\n', startIndex = (selStart - 1).coerceAtLeast(0)).let {
        if (it == -1) 0 else it + 1
    }
    val lineEnd = text.indexOf('\n', startIndex = selEnd).let {
        if (it == -1) text.length else it
    }

    val segment = text.substring(lineStart, lineEnd)
    val prefixed = segment.lines().joinToString("\n") { line ->
        if (line.startsWith(prefix)) line else prefix + line
    }
    val newText = text.substring(0, lineStart) + prefixed + text.substring(lineEnd)
    val added = prefixed.length - segment.length
    return value.copy(
        text = newText,
        selection = TextRange(selStart + added, selEnd + added)
    )
}
