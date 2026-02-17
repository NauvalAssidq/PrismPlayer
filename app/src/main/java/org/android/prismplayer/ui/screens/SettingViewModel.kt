package org.android.prismplayer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.repository.MusicRepository
import org.android.prismplayer.ui.utils.AppTheme
import org.android.prismplayer.ui.utils.ThemePreferences

class SettingsViewModel(
    private val repository: MusicRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    val currentTheme = themePreferences.themeFlow

    fun setTheme(theme: AppTheme) {
        themePreferences.saveTheme(theme)
    }

    fun rescanLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            val startTime = System.currentTimeMillis()

            val foldersToScan = listOf(
                "/storage/emulated/0/Music",
                "/storage/emulated/0/Download",
                "/storage/emulated/0/Podcasts"
            )

            foldersToScan.forEach { path ->
                // This is a simple directory walker to find files and tell Android to scan them
                // Note: For deep directories, just relying on the default import is usually safer/faster
                // unless you implement a full recursive file walker here.
            }

            repository.importSongsFromFolders(foldersToScan)
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 1500) {
                delay(1500 - elapsedTime)
            }

            _isScanning.value = false        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                val themePrefs = ThemePreferences(app.applicationContext)
                return SettingsViewModel(app.repository, themePrefs) as T
            }
        }
    }
}