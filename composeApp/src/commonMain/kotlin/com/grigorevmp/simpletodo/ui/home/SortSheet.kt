package com.grigorevmp.simpletodo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    current: SortConfig,
    onApply: (SortConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var primary by remember { mutableStateOf(current.primary) }
    var primaryDir by remember { mutableStateOf(current.primaryDir) }
    var secondary by remember { mutableStateOf(current.secondary) }
    var secondaryDir by remember { mutableStateOf(current.secondaryDir) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Sorting", style = MaterialTheme.typography.titleLarge)

            SortRow(
                title = "Primary field",
                field = primary,
                dir = primaryDir,
                onField = { primary = it },
                onDir = { primaryDir = it }
            )

            SortRow(
                title = "Secondary field",
                field = secondary,
                dir = secondaryDir,
                onField = { secondary = it },
                onDir = { secondaryDir = it }
            )

            Button(
                onClick = {
                    onApply(SortConfig(primary, primaryDir, secondary, secondaryDir))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Apply") }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SortRow(
    title: String,
    field: SortField,
    dir: SortDir,
    onField: (SortField) -> Unit,
    onDir: (SortDir) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldMenu(field, onField)
            DirMenu(dir, onDir)
        }
    }
}

@Composable
private fun FieldMenu(value: SortField, onValue: (SortField) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) {
        Text(fieldLabel(value))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        SortField.entries.forEach { f ->
            DropdownMenuItem(
                text = { Text(fieldLabel(f)) },
                onClick = { onValue(f); expanded = false }
            )
        }
    }
}

@Composable
private fun DirMenu(value: SortDir, onValue: (SortDir) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) {
        Text(if (value == SortDir.ASC) "ascending" else "descending")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("ascending") }, onClick = { onValue(SortDir.ASC); expanded = false })
        DropdownMenuItem(text = { Text("descending") }, onClick = { onValue(SortDir.DESC); expanded = false })
    }
}

private fun fieldLabel(f: SortField): String = when (f) {
    SortField.PLANNED_AT -> "planned time"
    SortField.DEADLINE -> "deadline"
    SortField.IMPORTANCE -> "priority"
    SortField.CREATED_AT -> "created"
    SortField.TITLE -> "title"
}
