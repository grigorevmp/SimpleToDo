package com.grigorevmp.simpletodo.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val colorIndex: Int
)

@Serializable
enum class Importance { LOW, NORMAL, HIGH, CRITICAL }

@Serializable
data class Subtask(
    val id: String,
    val text: String,
    val done: Boolean
)

@Serializable
data class NoteFolder(
    val id: String,
    val name: String,
    val parentId: String? = null,
    @Contextual val createdAt: Instant
)

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val taskId: String? = null,
    val folderId: String? = null,
    val favorite: Boolean = false,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant
)

@Serializable
data class TaskNoteLink(
    val taskId: String,
    val noteId: String
)

@Serializable
data class TodoTask(
    val id: String,
    val title: String,
    val plan: String,
    val noteId: String? = null,
    val subtasks: List<Subtask>,
    @Contextual val createdAt: Instant,
    @Contextual val plannedAt: Instant? = null,
    val estimateHours: Double? = null,
    @Contextual val deadline: Instant? = null,
    val importance: Importance = Importance.NORMAL,
    val tagId: String? = null,
    val done: Boolean = false
)

@Serializable
enum class SortField { PLANNED_AT, DEADLINE, IMPORTANCE, CREATED_AT, TITLE }

@Serializable
enum class SortDir { ASC, DESC }

@Serializable
data class SortConfig(
    val primary: SortField = SortField.PLANNED_AT,
    val primaryDir: SortDir = SortDir.ASC,
    val secondary: SortField = SortField.DEADLINE,
    val secondaryDir: SortDir = SortDir.ASC
)

@Serializable
enum class NoteSortField { DATE, NAME }

@Serializable
data class NoteSortConfig(
    val field: NoteSortField = NoteSortField.DATE,
    val foldersOnTop: Boolean = true
)

@Serializable
enum class ThemeMode { SYSTEM, DYNAMIC, DIM, AUTHOR }

@Serializable
data class AppPrefs(
    val remindersEnabled: Boolean = true,
    val reminderLeadMinutes: Int = 30,
    val sort: SortConfig = SortConfig(),
    val noteSort: NoteSortConfig = NoteSortConfig(),
    val showTagFilters: Boolean = false,
    val showCompletedTasks: Boolean = false,
    val dimScroll: Boolean = true,
    val liquidGlass: Boolean = true,
    val disableDarkTheme: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val authorAccentIndex: Int = 0,
    val tags: List<Tag> = listOf(
        Tag(id = "tag_work", name = "Work", colorIndex = 0),
        Tag(id = "tag_study", name = "Study", colorIndex = 1),
        Tag(id = "tag_home", name = "Home", colorIndex = 2),
        Tag(id = "tag_health", name = "Health", colorIndex = 3)
    )
)
