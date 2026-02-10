package com.grigorevmp.simpletodo.platform

import com.russhwolf.settings.Settings
import com.russhwolf.settings.apple.AppleSettings
import platform.Foundation.NSUserDefaults

actual fun createPlatformSettings(): Settings {
    return AppleSettings(NSUserDefaults.standardUserDefaults)
}
