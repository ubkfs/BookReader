package com.example.readvault

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.readvault.ui.screens.*
import com.example.readvault.ui.viewmodel.ReadVaultViewModel
import com.example.readvault.ui.viewmodel.Screen

@Composable
fun ReadVaultApp(
    viewModel: ReadVaultViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val showBottomBar = currentScreen is Screen.Library ||
            currentScreen is Screen.Vocabulary ||
            currentScreen is Screen.Goals ||
            currentScreen is Screen.Plans ||
            currentScreen is Screen.Support

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Library,
                        onClick = { viewModel.navigateTo(Screen.Library) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Library") },
                        label = { Text("Library") },
                        modifier = Modifier.testTag("nav_library")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Vocabulary,
                        onClick = { viewModel.navigateTo(Screen.Vocabulary) },
                        icon = { Icon(Icons.Default.School, contentDescription = "Vocabulary") },
                        label = { Text("Vocabulary") },
                        modifier = Modifier.testTag("nav_vocabulary")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Goals,
                        onClick = { viewModel.navigateTo(Screen.Goals) },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Goals") },
                        label = { Text("Goals") },
                        modifier = Modifier.testTag("nav_goals")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Plans,
                        onClick = { viewModel.navigateTo(Screen.Plans) },
                        icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Plans") },
                        label = { Text("Plans") },
                        modifier = Modifier.testTag("nav_plans")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Support,
                        onClick = { viewModel.navigateTo(Screen.Support) },
                        icon = { Icon(Icons.Default.SupportAgent, contentDescription = "Support") },
                        label = { Text("Support") },
                        modifier = Modifier.testTag("nav_support")
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val screen = currentScreen) {
            is Screen.Library -> LibraryScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            is Screen.Reader -> ReaderScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            is Screen.BookDetail -> BookDetailScreen(bookId = screen.bookId, viewModel = viewModel, modifier = Modifier.fillMaxSize())
            is Screen.Vocabulary -> VocabularyScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            is Screen.Goals -> GoalsScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            is Screen.Plans -> PlansScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            is Screen.Support -> SupportScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            is Screen.Settings -> SettingsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        }
    }
}
