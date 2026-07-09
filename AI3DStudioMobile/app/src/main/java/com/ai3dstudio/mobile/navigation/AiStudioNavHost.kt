package com.ai3dstudio.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ai3dstudio.mobile.R
import com.ai3dstudio.mobile.feature.assets.ui.AssetLibraryScreen
import com.ai3dstudio.mobile.feature.chat.ui.ChatScreen
import com.ai3dstudio.mobile.feature.game.ui.GameGenerationScreen
import com.ai3dstudio.mobile.feature.image.ui.ImageGenerationScreen
import com.ai3dstudio.mobile.feature.model3d.ui.Model3dGenerationScreen
import com.ai3dstudio.mobile.feature.projects.ui.ProjectsScreen
import com.ai3dstudio.mobile.feature.settings.ui.SettingsScreen
import com.ai3dstudio.mobile.feature.setup.ui.SetupScreen

sealed class AiStudioDestination(val route: String, val labelRes: Int) {
    data object Setup : AiStudioDestination("setup", R.string.setup_title)
    data object Projects : AiStudioDestination("projects", R.string.nav_projects)
    data object Chat : AiStudioDestination("chat/{projectId}", R.string.nav_chat) {
        fun createRoute(projectId: String) = "chat/$projectId"
    }
    data object ImageGen : AiStudioDestination("image", R.string.nav_image)
    data object Model3dGen : AiStudioDestination("model3d", R.string.nav_model3d)
    data object GameGen : AiStudioDestination("game", R.string.nav_game)
    data object Assets : AiStudioDestination("assets", R.string.nav_assets)
    data object Settings : AiStudioDestination("settings", R.string.nav_settings)
}

private val bottomBarDestinations = listOf(
    AiStudioDestination.Projects to Icons.Filled.Folder,
    AiStudioDestination.ImageGen to Icons.Filled.Image,
    AiStudioDestination.Model3dGen to Icons.Filled.ViewInAr,
    AiStudioDestination.GameGen to Icons.Filled.SportsEsports,
    AiStudioDestination.Assets to Icons.Filled.PhotoLibrary,
    AiStudioDestination.Settings to Icons.Filled.Settings
)

@Composable
fun AiStudioNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute != AiStudioDestination.Setup.route &&
                !currentRoute.startsWith("chat/")
            ) {
                NavigationBar {
                    bottomBarDestinations.forEach { (destination, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AiStudioDestination.Setup.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(AiStudioDestination.Setup.route) {
                SetupScreen(onConnected = {
                    navController.navigate(AiStudioDestination.Projects.route) {
                        popUpTo(AiStudioDestination.Setup.route) { inclusive = true }
                    }
                })
            }
            composable(AiStudioDestination.Projects.route) {
                ProjectsScreen(onOpenProject = { project ->
                    navController.navigate(AiStudioDestination.Chat.createRoute(project.id))
                })
            }
            composable(
                route = AiStudioDestination.Chat.route,
                arguments = listOf(navArgument("projectId") { })
            ) {
                ChatScreen()
            }
            composable(AiStudioDestination.ImageGen.route) { ImageGenerationScreen() }
            composable(AiStudioDestination.Model3dGen.route) { Model3dGenerationScreen() }
            composable(AiStudioDestination.GameGen.route) { GameGenerationScreen() }
            composable(AiStudioDestination.Assets.route) { AssetLibraryScreen() }
            composable(AiStudioDestination.Settings.route) {
                SettingsScreen(onChangeServer = {
                    navController.navigate(AiStudioDestination.Setup.route) {
                        popUpTo(0)
                    }
                })
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id = id)
