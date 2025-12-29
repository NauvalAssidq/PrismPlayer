package org.android.prismplayer

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import org.android.prismplayer.ui.navigation.AppNavigation
import org.android.prismplayer.ui.theme.PrismPlayerTheme
import org.android.prismplayer.ui.utils.AppTheme
import org.android.prismplayer.ui.utils.ThemePreferences

class MainActivity : ComponentActivity() {
    private var expandPlayerFromWidget by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        handleIntent(intent)

        val themePrefs = ThemePreferences(this)
        
        setContent {
            val appTheme by themePrefs.themeFlow.collectAsState()
            val isDarkTheme = when (appTheme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            PrismPlayerTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    expandPlayer = expandPlayerFromWidget,
                    onExpandConsumed = { expandPlayerFromWidget = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXPAND_PLAYER", false) == true) {
            expandPlayerFromWidget = true
            intent.removeExtra("EXPAND_PLAYER")
        }
    }
}
