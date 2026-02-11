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
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.platform.NotificationScheduler
import com.grigorevmp.simpletodo.util.newId
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class TodoRepository(
    private val settings: Settings,
    private val scheduler: NotificationScheduler
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantIso8601Serializer)
        }
    }

    private val mutex = Mutex()

    private val tasksKey = "tasks_json_v1"
    private val notesKey = "notes_json_v1"
    private val foldersKey = "note_folders_json_v1"
    private val prefsKey = "prefs_json_v1"

    private val _tasks = MutableStateFlow(loadTasks())
    val tasks: StateFlow<List<TodoTask>> = _tasks

    private val _notes = MutableStateFlow(loadNotes())
    val notes: StateFlow<List<Note>> = _notes

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
        settings.putString(tasksKey, json.encodeToString(ListSerializer(TodoTask.serializer()), list))
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

    private fun saveFolders(list: List<NoteFolder>) {
        settings.putString(foldersKey, json.encodeToString(ListSerializer(NoteFolder.serializer()), list))
    }

    private fun loadPrefs(): AppPrefs {
        val raw = settings.getStringOrNull(prefsKey) ?: return AppPrefs()
        return runCatching { json.decodeFromString(AppPrefs.serializer(), raw) }
            .getOrElse { AppPrefs() }
    }

    private fun savePrefs(prefs: AppPrefs) {
        settings.putString(prefsKey, json.encodeToString(AppPrefs.serializer(), prefs))
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
            val now = Clock.System.now()
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
                done = false
            )
            val newList = (_tasks.value + task)
            _tasks.value = newList
            saveTasks(newList)
            rescheduleIfNeeded(task)
            updateNoteLinkForTask(taskId = task.id, newNoteId = noteId, oldNoteId = null)
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
            updateNoteLinkForTask(taskId = task.id, newNoteId = task.noteId, oldNoteId = oldNoteId)
        }
    }

    suspend fun deleteTask(taskId: String) {
        mutex.withLock {
            scheduler.cancel(taskId)
            val newList = _tasks.value.filterNot { it.id == taskId }
            _tasks.value = newList
            saveTasks(newList)
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
        taskId: String?,
        folderId: String?
    ) {
        mutex.withLock {
            val now = Clock.System.now()
            val note = Note(
                id = newId("note"),
                title = title.trim(),
                content = content,
                taskId = taskId,
                folderId = folderId,
                createdAt = now,
                updatedAt = now
            )
            val newList = _notes.value + note
            _notes.value = newList
            saveNotes(newList)
            updateTaskLinkForNote(noteId = note.id, newTaskId = taskId, oldTaskId = null)
        }
    }

    suspend fun updateNote(note: Note) {
        mutex.withLock {
            val oldNote = _notes.value.firstOrNull { it.id == note.id }
            val oldTaskId = oldNote?.taskId
            val updated = note.copy(
                title = note.title.trim(),
                content = note.content,
                updatedAt = Clock.System.now()
            )
            val newList = _notes.value.map { if (it.id == note.id) updated else it }
            _notes.value = newList
            saveNotes(newList)
            updateTaskLinkForNote(noteId = note.id, newTaskId = updated.taskId, oldTaskId = oldTaskId)
        }
    }

    suspend fun deleteNote(noteId: String) {
        mutex.withLock {
            val newList = _notes.value.filterNot { it.id == noteId }
            _notes.value = newList
            saveNotes(newList)
        }
    }

    suspend fun addFolder(name: String, parentId: String?) {
        mutex.withLock {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return
            val now = Clock.System.now()
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

    fun sortedTasks(tasks: List<TodoTask>, prefs: AppPrefs): List<TodoTask> {
        val sort = prefs.sort
        val cmp = compareBy<TodoTask> { sortKey(it, sort.primary) }
        val primaryCmp = if (sort.primaryDir == SortDir.ASC) cmp else cmp.reversed()

        val cmp2 = compareBy<TodoTask> { sortKey(it, sort.secondary) }
        val secondaryCmp = if (sort.secondaryDir == SortDir.ASC) cmp2 else cmp2.reversed()

        val doneCmp = compareBy<TodoTask> { it.done }

        return tasks.sortedWith(doneCmp.then(primaryCmp).then(secondaryCmp))
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

        scheduler.schedule(task, p.reminderLeadMinutes)
    }

    private fun rescheduleAllLocked() {
        scheduler.cancelAll()
        val p = _prefs.value
        if (!p.remindersEnabled) return

        _tasks.value.forEach { t ->
            if (!t.done && t.deadline != null) {
                scheduler.schedule(t, p.reminderLeadMinutes)
            }
        }
    }

    private fun updateNoteLinkForTask(taskId: String, newNoteId: String?, oldNoteId: String?) {
        if (newNoteId == oldNoteId) return
        var updated = _notes.value
        var changed = false

        if (newNoteId != null) {
            val oldTaskId = updated.firstOrNull { it.id == newNoteId }?.taskId
            if (oldTaskId != null && oldTaskId != taskId) {
                _tasks.value = _tasks.value.map { t ->
                    if (t.id == oldTaskId && t.noteId == newNoteId) t.copy(noteId = null) else t
                }
                saveTasks(_tasks.value)
            }
        }

        if (oldNoteId != null && oldNoteId != newNoteId) {
            updated = updated.map { n ->
                if (n.id == oldNoteId && n.taskId == taskId) {
                    changed = true
                    n.copy(taskId = null)
                } else {
                    n
                }
            }
        }

        if (newNoteId != null) {
            updated = updated.map { n ->
                if (n.id == newNoteId && n.taskId != taskId) {
                    changed = true
                    n.copy(taskId = taskId)
                } else {
                    n
                }
            }
        }

        if (changed) {
            _notes.value = updated
            saveNotes(updated)
        }
    }

    private fun updateTaskLinkForNote(noteId: String, newTaskId: String?, oldTaskId: String?) {
        if (newTaskId == oldTaskId) return
        var updated = _tasks.value
        var changed = false

        if (newTaskId != null) {
            val oldNoteId = updated.firstOrNull { it.id == newTaskId }?.noteId
            if (oldNoteId != null && oldNoteId != noteId) {
                _notes.value = _notes.value.map { n ->
                    if (n.id == oldNoteId && n.taskId == newTaskId) n.copy(taskId = null) else n
                }
                saveNotes(_notes.value)
            }
        }

        if (oldTaskId != null && oldTaskId != newTaskId) {
            updated = updated.map { t ->
                if (t.id == oldTaskId && t.noteId == noteId) {
                    changed = true
                    t.copy(noteId = null)
                } else {
                    t
                }
            }
        }

        if (newTaskId != null) {
            updated = updated.map { t ->
                if (t.id == newTaskId && t.noteId != noteId) {
                    changed = true
                    t.copy(noteId = noteId)
                } else {
                    t
                }
            }
        }

        if (changed) {
            _tasks.value = updated
            saveTasks(updated)
        }
    }
}
