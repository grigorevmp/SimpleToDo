package com.grigorevmp.simpletodo.platform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
actual fun PlatformDateTimePicker(
    current: Instant?,
    onPicked: (Instant?) -> Unit
) {
    val tz = TimeZone.currentSystemDefault()
    var text by remember {
        mutableStateOf(
            current?.toLocalDateTime(tz)?.let { l ->
                "${l.year}-${l.monthNumber.toString().padStart(2, '0')}-${l.dayOfMonth.toString().padStart(2, '0')} " +
                    "${l.hour.toString().padStart(2, '0')}:${l.minute.toString().padStart(2, '0')}"
            } ?: ""
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("YYYY-MM-DD HH:MM") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedButton(onClick = {
                val parsed = parse(text)
                onPicked(parsed?.toInstant(tz))
            }) { Text("Apply") }

            TextButton(
                onClick = { text = ""; onPicked(null) },
                enabled = current != null
            ) { Text("Clear") }
        }
    }
}

private fun parse(s: String): LocalDateTime? {
    val t = s.trim()
    if (t.isEmpty()) return null
    val parts = t.split(" ")
    if (parts.size != 2) return null
    val date = parts[0].split("-")
    val time = parts[1].split(":")
    if (date.size != 3 || time.size != 2) return null
    val y = date[0].toIntOrNull() ?: return null
    val m = date[1].toIntOrNull() ?: return null
    val d = date[2].toIntOrNull() ?: return null
    val hh = time[0].toIntOrNull() ?: return null
    val mm = time[1].toIntOrNull() ?: return null
    return runCatching { LocalDateTime(y, m, d, hh, mm) }.getOrNull()
}
