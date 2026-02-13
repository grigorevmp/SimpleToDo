package com.grigorevmp.simpletodo.util

import kotlin.random.Random
import com.grigorevmp.simpletodo.util.nowInstant

fun newId(prefix: String): String {
    val time = nowInstant().toEpochMilliseconds().toString(16)
    val rnd = Random.nextLong().toString(16).replace("-", "f")
    return "${prefix}_${time}_$rnd"
}
