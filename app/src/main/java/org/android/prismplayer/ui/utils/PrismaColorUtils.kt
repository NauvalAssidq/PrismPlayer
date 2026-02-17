package org.android.prismplayer.ui.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrismaColorUtils {
    private val FALLBACK_COLOR = Color(0xFF202020)

    suspend fun extractDominantColor(bitmap: Bitmap?): Color {
        return withContext(Dispatchers.Default) {
            if (bitmap == null) return@withContext FALLBACK_COLOR

            val palette = Palette.from(bitmap).generate()
            val swatch = palette.vibrantSwatch
                ?: palette.darkVibrantSwatch
                ?: palette.dominantSwatch

            swatch?.rgb?.let { Color(it) } ?: FALLBACK_COLOR
        }
    }

    fun adjustForAccent(color: Color): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        hsl[2] *= 0.6f

        if (hsl[2] > 0.45f) hsl[2] = 0.5f
        if (hsl[2] < 0.15f) hsl[2] = 0.3f

        return Color(ColorUtils.HSLToColor(hsl))
    }

    fun adjustForBackground(color: Color): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        if (hsl[2] > 0.85f) {
            hsl[2] = 0.85f
        }
        return Color(ColorUtils.HSLToColor(hsl))
    }
}