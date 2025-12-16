package org.android.prismplayer.ui.utils

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val paletteCache = LruCache<String, Color>(20)

@Composable
fun rememberDominantColor(
    bitmap: Bitmap?,
    defaultColor: Color = Color(0xFF202020)
): Color {
    var color by remember { mutableStateOf(defaultColor) }

    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            val extracted = PrismaColorUtils.extractDominantColor(bitmap)
            color = PrismaColorUtils.adjustForBackground(extracted)
        }
    }
    return color
}

@Composable
fun rememberImmersiveColor(
    bitmap: Bitmap?,
    defaultColor: Color = Color(0xFF202020)
): Color {
    var color by remember { mutableStateOf(defaultColor) }
    LaunchedEffect(bitmap) {
        if (bitmap == null) return@LaunchedEffect
        launch(Dispatchers.Default) {
            val generationId = "immersive_${bitmap.hashCode()}"
            val cached = paletteCache.get(generationId)

            if (cached != null) {
                withContext(Dispatchers.Main) { color = cached }
            } else {
                val extracted = PrismaColorUtils.extractDominantColor(bitmap)
                val finalColor = PrismaColorUtils.adjustForAccent(extracted)
                paletteCache.put(generationId, finalColor)
                withContext(Dispatchers.Main) { color = finalColor }
            }
        }
    }

    return color
}