package org.android.prismplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.android.prismplayer.data.model.Playlist
import org.android.prismplayer.data.model.PlaylistEntry

@Dao
interface PlaylistDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun createPlaylist(playlist: Playlist): Long

  @Query("SELECT * FROM playlists ORDER BY name ASC")
  fun getAllPlaylists(): Flow<List<Playlist>>

  @Delete
  suspend fun deletePlaylist(playlist: Playlist)

  @Query("UPDATE playlists SET name = :newName WHERE playlistId = :id")
  suspend fun renamePlaylist(id: Long, newName: String)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun addSongToPlaylist(entry: PlaylistEntry)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun addSongsToPlaylist(entries: List<PlaylistEntry>)

  @Query("SELECT * FROM playlist_entries WHERE playlistId = :playlistId ORDER BY dateAdded ASC")
  fun getEntriesForPlaylist(playlistId: Long): Flow<List<PlaylistEntry>>

  @Query("DELETE FROM playlist_entries WHERE playlistId = :playlistId AND songId = :songId")
  suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

  @Query("SELECT EXISTS(SELECT 1 FROM playlist_entries WHERE playlistId = :playlistId AND songId = :songId)")
  suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
}