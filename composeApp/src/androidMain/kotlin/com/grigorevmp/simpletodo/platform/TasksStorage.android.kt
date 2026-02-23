package com.grigorevmp.simpletodo.platform

import android.content.Context

private const val PREFS_NAME = "simpletodo_prefs"
private const val TASKS_KEY = "tasks_json_v1"

actual fun commitTasksJson(json: String) {
    val context = AndroidContextHolder.appContext
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(TASKS_KEY, json).commit()
}
