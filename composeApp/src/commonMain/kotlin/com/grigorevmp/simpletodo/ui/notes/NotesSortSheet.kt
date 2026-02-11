package com.grigorevmp.simpletodo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.model.NoteSortConfig
import com.grigorevmp.simpletodo.model.NoteSortField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesSortSheet(
    current: NoteSortConfig,
    onApply: (NoteSortConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var field by remember { mutableStateOf(current.field) }
    var foldersOnTop by remember { mutableStateOf(current.foldersOnTop) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Фильтры", style = MaterialTheme.typography.titleLarge)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Сортировка", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = field == NoteSortField.DATE,
                        onClick = { field = NoteSortField.DATE },
                        label = { Text("По дате") }
                    )
                    FilterChip(
                        selected = field == NoteSortField.NAME,
                        onClick = { field = NoteSortField.NAME },
                        label = { Text("По имени") }
                    )
                }
            }

            FilterChip(
                selected = foldersOnTop,
                onClick = { foldersOnTop = !foldersOnTop },
                label = { Text("Папки сверху") }
            )

            Button(
                onClick = {
                    onApply(NoteSortConfig(field, foldersOnTop))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Применить") }

            Spacer(Modifier.height(18.dp))
        }
    }
}
