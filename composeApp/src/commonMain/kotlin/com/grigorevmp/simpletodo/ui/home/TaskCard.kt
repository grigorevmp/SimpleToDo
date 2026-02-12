package com.grigorevmp.simpletodo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.model.Importance
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.DeleteIcon
import com.grigorevmp.simpletodo.ui.components.EditIcon
import com.grigorevmp.simpletodo.ui.components.NoteIcon
import com.grigorevmp.simpletodo.ui.components.TagIcon
import com.grigorevmp.simpletodo.util.formatDeadline
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TaskCard(
    task: TodoTask,
    tagLabel: String?,
    noteCount: Int,
    backdrop: LayerBackdrop,
    onOpenNotes: () -> Unit,
    onToggleDone: () -> Unit,
    onToggleSub: (String) -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var subtasksExpanded by remember(task.id) { mutableStateOf(false) }
    var showActions by remember(task.id) { mutableStateOf(false) }
    val shape = MaterialTheme.shapes.large
    val cardInteraction = remember { MutableInteractionSource() }
    val cardIndication = LocalIndication.current
    val menuShape = MaterialTheme.shapes.large
    val glassBase = MaterialTheme.colorScheme.surfaceVariant
    val glassBrush = Brush.verticalGradient(
        listOf(
            glassBase.copy(alpha = 0.12f),
            glassBase.copy(alpha = 0.28f)
        )
    )
    val density = LocalDensity.current
    val blurPx = with(density) { 3.dp.toPx() }
    val lensInnerPx = with(density) { 10.dp.toPx() }
    val lensOuterPx = with(density) { 20.dp.toPx() }
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
            tonalElevation = 4.dp,
            shape = shape,
            color = tone,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .combinedClickable(
                    onClick = onOpenDetails,
                    onLongClick = { showActions = true },
                    role = Role.Button,
                    interactionSource = cardInteraction,
                    indication = cardIndication
                )
        ) {
            Column(
                Modifier.padding(14.dp)
                    .animateContentSize(animationSpec = tween(durationMillis = 100)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (deadlineSoon) {
                    StatusBanner(
                        text = task.deadline?.let { deadlineLabel(it) } ?: "",
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
                }

                if (task.plan.isNotBlank()) {
                    Text(task.plan, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.deadline?.let { InfoChip("Deadline: ${formatDeadline(it)}") }
                    task.estimateHours?.let { InfoChip(formatHours(it)) }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    if (noteCount > 0) {
                        val noteLabel = if (noteCount == 1) "Note" else "Notes"
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable { onOpenNotes() }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    NoteIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (noteCount == 1) noteLabel else "$noteLabel $noteCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (task.subtasks.isNotEmpty()) {
                        Spacer(Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { subtasksExpanded = !subtasksExpanded }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
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
                        Spacer(Modifier.width(6.dp))
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(6.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            DropdownMenu(
                expanded = showActions,
                onDismissRequest = { showActions = false },
                modifier = Modifier.width(200.dp),
                shape = menuShape,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    Modifier.padding(vertical = 6.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", style = MaterialTheme.typography.titleMedium) },
                        leadingIcon = { Icon(EditIcon, contentDescription = null) },
                        onClick = {
                            showActions = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", style = MaterialTheme.typography.titleMedium) },
                        leadingIcon = { Icon(DeleteIcon, contentDescription = null) },
                        onClick = {
                            showActions = false
                            onDelete()
                        }
                    )
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

private fun deadlineLabel(deadline: Instant): String {
    val now = Clock.System.now()
    if (deadline < now) return "Дедлайн истек"
    val days = daysUntil(deadline)
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
