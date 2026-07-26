package org.android.prismplayer.ui.utils

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.compose.material3.MaterialTheme
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
    artUri: String?,
    bitmap: Bitmap?,
    defaultColor: Color = MaterialTheme.colorScheme.secondary
): Color {
    val cacheKey = artUri ?: bitmap?.hashCode()?.toString() ?: ""
    val cachedColor = if (cacheKey.isNotEmpty()) paletteCache.get(cacheKey) else null

    var color by remember(cacheKey) { mutableStateOf(cachedColor ?: defaultColor) }

    LaunchedEffect(bitmap, cacheKey) {
        if (bitmap == null || cacheKey.isEmpty()) return@LaunchedEffect
        if (paletteCache.get(cacheKey) != null) {
            color = paletteCache.get(cacheKey)!!
            return@LaunchedEffect
        }
        launch(Dispatchers.Default) {
            val extracted = PrismaColorUtils.extractDominantColor(bitmap)
            val finalColor = PrismaColorUtils.adjustForAccent(extracted)
            paletteCache.put(cacheKey, finalColor)
            withContext(Dispatchers.Main) { color = finalColor }
        }
    }

    return color
}