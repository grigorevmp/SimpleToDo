package com.grigorevmp.simpletodo.ui.notes.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.grigorevmp.simpletodo.model.TodoTask
import com.grigorevmp.simpletodo.ui.components.FadingScrollEdges
import org.jetbrains.compose.resources.stringResource
import simpletodo.composeapp.generated.resources.Res
import simpletodo.composeapp.generated.resources.task_link_label
import simpletodo.composeapp.generated.resources.task_select_task
import simpletodo.composeapp.generated.resources.search_placeholder
import simpletodo.composeapp.generated.resources.task_no_task
import simpletodo.composeapp.generated.resources.task_close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskLinkPicker(
    tasks: List<TodoTask>, currentId: String?, dimScroll: Boolean, onPick: (String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val current = tasks.firstOrNull { it.id == currentId }?.title ?: stringResource(Res.string.task_no_task)
    val filtered = remember(tasks, query) {
        if (query.isBlank()) tasks else tasks.filter { it.title.contains(query, ignoreCase = true) }
    }
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(stringResource(Res.string.task_link_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDialog) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(Res.string.task_select_task)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                    Box(Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                        LazyColumn(
                            state = listState, modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                DropdownMenuItem(text = { Text(stringResource(Res.string.task_no_task)) }, onClick = {
                                    onPick(null)
                                    showDialog = false
                                })
                            }
                            items(filtered.size) { idx ->
                                val t = filtered[idx]
                                DropdownMenuItem(text = { Text(t.title) }, onClick = {
                                    onPick(t.id)
                                    showDialog = false
                                })
                            }
                        }
                        FadingScrollEdges(
                            listState = listState,
                            modifier = Modifier.matchParentSize(),
                            color = AlertDialogDefaults.containerColor,
                            enabled = dimScroll
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(Res.string.task_close)) }
            })
        LaunchedEffect(showDialog) {
            if (showDialog) focusRequester.requestFocus()
        }
    }
}
