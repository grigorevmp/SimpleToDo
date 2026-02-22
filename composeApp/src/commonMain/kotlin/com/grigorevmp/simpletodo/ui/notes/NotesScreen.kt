package com.grigorevmp.simpletodo.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.NoteFolder
import com.grigorevmp.simpletodo.model.NoteSortField
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.AppIconId
import com.grigorevmp.simpletodo.ui.components.PlatformIcon
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.luminance
import com.grigorevmp.simpletodo.ui.components.FolderIcon
import com.grigorevmp.simpletodo.ui.components.NoteIcon
import com.grigorevmp.simpletodo.ui.components.SimpleIcons
import com.grigorevmp.simpletodo.platform.isIos
import com.grigorevmp.simpletodo.platform.PlatformBackHandler
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.notes
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.notes_title
import simpletodo.composeapp.generated.resources.notes_edit_folder_title
import simpletodo.composeapp.generated.resources.notes_folder_name_label
import simpletodo.composeapp.generated.resources.notes_save
import simpletodo.composeapp.generated.resources.notes_delete
import simpletodo.composeapp.generated.resources.notes_delete_folder_title
import simpletodo.composeapp.generated.resources.notes_delete_folder_desc
import simpletodo.composeapp.generated.resources.notes_cancel
import simpletodo.composeapp.generated.resources.notes_filter_cd
import simpletodo.composeapp.generated.resources.notes_folder_cd
import simpletodo.composeapp.generated.resources.notes_note_cd
import simpletodo.composeapp.generated.resources.notes_favorite_cd
import simpletodo.composeapp.generated.resources.notes_folder_note_count
import simpletodo.composeapp.generated.resources.notes_linked_one
import simpletodo.composeapp.generated.resources.notes_linked_many
import simpletodo.composeapp.generated.resources.notes_favorite_add
import simpletodo.composeapp.generated.resources.notes_favorite_remove
import simpletodo.composeapp.generated.resources.notes_empty_folder
import simpletodo.composeapp.generated.resources.notes_empty_root
import simpletodo.composeapp.generated.resources.notes_empty_folder_desc
import simpletodo.composeapp.generated.resources.notes_empty_root_desc
import simpletodo.composeapp.generated.resources.notes_add_folder
import simpletodo.composeapp.generated.resources.notes_back
import simpletodo.composeapp.generated.resources.notes_new_folder_title
import simpletodo.composeapp.generated.resources.notes_create
import simpletodo.composeapp.generated.resources.notes_favorites_title
import simpletodo.composeapp.generated.resources.notes_count
import simpletodo.composeapp.generated.resources.notes_expand_cd
import simpletodo.composeapp.generated.resources.notes_collapse_cd

@Composable
fun NotesScreen(
    repo: TodoRepository,
    createNoteSignal: Int,
    onCreateNoteHandled: () -> Unit,
    openNoteId: String?,
    onOpenNoteHandled: () -> Unit
) {
    val tasks by repo.tasks.collectAsState()
    val notes by repo.notes.collectAsState()
    val links by repo.taskNoteLinks.collectAsState()
    val folders by repo.noteFolders.collectAsState()
    val prefs by repo.prefs.collectAsState()
    val tasksById = remember(tasks) { tasks.associateBy { it.id } }
    val linkedTasksByNote = remember(links, tasksById) {
        links.groupBy { it.noteId }.mapValues { entry ->
            entry.value.mapNotNull { link -> tasksById[link.taskId] }.distinctBy { it.id }
        }
    }
    val scope = rememberCoroutineScope()

    var currentFolderId by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editNote by remember { mutableStateOf<Note?>(null) }
    var showFolderInput by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var folderAction by remember { mutableStateOf<NoteFolder?>(null) }
    var editFolderName by remember { mutableStateOf("") }
    var showDeleteFolder by remember { mutableStateOf(false) }
    var deleteFolderId by remember { mutableStateOf<String?>(null) }
    var showSort by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(true) }

    val path = remember(currentFolderId, folders) { buildFolderPath(currentFolderId, folders) }
    val childFolders = remember(currentFolderId, folders, prefs.noteSort) {
        val filtered = folders.filter { it.parentId == currentFolderId }
        when (prefs.noteSort.field) {
            NoteSortField.DATE -> filtered.sortedByDescending { it.createdAt }
            NoteSortField.NAME -> filtered.sortedBy { it.name.lowercase() }
        }
    }
    val favoriteNotes = remember(notes) { notes.filter { it.favorite }.sortedByDescending { it.updatedAt } }
    val notesInFolder = remember(currentFolderId, notes, prefs.noteSort) {
        val filtered = notes.filter { it.folderId == currentFolderId }
        when (prefs.noteSort.field) {
            NoteSortField.DATE -> filtered.sortedByDescending { it.updatedAt }
            NoteSortField.NAME -> filtered.sortedBy { it.title.lowercase() }
        }
    }
    val regularNotesInFolder = remember(currentFolderId, notesInFolder) {
        if (currentFolderId == null) notesInFolder.filterNot { it.favorite } else notesInFolder
    }

    val combinedItems = remember(childFolders, regularNotesInFolder, prefs.noteSort) {
        if (prefs.noteSort.foldersOnTop) {
            childFolders.map { NotesListItem.FolderItem(it) } +
                regularNotesInFolder.map { NotesListItem.NoteItem(it) }
        } else {
            val items = mutableListOf<NotesListItem>()
            childFolders.forEach { items.add(NotesListItem.FolderItem(it)) }
            regularNotesInFolder.forEach { items.add(NotesListItem.NoteItem(it)) }
            items.sortedWith(notesComparator(prefs.noteSort.field))
        }
    }
    val noteCounts = remember(notes) { notes.groupingBy { it.folderId }.eachCount() }
    val folderCounts = remember(folders) { folders.groupingBy { it.parentId }.eachCount() }
    val backgroundColor = MaterialTheme.colorScheme.background
    val navigateBack: () -> Unit = {
        val parent = path.dropLast(1).lastOrNull()?.id
        currentFolderId = parent
    }

    PlatformBackHandler(enabled = showEditor || currentFolderId != null) {
        if (showEditor) {
            showEditor = false
        } else {
            navigateBack()
        }
    }

    val listBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    LaunchedEffect(createNoteSignal) {
        if (createNoteSignal > 0) {
            editNote = null
            showEditor = true
            onCreateNoteHandled()
        }
    }

    LaunchedEffect(openNoteId, notes) {
        val targetId = openNoteId ?: return@LaunchedEffect
        val note = notes.firstOrNull { it.id == targetId }
        if (note != null) {
            currentFolderId = note.folderId
            editNote = note
            showEditor = true
        }
        onOpenNoteHandled()
    }

    Column(Modifier.fillMaxSize()) {
        NotesTopBar(
            path = path,
            onSort = { showSort = true }
        )

        AnimatedVisibility(visible = path.isNotEmpty()) {
            val scrollState = rememberScrollState()
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BreadcrumbItem(
                    text = stringResource(Res.string.notes_title),
                    active = path.isEmpty(),
                    onClick = { currentFolderId = null }
                )
                path.forEachIndexed { index, folder ->
                    Text(
                        " / ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BreadcrumbItem(
                        text = folder.name,
                        active = index == path.lastIndex,
                        onClick = { currentFolderId = folder.id }
                    )
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                AnimatedVisibility (showFolderInput) {
                    NewFolderRow(
                        name = newFolderName,
                        onNameChange = { newFolderName = it },
                        onCreate = {
                            val name = newFolderName.trim()
                            if (name.isEmpty()) return@NewFolderRow
                            scope.launch { repo.addFolder(name, currentFolderId) }
                            newFolderName = ""
                            showFolderInput = false
                        },
                        onCancel = {
                            newFolderName = ""
                            showFolderInput = false
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (childFolders.isEmpty() && notesInFolder.isEmpty()) {
                    EmptyNotesState(currentFolderId != null)
                } else {
                    val listState = rememberLazyListState()
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp).run {
                                PaddingValues(
                                    start = calculateStartPadding(LayoutDirection.Ltr),
                                    top = 12.dp,
                                    end = calculateEndPadding(LayoutDirection.Ltr),
                                    bottom = 110.dp
                                )
                            },
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (currentFolderId == null && favoriteNotes.isNotEmpty()) {
                                item {
                                    FavoriteNotesSection(
                                        notes = favoriteNotes,
                                        expanded = showFavorites,
                                        onToggle = { showFavorites = !showFavorites }
                                    )
                                }
                                if (showFavorites) {
                                    items(favoriteNotes, key = { "fav_${it.id}" }) { fav ->
                                        NoteRow(
                                            note = fav,
                                            linkedTasks = linkedTasksByNote[fav.id].orEmpty(),
                                            onToggleFavorite = { scope.launch { repo.toggleNoteFavorite(fav.id) } },
                                            onOpen = { editNote = fav; showEditor = true },
                                            onDelete = { scope.launch { repo.deleteNote(fav.id) } }
                                        )
                                    }
                                }
                                item { Spacer(Modifier.height(8.dp)) }
                            }
                            items(combinedItems, key = { it.key }) { item ->
                                when (item) {
                                    is NotesListItem.FolderItem -> FolderRow(
                                        folder = item.folder,
                                        noteCount = noteCounts[item.folder.id] ?: 0,
                                        folderCount = folderCounts[item.folder.id] ?: 0,
                                        onOpen = { currentFolderId = item.folder.id },
                                        onLongPress = {
                                            folderAction = item.folder
                                            editFolderName = item.folder.name
                                        }
                                    )
                                    is NotesListItem.NoteItem -> NoteRow(
                                        note = item.note,
                                        linkedTasks = linkedTasksByNote[item.note.id].orEmpty(),
                                        onToggleFavorite = { scope.launch { repo.toggleNoteFavorite(item.note.id) } },
                                        onOpen = { editNote = item.note; showEditor = true },
                                        onDelete = { scope.launch { repo.deleteNote(item.note.id) } }
                                    )
                                }
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

            NotesActionBar(
                backdrop = listBackdrop,
                onNewFolder = {
                    newFolderName = ""
                    showFolderInput = true
                },
                enableEffects = prefs.liquidGlass && !isIos,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            )

            if (currentFolderId != null) {
                BackActionButton(
                    backdrop = listBackdrop,
                    onBack = { navigateBack() },
                    enableEffects = prefs.liquidGlass && !isIos,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 96.dp)
                )
            }
        }
    }

    if (showEditor) {
        NoteEditorScreen(
            repo = repo,
            initial = editNote,
            tasks = tasks,
            folderId = currentFolderId,
            onDismiss = { showEditor = false }
        )
    }

    if (showSort) {
        NotesSortSheet(
            current = prefs.noteSort,
            onApply = { cfg -> scope.launch { repo.setNoteSort(cfg) } },
            onDismiss = { showSort = false }
        )
    }

    folderAction?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderAction = null },
            title = { Text(stringResource(Res.string.notes_edit_folder_title)) },
            text = {
                OutlinedTextField(
                    value = editFolderName,
                    onValueChange = { editFolderName = it },
                    label = { Text(stringResource(Res.string.notes_folder_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = editFolderName.trim()
                        if (name.isEmpty()) return@TextButton
                        scope.launch { repo.renameFolder(folder.id, name) }
                        folderAction = null
                    }
                ) { Text(stringResource(Res.string.notes_save)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteFolder = true
                        deleteFolderId = folder.id
                        folderAction = null
                    }
                ) { Text(stringResource(Res.string.notes_delete)) }
            }
        )
    }

    if (showDeleteFolder) {
        AlertDialog(
            onDismissRequest = { showDeleteFolder = false },
            title = { Text(stringResource(Res.string.notes_delete_folder_title)) },
            text = { Text(stringResource(Res.string.notes_delete_folder_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deleteFolderId
                        if (id != null) scope.launch { repo.deleteFolder(id) }
                        showDeleteFolder = false
                        deleteFolderId = null
                    }
                ) { Text(stringResource(Res.string.notes_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFolder = false; deleteFolderId = null }) {
                    Text(stringResource(Res.string.notes_cancel))
                }
            }
        )
    }
}

@Composable
private fun NotesTopBar(path: List<NoteFolder>, onSort: () -> Unit) {
    val title = if (path.isEmpty()) stringResource(Res.string.notes_title) else path.last().name
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onSort) {
            PlatformIcon(
                id = AppIconId.Filter,
                contentDescription = stringResource(Res.string.notes_filter_cd),
                tint = LocalContentColor.current,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FolderRow(
    folder: NoteFolder,
    noteCount: Int,
    folderCount: Int,
    onOpen: () -> Unit,
    onLongPress: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onOpen, onLongClick = onLongPress)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                FolderIcon,
                contentDescription = stringResource(Res.string.notes_folder_cd),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(folder.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(Res.string.notes_folder_note_count, folderCount.toString(), noteCount.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
private fun NoteRow(
    note: Note,
    linkedTasks: List<TodoTask>,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var showActions by remember(note.id) { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = { showActions = true }
                )
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val notePainter = if (isIos) {
                    painterResource(Res.drawable.notes)
                } else {
                    rememberVectorPainter(NoteIcon)
                }
                val noteTint = MaterialTheme.colorScheme.primary
                Icon(
                    painter = notePainter,
                    contentDescription = stringResource(Res.string.notes_note_cd),
                    tint = noteTint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        note.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val snippet = noteSnippet(note.content)
                    if (snippet.isNotEmpty()) {
                        Text(
                            snippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (linkedTasks.isNotEmpty()) {
                        val label = if (linkedTasks.size == 1) {
                            stringResource(Res.string.notes_linked_one, linkedTasks.first().title)
                        } else {
                            stringResource(Res.string.notes_linked_many, linkedTasks.size.toString())
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (note.favorite) {
                    Icon(
                        imageVector = SimpleIcons.Star,
                        contentDescription = stringResource(Res.string.notes_favorite_cd),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        DropdownMenu(
            expanded = showActions,
            onDismissRequest = { showActions = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (note.favorite) {
                            stringResource(Res.string.notes_favorite_remove)
                        } else {
                            stringResource(Res.string.notes_favorite_add)
                        }
                    )
                },
                onClick = {
                    showActions = false
                    onToggleFavorite()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.notes_delete)) },
                onClick = {
                    showActions = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun EmptyNotesState(inFolder: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (inFolder) stringResource(Res.string.notes_empty_folder) else stringResource(Res.string.notes_empty_root),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (inFolder) {
                stringResource(Res.string.notes_empty_folder_desc)
            } else {
                stringResource(Res.string.notes_empty_root_desc)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotesActionBar(
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    onNewFolder: () -> Unit,
    enableEffects: Boolean,
    modifier: Modifier = Modifier
) {
    val container = MaterialTheme.colorScheme.surfaceVariant
    val containerBrush = Brush.verticalGradient(
        listOf(
            container.copy(alpha = 0.08f),
            container.copy(alpha = 0.22f)
        )
    )
    val fallbackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val density = LocalDensity.current
    val blurPx = with(density) { 3.dp.toPx() }
    val lensInnerPx = with(density) { 10.dp.toPx() }
    val lensOuterPx = with(density) { 20.dp.toPx() }
    val isDarkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassOverlay = if (isDarkSurface) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }

    Box(modifier = modifier
        .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(64.dp))
                .then(
                    if (enableEffects) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(64.dp) },
                            effects = {
                                vibrancy()
                                blur(blurPx)
                                lens(lensInnerPx, lensOuterPx)
                            },
                            onDrawSurface = {
                                drawRect(containerBrush)
                                drawRect(glassOverlay)
                            }
                        )
                    } else {
                        Modifier.background(fallbackColor, RoundedCornerShape(64.dp))
                    }
                )
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNewFolder,
                    shape = MaterialTheme.shapes.large,
                    color = Color.Transparent
                ) {
                    Box(
                        Modifier
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(Res.string.notes_add_folder),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackActionButton(
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    onBack: () -> Unit,
    enableEffects: Boolean,
    modifier: Modifier = Modifier
) {
    val container = MaterialTheme.colorScheme.surfaceVariant
    val containerBrush = Brush.verticalGradient(
        listOf(
            container.copy(alpha = 0.08f),
            container.copy(alpha = 0.22f)
        )
    )
    val fallbackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val density = LocalDensity.current
    val blurPx = with(density) { 3.dp.toPx() }
    val lensInnerPx = with(density) { 10.dp.toPx() }
    val lensOuterPx = with(density) { 20.dp.toPx() }
    val isDarkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassOverlay = if (isDarkSurface) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .padding(end = 16.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(64.dp))
                .then(
                    if (enableEffects) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(64.dp) },
                            effects = {
                                vibrancy()
                                blur(blurPx)
                                lens(lensInnerPx, lensOuterPx)
                            },
                            onDrawSurface = {
                                drawRect(containerBrush)
                                drawRect(glassOverlay)
                            }
                        )
                    } else {
                        Modifier.background(fallbackColor, RoundedCornerShape(64.dp))
                    }
                )
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = MaterialTheme.shapes.large,
                    color = Color.Transparent
                ) {
                    Box(
                        Modifier
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(Res.string.notes_back),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewFolderRow(
    name: String,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(Res.string.notes_new_folder_title), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.notes_folder_name_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) { Text(stringResource(Res.string.notes_cancel)) }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onCreate) { Text(stringResource(Res.string.notes_create)) }
            }
        }
    }
}

@Composable
private fun FavoriteNotesSection(
    notes: List<Note>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(Res.string.notes_favorites_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(Res.string.notes_count, notes.size.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (expanded) SimpleIcons.ArrowUp else SimpleIcons.ArrowDown,
                contentDescription = if (expanded) {
                    stringResource(Res.string.notes_collapse_cd)
                } else {
                    stringResource(Res.string.notes_expand_cd)
                },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BreadcrumbItem(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.clickable { onClick() }
    )
}

private fun buildFolderPath(currentFolderId: String?, folders: List<NoteFolder>): List<NoteFolder> {
    if (currentFolderId == null) return emptyList()
    val map = folders.associateBy { it.id }
    val path = mutableListOf<NoteFolder>()
    var current = map[currentFolderId]
    while (current != null) {
        path.add(current)
        current = current.parentId?.let { map[it] }
    }
    return path.reversed()
}

private sealed class NotesListItem(val key: String) {
    class FolderItem(val folder: NoteFolder) : NotesListItem("folder_${folder.id}")
    class NoteItem(val note: Note) : NotesListItem("note_${note.id}")
}

private fun notesComparator(field: NoteSortField): Comparator<NotesListItem> {
    return when (field) {
        NoteSortField.DATE -> compareByDescending<NotesListItem> {
            when (it) {
                is NotesListItem.FolderItem -> it.folder.createdAt
                is NotesListItem.NoteItem -> it.note.updatedAt
            }
        }
        NoteSortField.NAME -> compareBy<NotesListItem> {
            when (it) {
                is NotesListItem.FolderItem -> it.folder.name.lowercase()
                is NotesListItem.NoteItem -> it.note.title.lowercase()
            }
        }
    }
}

private fun noteSnippet(content: String): String {
    val line = content.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return ""
    return line
        .removePrefix("#")
        .removePrefix("##")
        .removePrefix("###")
        .removePrefix(">")
        .removePrefix("-")
        .removePrefix("*")
        .replace("`", "")
        .replace("**", "")
        .replace("*", "")
        .trim()
}
