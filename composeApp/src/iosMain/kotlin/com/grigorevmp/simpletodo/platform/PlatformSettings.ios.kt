package com.grigorevmp.simpletodo.platform

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun createPlatformSettings(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
