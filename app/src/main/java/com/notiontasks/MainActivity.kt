package com.notiontasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.notiontasks.ui.MainViewModel
import com.notiontasks.ui.screens.*
import com.notiontasks.ui.theme.NotionTasksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotionTasksTheme {
                NotionTasksApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NotionTasksApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        if (!uiState.isAuthenticated) {
            LoginScreen(
                onConnectNotion = { token ->
                    viewModel.connectWithToken(token)
                }
            )
        } else {
            var showViewSettings by remember { mutableStateOf(false) }

            NavHost(navController = navController, startDestination = "tasks") {
                composable("tasks") {
                    TaskListScreen(
                        uiState = uiState,
                        onToggleTask = viewModel::toggleTask,
                        onSyncClick = viewModel::sync,
                        onManageSources = { navController.navigate("sources") },
                        onViewSettings = { showViewSettings = true },
                        onSignOut = viewModel::signOut
                    )
                }
                composable("sources") {
                    ManageSourcesScreen(onBack = { navController.popBackStack() })
                }
            }

            if (showViewSettings) {
                ViewSettingsSheet(
                    current = uiState.viewSettings,
                    onDismiss = { showViewSettings = false },
                    onSave = { settings ->
                        viewModel.updateViewSettings(settings)
                        showViewSettings = false
                    }
                )
            }
        }
    }
}
