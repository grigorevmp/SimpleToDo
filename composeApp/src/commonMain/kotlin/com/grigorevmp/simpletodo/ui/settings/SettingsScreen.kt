package com.grigorevmp.simpletodo.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.model.AppLanguage
import com.grigorevmp.simpletodo.model.Tag
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.platform.AppInfo
import com.grigorevmp.simpletodo.platform.NotificationPermissionGate
import com.grigorevmp.simpletodo.platform.isIos
import com.grigorevmp.simpletodo.platform.rememberFileExportLauncher
import com.grigorevmp.simpletodo.platform.rememberFileImportLauncher
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import com.grigorevmp.simpletodo.ui.components.SimpleIcons
import com.grigorevmp.simpletodo.ui.theme.authorAccentColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.settings_about_title
import simpletodo.composeapp.generated.resources.settings_account_devices
import simpletodo.composeapp.generated.resources.settings_account_profile
import simpletodo.composeapp.generated.resources.settings_account_soon
import simpletodo.composeapp.generated.resources.settings_account_subscription
import simpletodo.composeapp.generated.resources.settings_account_subtitle
import simpletodo.composeapp.generated.resources.settings_account_title
import simpletodo.composeapp.generated.resources.settings_add_tag
import simpletodo.composeapp.generated.resources.settings_changelog
import simpletodo.composeapp.generated.resources.settings_changelog_title
import simpletodo.composeapp.generated.resources.settings_close
import simpletodo.composeapp.generated.resources.settings_copy
import simpletodo.composeapp.generated.resources.settings_data_delete
import simpletodo.composeapp.generated.resources.settings_data_export
import simpletodo.composeapp.generated.resources.settings_data_import
import simpletodo.composeapp.generated.resources.settings_data_title
import simpletodo.composeapp.generated.resources.settings_delete
import simpletodo.composeapp.generated.resources.settings_delete_desc
import simpletodo.composeapp.generated.resources.settings_delete_title
import simpletodo.composeapp.generated.resources.settings_dim_scroll_desc
import simpletodo.composeapp.generated.resources.settings_dim_scroll_title
import simpletodo.composeapp.generated.resources.settings_disable_dark_desc
import simpletodo.composeapp.generated.resources.settings_disable_dark_title
import simpletodo.composeapp.generated.resources.settings_done
import simpletodo.composeapp.generated.resources.settings_export_title
import simpletodo.composeapp.generated.resources.settings_export_error
import simpletodo.composeapp.generated.resources.settings_import_action
import simpletodo.composeapp.generated.resources.settings_import_error
import simpletodo.composeapp.generated.resources.settings_import_hint
import simpletodo.composeapp.generated.resources.settings_import_title
import simpletodo.composeapp.generated.resources.settings_language_en
import simpletodo.composeapp.generated.resources.settings_language_ru
import simpletodo.composeapp.generated.resources.settings_language_system
import simpletodo.composeapp.generated.resources.settings_language_title
import simpletodo.composeapp.generated.resources.settings_lead_time_label
import simpletodo.composeapp.generated.resources.settings_link_contact
import simpletodo.composeapp.generated.resources.settings_link_github
import simpletodo.composeapp.generated.resources.settings_liquid_glass_desc
import simpletodo.composeapp.generated.resources.settings_liquid_glass_title
import simpletodo.composeapp.generated.resources.settings_manage_tags_title
import simpletodo.composeapp.generated.resources.settings_new_tag_label
import simpletodo.composeapp.generated.resources.settings_notifications_desc
import simpletodo.composeapp.generated.resources.settings_notifications_title
import simpletodo.composeapp.generated.resources.settings_open
import simpletodo.composeapp.generated.resources.settings_selected
import simpletodo.composeapp.generated.resources.settings_status_badge_beta
import simpletodo.composeapp.generated.resources.settings_status_badge_early
import simpletodo.composeapp.generated.resources.settings_status_badge_local
import simpletodo.composeapp.generated.resources.settings_status_desc
import simpletodo.composeapp.generated.resources.settings_status_title
import simpletodo.composeapp.generated.resources.settings_subtitle
import simpletodo.composeapp.generated.resources.settings_tags_desc
import simpletodo.composeapp.generated.resources.settings_tags_manage
import simpletodo.composeapp.generated.resources.settings_tags_more
import simpletodo.composeapp.generated.resources.settings_tags_title
import simpletodo.composeapp.generated.resources.settings_theme_author
import simpletodo.composeapp.generated.resources.settings_theme_auto
import simpletodo.composeapp.generated.resources.settings_theme_custom
import simpletodo.composeapp.generated.resources.settings_theme_dark_gray
import simpletodo.composeapp.generated.resources.settings_theme_dim
import simpletodo.composeapp.generated.resources.settings_theme_dynamic
import simpletodo.composeapp.generated.resources.settings_theme_material_you
import simpletodo.composeapp.generated.resources.settings_theme_system
import simpletodo.composeapp.generated.resources.settings_theme_title
import simpletodo.composeapp.generated.resources.settings_title
import simpletodo.composeapp.generated.resources.settings_version_title

@Composable
fun SettingsScreen(
    repo: TodoRepository
) {
    val prefs by repo.prefs.collectAsState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current

    var newTag by remember { mutableStateOf("") }
    var showTagsDialog by remember { mutableStateOf(false) }
    var leadMinutesText by remember { mutableStateOf(prefs.reminderLeadMinutes.toString()) }
    var showChangelog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var exportText by remember { mutableStateOf("") }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf(false) }
    var exportFailed by remember { mutableStateOf(false) }
    val useFileImportExport = !isIos
    val exportLauncher = if (useFileImportExport) {
        rememberFileExportLauncher(defaultName = "simpletodo-backup.json") { ok ->
            if (!ok) exportFailed = true
        }
    } else null
    val importLauncher = if (useFileImportExport) {
        rememberFileImportLauncher { content ->
            if (content.isNullOrBlank()) {
                importError = true
            } else {
                scope.launch {
                    val result = repo.importData(content)
                    importError = result.isFailure
                }
            }
        }
    } else null

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
                    Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(Res.string.settings_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                AccountComingSoonCard()
            }

            item {
                StatusCard()
            }

            item {
                VersionAndResourcesCard(
                    onShowChangelog = { showChangelog = true },
                    onOpenUrl = { url -> uriHandler.openUri(url) }
                )
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Text(stringResource(Res.string.settings_notifications_title), style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = prefs.remindersEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch { repo.setReminders(enabled) }
                                }
                            )
                        }

                        NotificationPermissionGate(remindersEnabled = prefs.remindersEnabled)

                        Text(
                        stringResource(Res.string.settings_notifications_desc),
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
                        label = { Text(stringResource(Res.string.settings_lead_time_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
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
                    Text(stringResource(Res.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ThemeCard(
                                title = stringResource(Res.string.settings_theme_system),
                                subtitle = stringResource(Res.string.settings_theme_auto),
                                selected = prefs.themeMode == ThemeMode.SYSTEM,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.SYSTEM) } }
                            )
                            ThemeCard(
                                title = stringResource(Res.string.settings_theme_dynamic),
                                subtitle = stringResource(Res.string.settings_theme_material_you),
                                selected = prefs.themeMode == ThemeMode.DYNAMIC,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.DYNAMIC) } }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val dimEnabled = !prefs.disableDarkTheme
                            val dimModifier = Modifier
                                .alpha(if (dimEnabled) 1f else 0.5f)
                                .padding(bottom = if (dimEnabled) 0.dp else 1.dp)
                            ThemeCard(
                                title = stringResource(Res.string.settings_theme_dim),
                                subtitle = stringResource(Res.string.settings_theme_dark_gray),
                                selected = prefs.themeMode == ThemeMode.DIM,
                                onClick = {
                                    if (dimEnabled) {
                                        scope.launch { repo.setTheme(ThemeMode.DIM) }
                                    }
                                },
                                modifier = dimModifier
                            )
                            ThemeCard(
                                title = stringResource(Res.string.settings_theme_author),
                                subtitle = stringResource(Res.string.settings_theme_custom),
                                selected = prefs.themeMode == ThemeMode.AUTHOR,
                                onClick = { scope.launch { repo.setTheme(ThemeMode.AUTHOR) } }
                            )
                        }
                    }
                    SettingToggle(
                        title = stringResource(Res.string.settings_disable_dark_title),
                        subtitle = stringResource(Res.string.settings_disable_dark_desc),
                        checked = prefs.disableDarkTheme,
                        onToggle = { enabled -> scope.launch { repo.setDisableDarkTheme(enabled) } }
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
                    SettingToggle(
                        title = stringResource(Res.string.settings_dim_scroll_title),
                        subtitle = stringResource(Res.string.settings_dim_scroll_desc),
                        checked = prefs.dimScroll,
                        onToggle = { enabled -> scope.launch { repo.setDimScroll(enabled) } }
                    )
                    SettingToggle(
                        title = stringResource(Res.string.settings_liquid_glass_title),
                        subtitle = stringResource(Res.string.settings_liquid_glass_desc),
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
                    Text(stringResource(Res.string.settings_language_title), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = prefs.language == AppLanguage.SYSTEM,
                            onClick = { scope.launch { repo.setLanguage(AppLanguage.SYSTEM) } },
                            label = { Text(stringResource(Res.string.settings_language_system)) }
                        )
                        FilterChip(
                            selected = prefs.language == AppLanguage.EN,
                            onClick = { scope.launch { repo.setLanguage(AppLanguage.EN) } },
                            label = { Text(stringResource(Res.string.settings_language_en)) }
                        )
                        FilterChip(
                            selected = prefs.language == AppLanguage.RU,
                            onClick = { scope.launch { repo.setLanguage(AppLanguage.RU) } },
                            label = { Text(stringResource(Res.string.settings_language_ru)) }
                        )
                    }
                }
            }
        }

        item {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.settings_tags_title), style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showTagsDialog = true }) { Text(stringResource(Res.string.settings_tags_manage)) }
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
                                stringResource(Res.string.settings_tags_more, (prefs.tags.size - 8).toString()),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Text(
                        stringResource(Res.string.settings_tags_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text(stringResource(Res.string.settings_data_title), style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DataActionButton(
                            text = stringResource(Res.string.settings_data_export),
                            icon = SimpleIcons.ArrowUp,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                exportFailed = false
                                if (useFileImportExport) {
                                    scope.launch {
                                        exportText = repo.exportData()
                                        exportLauncher?.launch(exportText)
                                    }
                                } else {
                                    scope.launch {
                                        exportText = repo.exportData()
                                        showExportDialog = true
                                    }
                                }
                            }
                        )
                        DataActionButton(
                            text = stringResource(Res.string.settings_data_import),
                            icon = SimpleIcons.ArrowDown,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                importError = false
                                if (useFileImportExport) {
                                    importLauncher?.launch()
                                } else {
                                    importText = ""
                                    showImportDialog = true
                                }
                            }
                        )
                    }
                    if (useFileImportExport && exportFailed) {
                        Text(
                            stringResource(Res.string.settings_export_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (useFileImportExport && importError) {
                        Text(
                            stringResource(Res.string.settings_import_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = { showDeleteDataDialog = true }) {
                        Text(stringResource(Res.string.settings_data_delete), color = MaterialTheme.colorScheme.error)
                    }
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

    if (showChangelog) {
        AlertDialog(
            onDismissRequest = { showChangelog = false },
            title = { Text(stringResource(Res.string.settings_changelog_title)) },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(CHANGELOG, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangelog = false }) { Text(stringResource(Res.string.settings_close)) }
            }
        )
    }

    if (!useFileImportExport && showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(Res.string.settings_export_title)) },
            text = {
                OutlinedTextField(
                    value = exportText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(exportText))
                }) { Text(stringResource(Res.string.settings_copy)) }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text(stringResource(Res.string.settings_close)) }
            }
        )
    }

    if (!useFileImportExport && showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(Res.string.settings_import_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = importText,
                        onValueChange = {
                            importText = it
                            importError = false
                        },
                        placeholder = { Text(stringResource(Res.string.settings_import_hint)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
                    )
                    if (importError) {
                        Text(
                            stringResource(Res.string.settings_import_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val result = repo.importData(importText)
                        if (result.isSuccess) {
                            showImportDialog = false
                        } else {
                            importError = true
                        }
                    }
                }) { Text(stringResource(Res.string.settings_import_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text(stringResource(Res.string.settings_close)) }
            }
        )
    }

    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text(stringResource(Res.string.settings_delete_title)) },
            text = { Text(stringResource(Res.string.settings_delete_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repo.clearAllData()
                        showDeleteDataDialog = false
                    }
                }) { Text(stringResource(Res.string.settings_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) { Text(stringResource(Res.string.settings_close)) }
            }
        )
    }

    if (showTagsDialog) {
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text(stringResource(Res.string.settings_manage_tags_title)) },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text(stringResource(Res.string.settings_new_tag_label)) },
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
                    ) { Text(stringResource(Res.string.settings_add_tag)) }

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
                TextButton(onClick = { showTagsDialog = false }) { Text(stringResource(Res.string.settings_done)) }
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
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
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
            MaterialTheme.colorScheme.primaryContainer
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
                            SimpleIcons.Check,
                            contentDescription = stringResource(Res.string.settings_selected),
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
        TextButton(onClick = onDelete) { Text(stringResource(Res.string.settings_delete)) }
    }
}

@Composable
private fun AccountComingSoonCard() {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {}
                    Column {
                        Text(stringResource(Res.string.settings_account_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(Res.string.settings_account_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountPill(text = stringResource(Res.string.settings_account_profile))
                    AccountPill(text = stringResource(Res.string.settings_account_devices))
                    AccountPill(text = stringResource(Res.string.settings_account_subscription))
                }
            }

            RepairOverlay(
                text = stringResource(Res.string.settings_account_soon),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun AccountPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DataActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        shape = shape,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun RepairOverlay(
    text: String,
    modifier: Modifier = Modifier
) {
    val stripeColor = Color(0xFF111111)
    val baseColor = Color(0xFF7F6300)

    Box(
        modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.large)
    ) {
        Box(
            Modifier
                .padding(vertical = 12.dp)
                .align(Alignment.Center)
                .graphicsLayer(rotationZ = -6f)
                .clip(MaterialTheme.shapes.medium)
                .background(baseColor)
                .drawBehind {
                    val stripeWidth = 14.dp.toPx()
                    val gap = 10.dp.toPx()
                    var x = -size.width
                    while (x < size.width * 2) {
                        drawLine(
                            color = stripeColor,
                            start = androidx.compose.ui.geometry.Offset(x, -40f),
                            end = androidx.compose.ui.geometry.Offset(x + size.height, size.height + 40),
                            strokeWidth = stripeWidth
                        )
                        x += stripeWidth + gap
                    }
                }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

@Composable
private fun StatusCard() {
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.42f)
        )
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(Res.string.settings_status_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(Res.string.settings_status_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountPill(text = stringResource(Res.string.settings_status_badge_beta))
                AccountPill(text = stringResource(Res.string.settings_status_badge_early))
                AccountPill(text = stringResource(Res.string.settings_status_badge_local))
            }
        }
    }
}

@Composable
private fun VersionAndResourcesCard(
    onShowChangelog: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val versionName = remember { AppInfo.versionName }
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(Res.string.settings_about_title), style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onShowChangelog() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.settings_version_title), style = MaterialTheme.typography.labelMedium)
                    Text(versionName, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    stringResource(Res.string.settings_changelog),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ResourceLink(stringResource(Res.string.settings_link_github), "https://github.com/grigorevmp/SimpleToDo", onOpenUrl)
                ResourceLink(stringResource(Res.string.settings_link_contact), "https://t.me/grigorevmp", onOpenUrl)
            }
        }
    }
}

@Composable
private fun ResourceLink(
    title: String,
    url: String,
    onOpenUrl: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable { onOpenUrl(url) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(
            stringResource(Res.string.settings_open),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@Preview
@Composable
fun RepairOverlayPreview() {
    RepairOverlay(
        text = "TEST"
    )
}


@Preview
@Composable
fun StatusCardPreview() {
    StatusCard()
}
