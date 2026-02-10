package com.grigorevmp.simpletodo.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDeadline(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val y = dt.year.toString().padStart(4, '0')
    val m = dt.monthNumber.toString().padStart(2, '0')
    val d = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mm = dt.minute.toString().padStart(2, '0')
    return "$d.$m.$y $hh:$mm"
}

fun dateKey(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val y = dt.year.toString().padStart(4, '0')
    val m = dt.monthNumber.toString().padStart(2, '0')
    val d = dt.dayOfMonth.toString().padStart(2, '0')
    return "$d.$m.$y"
}
