package com.grigorevmp.simpletodo.platform

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun PlatformDateTimePicker(
    current: Instant?,
    onPicked: (Instant?) -> Unit
) {
    val tz = TimeZone.currentSystemDefault()

    val local = current?.toLocalDateTime(tz)
    val label = if (current == null) {
        "Not set"
    } else {
        val d = local ?: Clock.System.now().toLocalDateTime(tz)
        "${d.dayOfMonth.toString().padStart(2, '0')}." +
            "${d.monthNumber.toString().padStart(2, '0')}." +
            "${d.year} ${d.hour.toString().padStart(2, '0')}:" +
            d.minute.toString().padStart(2, '0')
    }

    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }
    val base = local ?: Clock.System.now().toLocalDateTime(tz)

    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                showDateDialog = true
            },
            modifier = Modifier.weight(1f)
        ) { Text("Date/time: $label") }

        TextButton(
            onClick = { onPicked(null) },
            enabled = current != null
        ) { Text("Clear") }
    }

    if (showDateDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = current?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            pendingDateMillis = selected
                            showDateDialog = false
                            showTimeDialog = true
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimeDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = base.hour,
            initialMinute = base.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = pendingDateMillis ?: return@TextButton
                        val date = Instant.fromEpochMilliseconds(selected)
                            .toLocalDateTime(tz)
                        val picked = LocalDateTime(
                            year = date.year,
                            monthNumber = date.monthNumber,
                            dayOfMonth = date.dayOfMonth,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                        showTimeDialog = false
                        onPicked(picked.toInstant(tz))
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("Cancel") }
            },
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
