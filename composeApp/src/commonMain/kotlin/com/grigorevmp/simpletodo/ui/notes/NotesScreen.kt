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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.NoteFolder
import com.grigorevmp.simpletodo.model.NoteSortField
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.FilterIcon
import com.grigorevmp.simpletodo.ui.components.FolderIcon
import com.grigorevmp.simpletodo.ui.components.NoteIcon
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch

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
    val folders by repo.noteFolders.collectAsState()
    val prefs by repo.prefs.collectAsState()
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

    val path = remember(currentFolderId, folders) { buildFolderPath(currentFolderId, folders) }
    val childFolders = remember(currentFolderId, folders, prefs.noteSort) {
        val filtered = folders.filter { it.parentId == currentFolderId }
        when (prefs.noteSort.field) {
            NoteSortField.DATE -> filtered.sortedByDescending { it.createdAt }
            NoteSortField.NAME -> filtered.sortedBy { it.name.lowercase() }
        }
    }
    val notesInFolder = remember(currentFolderId, notes, prefs.noteSort) {
        val filtered = notes.filter { it.folderId == currentFolderId }
        when (prefs.noteSort.field) {
            NoteSortField.DATE -> filtered.sortedByDescending { it.updatedAt }
            NoteSortField.NAME -> filtered.sortedBy { it.title.lowercase() }
        }
    }
    val combinedItems = remember(childFolders, notesInFolder, prefs.noteSort) {
        if (prefs.noteSort.foldersOnTop) {
            childFolders.map { NotesListItem.FolderItem(it) } +
                notesInFolder.map { NotesListItem.NoteItem(it) }
        } else {
            val items = mutableListOf<NotesListItem>()
            childFolders.forEach { items.add(NotesListItem.FolderItem(it)) }
            notesInFolder.forEach { items.add(NotesListItem.NoteItem(it)) }
            items.sortedWith(notesComparator(prefs.noteSort.field))
        }
    }
    val noteCounts = remember(notes) { notes.groupingBy { it.folderId }.eachCount() }
    val folderCounts = remember(folders) { folders.groupingBy { it.parentId }.eachCount() }
    val backgroundColor = MaterialTheme.colorScheme.background
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
            Text(
                text = buildBreadcrumb(path),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
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
                                        onOpen = { editNote = item.note; showEditor = true }
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
                enableEffects = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            )

            if (currentFolderId != null) {
                BackActionButton(
                    backdrop = listBackdrop,
                    onBack = {
                        val parent = path.dropLast(1).lastOrNull()?.id
                        currentFolderId = parent
                    },
                    enableEffects = true,
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
            title = { Text("Edit folder") },
            text = {
                OutlinedTextField(
                    value = editFolderName,
                    onValueChange = { editFolderName = it },
                    label = { Text("Folder name") },
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
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteFolder = true
                        deleteFolderId = folder.id
                        folderAction = null
                    }
                ) { Text("Delete") }
            }
        )
    }

    if (showDeleteFolder) {
        AlertDialog(
            onDismissRequest = { showDeleteFolder = false },
            title = { Text("Delete folder?") },
            text = { Text("All notes inside will be moved to the root.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deleteFolderId
                        if (id != null) scope.launch { repo.deleteFolder(id) }
                        showDeleteFolder = false
                        deleteFolderId = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFolder = false; deleteFolderId = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun NotesTopBar(path: List<NoteFolder>, onSort: () -> Unit) {
    val title = if (path.isEmpty()) "Notes" else path.last().name
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onSort) {
            Icon(
                imageVector = FilterIcon,
                contentDescription = "Filter"
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
                contentDescription = "Folder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(folder.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "$folderCount folders • $noteCount notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoteRow(note: Note, onOpen: () -> Unit) {
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                NoteIcon,
                contentDescription = "Note",
                tint = MaterialTheme.colorScheme.primary,
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
            }
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
            if (inFolder) "Folder is empty." else "No notes yet.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (inFolder) {
                "Create a note or a subfolder to organize your ideas."
            } else {
                "Create your first note or add folders to organize." 
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

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
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
                            "Add Folder",
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

    Box(
        modifier = modifier
            .padding(end = 16.dp),
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
                            "Back",
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
            Text("New folder", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Folder name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onCreate) { Text("Create") }
            }
        }
    }
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

private fun buildBreadcrumb(path: List<NoteFolder>): String {
    val names = path.joinToString(" / ") { it.name }
    return if (names.isBlank()) "Notes" else "Notes / $names"
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
