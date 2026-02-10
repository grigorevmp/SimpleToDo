package com.grigorevmp.simpletodo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.DeleteIcon
import com.grigorevmp.simpletodo.ui.components.EditIcon
import com.grigorevmp.simpletodo.ui.components.TagIcon
import com.grigorevmp.simpletodo.ui.components.NoteIcon
import com.grigorevmp.simpletodo.util.formatDeadline
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TaskCard(
    task: TodoTask,
    tagLabel: String?,
    noteTitle: String?,
    onOpenNote: () -> Unit,
    onToggleDone: () -> Unit,
    onToggleSub: (String) -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var subtasksExpanded by remember(task.id) { mutableStateOf(false) }
    val tone = when (task.importance) {
        Importance.LOW -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
        Importance.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
        Importance.HIGH -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        Importance.CRITICAL -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    }
    val deadlineDays = task.deadline?.let { daysUntil(it) }
    val deadlineSoon = (deadlineDays ?: Int.MAX_VALUE) in 0..4
    val longEstimate = (task.estimateHours ?: 0.0) > 6.0
    val remainingSubs = task.subtasks.count { !it.done }
    val subLabel = if (remainingSubs > 0) {
        "Осталось: $remainingSubs"
    } else {
        "Подзадачи"
    }

    Box(Modifier.fillMaxWidth()) {
        ImportanceFlameBackdrop(
            importance = task.importance,
            modifier = Modifier
                .matchParentSize()
                .padding(10.dp)
        )
        Surface(
            onClick = onOpenDetails,
            tonalElevation = 4.dp,
            shape = MaterialTheme.shapes.large,
            color = tone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(14.dp)
                    .animateContentSize(animationSpec = tween(durationMillis = 100)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (deadlineSoon) {
                    StatusBanner(
                        text = deadlineLabel(deadlineDays ?: 0),
                        color = MaterialTheme.colorScheme.error,
                        onColor = MaterialTheme.colorScheme.onError
                    )
                }
                if (longEstimate) {
                    StatusBanner(
                        text = "Долгая задача",
                        color = MaterialTheme.colorScheme.onSurface,
                        onColor = MaterialTheme.colorScheme.surface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.done,
                        onCheckedChange = { onToggleDone() }
                    )
                    Spacer(Modifier.width(8.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(EditIcon, contentDescription = "Edit")
                    }

                    IconButton(onClick = onDelete) {
                        Icon(DeleteIcon, contentDescription = "Delete")
                    }
                }

                if (task.plan.isNotBlank()) {
                    Text(task.plan, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.deadline?.let { InfoChip("Deadline: ${formatDeadline(it)}") }
                    task.estimateHours?.let { InfoChip(formatHours(it)) }
                }

                Row {
                    if (!tagLabel.isNullOrBlank()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(TagIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text(tagLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (!noteTitle.isNullOrBlank()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable { onOpenNote() }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(NoteIcon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Note",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    if (task.subtasks.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.CenterVertically)
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { subtasksExpanded = !subtasksExpanded }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (subtasksExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = subLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = task.subtasks.isNotEmpty() && subtasksExpanded,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    SubTaskCard(task, onToggleSub)
                }
            }
        }

    }
}

@Composable
private fun SubTaskCard(
    task: TodoTask,
    onToggleSub: (String) -> Unit
) {
    Column(
        Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        task.subtasks.forEach { s ->
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = s.done,
                        onCheckedChange = { onToggleSub(s.id) }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(s.text, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun formatHours(v: Double): String {
    val scaled = (v * 10).roundToInt()
    val intPart = scaled / 10
    val frac = abs(scaled % 10)
    val text = if (frac == 0) {
        intPart.toString()
    } else {
        "$intPart.$frac"
    }
    val suffix = hourSuffix(intPart, frac != 0)
    return "$text $suffix"
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusBanner(text: String, color: androidx.compose.ui.graphics.Color, onColor: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = onColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun daysUntil(deadline: Instant): Int {
    val now = Clock.System.now()
    val diffMs = deadline.toEpochMilliseconds() - now.toEpochMilliseconds()
    val dayMs = 24 * 60 * 60 * 1000L
    val days = ((diffMs + dayMs - 1) / dayMs).toInt()
    return if (days < 0) 0 else days
}

private fun deadlineLabel(days: Int): String {
    return when (days) {
        0 -> "Дедлайн сегодня"
        1 -> "Дедлайн через 1 день"
        in 2..4 -> "Дедлайн через $days дня"
        else -> "Дедлайн скоро"
    }
}

private fun hourSuffix(intPart: Int, hasFraction: Boolean): String {
    if (hasFraction) return "часа"
    val n = intPart % 100
    val n1 = intPart % 10
    return when {
        n in 11..19 -> "часов"
        n1 == 1 -> "час"
        n1 in 2..4 -> "часа"
        else -> "часов"
    }
}

@Composable
private fun ImportanceFlameBackdrop(importance: Importance, modifier: Modifier = Modifier) {
    val count = when (importance) {
        Importance.LOW -> 0
        Importance.NORMAL -> 1
        Importance.HIGH -> 3
        Importance.CRITICAL -> 6
    }
    if (count == 0) return
    val sizes = listOf(18.dp, 22.dp, 26.dp, 30.dp, 34.dp, 38.dp)
    val positions = flamePositions(count)
    BoxWithConstraints(modifier = modifier.alpha(0.85f)) {
        val w = maxWidth
        val h = maxHeight
        positions.forEachIndexed { index, (x, y) ->
            val size = sizes[index.coerceAtMost(sizes.lastIndex)]
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .offset(x = w * x, y = h * y)
            )
        }
    }
}

private fun flamePositions(count: Int): List<Pair<Float, Float>> {
    val all = listOf(
        0.08f to 0.12f,
        0.68f to 0.10f,
        0.18f to 0.52f,
        0.62f to 0.48f,
        0.10f to 0.78f,
        0.72f to 0.76f
    )
    return all.take(count)
}

@Preview
@Composable
fun SubTaskPreview() {
    SubTaskCard(
        TodoTask(
            id = "0",
            title = "Z",
            plan = "Z",
            subtasks = emptyList(),
            createdAt = Clock.System.now(),
            importance = Importance.NORMAL
        ),
        { }
    )
}
