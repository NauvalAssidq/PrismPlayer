package org.android.prismplayer.data.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.android.prismplayer.data.dao.LyricsDao
import org.android.prismplayer.data.model.LyricsEntity
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.network.RetrofitClient

/**
 * Handles fetching and caching lyrics.
 * Team Note: Edit this if you are changing the Lyrics API or caching logic.
 */
class LyricsSource(private val lyricsDao: LyricsDao) {

    suspend fun getCached(songId: Long): LyricsEntity? = withContext(Dispatchers.IO) {
        lyricsDao.getLyrics(songId)
    }

    suspend fun fetchFromNetwork(song: Song): LyricsEntity? = withContext(Dispatchers.IO) {
        try {
            val durationSeconds = (song.duration / 1000).toInt()
            val response = RetrofitClient.api.getLyrics(
                artistName = song.artist,
                trackName = song.title,
                albumName = song.albumName,
                duration = durationSeconds
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val entity = LyricsEntity(
                    songId = song.id,
                    plainLyrics = body.plainLyrics,
                    syncedLyrics = body.syncedLyrics
                )
                lyricsDao.saveLyrics(entity)
                return@withContext entity
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}