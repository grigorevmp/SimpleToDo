package com.grigorevmp.simpletodo.di

import com.grigorevmp.simpletodo.data.TodoRepository
import com.grigorevmp.simpletodo.platform.createNotificationScheduler
import com.grigorevmp.simpletodo.platform.createPlatformSettings

class AppComponent {
    private val settings = createPlatformSettings()
    private val scheduler = createNotificationScheduler()

    val repo = TodoRepository(settings, scheduler)
}
