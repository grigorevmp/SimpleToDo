package com.grigorevmp.simpletodo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.Crossfade
import com.grigorevmp.simpletodo.di.AppComponent
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.ui.components.AppTab
import com.grigorevmp.simpletodo.ui.components.CreateAction
import com.grigorevmp.simpletodo.ui.components.PlatformBottomBar
import com.grigorevmp.simpletodo.ui.components.AddIcon
import com.grigorevmp.simpletodo.ui.home.HomeScreen
import com.grigorevmp.simpletodo.ui.notes.NotesScreen
import com.grigorevmp.simpletodo.ui.settings.SettingsScreen
import com.grigorevmp.simpletodo.ui.theme.DinoTheme
import com.grigorevmp.simpletodo.platform.PlatformSystemBars
import com.grigorevmp.simpletodo.platform.isIos
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun App() {
    val component = remember { AppComponent() }

    val dark = isSystemInDarkTheme()
    val prefs by component.repo.prefs.collectAsState()
    val mode = prefs.themeMode
    val forceLight = prefs.disableDarkTheme
    val isDark = if (forceLight) {
        false
    } else {
        when (mode) {
            ThemeMode.DIM -> true
            else -> dark
        }
    }
    val effectiveMode = if (forceLight && mode == ThemeMode.DIM) ThemeMode.SYSTEM else mode
    DinoTheme(dark = isDark, mode = effectiveMode, authorAccentIndex = prefs.authorAccentIndex) {
        val navBarColor = if (isDark) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surface
        }
        PlatformSystemBars(
            isDark = isDark,
            backgroundColor = navBarColor
        )
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route ?: "home"
        val iosRoute = remember { androidx.compose.runtime.mutableStateOf("home") }
        val activeRoute = if (isIos) iosRoute.value else currentRoute
        val tab = when (activeRoute) {
            "settings" -> AppTab.SETTINGS
            "notes" -> AppTab.NOTES
            else -> AppTab.HOME
        }
        val createTaskSignal = remember { androidx.compose.runtime.mutableIntStateOf(0) }
        val createNoteSignal = remember { androidx.compose.runtime.mutableIntStateOf(0) }
        val openNoteId = remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
        val backgroundColor = MaterialTheme.colorScheme.background
        val backdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { pad ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop)
                ) {
                    if (isIos) {
                        Crossfade(targetState = activeRoute, label = "ios_nav_fade") { route ->
                            when (route) {
                                "notes" -> NotesScreen(
                                    repo = component.repo,
                                    createNoteSignal = createNoteSignal.intValue,
                                    onCreateNoteHandled = { createNoteSignal.intValue = 0 },
                                    openNoteId = openNoteId.value,
                                    onOpenNoteHandled = { openNoteId.value = null }
                                )
                                "settings" -> SettingsScreen(component.repo)
                                else -> HomeScreen(
                                    repo = component.repo,
                                    createSignal = createTaskSignal.intValue,
                                    onCreateHandled = { createTaskSignal.intValue = 0 },
                                    onEditNote = { noteId ->
                                        openNoteId.value = noteId
                                        iosRoute.value = "notes"
                                    }
                                )
                            }
                        }
                    } else {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    repo = component.repo,
                                    createSignal = createTaskSignal.intValue,
                                    onCreateHandled = { createTaskSignal.intValue = 0 },
                                    onEditNote = { noteId ->
                                        openNoteId.value = noteId
                                        if (currentRoute != "notes") {
                                            navController.navigate("notes") { launchSingleTop = true }
                                        }
                                    }
                                )
                            }
                            composable("notes") {
                                NotesScreen(
                                    repo = component.repo,
                                    createNoteSignal = createNoteSignal.intValue,
                                    onCreateNoteHandled = { createNoteSignal.intValue = 0 },
                                    openNoteId = openNoteId.value,
                                    onOpenNoteHandled = { openNoteId.value = null }
                                )
                            }
                            composable("settings") { SettingsScreen(component.repo) }
                        }
                    }
                }

                    PlatformBottomBar(
                        tab = tab,
                        onTab = { target ->
                            val route = when (target) {
                                AppTab.HOME -> "home"
                            AppTab.NOTES -> "notes"
                            AppTab.SETTINGS -> "settings"
                        }
                        if (isIos) {
                            iosRoute.value = route
                        } else if (currentRoute != route) {
                            navController.navigate(route) { launchSingleTop = true }
                        }
                    },
                    createActions = when (tab) {
                        AppTab.HOME -> listOf(
                            CreateAction(
                                id = "new_task",
                                label = "New",
                                contentDescription = "Create task",
                                icon = AddIcon,
                                onClick = {
                                    if (isIos) {
                                        iosRoute.value = "home"
                                    } else if (currentRoute != "home") {
                                        navController.navigate("home") { launchSingleTop = true }
                                    }
                                    createTaskSignal.intValue += 1
                                }
                            )
                        )
                        AppTab.NOTES -> listOf(
                            CreateAction(
                                id = "new_note",
                                label = "New",
                                contentDescription = "Create note",
                                icon = AddIcon,
                                onClick = {
                                    if (isIos) {
                                        iosRoute.value = "notes"
                                    } else if (currentRoute != "notes") {
                                        navController.navigate("notes") { launchSingleTop = true }
                                    }
                                    createNoteSignal.intValue += 1
                                }
                            )
                        )
                        AppTab.SETTINGS -> emptyList()
                    },
                    enableEffects = prefs.liquidGlass && !isIos,
                    backdrop = backdrop,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 22.dp, vertical = if (isIos) 8.dp else 12.dp)
                        .padding(bottom = if (isIos) 2.dp else 8.dp)
                )
            }
        }
    }
}
