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
import com.grigorevmp.simpletodo.di.AppComponent
import com.grigorevmp.simpletodo.model.ThemeMode
import com.grigorevmp.simpletodo.ui.components.AppTab
import com.grigorevmp.simpletodo.ui.components.CreateAction
import com.grigorevmp.simpletodo.ui.components.FloatingNavBar
import com.grigorevmp.simpletodo.ui.components.AddIcon
import com.grigorevmp.simpletodo.ui.home.HomeScreen
import com.grigorevmp.simpletodo.ui.notes.NotesScreen
import com.grigorevmp.simpletodo.ui.settings.SettingsScreen
import com.grigorevmp.simpletodo.ui.theme.DinoTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun App() {
    val component = remember { AppComponent() }

    val dark = isSystemInDarkTheme()
    val prefs by component.repo.prefs.collectAsState()
    val mode = prefs.themeMode
    val isDark = when (mode) {
        ThemeMode.DIM -> true
        else -> dark
    }
    DinoTheme(dark = isDark, mode = mode, authorAccentIndex = prefs.authorAccentIndex) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route ?: "home"
        val tab = when (currentRoute) {
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

                FloatingNavBar(
                    tab = tab,
                    onTab = { target ->
                        val route = when (target) {
                            AppTab.HOME -> "home"
                            AppTab.NOTES -> "notes"
                            AppTab.SETTINGS -> "settings"
                        }
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
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
                                    if (currentRoute != "home") {
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
                                    if (currentRoute != "notes") {
                                        navController.navigate("notes") { launchSingleTop = true }
                                    }
                                    createNoteSignal.intValue += 1
                                }
                            )
                        )
                        AppTab.SETTINGS -> emptyList()
                    },
                    enableEffects = prefs.liquidGlass,
                    backdrop = backdrop,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
