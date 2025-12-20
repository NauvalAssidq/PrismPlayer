package org.android.prismplayer.ui.theme

import android.app.Activity
import android.os.Build
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


val PrismBlack = Color(0xFF000000)
val PrismDarkGrey = Color(0xFF121212)
val PrismGrey = Color(0xFF262626)
val PrismLightGrey = Color(0xFF808080)
val PrismWhite = Color(0xFFFFFFFF)
val NothingRed = Color(0xFFD71921)


private val DarkColorScheme = darkColorScheme(
    primary = PrismWhite,
    onPrimary = PrismBlack,
    primaryContainer = PrismDarkGrey,
    onPrimaryContainer = PrismWhite,

    secondary = NothingRed,
    onSecondary = PrismWhite,
    secondaryContainer = NothingRed.copy(alpha = 0.2f),
    onSecondaryContainer = NothingRed,

    background = PrismBlack,
    onBackground = PrismWhite,

    surface = PrismBlack,
    onSurface = PrismWhite,
    surfaceVariant = PrismDarkGrey,
    onSurfaceVariant = PrismLightGrey,

    outline = PrismGrey,
    outlineVariant = PrismDarkGrey
)

private val LightColorScheme = lightColorScheme(
    primary = PrismBlack,
    onPrimary = PrismWhite,
    secondary = NothingRed,
    background = Color(0xFFF0F0F0),
    onBackground = PrismBlack,
    surface = PrismWhite,
    onSurface = PrismBlack
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrismTypography,
        content = content
    )
}