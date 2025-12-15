package org.android.prismplayer.ui.utils

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrismaColorUtils {
    suspend fun extractDominantColor(bitmap: Bitmap?): Color {
        return withContext(Dispatchers.Default) {
            if (bitmap == null) return@withContext Color(0xFF1DB954) // Default Green

            val palette = Palette.from(bitmap).generate()
            val swatch = palette.vibrantSwatch
                ?: palette.lightVibrantSwatch
                ?: palette.dominantSwatch

            swatch?.rgb?.let { Color(it) } ?: Color(0xFF1DB954)
        }
    }

    fun adjustForAccent(color: Color): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        if (hsl[1] < 0.4f) hsl[1] = 0.4f
        hsl[2] = hsl[2].coerceIn(0.5f, 0.8f)
        return Color(ColorUtils.HSLToColor(hsl))
    }

    fun adjustForBackground(color: Color): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        hsl[2] = 0.15f
        return Color(ColorUtils.HSLToColor(hsl))
    }
}