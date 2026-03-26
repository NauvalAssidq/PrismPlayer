package org.android.prismplayer.ui.screens

import android.content.Context
import android.content.SharedPreferences
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
import androidx.core.content.edit

class SettingsViewModel(
    private val repository: MusicRepository,
    private val themePreferences: ThemePreferences,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    val currentTheme = themePreferences.themeFlow

    private val _geminiApiKey = MutableStateFlow(sharedPreferences.getString("gemini_api_key", "") ?: "")
    val geminiApiKey = _geminiApiKey.asStateFlow()

    private val _aiSystemPrompt = MutableStateFlow(
        sharedPreferences.getString("gemini_system_prompt", DEFAULT_GEMINI_PROMPT) ?: DEFAULT_GEMINI_PROMPT
    )
    val aiSystemPrompt = _aiSystemPrompt.asStateFlow()

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        sharedPreferences.edit { putString("gemini_api_key", key) }
    }

    fun setAiSystemPrompt(prompt: String) {
        _aiSystemPrompt.value = prompt
        sharedPreferences.edit { putString("gemini_system_prompt", prompt) }
    }

    fun resetAiSystemPrompt() {
        setAiSystemPrompt(DEFAULT_GEMINI_PROMPT)
    }

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
            }

            repository.importSongsFromFolders(foldersToScan)
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 1500) {
                delay(1500 - elapsedTime)
            }

            _isScanning.value = false
        }
    }

    companion object {
        const val DEFAULT_GEMINI_PROMPT =
        """
            [SYSTEM_OVERRIDE // BILINGUAL_CURATOR_MODE]
            You are an elite AI music curator operating within PrismPlayer.
            
            TASK:
            Filter the provided JSON library of songs based on the user's requested VIBE/HEURISTIC. The user query may be in English or Indonesian.
            
            RULES:
            1. Select only songs that strongly match the requested vibe/genre/mood.
            2. Do NOT hallucinate or invent IDs. Only use IDs from the provided library list.
            3. Return STRICTLY a raw JSON array of integers representing the chosen Song IDs.
            4. NO markdown formatting, NO backticks (```json), NO conversational text. Just the array.
            
            OUTPUT FORMAT EXPECTED:
            [12, 45, 88, 102]
        """

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                val themePrefs = ThemePreferences(app.applicationContext)
                val sharedPrefs = app.getSharedPreferences("prism_config", Context.MODE_PRIVATE)

                return SettingsViewModel(app.repository, themePrefs, sharedPrefs) as T
            }
        }
    }
}