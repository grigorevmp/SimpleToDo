package com.grigorevmp.simpletodo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.notes_sort_title
import simpletodo.composeapp.generated.resources.notes_sort_subtitle
import simpletodo.composeapp.generated.resources.notes_sort_sorting_title
import simpletodo.composeapp.generated.resources.notes_sort_sorting_desc
import simpletodo.composeapp.generated.resources.notes_sort_by_date
import simpletodo.composeapp.generated.resources.notes_sort_by_name
import simpletodo.composeapp.generated.resources.notes_sort_folders_title
import simpletodo.composeapp.generated.resources.notes_sort_folders_desc
import simpletodo.composeapp.generated.resources.notes_sort_folders_on_top
import simpletodo.composeapp.generated.resources.notes_sort_apply

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
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.notes_sort_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(Res.string.notes_sort_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(Res.string.notes_sort_sorting_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(Res.string.notes_sort_sorting_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = field == NoteSortField.DATE,
                            onClick = { field = NoteSortField.DATE },
                            label = { Text(stringResource(Res.string.notes_sort_by_date)) }
                        )
                        FilterChip(
                            selected = field == NoteSortField.NAME,
                            onClick = { field = NoteSortField.NAME },
                            label = { Text(stringResource(Res.string.notes_sort_by_name)) }
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(Res.string.notes_sort_folders_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(Res.string.notes_sort_folders_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilterChip(
                        selected = foldersOnTop,
                        onClick = { foldersOnTop = !foldersOnTop },
                        label = { Text(stringResource(Res.string.notes_sort_folders_on_top)) }
                    )
                }
            }

            Button(
                onClick = {
                    onApply(NoteSortConfig(field, foldersOnTop))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(Res.string.notes_sort_apply)) }

            Spacer(Modifier.height(18.dp))
        }
    }
}
