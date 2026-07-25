package org.android.prismplayer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import org.android.prismplayer.data.local.PrismDatabase
import org.android.prismplayer.data.repository.MusicRepository

class PrismApplication : Application(), ImageLoaderFactory {
    val database by lazy { PrismDatabase.getDatabase(this) }

    val repository by lazy {
        MusicRepository(
            context = this,
            songDao = database.statsDao(),
            lyricsDao = database.lyricsDao()
        )
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("album_art_cache"))
                    .maxSizeBytes(100 * 1024 * 1024L)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
    }
}