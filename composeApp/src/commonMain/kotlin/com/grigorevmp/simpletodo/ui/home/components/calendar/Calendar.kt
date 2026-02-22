package com.grigorevmp.simpletodo.ui.home.components.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.SimpleIcons
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.itemPlacement
import com.grigorevmp.simpletodo.ui.home.EmptyState
import com.grigorevmp.simpletodo.ui.home.TaskCard
import com.grigorevmp.simpletodo.util.nowInstant
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.calendar_empty_body
import simpletodo.composeapp.generated.resources.calendar_empty_title
import simpletodo.composeapp.generated.resources.calendar_selected_day
import simpletodo.composeapp.generated.resources.day_fri
import simpletodo.composeapp.generated.resources.day_mon
import simpletodo.composeapp.generated.resources.day_sat
import simpletodo.composeapp.generated.resources.day_sun
import simpletodo.composeapp.generated.resources.day_thu
import simpletodo.composeapp.generated.resources.day_tue
import simpletodo.composeapp.generated.resources.day_wed
import simpletodo.composeapp.generated.resources.hour_short
import simpletodo.composeapp.generated.resources.month_apr
import simpletodo.composeapp.generated.resources.month_aug
import simpletodo.composeapp.generated.resources.month_dec
import simpletodo.composeapp.generated.resources.month_feb
import simpletodo.composeapp.generated.resources.month_jan
import simpletodo.composeapp.generated.resources.month_jul
import simpletodo.composeapp.generated.resources.month_jun
import simpletodo.composeapp.generated.resources.month_mar
import simpletodo.composeapp.generated.resources.month_may
import simpletodo.composeapp.generated.resources.month_nov
import simpletodo.composeapp.generated.resources.month_oct
import simpletodo.composeapp.generated.resources.month_sep


@Composable
fun CalendarTab(
    tasks: List<TodoTask>,
    onToggleDone: (String) -> Unit,
    onToggleSub: (String, String) -> Unit,
    onOpenDetails: (TodoTask) -> Unit,
    onEdit: (TodoTask) -> Unit,
    onDelete: (String) -> Unit,
    onClearCompleted: () -> Unit,
    showCompleted: Boolean,
    tagName: (String?) -> String?,
    noteCount: (TodoTask) -> Int,
    onOpenNotes: (TodoTask) -> Unit,
    dimScroll: Boolean,
    backdrop: LayerBackdrop
) {
    val tz = TimeZone.currentSystemDefault()
    val today = nowInstant().toLocalDateTime(tz).date
    val monthNames = rememberMonthNames()
    val dayLabels = rememberDayLabels()
    val hourLabel = stringResource(Res.string.hour_short)

    var selectedYear by remember { mutableStateOf(today.year) }
    var selectedMonth by remember { mutableStateOf(today.monthNumber) }
    var selectedDay by remember { mutableStateOf(today.dayOfMonth) }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        daysInMonth(selectedYear, selectedMonth)
    }
    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }
    val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
        LocalDate(selectedYear, selectedMonth, selectedDay)
    }

    val visibleTasks = remember(tasks, showCompleted) {
        if (showCompleted) tasks else tasks.filter { !it.done }
    }
    val tasksByDate = remember(visibleTasks) {
        val nowMs = nowInstant().toEpochMilliseconds()
        val map = LinkedHashMap<LocalDate, MutableList<TodoTask>>()
        visibleTasks.forEach { task ->
            val instant = relevantInstant(task, nowMs) ?: return@forEach
            val date = instant.toLocalDateTime(tz).date
            map.getOrPut(date) { mutableListOf() }.add(task)
        }
        map
    }

    val hoursByDate = remember(tasksByDate) {
        tasksByDate.mapValues { (_, list) ->
            list.sumOf { it.estimateHours ?: 0.0 }
        }
    }

    val dayTasks = tasksByDate[selectedDate].orEmpty()
    val selectedLabel = formatLongDate(selectedDate, monthNames)
    val listState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CalendarHeader(
                    monthNames = monthNames,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onPrev = {
                        val prev = LocalDate(selectedYear, selectedMonth, 1).plus(DatePeriod(months = -1))
                        selectedYear = prev.year
                        selectedMonth = prev.monthNumber
                        selectedDay = 1
                    },
                    onNext = {
                        val next = LocalDate(selectedYear, selectedMonth, 1).plus(DatePeriod(months = 1))
                        selectedYear = next.year
                        selectedMonth = next.monthNumber
                        selectedDay = 1
                    },
                    onPickYear = { y ->
                        selectedYear = y
                        selectedDay = 1
                    }
                )
                CalendarGrid(
                    dayLabels = dayLabels,
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    selectedDate = selectedDate,
                    today = today,
                    hoursByDate = hoursByDate,
                    hourLabel = hourLabel,
                    onSelectDate = { date ->
                        selectedYear = date.year
                        selectedMonth = date.monthNumber
                        selectedDay = date.dayOfMonth
                    }
                )
            }
            item {
                Text(
                    text = stringResource(Res.string.calendar_selected_day, selectedLabel),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                AnimatedContent(
                    targetState = dayTasks,
                    transitionSpec = {
                        fadeIn(tween(180)) + expandVertically(tween(220)) togetherWith
                            fadeOut(tween(120)) + shrinkVertically(tween(180))
                    },
                    label = "calendar-day-tasks"
                ) { list ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        if (list.isEmpty()) {
                            EmptyState(
                                title = stringResource(Res.string.calendar_empty_title),
                                body = stringResource(Res.string.calendar_empty_body),
                                showMascot = true
                            )
                        } else {
                            list.forEach { t ->
                                Box(Modifier.itemPlacement()) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = showCompleted || !t.done,
                                        enter = fadeIn(tween(160)) + expandVertically(tween(220)),
                                        exit = shrinkVertically(tween(320)) + fadeOut(tween(260))
                                    ) {
                                        TaskCard(
                                            task = t,
                                            tagLabel = tagName(t.tagId),
                                            noteCount = noteCount(t),
                                            onOpenNotes = { onOpenNotes(t) },
                                            onToggleDone = { onToggleDone(t.id) },
                                            onToggleSub = { subId -> onToggleSub(t.id, subId) },
                                            onOpenDetails = { onOpenDetails(t) },
                                            onEdit = { onEdit(t) },
                                            onDelete = { onDelete(t.id) },
                                            onClearCompleted = onClearCompleted
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(140.dp)) }
        }
        FadingScrollEdges(
            listState = listState,
            modifier = Modifier.matchParentSize(),
            enabled = dimScroll
        )
    }
}

@Composable
fun CalendarHeader(
    monthNames: List<String>,
    selectedMonth: Int,
    selectedYear: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPickYear: (Int) -> Unit
) {
    var yearExpanded by remember { mutableStateOf(false) }
    val years = remember(selectedYear) { (selectedYear - 5..selectedYear + 5).toList() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { yearExpanded = true }
            ) {
                Text(
                    text = selectedYear.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onPickYear(year)
                            yearExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = SimpleIcons.ArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(90f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = monthNames[(selectedMonth - 1).coerceIn(0, 11)],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = SimpleIcons.ArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(-90f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    dayLabels: List<String>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDate: LocalDate,
    today: LocalDate,
    hoursByDate: Map<LocalDate, Double>,
    hourLabel: String,
    onSelectDate: (LocalDate) -> Unit
) {
    val firstDay = LocalDate(selectedYear, selectedMonth, 1)
    val offset = firstDay.dayOfWeek.ordinal
    val startDate = firstDay.plus(DatePeriod(days = -offset))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            dayLabels.forEach { label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(6) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                ) {
                    repeat(7) { col ->
                        val date = startDate.plus(DatePeriod(days = row * 7 + col))
                        val inMonth = date.monthNumber == selectedMonth && date.year == selectedYear
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hours = hoursByDate[date] ?: 0.0
                        val hoursText = formatHours(hours, hourLabel)
                        val bg = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                        val cellHeight = if (hours > 0) 56.dp else 48.dp
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .alpha(if (inMonth) 1f else 0.35f)
                                .clickable { onSelectDate(date) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = contentColor
                                )

                                if (hours > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                                                } else {
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                                }
                                            )
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = hoursText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberMonthNames(): List<String> = listOf(
    stringResource(Res.string.month_jan),
    stringResource(Res.string.month_feb),
    stringResource(Res.string.month_mar),
    stringResource(Res.string.month_apr),
    stringResource(Res.string.month_may),
    stringResource(Res.string.month_jun),
    stringResource(Res.string.month_jul),
    stringResource(Res.string.month_aug),
    stringResource(Res.string.month_sep),
    stringResource(Res.string.month_oct),
    stringResource(Res.string.month_nov),
    stringResource(Res.string.month_dec)
)

@Composable
private fun rememberDayLabels(): List<String> = listOf(
    stringResource(Res.string.day_mon),
    stringResource(Res.string.day_tue),
    stringResource(Res.string.day_wed),
    stringResource(Res.string.day_thu),
    stringResource(Res.string.day_fri),
    stringResource(Res.string.day_sat),
    stringResource(Res.string.day_sun)
)

private fun daysInMonth(year: Int, month: Int): Int {
    val first = LocalDate(year, month, 1)
    val next = first.plus(DatePeriod(months = 1))
    val last = next.plus(DatePeriod(days = -1))
    return last.dayOfMonth
}

private fun relevantInstant(task: TodoTask, nowMs: Long): Instant? {
    val planned = task.plannedAt
    val deadline = task.deadline
    return when {
        planned == null && deadline != null -> deadline
        planned == null -> null
        planned.toEpochMilliseconds() < nowMs && deadline != null -> deadline
        else -> planned
    }
}

private fun formatLongDate(date: LocalDate, monthNames: List<String>): String {
    val name = monthNames[(date.monthNumber - 1).coerceIn(0, 11)]
    return "${date.dayOfMonth} $name ${date.year}"
}

private fun formatHours(hours: Double, suffix: String): String {
    val rounded = kotlin.math.round(hours * 10.0) / 10.0
    val text = when {
        rounded >= 10.0 -> rounded.toInt().toString()
        rounded % 1.0 == 0.0 -> rounded.toInt().toString()
        else -> rounded.toString()
    }
    return "$text$suffix"
}

@Preview
@Composable
private fun CalendarHeaderPreview() {
    CalendarHeader(
        monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ),
        selectedMonth = 2,
        selectedYear = 2026,
        onPrev = {},
        onNext = {},
        onPickYear = {}
    )
}

@Preview
@Composable
private fun CalendarGridPreview() {
    val today = LocalDate(2026, 2, 22)
    val hours = mapOf(
        today to 3.5,
        LocalDate(2026, 2, 25) to 1.0,
        LocalDate(2026, 2, 28) to 6.0
    )
    CalendarGrid(
        dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        selectedYear = 2026,
        selectedMonth = 2,
        selectedDate = today,
        today = today,
        hoursByDate = hours,
        hourLabel = "h",
        onSelectDate = {}
    )
}

@Preview
@Composable
private fun CalendarTabPreview() {
    val now = nowInstant()
    val tomorrow = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + 24L * 60 * 60 * 1000)
    val tasks = listOf(
        TodoTask(
            id = "t1",
            title = "Design review",
            plan = "",
            subtasks = emptyList(),
            createdAt = now,
            plannedAt = now,
            estimateHours = 2.5
        ),
        TodoTask(
            id = "t2",
            title = "Implement calendar",
            plan = "",
            subtasks = emptyList(),
            createdAt = now,
            plannedAt = tomorrow,
            estimateHours = 4.0
        )
    )

    CalendarTab(
        tasks = tasks,
        onToggleDone = {},
        onToggleSub = { _, _ -> },
        onOpenDetails = {},
        onEdit = {},
        onDelete = {},
        onClearCompleted = {},
        showCompleted = true,
        tagName = { null },
        noteCount = { 0 },
        onOpenNotes = {},
        dimScroll = false,
        backdrop = rememberLayerBackdrop { drawContent() }
    )
}
