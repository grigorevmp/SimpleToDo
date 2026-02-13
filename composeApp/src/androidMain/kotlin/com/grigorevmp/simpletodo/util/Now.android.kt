package com.grigorevmp.simpletodo.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

actual fun nowInstant(): Instant = Clock.System.now()
