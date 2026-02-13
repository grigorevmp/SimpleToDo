package com.grigorevmp.simpletodo.util

import kotlinx.datetime.Instant
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
actual fun nowInstant(): Instant {
    val seconds = time(null)
    return Instant.fromEpochMilliseconds(seconds * 1000L)
}
