package org.android.prismplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrismColor.White,
    onPrimary = PrismColor.Black,
    primaryContainer = PrismColor.DarkGrey,
    onPrimaryContainer = PrismColor.White,
    secondary = PrismColor.Red,
    onSecondary = PrismColor.White,
    secondaryContainer = PrismColor.Red.copy(alpha = 0.2f),
    onSecondaryContainer = PrismColor.Red,
    background = PrismColor.Black,
    onBackground = PrismColor.White,
    surface = PrismColor.Black,
    onSurface = PrismColor.White,
    surfaceVariant = PrismColor.DarkGrey,
    onSurfaceVariant = PrismColor.LightGrey,
    outline = PrismColor.Grey,
    outlineVariant = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = PrismColor.Black,
    onPrimary = PrismColor.White,
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = PrismColor.Black,
    secondary = PrismColor.Red,
    onSecondary = PrismColor.White,
    secondaryContainer = PrismColor.Red.copy(alpha = 0.15f),
    onSecondaryContainer = PrismColor.Red,
    background = Color(0xFFF6F6F6),
    onBackground = PrismColor.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = PrismColor.Black,
    surfaceVariant = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun PrismPlayerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = PrismTypography, content = content)
}
