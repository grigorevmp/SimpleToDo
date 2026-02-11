package com.grigorevmp.simpletodo.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.Tag
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.platform.NotificationPermissionGate
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.theme.authorAccentColors
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState

@Composable
fun SettingsScreen(repo: TodoRepository) {
    val prefs by repo.prefs.collectAsState()
    val scope = rememberCoroutineScope()

    var newTag by remember { mutableStateOf("") }
    var showTagsDialog by remember { mutableStateOf(false) }
    var leadMinutesText by remember { mutableStateOf(prefs.reminderLeadMinutes.toString()) }

    LaunchedEffect(prefs.reminderLeadMinutes) {
        leadMinutesText = prefs.reminderLeadMinutes.toString()
    }

    val listState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Settings", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Tags, notifications, and behavior",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Notifications", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = prefs.remindersEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch { repo.setReminders(enabled) }
                            }
                        )
                    }

                    NotificationPermissionGate(remindersEnabled = prefs.remindersEnabled)

                    Text(
                        "Reminders are sent for planned time and deadline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = leadMinutesText,
                        onValueChange = { text ->
                            val cleaned = text.filter { it.isDigit() }
                            leadMinutesText = cleaned
                            val value = cleaned.toIntOrNull() ?: return@OutlinedTextField
                            scope.launch { repo.setLeadMinutes(value) }
                        },
                        label = { Text("Lead time (minutes)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        item {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ThemeCard(
                                title = "System",
                                subtitle = "Auto",
                                selected = prefs.themeMode == ThemeMode.SYSTEM,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.SYSTEM) } }
                            )
                            ThemeCard(
                                title = "Dynamic",
                                subtitle = "Material You",
                                selected = prefs.themeMode == ThemeMode.DYNAMIC,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.DYNAMIC) } }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ThemeCard(
                                title = "Dim",
                                subtitle = "Dark gray",
                                selected = prefs.themeMode == ThemeMode.DIM,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.DIM) } }
                            )
                            ThemeCard(
                                title = "Author",
                                subtitle = "Custom",
                                selected = prefs.themeMode == ThemeMode.AUTHOR,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.AUTHOR) } }
                            )
                        }
                    }
                    if (prefs.themeMode == ThemeMode.AUTHOR) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            authorAccentColors().forEachIndexed { idx, color ->
                                AccentSwatch(
                                    color = color,
                                    selected = prefs.authorAccentIndex == idx,
                                    onClick = { scope.launch { repo.setAuthorAccent(idx) } }
                                )
                            }
                        }
                    }
                    SettingToggle(
                        title = "Dim on scroll",
                        subtitle = "Fade edges while scrolling",
                        checked = prefs.dimScroll,
                        onToggle = { enabled -> scope.launch { repo.setDimScroll(enabled) } }
                    )
                    SettingToggle(
                        title = "Liquid glass",
                        subtitle = "Use blur glass surfaces",
                        checked = prefs.liquidGlass,
                        onToggle = { enabled -> scope.launch { repo.setLiquidGlass(enabled) } }
                    )
                }
            }
        }

        item {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tags", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showTagsDialog = true }) { Text("Manage") }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prefs.tags.take(8).forEach { tag ->
                            TagChip(tag = tag)
                        }
                        if (prefs.tags.size > 8) {
                            Text(
                                "+${prefs.tags.size - 8}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Text(
                        "Add, remove, and organize tags in a dedicated dialog.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
        }

        FadingScrollEdges(
            listState = listState,
            modifier = Modifier.matchParentSize(),
            enabled = prefs.dimScroll
        )
    }

    if (showTagsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Manage tags") },
            text = {
                androidx.compose.foundation.layout.Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("New tag") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                repo.addTag(newTag)
                                newTag = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Add tag") }

                    val tagsListState = rememberLazyListState()
                    Box(Modifier.fillMaxWidth()) {
                        LazyColumn(
                            state = tagsListState,
                            contentPadding = PaddingValues(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(prefs.tags, key = { it.id }) { t ->
                                TagRow(
                                    tag = t,
                                    onDelete = { scope.launch { repo.deleteTag(t.id) } }
                                )
                            }
                        }
                        FadingScrollEdges(
                            listState = tagsListState,
                            modifier = Modifier.matchParentSize(),
                            color = MaterialTheme.colorScheme.surface,
                            enabled = prefs.dimScroll
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagsDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = color,
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
        modifier = Modifier.size(32.dp)
    ) {}
}

@Composable
private fun RowScope.ThemeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(240),
        label = "theme-bg"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (selected) 0.7f else 0.15f,
        animationSpec = tween(240),
        label = "theme-border"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 1.dp,
        animationSpec = tween(240),
        label = "theme-elev"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(240),
        label = "theme-scale"
    )
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        color = bg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha)),
        modifier = modifier
            .weight(1f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Box(Modifier.padding(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
            ) {
                val checkBg by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(200),
                    label = "theme-check-bg"
                )
                val checkScale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.9f,
                    animationSpec = tween(200),
                    label = "theme-check-scale"
                )
                Surface(
                    shape = CircleShape,
                    color = checkBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = checkScale, scaleY = checkScale)
                ) {
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val accent by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "toggle-accent"
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = accent)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun TagChip(tag: Tag) {
    val colors = authorAccentColors()
    val base = MaterialTheme.colorScheme.primary
    val accent = colors[tag.colorIndex % colors.size]
    val border by animateColorAsState(
        targetValue = lerp(base.copy(alpha = 0.5f), accent.copy(alpha = 0.5f), 0.55f),
        animationSpec = tween(200),
        label = "tag-border"
    )
    val fill = lerp(base.copy(alpha = 0.12f), accent.copy(alpha = 0.22f), 0.6f)
    Surface(
        shape = MaterialTheme.shapes.large,
        color = fill,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(accent, shape = MaterialTheme.shapes.small)
            )
            Text(tag.name, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TagRow(tag: Tag, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TagChip(tag = tag)
        TextButton(onClick = onDelete) { Text("Delete") }
    }
}
