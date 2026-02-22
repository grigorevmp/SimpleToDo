package com.grigorevmp.simpletodo.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import kotlinx.coroutines.delay
import com.grigorevmp.simpletodo.ui.components.SimpleIcons
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.platform.isIos
import com.grigorevmp.simpletodo.ui.components.CloseIcon
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.NoOverscroll
import com.grigorevmp.simpletodo.ui.components.VisibilityIcon
import com.grigorevmp.simpletodo.ui.components.VisibilityOffIcon
import com.grigorevmp.simpletodo.ui.components.platformSavePainter
import com.grigorevmp.simpletodo.ui.notes.create.MarkdownToolbar
import com.grigorevmp.simpletodo.ui.notes.create.TaskLinkPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.close
import simpletodo.composeapp.generated.resources.save
import simpletodo.composeapp.generated.resources.visibility_off
import simpletodo.composeapp.generated.resources.visibility_on
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.note_editor_untitled
import simpletodo.composeapp.generated.resources.note_editor_new
import simpletodo.composeapp.generated.resources.note_editor_edit
import simpletodo.composeapp.generated.resources.note_editor_edit_mode_cd
import simpletodo.composeapp.generated.resources.note_editor_preview_mode_cd
import simpletodo.composeapp.generated.resources.note_editor_unfavorite_cd
import simpletodo.composeapp.generated.resources.note_editor_favorite_cd
import simpletodo.composeapp.generated.resources.note_editor_close_cd
import simpletodo.composeapp.generated.resources.note_editor_title_label
import simpletodo.composeapp.generated.resources.note_editor_text_label
import simpletodo.composeapp.generated.resources.note_editor_linked_task
import simpletodo.composeapp.generated.resources.note_editor_no_task
import simpletodo.composeapp.generated.resources.note_editor_no_content
import simpletodo.composeapp.generated.resources.note_editor_create
import simpletodo.composeapp.generated.resources.note_editor_save
import simpletodo.composeapp.generated.resources.note_editor_save_cd

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun NoteEditorScreen(
    repo: TodoRepository,
    initial: Note?,
    tasks: List<TodoTask>,
    folderId: String?,
    onDismiss: () -> Unit
) {
    val prefs by repo.prefs.collectAsState()
    var title by remember(initial?.id) { mutableStateOf(initial?.title ?: "") }
    var content by remember(initial?.id) { mutableStateOf(TextFieldValue(initial?.content ?: "")) }
    var taskId by remember(initial?.id) { mutableStateOf(initial?.taskId) }
    var favorite by remember(initial?.id) { mutableStateOf(initial?.favorite ?: false) }
    var preview by remember(initial?.id) { mutableStateOf(initial != null) }

    val linkedTaskTitle = remember(taskId, tasks) {
        tasks.firstOrNull { it.id == taskId }?.title
    }

    val scope = rememberCoroutineScope()
    var editorVisible by remember { mutableStateOf(true) }
    val exitDurationMs = 220

    fun requestDismiss(animate: Boolean) {
        if (!animate) {
            onDismiss()
            return
        }
        if (!editorVisible) return
        editorVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismiss()
        }
    }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val imeVisible = imeBottom > 0

    val untitledNote = stringResource(Res.string.note_editor_untitled)
    val saveNote: () -> Unit = saveNote@{
        val t = title.trim().ifBlank { untitledNote }

        scope.launch {
            if (initial == null) {
                repo.addNote(
                    title = t, content = content.text, taskId = taskId, folderId = folderId, favorite = favorite
                )
            } else {
                repo.updateNote(
                    initial.copy(
                        title = t, content = content.text, taskId = taskId, favorite = favorite
                    )
                )
            }
            requestDismiss(animate = true)
        }
    }

    AnimatedVisibility(
        visible = editorVisible,
        enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.98f),
        exit = fadeOut(tween(exitDurationMs)) + scaleOut(tween(exitDurationMs), targetScale = 0.98f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (initial == null) stringResource(Res.string.note_editor_new) else stringResource(Res.string.note_editor_edit),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iosTopBarIconSize: Dp = 24.dp
                        val visibilityPainter = if (isIos) {
                            painterResource(
                                if (preview) Res.drawable.visibility_off else Res.drawable.visibility_on
                            )
                        } else {
                            rememberVectorPainter(if (preview) VisibilityOffIcon else VisibilityIcon)
                        }
                        val visibilityTint = if (isIos) MaterialTheme.colorScheme.onSurface else LocalContentColor.current
                        IconButton(onClick = { preview = !preview }) {
                            Icon(
                                painter = visibilityPainter,
                                contentDescription = if (preview) {
                                    stringResource(Res.string.note_editor_edit_mode_cd)
                                } else {
                                    stringResource(Res.string.note_editor_preview_mode_cd)
                                },
                                tint = visibilityTint,
                                modifier = if (isIos) Modifier.size(iosTopBarIconSize) else Modifier
                            )
                        }
                        IconButton(onClick = {
                            favorite = !favorite
                            if (initial != null) {
                                scope.launch { repo.toggleNoteFavorite(initial.id) }
                            }
                        }) {
                            Icon(
                                imageVector = SimpleIcons.Star,
                                contentDescription = if (favorite) {
                                    stringResource(Res.string.note_editor_unfavorite_cd)
                                } else {
                                    stringResource(Res.string.note_editor_favorite_cd)
                                },
                                tint = if (favorite) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                                modifier = Modifier.size(iosTopBarIconSize)
                            )
                        }
                        val closePainter = if (isIos) {
                            painterResource(Res.drawable.close)
                        } else {
                            rememberVectorPainter(CloseIcon)
                        }
                        val closeTint = if (isIos) MaterialTheme.colorScheme.onSurface else LocalContentColor.current
                        IconButton(onClick = { requestDismiss(animate = false) }) {
                            Icon(
                                painter = closePainter,
                                contentDescription = stringResource(Res.string.note_editor_close_cd),
                                tint = closeTint,
                                modifier = if (isIos) Modifier.size(iosTopBarIconSize) else Modifier
                            )
                        }
                    }
                }

                NoOverscroll {
                    Box(
                        Modifier.fillMaxWidth().weight(1f)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().verticalScroll(scrollState)
                                .padding(horizontal = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Crossfade(targetState = preview, label = "note-preview") { isPreview ->
                                if (!isPreview) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = title,
                                            onValueChange = { title = it },
                                            label = { Text(stringResource(Res.string.note_editor_title_label)) },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        TaskLinkPicker(
                                            tasks = tasks,
                                            currentId = taskId,
                                            dimScroll = prefs.dimScroll,
                                            onPick = { taskId = it })

                                        OutlinedTextField(
                                            value = content,
                                            onValueChange = { content = it },
                                            label = { Text(stringResource(Res.string.note_editor_text_label)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 12
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            title.ifBlank { stringResource(Res.string.note_editor_untitled) },
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            stringResource(
                                                Res.string.note_editor_linked_task,
                                                linkedTaskTitle ?: stringResource(Res.string.note_editor_no_task)
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (content.text.isBlank()) {
                                            Text(
                                                stringResource(Res.string.note_editor_no_content),
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
                            modifier = Modifier.matchParentSize(),
                            enabled = prefs.dimScroll
                        )
                    }
                }

                AnimatedVisibility(!preview) {
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
                        onLink = { content = wrapSelection(content, "[", "](url)") })
                }

                AnimatedVisibility(!imeVisible) {
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp).padding(bottom = 80.dp)
                    ) {
                        Button(
                            onClick = saveNote, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (initial == null) stringResource(Res.string.note_editor_create) else stringResource(Res.string.note_editor_save))
                        }
                    }
                }
            }

            if (imeVisible) {
                val savePainter = platformSavePainter()
                FloatingActionButton(
                    onClick = saveNote,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).imePadding(),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = savePainter,
                        contentDescription = stringResource(Res.string.note_editor_save_cd),
                        tint = if (isIos) Color.Unspecified else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        }
    }
}

private fun wrapSelection(
    value: TextFieldValue, prefix: String, suffix: String
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
            text = newText, selection = TextRange(start + prefix.length, end + prefix.length)
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
    val newText = text.take(lineStart) + prefixed + text.substring(lineEnd)
    val added = prefixed.length - segment.length
    return value.copy(
        text = newText, selection = TextRange(selStart + added, selEnd + added)
    )
}
