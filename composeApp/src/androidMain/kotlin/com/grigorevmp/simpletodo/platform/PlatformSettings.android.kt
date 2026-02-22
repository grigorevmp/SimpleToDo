package com.grigorevmp.simpletodo.platform

import android.app.Activity
import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private const val PREFS_NAME = "simpletodo_prefs"

internal object AndroidContextHolder {
    lateinit var appContext: Context
    var currentActivity: Activity? = null
}

actual fun createPlatformSettings(): Settings {
    val sp = AndroidContextHolder.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sp)
}
