package com.grigorevmp.simpletodo.platform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.UIKit.UIDatePicker

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformDateTimePicker(
    current: Instant?,
    onPicked: (Instant?) -> Unit
) {
    var selected by remember { mutableStateOf(current) }
    var pickerRef by remember { mutableStateOf<UIDatePicker?>(null) }

    LaunchedEffect(current) {
        selected = current
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        UIKitView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            factory = {
                UIDatePicker().apply {
                    selected?.let { setDate(it.toNSDate(), animated = false) }
                    pickerRef = this
                }
            },
            update = { picker ->
                pickerRef = picker
                val targetDate = selected?.toNSDate()
                if (targetDate != null) {
                    picker.setDate(targetDate, animated = false)
                }
            }
        )

        Row(modifier = Modifier.padding(top = 6.dp)) {
            TextButton(
                onClick = {
                    val picked = pickerRef?.date?.toInstant()
                    selected = picked
                    onPicked(picked)
                }
            ) {
                Text("Apply")
            }

            TextButton(
                onClick = {
                    selected = null
                    onPicked(null)
                },
                enabled = selected != null
            ) {
                Text("Clear")
            }
        }
    }
}

private fun Instant.toNSDate(): NSDate {
    val seconds = this.toEpochMilliseconds() / 1000.0
    return NSDate(timeIntervalSinceReferenceDate = seconds - NSTimeIntervalSince1970)
}

private fun NSDate.toInstant(): Instant {
    val seconds = this.timeIntervalSinceReferenceDate + NSTimeIntervalSince1970
    return Instant.fromEpochMilliseconds((seconds * 1000.0).toLong())
}

private const val NSTimeIntervalSince1970 = 978307200.0
