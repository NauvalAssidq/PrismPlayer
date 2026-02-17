package org.android.prismplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.android.prismplayer.data.model.LyricsEntity

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics_cache WHERE songId = :songId")
    fun getLyrics(songId: Long): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLyrics(lyrics: LyricsEntity)
}