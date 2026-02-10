package com.grigorevmp.simpletodo.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.platform.NotificationPermissionGate
import com.grigorevmp.simpletodo.ui.theme.authorAccentColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(repo: TodoRepository) {
    val prefs by repo.prefs.collectAsState()
    val scope = rememberCoroutineScope()

    var newTag by remember { mutableStateOf("") }
    var showTagsDialog by remember { mutableStateOf(false) }

    LazyColumn(
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
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                androidx.compose.foundation.layout.Column(
                    Modifier.fillMaxWidth().padding(14.dp),
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lead time before deadline", style = MaterialTheme.typography.bodyMedium)
                        Text("${prefs.reminderLeadMinutes} min", style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { scope.launch { repo.setLeadMinutes(prefs.reminderLeadMinutes - 5) } },
                            enabled = prefs.reminderLeadMinutes >= 5
                        ) { Text("-5") }

                        androidx.compose.material3.OutlinedButton(
                            onClick = { scope.launch { repo.setLeadMinutes(prefs.reminderLeadMinutes + 5) } }
                        ) { Text("+5") }
                    }
                }
            }
        }

        item {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    ThemeOption(
                        label = "System (dynamic if supported)",
                        selected = prefs.themeMode == ThemeMode.SYSTEM,
                        onSelect = { scope.launch { repo.setTheme(ThemeMode.SYSTEM) } }
                    )
                    ThemeOption(
                        label = "Dynamic colors",
                        selected = prefs.themeMode == ThemeMode.DYNAMIC,
                        onSelect = { scope.launch { repo.setTheme(ThemeMode.DYNAMIC) } }
                    )
                    ThemeOption(
                        label = "Dim (dark gray)",
                        selected = prefs.themeMode == ThemeMode.DIM,
                        onSelect = { scope.launch { repo.setTheme(ThemeMode.DIM) } }
                    )
                    ThemeOption(
                        label = "Author palette",
                        selected = prefs.themeMode == ThemeMode.AUTHOR,
                        onSelect = { scope.launch { repo.setTheme(ThemeMode.AUTHOR) } }
                    )
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
                }
            }
        }

        item {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
                androidx.compose.foundation.layout.Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tags", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showTagsDialog = true }) { Text("Manage") }
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

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(prefs.tags, key = { it.id }) { t ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(t.name, style = MaterialTheme.typography.bodyLarge)
                                IconButton(
                                    onClick = { scope.launch { repo.deleteTag(t.id) } }
                                ) { Text("Delete", style = MaterialTheme.typography.labelLarge) }
                            }
                        }
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
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onSelect)
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
