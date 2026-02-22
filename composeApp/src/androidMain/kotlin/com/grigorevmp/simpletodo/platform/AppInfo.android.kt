package com.grigorevmp.simpletodo.platform

import com.grigorevmp.simpletodo.BuildConfig

actual object AppInfo {
    actual val versionName: String = BuildConfig.VERSION_NAME
    actual val versionCode: String = BuildConfig.VERSION_CODE.toString()
}
