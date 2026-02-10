package com.grigorevmp.simpletodo.platform

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.Clock
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
    val ctx = LocalContext.current
    val activity = remember(ctx) { ctx.findActivity() }
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

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                val act = activity ?: return@OutlinedButton
                if (act.isFinishing || act.isDestroyed) return@OutlinedButton
                val base = local ?: Clock.System.now().toLocalDateTime(tz)
                DatePickerDialog(
                    act,
                    { _, y, m0, d ->
                        val m = m0 + 1
                        TimePickerDialog(
                            act,
                            { _, hh, mm ->
                                val picked = LocalDateTime(y, m, d, hh, mm)
                                onPicked(picked.toInstant(tz))
                            },
                            base.hour,
                            base.minute,
                            true
                        ).show()
                    },
                    base.year,
                    base.monthNumber - 1,
                    base.dayOfMonth
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pick date/time: $label") }

        TextButton(
            onClick = { onPicked(null) },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            enabled = current != null
        ) { Text("Clear deadline") }
    }
}

private fun Context.findActivity(): Activity? {
    var cur: Context = this
    while (cur is ContextWrapper) {
        if (cur is Activity) return cur
        cur = cur.baseContext
    }
    return null
}
