package com.grigorevmp.simpletodo.platform

import platform.Foundation.NSBundle

actual object AppInfo {
    actual val versionName: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: "1.0"

    actual val versionCode: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
            ?: "1"
}
