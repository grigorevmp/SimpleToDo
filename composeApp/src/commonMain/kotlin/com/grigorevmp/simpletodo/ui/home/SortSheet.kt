package com.grigorevmp.simpletodo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.model.SortConfig
import com.grigorevmp.simpletodo.model.SortDir
import com.grigorevmp.simpletodo.model.SortField
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.sort_title
import simpletodo.composeapp.generated.resources.sort_subtitle
import simpletodo.composeapp.generated.resources.sort_primary_title
import simpletodo.composeapp.generated.resources.sort_primary_subtitle
import simpletodo.composeapp.generated.resources.sort_secondary_title
import simpletodo.composeapp.generated.resources.sort_secondary_subtitle
import simpletodo.composeapp.generated.resources.sort_show_completed_title
import simpletodo.composeapp.generated.resources.sort_show_completed_desc
import simpletodo.composeapp.generated.resources.sort_reset_default
import simpletodo.composeapp.generated.resources.sort_apply
import simpletodo.composeapp.generated.resources.sort_field_label
import simpletodo.composeapp.generated.resources.sort_direction_label
import simpletodo.composeapp.generated.resources.sort_ascending
import simpletodo.composeapp.generated.resources.sort_descending
import simpletodo.composeapp.generated.resources.sort_field_planned
import simpletodo.composeapp.generated.resources.sort_field_deadline
import simpletodo.composeapp.generated.resources.sort_field_priority
import simpletodo.composeapp.generated.resources.sort_field_created
import simpletodo.composeapp.generated.resources.sort_field_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    current: SortConfig,
    showCompleted: Boolean,
    onShowCompleted: (Boolean) -> Unit,
    onApply: (SortConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var primary by remember { mutableStateOf(current.primary) }
    var primaryDir by remember { mutableStateOf(current.primaryDir) }
    var secondary by remember { mutableStateOf(current.secondary) }
    var secondaryDir by remember { mutableStateOf(current.secondaryDir) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.sort_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(Res.string.sort_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SortRow(
                title = stringResource(Res.string.sort_primary_title),
                subtitle = stringResource(Res.string.sort_primary_subtitle),
                field = primary,
                dir = primaryDir,
                onField = { primary = it },
                onDir = { primaryDir = it }
            )

            SortRow(
                title = stringResource(Res.string.sort_secondary_title),
                subtitle = stringResource(Res.string.sort_secondary_subtitle),
                field = secondary,
                dir = secondaryDir,
                onField = { secondary = it },
                onDir = { secondaryDir = it }
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(Res.string.sort_show_completed_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(Res.string.sort_show_completed_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showCompleted,
                        onCheckedChange = onShowCompleted
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val def = SortConfig()
                        primary = def.primary
                        primaryDir = def.primaryDir
                        secondary = def.secondary
                        secondaryDir = def.secondaryDir
                        onApply(def)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(Res.string.sort_reset_default)) }
                Button(
                    onClick = {
                        onApply(SortConfig(primary, primaryDir, secondary, secondaryDir))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(Res.string.sort_apply)) }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SortRow(
    title: String,
    subtitle: String,
    field: SortField,
    dir: SortDir,
    onField: (SortField) -> Unit,
    onDir: (SortDir) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FieldMenu(field, onField, modifier = Modifier.weight(1f))
                DirMenu(dir, onDir, modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldMenu(
    value: SortField,
    onValue: (SortField) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(Res.string.sort_field_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = fieldLabel(value),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SortField.entries.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(fieldLabel(f)) },
                        onClick = { onValue(f); expanded = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirMenu(
    value: SortDir,
    onValue: (SortDir) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(Res.string.sort_direction_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = if (value == SortDir.ASC) stringResource(Res.string.sort_ascending) else stringResource(Res.string.sort_descending),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(Res.string.sort_ascending)) }, onClick = { onValue(SortDir.ASC); expanded = false })
                DropdownMenuItem(text = { Text(stringResource(Res.string.sort_descending)) }, onClick = { onValue(SortDir.DESC); expanded = false })
            }
        }
    }
}

@Composable
private fun fieldLabel(f: SortField): String = when (f) {
    SortField.PLANNED_AT -> stringResource(Res.string.sort_field_planned)
    SortField.DEADLINE -> stringResource(Res.string.sort_field_deadline)
    SortField.IMPORTANCE -> stringResource(Res.string.sort_field_priority)
    SortField.CREATED_AT -> stringResource(Res.string.sort_field_created)
    SortField.TITLE -> stringResource(Res.string.sort_field_title)
}
