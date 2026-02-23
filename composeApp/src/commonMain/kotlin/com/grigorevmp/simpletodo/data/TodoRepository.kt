package com.grigorevmp.simpletodo.data

import com.grigorevmp.simpletodo.model.AppPrefs
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.Note
import com.grigorevmp.simpletodo.model.NoteFolder
import com.grigorevmp.simpletodo.model.NoteSortConfig
import com.grigorevmp.simpletodo.model.SortConfig
import com.grigorevmp.simpletodo.model.SortDir
import com.grigorevmp.simpletodo.model.SortField
import com.grigorevmp.simpletodo.model.Subtask
import com.grigorevmp.simpletodo.model.Tag
import com.grigorevmp.simpletodo.model.TaskNoteLink
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.model.AppLanguage
import com.grigorevmp.simpletodo.platform.NotificationScheduler
import com.grigorevmp.simpletodo.platform.commitTasksJson
import com.grigorevmp.simpletodo.platform.requestPinnedTasksNotificationUpdate
import com.grigorevmp.simpletodo.platform.requestTasksWidgetUpdate
import com.grigorevmp.simpletodo.util.newId
import com.grigorevmp.simpletodo.util.nowInstant
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class TodoRepository(
    private val settings: Settings,
    private val scheduler: NotificationScheduler
) {
    @kotlinx.serialization.Serializable
    private data class BackupPayload(
        val tasks: List<TodoTask>,
        val notes: List<Note>,
        val folders: List<NoteFolder>,
        val links: List<TaskNoteLink>,
        val prefs: AppPrefs
    )
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "blockKind"
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantAsStringSerializer)
        }
    }

    private val mutex = Mutex()

    private val tasksKey = "tasks_json_v1"
    private val notesKey = "notes_json_v1"
    private val foldersKey = "note_folders_json_v1"
    private val linksKey = "task_note_links_json_v1"
    private val prefsKey = "prefs_json_v1"

    private val _tasks = MutableStateFlow(loadTasks())
    val tasks: StateFlow<List<TodoTask>> = _tasks

    private val _notes = MutableStateFlow(loadNotes())
    val notes: StateFlow<List<Note>> = _notes

    private val _taskNoteLinks = MutableStateFlow(loadLinks())
    val taskNoteLinks: StateFlow<List<TaskNoteLink>> = _taskNoteLinks

    private val _noteFolders = MutableStateFlow(loadFolders())
    val noteFolders: StateFlow<List<NoteFolder>> = _noteFolders

    private val _prefs = MutableStateFlow(loadPrefs())
    val prefs: StateFlow<AppPrefs> = _prefs

    private fun loadTasks(): List<TodoTask> {
        val raw = settings.getStringOrNull(tasksKey) ?: return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(TodoTask.serializer()), raw) }
            .getOrElse { emptyList() }
    }

    private fun saveTasks(list: List<TodoTask>) {
        val encoded = json.encodeToString(ListSerializer(TodoTask.serializer()), list)
        settings.putString(tasksKey, encoded)
        commitTasksJson(encoded)
        requestTasksWidgetUpdate()
        requestPinnedTasksNotificationUpdate()
    }

    private fun loadNotes(): List<Note> {
        val raw = settings.getStringOrNull(notesKey) ?: return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(Note.serializer()), raw) }
            .getOrElse { emptyList() }
    }

    private fun saveNotes(list: List<Note>) {
        settings.putString(notesKey, json.encodeToString(ListSerializer(Note.serializer()), list))
    }

    private fun loadFolders(): List<NoteFolder> {
        val raw = settings.getStringOrNull(foldersKey) ?: return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(NoteFolder.serializer()), raw) }
            .getOrElse { emptyList() }
    }

    private fun loadLinks(): List<TaskNoteLink> {
        val raw = settings.getStringOrNull(linksKey)
        if (raw != null) {
            return runCatching { json.decodeFromString(ListSerializer(TaskNoteLink.serializer()), raw) }
                .getOrElse { emptyList() }
        }

        val tasks = loadTasks()
        val notes = loadNotes()
        val set = LinkedHashSet<TaskNoteLink>()
        tasks.forEach { t ->
            t.noteId?.let { set.add(TaskNoteLink(taskId = t.id, noteId = it)) }
        }
        notes.forEach { n ->
            n.taskId?.let { set.add(TaskNoteLink(taskId = it, noteId = n.id)) }
        }
        val migrated = set.toList()
        saveLinks(migrated)
        return migrated
    }

    private fun saveFolders(list: List<NoteFolder>) {
        settings.putString(foldersKey, json.encodeToString(ListSerializer(NoteFolder.serializer()), list))
    }

    private fun saveLinks(list: List<TaskNoteLink>) {
        settings.putString(linksKey, json.encodeToString(ListSerializer(TaskNoteLink.serializer()), list))
    }

    private fun loadPrefs(): AppPrefs {
        val raw = settings.getStringOrNull(prefsKey) ?: return AppPrefs()
        return runCatching { json.decodeFromString(AppPrefs.serializer(), raw) }
            .getOrElse { AppPrefs() }
    }

    private fun savePrefs(prefs: AppPrefs) {
        settings.putString(prefsKey, json.encodeToString(AppPrefs.serializer(), prefs))
        requestPinnedTasksNotificationUpdate()
    }

    suspend fun addTask(
        title: String,
        plan: String,
        noteId: String?,
        plannedAt: kotlinx.datetime.Instant?,
        estimateHours: Double?,
        deadline: kotlinx.datetime.Instant?,
        importance: Importance,
        tagId: String?,
        subtasks: List<Subtask>
    ) {
        mutex.withLock {
            val now = nowInstant()
            val task = TodoTask(
                id = newId("task"),
                title = title.trim(),
                plan = plan.trim(),
                noteId = noteId,
                subtasks = subtasks,
                createdAt = now,
                plannedAt = plannedAt,
                estimateHours = estimateHours,
                deadline = deadline,
                importance = importance,
                tagId = tagId,
                done = false,
                pinned = false
            )
            val newList = (_tasks.value + task)
            _tasks.value = newList
            saveTasks(newList)
            rescheduleIfNeeded(task)
            if (noteId != null) {
                addLink(taskId = task.id, noteId = noteId)
            }
        }
    }

    suspend fun updateTask(task: TodoTask) {
        mutex.withLock {
            val oldTask = _tasks.value.firstOrNull { it.id == task.id }
            val oldNoteId = oldTask?.noteId
            val newList = _tasks.value.map { if (it.id == task.id) task else it }
            _tasks.value = newList
            saveTasks(newList)
            rescheduleIfNeeded(task)
            if (oldNoteId != task.noteId && oldNoteId != null) {
                removeLink(taskId = task.id, noteId = oldNoteId)
            }
            if (task.noteId != null) {
                addLink(taskId = task.id, noteId = task.noteId)
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        mutex.withLock {
            scheduler.cancel(taskId)
            val newList = _tasks.value.filterNot { it.id == taskId }
            _tasks.value = newList
            saveTasks(newList)
            removeLinksForTask(taskId)
        }
    }

    suspend fun toggleDone(taskId: String) {
        mutex.withLock {
            val newList = _tasks.value.map {
                if (it.id == taskId) it.copy(done = !it.done) else it
            }
            _tasks.value = newList
            saveTasks(newList)
            newList.firstOrNull { it.id == taskId }?.let { rescheduleIfNeeded(it) }
        }
    }

    suspend fun togglePinned(taskId: String) {
        mutex.withLock {
            val newList = _tasks.value.map {
                if (it.id == taskId) it.copy(pinned = !it.pinned) else it
            }
            _tasks.value = newList
            saveTasks(newList)
        }
    }

    suspend fun toggleSubtask(taskId: String, subtaskId: String) {
        mutex.withLock {
            val updated = _tasks.value.map { t ->
                if (t.id != taskId) return@map t
                t.copy(
                    subtasks = t.subtasks.map { s ->
                        if (s.id == subtaskId) s.copy(done = !s.done) else s
                    }
                )
            }
            _tasks.value = updated
            saveTasks(updated)
            updated.firstOrNull { it.id == taskId }?.let { rescheduleIfNeeded(it) }
        }
    }

    suspend fun addNote(
        title: String,
        content: String,
        blocks: List<com.grigorevmp.simpletodo.model.NoteBlock> = emptyList(),
        taskId: String?,
        folderId: String?,
        favorite: Boolean = false
    ) {
        mutex.withLock {
            val now = nowInstant()
            val note = Note(
                id = newId("note"),
                title = title.trim(),
                content = content,
                blocks = blocks,
                taskId = taskId,
                folderId = folderId,
                favorite = favorite,
                createdAt = now,
                updatedAt = now
            )
            val newList = _notes.value + note
            _notes.value = newList
            saveNotes(newList)
            if (taskId != null) {
                addLink(taskId = taskId, noteId = note.id)
            }
        }
    }

    suspend fun updateNote(note: Note) {
        mutex.withLock {
            val oldNote = _notes.value.firstOrNull { it.id == note.id }
            val oldTaskId = oldNote?.taskId
            val updated = note.copy(
                title = note.title.trim(),
                content = note.content,
                updatedAt = nowInstant()
            )
            val newList = _notes.value.map { if (it.id == note.id) updated else it }
            _notes.value = newList
            saveNotes(newList)
            if (oldTaskId != updated.taskId && oldTaskId != null) {
                removeLink(taskId = oldTaskId, noteId = note.id)
            }
            if (updated.taskId != null) {
                addLink(taskId = updated.taskId, noteId = note.id)
            }
        }
    }

    suspend fun toggleNoteFavorite(noteId: String) {
        mutex.withLock {
            val updated = _notes.value.map { n ->
                if (n.id == noteId) n.copy(favorite = !n.favorite, updatedAt = nowInstant()) else n
            }
            _notes.value = updated
            saveNotes(updated)
        }
    }

    suspend fun deleteNote(noteId: String) {
        mutex.withLock {
            val newList = _notes.value.filterNot { it.id == noteId }
            _notes.value = newList
            saveNotes(newList)
            removeLinksForNote(noteId)
        }
    }

    suspend fun addFolder(name: String, parentId: String?) {
        mutex.withLock {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return
            val now = nowInstant()
            val folder = NoteFolder(
                id = newId("folder"),
                name = trimmed,
                parentId = parentId,
                createdAt = now
            )
            val newList = _noteFolders.value + folder
            _noteFolders.value = newList
            saveFolders(newList)
        }
    }

    suspend fun renameFolder(folderId: String, name: String) {
        mutex.withLock {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return
            val updated = _noteFolders.value.map { f ->
                if (f.id == folderId) f.copy(name = trimmed) else f
            }
            _noteFolders.value = updated
            saveFolders(updated)
        }
    }

    suspend fun deleteFolder(folderId: String) {
        mutex.withLock {
            val childrenMap = _noteFolders.value.groupBy { it.parentId }
            val toDelete = mutableSetOf<String>()
            val stack = ArrayDeque<String>()
            stack.add(folderId)
            while (stack.isNotEmpty()) {
                val id = stack.removeLast()
                if (!toDelete.add(id)) continue
                childrenMap[id]?.forEach { child -> stack.add(child.id) }
            }

            val foldersLeft = _noteFolders.value.filterNot { it.id in toDelete }
            _noteFolders.value = foldersLeft
            saveFolders(foldersLeft)

            val notesUpdated = _notes.value.map { n ->
                if (n.folderId in toDelete) n.copy(folderId = null) else n
            }
            _notes.value = notesUpdated
            saveNotes(notesUpdated)
        }
    }

    suspend fun setSort(sort: SortConfig) {
        mutex.withLock {
            val p = _prefs.value.copy(sort = sort)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setNoteSort(sort: NoteSortConfig) {
        mutex.withLock {
            val p = _prefs.value.copy(noteSort = sort)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setShowTagFilters(show: Boolean) {
        mutex.withLock {
            val p = _prefs.value.copy(showTagFilters = show)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setShowCompletedTasks(show: Boolean) {
        mutex.withLock {
            val p = _prefs.value.copy(showCompletedTasks = show)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setDimScroll(enabled: Boolean) {
        mutex.withLock {
            val p = _prefs.value.copy(dimScroll = enabled)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setLiquidGlass(enabled: Boolean) {
        mutex.withLock {
            val p = _prefs.value.copy(liquidGlass = enabled)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setDisableDarkTheme(enabled: Boolean) {
        mutex.withLock {
            var p = _prefs.value.copy(disableDarkTheme = enabled)
            if (enabled && p.themeMode == ThemeMode.DIM) {
                p = p.copy(themeMode = ThemeMode.SYSTEM)
            }
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setReminders(enabled: Boolean) {
        mutex.withLock {
            val p = _prefs.value.copy(remindersEnabled = enabled)
            _prefs.value = p
            savePrefs(p)
            rescheduleAllLocked()
        }
    }

    suspend fun setLeadMinutes(minutes: Int) {
        mutex.withLock {
            val m = minutes.coerceIn(0, 7 * 24 * 60)
            val p = _prefs.value.copy(reminderLeadMinutes = m)
            _prefs.value = p
            savePrefs(p)
            rescheduleAllLocked()
        }
    }

    suspend fun setPinnedNotifications(enabled: Boolean) {
        mutex.withLock {
            val p = _prefs.value.copy(pinPinnedInNotifications = enabled)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun refreshNotificationsOnLaunch() {
        mutex.withLock {
            rescheduleAllLocked()
            requestPinnedTasksNotificationUpdate()
        }
    }

    suspend fun addTag(name: String) {
        mutex.withLock {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return
            val exists = _prefs.value.tags.any { it.name.equals(trimmed, ignoreCase = true) }
            if (exists) return

            val idx = (_prefs.value.tags.maxOfOrNull { it.colorIndex } ?: -1) + 1
            val newTag = Tag(id = newId("tag"), name = trimmed, colorIndex = idx)
            val p = _prefs.value.copy(tags = _prefs.value.tags + newTag)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun deleteTag(tagId: String) {
        mutex.withLock {
            val p = _prefs.value.copy(tags = _prefs.value.tags.filterNot { it.id == tagId })
            _prefs.value = p
            savePrefs(p)

            val updatedTasks = _tasks.value.map { t ->
                if (t.tagId == tagId) t.copy(tagId = null) else t
            }
            _tasks.value = updatedTasks
            saveTasks(updatedTasks)
        }
    }

    suspend fun setTheme(mode: ThemeMode) {
        mutex.withLock {
            val p = _prefs.value.copy(themeMode = mode)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setAuthorAccent(index: Int) {
        mutex.withLock {
            val p = _prefs.value.copy(authorAccentIndex = index.coerceIn(0, 4))
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        mutex.withLock {
            val p = _prefs.value.copy(language = language)
            _prefs.value = p
            savePrefs(p)
        }
    }

    suspend fun exportData(): String = mutex.withLock {
        val payload = BackupPayload(
            tasks = _tasks.value,
            notes = _notes.value,
            folders = _noteFolders.value,
            links = _taskNoteLinks.value,
            prefs = _prefs.value
        )
        json.encodeToString(BackupPayload.serializer(), payload)
    }

    suspend fun importData(payload: String): Result<Unit> = runCatching {
        val data = json.decodeFromString(BackupPayload.serializer(), payload)
        mutex.withLock {
            _tasks.value = data.tasks
            _notes.value = data.notes
            _noteFolders.value = data.folders
            _taskNoteLinks.value = data.links
            _prefs.value = data.prefs
            saveTasks(data.tasks)
            saveNotes(data.notes)
            saveFolders(data.folders)
            saveLinks(data.links)
            savePrefs(data.prefs)
            rescheduleAllLocked()
        }
    }

    suspend fun clearAllData() {
        mutex.withLock {
            scheduler.cancelAll()
            _tasks.value = emptyList()
            _notes.value = emptyList()
            _noteFolders.value = emptyList()
            _taskNoteLinks.value = emptyList()
            _prefs.value = AppPrefs()
            saveTasks(_tasks.value)
            saveNotes(_notes.value)
            saveFolders(_noteFolders.value)
            saveLinks(_taskNoteLinks.value)
            savePrefs(_prefs.value)
        }
    }


    suspend fun clearCompletedTasks() {
        mutex.withLock {
            val doneIds = _tasks.value.filter { it.done }.map { it.id }.toSet()
            if (doneIds.isEmpty()) return
            doneIds.forEach { scheduler.cancel(it) }
            val updated = _tasks.value.filterNot { it.id in doneIds }
            _tasks.value = updated
            saveTasks(updated)
            removeLinksForTasks(doneIds)
        }
    }

    fun sortedTasks(tasks: List<TodoTask>, prefs: AppPrefs): List<TodoTask> {
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

    private fun rescheduleIfNeeded(task: TodoTask) {
        val p = _prefs.value
        scheduler.cancel(task.id)

        if (!p.remindersEnabled) return
        if (task.done) return
        if (task.deadline == null) return
        if (isOverdue(task, nowInstant().toEpochMilliseconds())) return

        scheduler.schedule(task, p.reminderLeadMinutes)
    }

    private fun rescheduleAllLocked() {
        scheduler.cancelAll()
        val p = _prefs.value
        if (!p.remindersEnabled) return

        _tasks.value.forEach { t ->
            if (!t.done && t.deadline != null && !isOverdue(t, nowInstant().toEpochMilliseconds())) {
                scheduler.schedule(t, p.reminderLeadMinutes)
            }
        }
    }

    private fun addLink(taskId: String, noteId: String) {
        val link = TaskNoteLink(taskId = taskId, noteId = noteId)
        if (_taskNoteLinks.value.any { it.taskId == taskId && it.noteId == noteId }) return
        val updated = _taskNoteLinks.value + link
        _taskNoteLinks.value = updated
        saveLinks(updated)
    }

    private fun removeLink(taskId: String, noteId: String) {
        val updated = _taskNoteLinks.value.filterNot { it.taskId == taskId && it.noteId == noteId }
        if (updated.size == _taskNoteLinks.value.size) return
        _taskNoteLinks.value = updated
        saveLinks(updated)
    }

    private fun removeLinksForTask(taskId: String) {
        val updated = _taskNoteLinks.value.filterNot { it.taskId == taskId }
        if (updated.size == _taskNoteLinks.value.size) return
        _taskNoteLinks.value = updated
        saveLinks(updated)
    }

    private fun removeLinksForTasks(taskIds: Set<String>) {
        val updated = _taskNoteLinks.value.filterNot { it.taskId in taskIds }
        if (updated.size == _taskNoteLinks.value.size) return
        _taskNoteLinks.value = updated
        saveLinks(updated)
    }

    private fun removeLinksForNote(noteId: String) {
        val updated = _taskNoteLinks.value.filterNot { it.noteId == noteId }
        if (updated.size == _taskNoteLinks.value.size) return
        _taskNoteLinks.value = updated
        saveLinks(updated)
    }
}
