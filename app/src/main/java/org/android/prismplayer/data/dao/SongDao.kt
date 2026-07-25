package org.android.prismplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.android.prismplayer.data.model.PlayHistory
import org.android.prismplayer.data.model.Song

import androidx.room.Transaction

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 20")
    fun getRecentlyAddedSongs(): Flow<List<Song>>

    @Query("SELECT s.* FROM songs s INNER JOIN play_history h ON s.id = h.songId GROUP BY s.id ORDER BY MAX(h.timestamp) DESC LIMIT :limit")
    fun getRecentlyPlayedSongs(limit: Int = 20): Flow<List<Song>>

    @Insert
    suspend fun logPlay(history: PlayHistory)

    @Query("SELECT COUNT(*) FROM play_history")
    fun getPlayCount(): Flow<Int>

    @Query("SELECT artistName FROM play_history GROUP BY artistName ORDER BY COUNT(*) DESC LIMIT 1")
    fun getTopArtist(): Flow<String?>

    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE albumName = :name")
    fun getSongsByAlbumName(name: String): Flow<List<Song>>

    @Update
    suspend fun updateSong(song: Song)

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT id FROM songs")
    suspend fun getAllSongIds(): List<Long>

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Transaction
    suspend fun replaceAllSongs(songs: List<Song>) {
        deleteAllSongs()
        insertSongs(songs)
    }

    @Query("SELECT path FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongPathById(id: Long): String?

}