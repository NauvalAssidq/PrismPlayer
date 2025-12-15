package org.android.prismplayer.ui.utils

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val paletteCache = LruCache<String, Color>(20)

@Composable
fun rememberDominantColor(bitmap: Bitmap?): Color {
    var color by remember { mutableStateOf(Color(0xFF1DB954)) }

    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            color = PrismaColorUtils.extractDominantColor(bitmap)
        }
    }
    return color
}

@Composable
fun rememberImmersiveColor(bitmap: Bitmap?, defaultColor: Color = Color(0xFF1D1D1D)): Color {
    var color by remember { mutableStateOf(defaultColor) }

    LaunchedEffect(bitmap) {
        if (bitmap == null) {
            color = defaultColor
            return@LaunchedEffect
        }

        launch(Dispatchers.Default) {
            val generationId = "bitmap_${bitmap.hashCode()}"
            val cached = paletteCache.get(generationId)

            if (cached != null) {
                withContext(Dispatchers.Main) { color = cached }
            } else {
                val palette = Palette.from(bitmap)
                    .maximumColorCount(24)
                    .generate()

                val targetSwatch = palette.vibrantSwatch
                    ?: palette.darkVibrantSwatch
                    ?: palette.lightVibrantSwatch
                    ?: palette.dominantSwatch

                val calculatedColor = if (targetSwatch != null) {
                    val intColor = targetSwatch.rgb
                    if (isColorTooBright(intColor)) {
                        shiftColorToDark(intColor, 0.4f)
                    } else {
                        Color(intColor)
                    }
                } else {
                    defaultColor
                }

                paletteCache.put(generationId, calculatedColor)
                withContext(Dispatchers.Main) { color = calculatedColor }
            }
        }
    }

    return color
}

private fun isColorTooBright(color: Int): Boolean {
    val r = (color shr 16) and 0xFF
    val g = (color shr 8) and 0xFF
    val b = color and 0xFF

    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    return luminance > 0.5
}


private fun shiftColorToDark(color: Int, factor: Float): Color {
    val r = (color shr 16) and 0xFF
    val g = (color shr 8) and 0xFF
    val b = color and 0xFF

    val newR = (r * (1f - factor)).toInt().coerceIn(0, 255)
    val newG = (g * (1f - factor)).toInt().coerceIn(0, 255)
    val newB = (b * (1f - factor)).toInt().coerceIn(0, 255)

    return Color(0xFF000000.toInt() or (newR shl 16) or (newG shl 8) or newB)
}
