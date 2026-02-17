package org.android.prismplayer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.model.FolderItem
import org.android.prismplayer.ui.MainLayout
import org.android.prismplayer.ui.player.AudioViewModel
import org.android.prismplayer.ui.screens.EditTrackRoute
import org.android.prismplayer.ui.screens.FolderSelectionScreen
import org.android.prismplayer.ui.screens.HomeViewModel
import org.android.prismplayer.ui.screens.ManageFoldersScreen
import org.android.prismplayer.ui.screens.PermissionScreen
import org.android.prismplayer.ui.screens.SplashScreen
import org.android.prismplayer.ui.utils.LibraryPreferences

@Composable
fun AppNavigation(
    expandPlayer: Boolean,
    onExpandConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as PrismApplication
    val audioViewModel: AudioViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onPermissionsGranted = { navController.navigate("main") { popUpTo("splash") { inclusive = true } } },
                onPermissionsMissing = { navController.navigate("permissions") { popUpTo("splash") { inclusive = true } } }
            )
        }

        composable("permissions") {
            PermissionScreen(onAllPermissionsGranted = { navController.navigate("folder_selection") { popUpTo("permissions") { inclusive = true } } })
        }

        composable("folder_selection") {
            val scope = rememberCoroutineScope()
            // Refactored: Use repository instead of top-level function
            val folderState = produceState<List<FolderItem>?>(initialValue = null) { 
                value = app.repository.scanAudioFolders() 
            }

            if (folderState.value == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary) }
            } else {
                FolderSelectionScreen(
                    folders = folderState.value!!,
                    onFinish = { selectedPaths ->
                        scope.launch {
                            app.repository.importSongsFromFolders(selectedPaths)
                            navController.navigate("main") { popUpTo("folder_selection") { inclusive = true }; popUpTo("splash") { inclusive = true } }
                        }
                    }
                )
            }
        }

        composable("manage_library") {
            val scope = rememberCoroutineScope()
            val libraryPrefs = remember { LibraryPreferences(context) }

            val savedPaths by produceState<List<String>>(initialValue = emptyList()) {
                value = withContext(Dispatchers.IO) {
                    libraryPrefs.getSavedFolders()
                }
            }

            if (savedPaths.isNotEmpty()) {
                ManageFoldersScreen(
                    currentPaths = savedPaths,
                    onBack = { navController.popBackStack() },
                    onSave = { newPaths ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                libraryPrefs.saveFolders(newPaths)
                            }
                            app.repository.importSongsFromFolders(newPaths)
                            navController.popBackStack()
                        }
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        composable("main") {
            MainLayout(
                audioViewModel = audioViewModel,
                homeViewModel = homeViewModel,
                expandPlayer = expandPlayer,
                onExpandConsumed = onExpandConsumed,
                onEditSong = { songId -> navController.navigate("edit_track/$songId") },
                onReselectFolders = {
                    navController.navigate("manage_library")
                }
            )
        }

        composable(
            route = "edit_track/{songId}",
            arguments = listOf(navArgument("songId") { type = NavType.LongType })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getLong("songId") ?: -1L
            EditTrackRoute(
                songId = songId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
