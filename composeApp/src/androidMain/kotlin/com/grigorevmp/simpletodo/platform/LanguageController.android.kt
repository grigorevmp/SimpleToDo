package com.grigorevmp.simpletodo.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.grigorevmp.simpletodo.model.AppLanguage

actual fun applyAppLanguage(language: AppLanguage) {
    val locales = when (language) {
        AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
        AppLanguage.EN -> LocaleListCompat.forLanguageTags("en")
        AppLanguage.RU -> LocaleListCompat.forLanguageTags("ru")
    }
    val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val desiredTags = locales.toLanguageTags()
    if (currentTags == desiredTags) return
    AppCompatDelegate.setApplicationLocales(locales)
    AndroidContextHolder.currentActivity?.recreate()
}
