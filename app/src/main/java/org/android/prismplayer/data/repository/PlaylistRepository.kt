package org.android.prismplayer.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.android.prismplayer.data.dao.PlaylistDao
import org.android.prismplayer.data.model.Playlist
import org.android.prismplayer.data.model.PlaylistEntry
import org.android.prismplayer.data.model.Song

class PlaylistRepository(private val playlistDao: PlaylistDao) {

  val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

  suspend fun createPlaylist(name: String): Long {
    return playlistDao.createPlaylist(Playlist(name = name))
  }

  suspend fun deletePlaylist(playlist: Playlist) {
    playlistDao.deletePlaylist(playlist)
  }

  suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
    val entry = PlaylistEntry(playlistId = playlistId, songId = songId)
    playlistDao.addSongToPlaylist(entry)
  }

  suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
    val entries = songIds.map { PlaylistEntry(playlistId = playlistId, songId = it) }
    playlistDao.addSongsToPlaylist(entries)
  }

  fun getSongsInPlaylist(playlistId: Long, allLibrarySongs: List<Song>): Flow<List<Song>> {
    return playlistDao.getEntriesForPlaylist(playlistId).map { entries ->
      entries.mapNotNull { entry ->
        allLibrarySongs.find { it.id == entry.songId }
      }
    }
  }

  suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
    playlistDao.removeSongFromPlaylist(playlistId, songId)
  }

  suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean {
    return playlistDao.isSongInPlaylist(playlistId, songId)
  }

  fun getPlaylistCovers(playlistId: Long, allLibrarySongs: List<Song>): Flow<List<String>> {
    return playlistDao.getEntriesForPlaylist(playlistId).map { entries ->
      entries.mapNotNull { entry ->
        allLibrarySongs.find { it.id == entry.songId }?.songArtUri
      }
        .distinct()
        .take(4)
    }
  }
}