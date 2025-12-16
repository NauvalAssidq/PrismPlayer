package org.android.prismplayer

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.android.prismplayer.ui.MainLayout
import org.android.prismplayer.ui.player.AudioViewModel
import org.android.prismplayer.ui.screens.*
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            MaterialTheme {
                PrismNavigation()
            }
        }
    }
}

@Composable
fun PrismNavigation() {
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
            val folderState = produceState<List<FolderItem>?>(initialValue = null) { value = scanAudioFolders(context) }

            if (folderState.value == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF1DB954)) }
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
            val libraryPrefs = remember { org.android.prismplayer.ui.utils.LibraryPreferences(context) }

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
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            }
        }

        composable("main") {
            MainLayout(
                audioViewModel = audioViewModel,
                homeViewModel = homeViewModel,
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

suspend fun scanAudioFolders(context: Context): List<FolderItem> {
    return withContext(Dispatchers.IO) {
        val folderMap = mutableMapOf<String, Int>()
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%' AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        try {
            context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)?.use { cursor ->
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                while (cursor.moveToNext()) {
                    val fullPath = cursor.getString(pathCol)
                    if (File(fullPath).exists()) {
                        File(fullPath).parentFile?.absolutePath?.let { folderMap[it] = folderMap.getOrDefault(it, 0) + 1 }
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        folderMap.map { (path, count) ->
            val name = File(path).name
            val isImportant = name.contains("Music", true) || name.contains("Download", true) || count > 5
            FolderItem(name, path, count, isImportant)
        }.sortedByDescending { it.count }
    }
}