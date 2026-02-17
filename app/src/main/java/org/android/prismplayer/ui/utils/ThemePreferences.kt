package org.android.prismplayer.ui.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prism_theme_prefs", Context.MODE_PRIVATE)
    private val KEY_THEME = "app_theme"

    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_THEME) {
            _themeFlow.value = getTheme()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun getTheme(): AppTheme {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM
        }
    }

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        // No need to manually update _themeFlow here; the listener will catch it.
        // This ensures synchronization across instances since they share the same underlying prefs.
    }
}
