package com.grigorevmp.simpletodo.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.Subtask
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.theme.DinoTheme
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.datetime.Clock

@Preview(name = "TaskCard - Minimal", showBackground = true)
@Composable
private fun TaskCardPreviewMinimal() {
    DinoTheme(dark = false, mode = ThemeMode.SYSTEM, authorAccentIndex = 0) {
        val backdrop = rememberLayerBackdrop {
            drawContent()
        }

        TaskCard(
            task = sampleTaskMinimal,
            tagLabel = null,
            onToggleDone = {},
            onToggleSub = {},
            onOpenDetails = {},
            onEdit = {},
            onDelete = {},
            onOpenNotes = {},
            noteCount = 0,
            backdrop = backdrop,
        )
    }
}

@Preview(name = "TaskCard - Planned", showBackground = true)
@Composable
private fun TaskCardPreviewPlanned() {
    DinoTheme(dark = false, mode = ThemeMode.SYSTEM, authorAccentIndex = 0) {
        val backdrop = rememberLayerBackdrop {
            drawContent()
        }

        TaskCard(
            task = sampleTaskPlanned,
            tagLabel = "Work",
            onToggleDone = {},
            onToggleSub = {},
            onOpenDetails = {},
            onEdit = {},
            onDelete = {},
            onOpenNotes = {},
            noteCount = 0,
            backdrop = backdrop,
        )
    }
}

@Preview(name = "TaskCard - Critical", showBackground = true)
@Composable
private fun TaskCardPreviewCritical() {
    DinoTheme(dark = false, mode = ThemeMode.SYSTEM, authorAccentIndex = 0) {
        val backdrop = rememberLayerBackdrop {
            drawContent()
        }

        TaskCard(
            task = sampleTaskCritical,
            tagLabel = "Release",
            onToggleDone = {},
            onToggleSub = {},
            onOpenDetails = {},
            onEdit = {},
            onDelete = {},
            onOpenNotes = {},
            noteCount = 0,
            backdrop = backdrop,
        )
    }
}

@Preview(name = "TaskCard - Done", showBackground = true)
@Composable
private fun TaskCardPreviewDone() {
    DinoTheme(dark = false, mode = ThemeMode.SYSTEM, authorAccentIndex = 0) {
        val backdrop = rememberLayerBackdrop {
            drawContent()
        }

        TaskCard(
            task = sampleTaskDone,
            tagLabel = "Home",
            onToggleDone = {},
            onToggleSub = {},
            onOpenDetails = {},
            onEdit = {},
            onDelete = {},
            onOpenNotes = {},
            noteCount = 0,
            backdrop = backdrop,
        )
    }
}

private val sampleTaskMinimal = TodoTask(
    id = "task_min",
    title = "Buy coffee",
    plan = "",
    subtasks = emptyList(),
    createdAt = kotlinx.datetime.Instant.parse("2026-02-09T09:00:00Z"),
    plannedAt = null,
    estimateHours = null,
    deadline = null,
    importance = Importance.NORMAL,
    tagId = null,
    done = false
)

private val sampleTaskPlanned = TodoTask(
    id = "task_plan",
    title = "Write release notes",
    plan = "Draft summary, list highlights, and verify dates.",
    subtasks = listOf(
        Subtask(id = "s1", text = "Collect changes", done = true),
        Subtask(id = "s2", text = "Write summary", done = false),
        Subtask(id = "s3", text = "Proofread", done = false)
    ),
    createdAt = Clock.System.now(),
    plannedAt = Clock.System.now(),
    estimateHours = 1.5,
    deadline = Clock.System.now(),
    importance = Importance.HIGH,
    tagId = "tag_work",
    done = false
)

private val sampleTaskCritical = TodoTask(
    id = "task_critical",
    title = "Fix login regression",
    plan = "Reproduce, bisect, patch, and add test.",
    subtasks = listOf(
        Subtask(id = "s1", text = "Reproduce on staging", done = true),
        Subtask(id = "s2", text = "Identify root cause", done = false)
    ),
    createdAt = Clock.System.now(),
    plannedAt = Clock.System.now(),
    estimateHours = 2.0,
    deadline = Clock.System.now(),
    importance = Importance.CRITICAL,
    tagId = "tag_work",
    done = false
)

private val sampleTaskDone = TodoTask(
    id = "task_done",
    title = "Pay electricity bill",
    plan = "",
    subtasks = emptyList(),
    createdAt = Clock.System.now(),
    plannedAt = Clock.System.now(),
    estimateHours = 0.3,
    deadline = Clock.System.now(),
    importance = Importance.LOW,
    tagId = "tag_home",
    done = true
)
