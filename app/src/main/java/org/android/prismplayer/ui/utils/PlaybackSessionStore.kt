package org.android.prismplayer.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.android.prismplayer.data.model.Song
import androidx.core.content.edit

class PlaybackSessionStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prism_session_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_LAST_TITLE = "last_title"
        private const val KEY_LAST_ARTIST = "last_artist"
        private const val KEY_LAST_ART_URI = "last_art_uri"
        private const val KEY_COLOR_ACCENT = "last_color_accent"
        private const val KEY_COLOR_BG = "last_color_bg"

        private const val KEY_QUEUE_IDS = "last_queue_ids"
        private const val KEY_CURRENT_INDEX = "last_index"
        private const val KEY_POSITION = "last_position"
        private const val KEY_REPEAT_MODE = "last_repeat"
        private const val KEY_SHUFFLE_MODE = "last_shuffle"
        private const val KEY_MEDIA_PATH = "last_media_path"
        private const val KEY_SONG_ID = "last_song_id"
        private const val KEY_DURATION = "last_duration"
    }


    fun saveCurrentSong(song: Song?, accentColor: Int, bgColor: Int) {
        if (song == null) return
        prefs.edit {
            putString(KEY_LAST_TITLE, song.title)
            putString(KEY_LAST_ARTIST, song.artist)
            putString(KEY_LAST_ART_URI, song.songArtUri)
            putInt(KEY_COLOR_ACCENT, accentColor)
            putInt(KEY_COLOR_BG, bgColor)

            putString(KEY_MEDIA_PATH, song.path)
            putLong(KEY_SONG_ID, song.id)
            putLong(KEY_DURATION, song.duration)
        }
    }

    fun saveQueueState(queue: List<Song>, currentIndex: Int, isShuffle: Boolean, repeatMode: Int) {
        val ids = queue.map { it.id }
        val json = gson.toJson(ids)

        prefs.edit {
            putString(KEY_QUEUE_IDS, json)
            putInt(KEY_CURRENT_INDEX, currentIndex)
            putBoolean(KEY_SHUFFLE_MODE, isShuffle)
            putInt(KEY_REPEAT_MODE, repeatMode)
        }
    }

    fun savePosition(positionMs: Long) {
        prefs.edit { putLong(KEY_POSITION, positionMs) }
    }


    fun getLastMetadata(): LastMetadata {
        return LastMetadata(
            title = prefs.getString(KEY_LAST_TITLE, "PRISM PLAYER") ?: "PRISM PLAYER",
            artist = prefs.getString(KEY_LAST_ARTIST, "READY") ?: "READY",
            artUri = prefs.getString(KEY_LAST_ART_URI, null),
            accentColor = prefs.getInt(KEY_COLOR_ACCENT, 0xFFD71921.toInt()), // Default Red
            bgColor = prefs.getInt(KEY_COLOR_BG, Color.BLACK) // Default Black
        )
    }

    fun getLastPlaybackState(): LastPlaybackState {
        val json = prefs.getString(KEY_QUEUE_IDS, "[]")
        val type = object : TypeToken<List<Long>>() {}.type
        val ids: List<Long> = try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }

        return LastPlaybackState(
            queueIds = ids,
            currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0),
            positionMs = prefs.getLong(KEY_POSITION, 0L),
            isShuffle = prefs.getBoolean(KEY_SHUFFLE_MODE, false),
            repeatMode = prefs.getInt(KEY_REPEAT_MODE, 0) // 0 = REPEAT_OFF
        )
    }

    fun clear() {
        prefs.edit { clear() }
    }

    fun getLastSong(): Song? {
        val path = prefs.getString(KEY_MEDIA_PATH, null) ?: return null
        return Song(
            id = prefs.getLong(KEY_SONG_ID, -1L),
            title = prefs.getString(KEY_LAST_TITLE, "") ?: "",
            artist = prefs.getString(KEY_LAST_ARTIST, "") ?: "",
            path = path,
            duration = prefs.getLong(KEY_DURATION, 0L),
            songArtUri = prefs.getString(KEY_LAST_ART_URI, "") ?: "",
            albumName = "", albumId = 0L, folderName = "",
            dateAdded = 0L, year = 0, trackNumber = 0, dateModified = 0L, genre = ""
        )
    }

    data class LastMetadata(
        val title: String,
        val artist: String,
        val artUri: String?,
        val accentColor: Int,
        val bgColor: Int
    )

    data class LastPlaybackState(
        val queueIds: List<Long>,
        val currentIndex: Int,
        val positionMs: Long,
        val isShuffle: Boolean,
        val repeatMode: Int
    )
}