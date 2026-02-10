package com.grigorevmp.simpletodo.util

import kotlin.random.Random
import kotlinx.datetime.Clock

fun newId(prefix: String): String {
    val time = Clock.System.now().toEpochMilliseconds().toString(16)
    val rnd = Random.nextLong().toString(16).replace("-", "f")
    return "${prefix}_${time}_$rnd"
}
