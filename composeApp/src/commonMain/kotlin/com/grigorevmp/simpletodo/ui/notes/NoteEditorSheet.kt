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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.layout.onSizeChanged
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.platform.isIos
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.NoOverscroll
import com.grigorevmp.simpletodo.ui.components.VisibilityIcon
import com.grigorevmp.simpletodo.ui.components.VisibilityOffIcon
import com.grigorevmp.simpletodo.ui.components.AddIcon
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
import simpletodo.composeapp.generated.resources.note_block_add
import simpletodo.composeapp.generated.resources.note_block_duplicate
import simpletodo.composeapp.generated.resources.note_block_delete
import simpletodo.composeapp.generated.resources.note_block_turn_into
import simpletodo.composeapp.generated.resources.note_slash_hint
import simpletodo.composeapp.generated.resources.note_block_h1
import simpletodo.composeapp.generated.resources.note_block_h2
import simpletodo.composeapp.generated.resources.note_block_h3
import simpletodo.composeapp.generated.resources.note_block_text
import simpletodo.composeapp.generated.resources.note_block_bullet
import simpletodo.composeapp.generated.resources.note_block_numbered
import simpletodo.composeapp.generated.resources.note_block_todo
import simpletodo.composeapp.generated.resources.note_block_quote
import simpletodo.composeapp.generated.resources.note_block_code
import simpletodo.composeapp.generated.resources.note_block_divider
import simpletodo.composeapp.generated.resources.note_block_callout
import simpletodo.composeapp.generated.resources.note_block_table
import simpletodo.composeapp.generated.resources.note_block_kanban
import simpletodo.composeapp.generated.resources.note_block_attachment
import simpletodo.composeapp.generated.resources.note_block_note_link
import simpletodo.composeapp.generated.resources.note_block_add_row
import simpletodo.composeapp.generated.resources.note_block_add_column
import simpletodo.composeapp.generated.resources.note_block_add_card
import simpletodo.composeapp.generated.resources.note_block_add_column_label
import simpletodo.composeapp.generated.resources.note_block_pick_file
import simpletodo.composeapp.generated.resources.note_block_open_file
import simpletodo.composeapp.generated.resources.note_block_linked_from
import com.grigorevmp.simpletodo.model.NoteBlockType
import com.grigorevmp.simpletodo.model.TextBlock
import com.grigorevmp.simpletodo.model.DividerBlock
import com.grigorevmp.simpletodo.model.TableBlock
import com.grigorevmp.simpletodo.model.KanbanBlock
import com.grigorevmp.simpletodo.model.KanbanColumn
import com.grigorevmp.simpletodo.model.AttachmentBlock
import com.grigorevmp.simpletodo.model.NoteLinkBlock
import com.grigorevmp.simpletodo.model.NoteBlock
import com.grigorevmp.simpletodo.ui.notes.blocks.defaultBlocksFromContent
import com.grigorevmp.simpletodo.ui.notes.blocks.blocksToPlainText
import com.grigorevmp.simpletodo.util.newId

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalResourceApi::class
)

private data class BlockState(
    val id: String,
    val type: NoteBlockType,
    val value: TextFieldValue,
    val checked: Boolean = false,
    val table: TableBlock? = null,
    val kanban: KanbanBlock? = null,
    val attachment: AttachmentBlock? = null,
    val noteLinkId: String? = null
)

@Composable
fun NoteEditorScreen(
    repo: TodoRepository,
    initial: Note?,
    tasks: List<TodoTask>,
    folderId: String?,
    onDismiss: () -> Unit
) {
    val prefs by repo.prefs.collectAsState()
    val allNotes by repo.notes.collectAsState()
    var title by remember(initial?.id) { mutableStateOf(initial?.title ?: "") }
    var taskId by remember(initial?.id) { mutableStateOf(initial?.taskId) }
    var favorite by remember(initial?.id) { mutableStateOf(initial?.favorite ?: false) }
    var preview by remember(initial?.id) { mutableStateOf(initial != null) }

    val initialBlocks = remember(initial?.id) {
        val source = if (initial?.blocks?.isNotEmpty() == true) {
            initial.blocks
        } else {
            defaultBlocksFromContent(initial?.content ?: "")
        }
        source.map { block ->
            when (block) {
                is TextBlock -> BlockState(
                    id = block.id,
                    type = block.type,
                    value = TextFieldValue(block.text),
                    checked = block.checked
                )
                is DividerBlock -> BlockState(
                    id = block.id,
                    type = NoteBlockType.DIVIDER,
                    value = TextFieldValue(""),
                    checked = false
                )
                is TableBlock -> BlockState(
                    id = block.id,
                    type = NoteBlockType.TABLE,
                    value = TextFieldValue(""),
                    table = block
                )
                is KanbanBlock -> BlockState(
                    id = block.id,
                    type = NoteBlockType.KANBAN,
                    value = TextFieldValue(""),
                    kanban = block
                )
                is AttachmentBlock -> BlockState(
                    id = block.id,
                    type = NoteBlockType.ATTACHMENT,
                    value = TextFieldValue(block.uri),
                    attachment = block
                )
                is NoteLinkBlock -> BlockState(
                    id = block.id,
                    type = NoteBlockType.NOTE_LINK,
                    value = TextFieldValue(""),
                    noteLinkId = block.noteId
                )
            }
        }
    }
    val blocks = remember(initial?.id) { mutableStateListOf<BlockState>().apply { addAll(initialBlocks) } }
    var activeBlockId by remember(initial?.id) { mutableStateOf(blocks.firstOrNull()?.id) }

    val linkedTaskTitle = remember(taskId, tasks) {
        tasks.firstOrNull { it.id == taskId }?.title
    }
    val backlinks = remember(allNotes, initial?.id) {
        val currentId = initial?.id ?: return@remember emptyList()
        allNotes.filter { note ->
            note.id != currentId && note.blocks.any { it is NoteLinkBlock && it.noteId == currentId }
        }
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
    val navBottom = WindowInsets.navigationBars.getBottom(density)
    val imeVisible = imeBottom > 0
    val imeBottomDp = with(density) { imeBottom.toDp() }
    val navBottomDp = with(density) { navBottom.toDp() }
    val listState = rememberLazyListState()
    val blockIndexOffset = 2
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    val untitledNote = stringResource(Res.string.note_editor_untitled)
    var bottomBarHeightDp by remember { mutableStateOf(120.dp) }
    val blockLabels = mapOf(
        NoteBlockType.HEADING_1 to stringResource(Res.string.note_block_h1),
        NoteBlockType.HEADING_2 to stringResource(Res.string.note_block_h2),
        NoteBlockType.HEADING_3 to stringResource(Res.string.note_block_h3),
        NoteBlockType.PARAGRAPH to stringResource(Res.string.note_block_text),
        NoteBlockType.BULLET to stringResource(Res.string.note_block_bullet),
        NoteBlockType.NUMBERED to stringResource(Res.string.note_block_numbered),
        NoteBlockType.TODO to stringResource(Res.string.note_block_todo),
        NoteBlockType.QUOTE to stringResource(Res.string.note_block_quote),
        NoteBlockType.CODE to stringResource(Res.string.note_block_code),
        NoteBlockType.DIVIDER to stringResource(Res.string.note_block_divider),
        NoteBlockType.CALLOUT to stringResource(Res.string.note_block_callout),
        NoteBlockType.TABLE to stringResource(Res.string.note_block_table),
        NoteBlockType.KANBAN to stringResource(Res.string.note_block_kanban),
        NoteBlockType.ATTACHMENT to stringResource(Res.string.note_block_attachment),
        NoteBlockType.NOTE_LINK to stringResource(Res.string.note_block_note_link)
    )

    fun updateBlock(id: String, updater: (BlockState) -> BlockState) {
        val idx = blocks.indexOfFirst { it.id == id }
        if (idx == -1) return
        blocks[idx] = updater(blocks[idx])
    }

    fun insertBlockAfter(index: Int, type: NoteBlockType = NoteBlockType.PARAGRAPH) {
        val newBlock = when (type) {
            NoteBlockType.TABLE -> BlockState(
                id = newId("nb"),
                type = NoteBlockType.TABLE,
                value = TextFieldValue(""),
                table = TableBlock(
                    id = newId("tbl"),
                    rows = 2,
                    cols = 2,
                    cells = List(2) { List(2) { "" } }
                )
            )
            NoteBlockType.KANBAN -> BlockState(
                id = newId("nb"),
                type = NoteBlockType.KANBAN,
                value = TextFieldValue(""),
                kanban = KanbanBlock(
                    id = newId("kb"),
                    columns = listOf(
                        KanbanColumn(id = newId("kc"), title = "Todo", cards = emptyList()),
                        KanbanColumn(id = newId("kc"), title = "Doing", cards = emptyList()),
                        KanbanColumn(id = newId("kc"), title = "Done", cards = emptyList())
                    )
                )
            )
            NoteBlockType.ATTACHMENT -> BlockState(
                id = newId("nb"),
                type = NoteBlockType.ATTACHMENT,
                value = TextFieldValue(""),
                attachment = AttachmentBlock(id = newId("att"), uri = "")
            )
            NoteBlockType.NOTE_LINK -> BlockState(
                id = newId("nb"),
                type = NoteBlockType.NOTE_LINK,
                value = TextFieldValue(""),
                noteLinkId = null
            )
            NoteBlockType.DIVIDER -> BlockState(
                id = newId("nb"),
                type = NoteBlockType.DIVIDER,
                value = TextFieldValue("")
            )
            else -> BlockState(
                id = newId("nb"),
                type = type,
                value = TextFieldValue(""),
                checked = false
            )
        }
        blocks.add(index + 1, newBlock)
        activeBlockId = newBlock.id
    }

    fun removeBlock(index: Int) {
        if (blocks.size <= 1) return
        val removed = blocks.removeAt(index)
        if (activeBlockId == removed.id) {
            activeBlockId = blocks.getOrNull(index)?.id ?: blocks.lastOrNull()?.id
        }
    }

    fun duplicateBlock(index: Int) {
        val src = blocks[index]
        val dup = src.copy(id = newId("nb"))
        blocks.add(index + 1, dup)
        activeBlockId = dup.id
    }

    fun updateActiveBlock(transform: (TextFieldValue) -> TextFieldValue) {
        val id = activeBlockId ?: return
        updateBlock(id) { it.copy(value = transform(it.value)) }
    }
    val saveNote: () -> Unit = saveNote@{
        val t = title.trim().ifBlank { untitledNote }
        val savedBlocks: List<NoteBlock> = blocks.map { b ->
            when (b.type) {
                NoteBlockType.DIVIDER -> DividerBlock(id = b.id)
                NoteBlockType.TABLE -> b.table ?: TableBlock(
                    id = b.id,
                    rows = 2,
                    cols = 2,
                    cells = List(2) { List(2) { "" } }
                )
                NoteBlockType.KANBAN -> b.kanban ?: KanbanBlock(
                    id = b.id,
                    columns = listOf(
                        KanbanColumn(id = newId("kc"), title = "Todo", cards = emptyList())
                    )
                )
                NoteBlockType.ATTACHMENT -> b.attachment ?: AttachmentBlock(
                    id = b.id,
                    uri = b.value.text,
                    name = null,
                    mime = null
                )
                NoteBlockType.NOTE_LINK -> NoteLinkBlock(
                    id = b.id,
                    noteId = b.noteLinkId ?: ""
                )
                else -> TextBlock(
                    id = b.id,
                    text = b.value.text,
                    type = b.type,
                    checked = b.checked
                )
            }
        }
        val contentText = blocksToPlainText(savedBlocks)

        scope.launch {
            if (initial == null) {
                repo.addNote(
                    title = t,
                    content = contentText,
                    blocks = savedBlocks,
                    taskId = taskId,
                    folderId = folderId,
                    favorite = favorite
                )
            } else {
                repo.updateNote(
                    initial.copy(
                        title = t,
                        content = contentText,
                        blocks = savedBlocks,
                        taskId = taskId,
                        favorite = favorite
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
                val bottomBarHeight = if (preview) 72.dp else 120.dp
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (initial == null) stringResource(Res.string.note_editor_new) else stringResource(
                                Res.string.note_editor_edit
                            ),
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
                            val visibilityTint =
                                if (isIos) MaterialTheme.colorScheme.onSurface else LocalContentColor.current
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
                        }
                    }

                    NoOverscroll {
                        Box(
                            Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Crossfade(
                                targetState = preview,
                                label = "note-preview"
                            ) { isPreview ->
                                if (isPreview) {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(scrollState)
                                            .padding(horizontal = 18.dp)
                                            .padding(
                                                bottom = bottomBarHeight +
                                                    if (imeVisible) imeBottomDp else navBottomDp
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            title.ifBlank { stringResource(Res.string.note_editor_untitled) },
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            stringResource(
                                                Res.string.note_editor_linked_task,
                                                linkedTaskTitle
                                                    ?: stringResource(Res.string.note_editor_no_task)
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (blocks.isEmpty() || blocks.all { it.value.text.isBlank() && it.type != NoteBlockType.DIVIDER }) {
                                            Text(
                                                stringResource(Res.string.note_editor_no_content),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            NoteBlocksPreview(blocks = blocks, notes = allNotes)
                                        }
                                        if (backlinks.isNotEmpty()) {
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                stringResource(Res.string.note_block_linked_from),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                backlinks.forEach { note ->
                                                    Text(
                                                        note.title.ifBlank { stringResource(Res.string.note_editor_untitled) },
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
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
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            start = 18.dp,
                                            end = 18.dp,
                                            top = 0.dp,
                                            bottom = bottomBarHeightDp + navBottomDp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.imePadding()
                                    ) {
                                        item {
                                            OutlinedTextField(
                                                value = title,
                                                onValueChange = { title = it },
                                                label = { Text(stringResource(Res.string.note_editor_title_label)) },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        item {
                                            TaskLinkPicker(
                                                tasks = tasks,
                                                currentId = taskId,
                                                dimScroll = prefs.dimScroll,
                                                onPick = { taskId = it })
                                        }

                                        itemsIndexed(blocks, key = { _, b -> b.id }) { index, block ->
                                            val isDragging = draggingIndex == index
                                            BlockRow(
                                                block = block,
                                                index = index,
                                                isActive = activeBlockId == block.id,
                                                typeLabels = blockLabels,
                                                notes = allNotes,
                                                currentNoteId = initial?.id,
                                                onActive = { activeBlockId = block.id },
                                                onChangeValue = { v -> updateBlock(block.id) { it.copy(value = v) } },
                                                onToggleTodo = { checked -> updateBlock(block.id) { it.copy(checked = checked) } },
                                                onUpdateTable = { table -> updateBlock(block.id) { it.copy(table = table) } },
                                                onUpdateKanban = { kanban -> updateBlock(block.id) { it.copy(kanban = kanban) } },
                                                onUpdateAttachment = { att, v ->
                                                    updateBlock(block.id) { it.copy(attachment = att, value = v) }
                                                },
                                                onUpdateNoteLink = { noteId -> updateBlock(block.id) { it.copy(noteLinkId = noteId) } },
                                                onAddBelow = { insertBlockAfter(index) },
                                                onDuplicate = { duplicateBlock(index) },
                                                onDelete = { removeBlock(index) },
                                                onChangeType = { type ->
                                                    when (type) {
                                                        NoteBlockType.DIVIDER -> {
                                                            blocks[index] = block.copy(type = NoteBlockType.DIVIDER, value = TextFieldValue(""))
                                                            insertBlockAfter(index)
                                                        }
                                                        NoteBlockType.TABLE -> {
                                                            blocks[index] = block.copy(
                                                                type = NoteBlockType.TABLE,
                                                                value = TextFieldValue(""),
                                                                table = TableBlock(
                                                                    id = newId("tbl"),
                                                                    rows = 2,
                                                                    cols = 2,
                                                                    cells = List(2) { List(2) { "" } }
                                                                )
                                                            )
                                                        }
                                                        NoteBlockType.KANBAN -> {
                                                            blocks[index] = block.copy(
                                                                type = NoteBlockType.KANBAN,
                                                                value = TextFieldValue(""),
                                                                kanban = KanbanBlock(
                                                                    id = newId("kb"),
                                                                    columns = listOf(
                                                                        KanbanColumn(id = newId("kc"), title = "Todo", cards = emptyList()),
                                                                        KanbanColumn(id = newId("kc"), title = "Doing", cards = emptyList()),
                                                                        KanbanColumn(id = newId("kc"), title = "Done", cards = emptyList())
                                                                    )
                                                                )
                                                            )
                                                        }
                                                        NoteBlockType.ATTACHMENT -> {
                                                            blocks[index] = block.copy(
                                                                type = NoteBlockType.ATTACHMENT,
                                                                value = TextFieldValue(""),
                                                                attachment = AttachmentBlock(id = newId("att"), uri = "")
                                                            )
                                                        }
                                                        NoteBlockType.NOTE_LINK -> {
                                                            blocks[index] = block.copy(
                                                                type = NoteBlockType.NOTE_LINK,
                                                                value = TextFieldValue(""),
                                                                noteLinkId = null
                                                            )
                                                        }
                                                        else -> {
                                                            val cleanText = block.value.text.removePrefix("/")
                                                            blocks[index] = block.copy(type = type, value = TextFieldValue(cleanText))
                                                        }
                                                    }
                                                },
                                                onDragStart = {
                                                    draggingIndex = index
                                                    dragOffset = 0f
                                                },
                                                onDrag = { dy ->
                                                    val currentIndex = draggingIndex ?: return@BlockRow
                                                    dragOffset += dy
                                                    val info = listState.layoutInfo.visibleItemsInfo
                                                    val listIndex = currentIndex + blockIndexOffset
                                                    val current = info.firstOrNull { it.index == listIndex } ?: return@BlockRow
                                                    val target = if (dragOffset > 0) {
                                                        info.firstOrNull { it.index == listIndex + 1 }?.takeIf {
                                                            current.offset + dragOffset > it.offset + it.size / 2
                                                        }
                                                    } else if (dragOffset < 0) {
                                                        info.firstOrNull { it.index == listIndex - 1 }?.takeIf {
                                                            current.offset + dragOffset < it.offset + it.size / 2
                                                        }
                                                    } else null
                                                    if (target != null) {
                                                        val targetBlockIndex = target.index - blockIndexOffset
                                                        if (targetBlockIndex in blocks.indices) {
                                                            blocks.add(targetBlockIndex, blocks.removeAt(currentIndex))
                                                            draggingIndex = targetBlockIndex
                                                            dragOffset -= (target.offset - current.offset)
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggingIndex = null
                                                    dragOffset = 0f
                                                },
                                                dragOffset = if (isDragging) dragOffset else 0f
                                            )
                                        }

                                        item {
                                            Text(
                                                stringResource(Res.string.note_slash_hint),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.alpha(0.7f)
                                            )
                                        }
                                    }

                                    FadingScrollEdges(
                                        listState = listState,
                                        modifier = Modifier.matchParentSize(),
                                        enabled = prefs.dimScroll
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Surface(
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .imePadding()
                        .padding(bottom = if (navBottomDp > 6.dp) navBottomDp - 6.dp else 0.dp)
                        .onSizeChanged { size ->
                            bottomBarHeightDp = with(density) { size.height.toDp() }
                        }
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedVisibility(!preview) {
                            MarkdownToolbar(
                                onWrapBold = { updateActiveBlock { wrapSelection(it, "**", "**") } },
                                onWrapItalic = { updateActiveBlock { wrapSelection(it, "*", "*") } },
                                onWrapCode = { updateActiveBlock { wrapSelection(it, "`", "`") } },
                                onH1 = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.HEADING_1) } } },
                                onH2 = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.HEADING_2) } } },
                                onBullet = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.BULLET) } } },
                                onTodo = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.TODO, checked = false) } } },
                                onOrdered = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.NUMBERED) } } },
                                onQuote = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.QUOTE) } } },
                                onCodeBlock = { activeBlockId?.let { id -> updateBlock(id) { it.copy(type = NoteBlockType.CODE) } } },
                                onLink = { updateActiveBlock { wrapSelection(it, "[", "](url)") } })
                        }
                        AnimatedVisibility(!preview) {
                            Button(
                                onClick = saveNote, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (initial == null) stringResource(Res.string.note_editor_create) else stringResource(
                                        Res.string.note_editor_save
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockRow(
    block: BlockState,
    index: Int,
    isActive: Boolean,
    typeLabels: Map<NoteBlockType, String>,
    notes: List<Note>,
    currentNoteId: String?,
    onActive: () -> Unit,
    onChangeValue: (TextFieldValue) -> Unit,
    onToggleTodo: (Boolean) -> Unit,
    onUpdateTable: (TableBlock) -> Unit,
    onUpdateKanban: (KanbanBlock) -> Unit,
    onUpdateAttachment: (AttachmentBlock, TextFieldValue) -> Unit,
    onUpdateNoteLink: (String?) -> Unit,
    onAddBelow: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onChangeType: (NoteBlockType) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    dragOffset: Float
) {
    var showMenu by remember(block.id) { mutableStateOf(false) }
    val text = block.value.text
    val slashQuery = if (text.startsWith("/")) text.drop(1) else null
    val showSlashMenu = slashQuery != null && !slashQuery.contains(" ")

    val typeItems = remember(typeLabels) {
        listOf(
            NoteBlockType.HEADING_1,
            NoteBlockType.HEADING_2,
            NoteBlockType.HEADING_3,
            NoteBlockType.PARAGRAPH,
            NoteBlockType.BULLET,
            NoteBlockType.NUMBERED,
            NoteBlockType.TODO,
            NoteBlockType.QUOTE,
            NoteBlockType.CODE,
            NoteBlockType.DIVIDER,
            NoteBlockType.CALLOUT,
            NoteBlockType.TABLE,
            NoteBlockType.KANBAN,
            NoteBlockType.ATTACHMENT,
            NoteBlockType.NOTE_LINK
        )
    }
    val filtered = if (showSlashMenu) {
        typeItems.filter { type ->
            typeLabels[type]?.contains(slashQuery ?: "", ignoreCase = true) == true
        }
    } else typeItems

    val textStyle = when (block.type) {
        NoteBlockType.HEADING_1 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        NoteBlockType.HEADING_2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        NoteBlockType.HEADING_3 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        NoteBlockType.CODE -> MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
        else -> MaterialTheme.typography.bodyMedium
    }

    val containerColor = when (block.type) {
        NoteBlockType.CODE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        NoteBlockType.CALLOUT -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        else -> Color.Transparent
    }
    val uriHandler = LocalUriHandler.current

    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = dragOffset }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = SimpleIcons.Drag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 6.dp)
                        .pointerInput(block.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                                onDrag = { _, dragAmount -> onDrag(dragAmount.y) }
                            )
                        }
                )

                if (block.type == NoteBlockType.TODO) {
                    Checkbox(
                        checked = block.checked,
                        onCheckedChange = { onToggleTodo(it) },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (block.type == NoteBlockType.BULLET) {
                    Text("•", modifier = Modifier.padding(top = 10.dp))
                } else if (block.type == NoteBlockType.NUMBERED) {
                    Text("${index + 1}.", modifier = Modifier.padding(top = 10.dp))
                } else if (block.type == NoteBlockType.QUOTE) {
                    Text("❝", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
                } else if (block.type == NoteBlockType.CALLOUT) {
                    Text("!", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
                }

                when (block.type) {
                    NoteBlockType.DIVIDER -> Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    )
                    NoteBlockType.TABLE -> {
                        val table = block.table ?: TableBlock(
                            id = block.id,
                            rows = 2,
                            cols = 2,
                            cells = List(2) { List(2) { "" } }
                        )
                        TableBlockEditor(table = table, onUpdate = onUpdateTable)
                    }
                    NoteBlockType.KANBAN -> {
                        val kb = block.kanban ?: KanbanBlock(
                            id = block.id,
                            columns = listOf(
                                KanbanColumn(id = newId("kc"), title = "Todo", cards = emptyList())
                            )
                        )
                        KanbanBlockEditor(block = kb, onUpdate = onUpdateKanban)
                    }
                    NoteBlockType.ATTACHMENT -> {
                        val att = block.attachment ?: AttachmentBlock(id = block.id, uri = block.value.text)
                        AttachmentBlockEditor(
                            attachment = att,
                            value = block.value,
                            onUpdate = { a, v -> onUpdateAttachment(a, v) },
                            onOpen = { uriHandler.openUri(it) }
                        )
                    }
                    NoteBlockType.NOTE_LINK -> {
                        NoteLinkBlockEditor(
                            currentNoteId = currentNoteId,
                            notes = notes,
                            selectedId = block.noteLinkId,
                            onPick = { onUpdateNoteLink(it) }
                        )
                    }
                    else -> {
                        val isMultiLine = block.type in setOf(
                            NoteBlockType.PARAGRAPH,
                            NoteBlockType.CODE,
                            NoteBlockType.QUOTE,
                            NoteBlockType.CALLOUT
                        )
                        TextField(
                            value = block.value,
                            onValueChange = {
                                onChangeValue(it)
                            },
                            textStyle = textStyle,
                            singleLine = !isMultiLine,
                            minLines = if (isMultiLine) 3 else 1,
                            placeholder = {
                                if (block.value.text.isBlank()) {
                                    Text(typeLabels[block.type] ?: "")
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = containerColor,
                                unfocusedContainerColor = containerColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { if (it.isFocused) onActive() }
                        )
                    }
                }

                IconButton(onClick = { onAddBelow() }) {
                    Icon(AddIcon, contentDescription = stringResource(Res.string.note_block_add))
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(SimpleIcons.More, contentDescription = null)
                }
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.note_block_duplicate)) },
                    onClick = { showMenu = false; onDuplicate() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.note_block_delete)) },
                    onClick = { showMenu = false; onDelete() }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.note_block_turn_into)) },
                    onClick = {},
                    enabled = false
                )
                typeItems.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(typeLabels[type] ?: type.name) },
                        onClick = {
                            showMenu = false
                            onChangeType(type)
                        }
                    )
                }
            }

            DropdownMenu(expanded = showSlashMenu, onDismissRequest = { /* keep open until text changes */ }) {
                filtered.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(typeLabels[type] ?: type.name) },
                        onClick = { onChangeType(type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TableBlockEditor(
    table: TableBlock,
    onUpdate: (TableBlock) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        table.cells.forEachIndexed { r, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEachIndexed { c, cell ->
                    TextField(
                        value = cell,
                        onValueChange = { text ->
                            val newCells = table.cells.mapIndexed { ri, rowCells ->
                                if (ri != r) rowCells else rowCells.mapIndexed { ci, value ->
                                    if (ci == c) text else value
                                }
                            }
                            onUpdate(table.copy(cells = newCells))
                        },
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                val newRow = List(table.cols) { "" }
                onUpdate(table.copy(rows = table.rows + 1, cells = table.cells + listOf(newRow)))
            }) {
                Text(stringResource(Res.string.note_block_add_row))
            }
            TextButton(onClick = {
                val newCells = table.cells.map { it + "" }
                onUpdate(table.copy(cols = table.cols + 1, cells = newCells))
            }) {
                Text(stringResource(Res.string.note_block_add_column))
            }
        }
    }
}

@Composable
private fun KanbanBlockEditor(
    block: KanbanBlock,
    onUpdate: (KanbanBlock) -> Unit
) {
    val addColumnLabel = stringResource(Res.string.note_block_add_column_label)
    val addColumnText = stringResource(Res.string.note_block_add_column)
    val scroll = rememberScrollState()
    Row(
        Modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        block.columns.forEachIndexed { idx, col ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    Modifier.width(220.dp).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = col.title,
                        onValueChange = { title ->
                            val updated = block.columns.mapIndexed { i, c ->
                                if (i == idx) c.copy(title = title) else c
                            }
                            onUpdate(block.copy(columns = updated))
                        },
                        textStyle = MaterialTheme.typography.labelLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    col.cards.forEachIndexed { ci, card ->
                        TextField(
                            value = card,
                            onValueChange = { text ->
                                val updatedCols = block.columns.mapIndexed { i, c ->
                                    if (i != idx) c else c.copy(
                                        cards = c.cards.mapIndexed { j, v -> if (j == ci) text else v }
                                    )
                                }
                                onUpdate(block.copy(columns = updatedCols))
                            },
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    TextButton(onClick = {
                        val updatedCols = block.columns.mapIndexed { i, c ->
                            if (i != idx) c else c.copy(cards = c.cards + "")
                        }
                        onUpdate(block.copy(columns = updatedCols))
                    }) {
                        Text(stringResource(Res.string.note_block_add_card))
                    }
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            shape = MaterialTheme.shapes.medium
        ) {
            TextButton(
                onClick = {
                    val updated = block.columns + KanbanColumn(
                        id = newId("kc"),
                        title = addColumnLabel,
                        cards = emptyList()
                    )
                    onUpdate(block.copy(columns = updated))
                },
                modifier = Modifier.width(140.dp).padding(10.dp)
            ) {
                Text(addColumnText)
            }
        }
    }
}

@Composable
private fun AttachmentBlockEditor(
    attachment: AttachmentBlock,
    value: TextFieldValue,
    onUpdate: (AttachmentBlock, TextFieldValue) -> Unit,
    onOpen: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TextField(
            value = value,
            onValueChange = { v -> onUpdate(attachment.copy(uri = v.text), v) },
            placeholder = { Text(stringResource(Res.string.note_block_pick_file)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (value.text.isNotBlank()) {
            TextButton(onClick = { onOpen(value.text) }) {
                Text(stringResource(Res.string.note_block_open_file))
            }
        }
    }
}

@Composable
private fun NoteLinkBlockEditor(
    currentNoteId: String?,
    notes: List<Note>,
    selectedId: String?,
    onPick: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = notes.filter { it.id != currentNoteId }
    val current = options.firstOrNull { it.id == selectedId }?.title
        ?: stringResource(Res.string.note_block_note_link)
    Column {
        TextButton(onClick = { expanded = true }) { Text(current) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { note ->
                DropdownMenuItem(
                    text = { Text(note.title.ifBlank { stringResource(Res.string.note_editor_untitled) }) },
                    onClick = { expanded = false; onPick(note.id) }
                )
            }
        }
    }
}

@Composable
private fun NoteBlocksPreview(blocks: List<BlockState>, notes: List<Note>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEachIndexed { index, block ->
            when (block.type) {
                NoteBlockType.DIVIDER -> Divider()
                NoteBlockType.HEADING_1 -> MarkdownText(block.value.text, style = MaterialTheme.typography.headlineSmall)
                NoteBlockType.HEADING_2 -> MarkdownText(block.value.text, style = MaterialTheme.typography.titleLarge)
                NoteBlockType.HEADING_3 -> MarkdownText(block.value.text, style = MaterialTheme.typography.titleMedium)
                NoteBlockType.BULLET -> MarkdownText("• ${block.value.text}")
                NoteBlockType.NUMBERED -> MarkdownText("${index + 1}. ${block.value.text}")
                NoteBlockType.TODO -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = block.checked, onCheckedChange = null)
                        MarkdownText(block.value.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                NoteBlockType.QUOTE -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        MarkdownText(
                            block.value.text,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                NoteBlockType.CODE -> Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        block.value.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                NoteBlockType.CALLOUT -> Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    MarkdownText(block.value.text, modifier = Modifier.padding(8.dp))
                }
                NoteBlockType.TABLE -> {
                    val table = block.table
                    if (table != null) {
                        TablePreview(table)
                    }
                }
                NoteBlockType.KANBAN -> {
                    val kb = block.kanban
                    if (kb != null) {
                        KanbanPreview(kb)
                    }
                }
                NoteBlockType.ATTACHMENT -> {
                    val label = block.attachment?.name ?: block.value.text
                    Text("📎 $label")
                }
                NoteBlockType.NOTE_LINK -> {
                    val title = notes.firstOrNull { it.id == block.noteLinkId }?.title
                    if (title != null) {
                        Text("↗ $title", color = MaterialTheme.colorScheme.primary)
                    }
                }
                NoteBlockType.PARAGRAPH -> MarkdownText(block.value.text)
            }
        }
    }
}

@Composable
private fun TablePreview(table: TableBlock) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        table.cells.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { cell ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(cell.ifBlank { " " }, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanPreview(block: KanbanBlock) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        block.columns.forEach { col ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(col.title, style = MaterialTheme.typography.labelLarge)
                    col.cards.take(3).forEach { card ->
                        Text("• $card", style = MaterialTheme.typography.bodySmall)
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
