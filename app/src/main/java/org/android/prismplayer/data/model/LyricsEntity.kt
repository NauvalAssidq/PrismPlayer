package org.android.prismplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_cache")
data class LyricsEntity(
    @PrimaryKey val songId: Long,
    val plainLyrics: String?,
    val syncedLyrics: String?,
    val source: String = "LRCLIB",
    val lastUpdated: Long = System.currentTimeMillis()
)