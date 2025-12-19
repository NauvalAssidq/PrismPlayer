package org.android.prismplayer.data.source

import org.android.prismplayer.data.dao.SongDao
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.PlayHistory
import org.android.prismplayer.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Handles all interactions with the local Room Database.
 * Team Note: Edit this file if you are changing how data is saved/loaded from the DB.
 */
class LocalLibrarySource(private val songDao: SongDao) {

    // Song CRUD
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun getRecentSongs(): Flow<List<Song>> = songDao.getRecentlyAddedSongs()

    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = songDao.getSongsByAlbum(albumId)

    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)

    suspend fun refreshLibrary(songs: List<Song>) {
        if (songs.isNotEmpty()) {
            songDao.deleteAllSongs()
            songDao.insertSongs(songs)
        }
    }

    suspend fun updateSong(song: Song) = songDao.updateSong(song)

    suspend fun replaceSongEntry(oldId: Long, newSong: Song) {
        songDao.deleteSongById(oldId)
        songDao.insertSongs(listOf(newSong))
    }

    //  Statistics & History 
    suspend fun logPlay(history: PlayHistory) = songDao.logPlay(history)

    fun getListeningHours(): Flow<Float> = songDao.getPlayCount().map { (it * 3.5f) / 60f }

    fun getTopGenreDescription(): Flow<String> = songDao.getTopArtist().map { if (!it.isNullOrBlank()) "Vibe of $it" else "Eclectic" }

    // Album Logic
    fun getAlbumsStream(): Flow<List<Album>> {
        return songDao.getAllSongs().map { songs ->
            songs
                .groupBy { it.albumName.trim() }
                .map { (name, groupedSongs) ->
                    val first = groupedSongs.first()
                    // Determine main artist (handle compilations)
                    val mainArtist = groupedSongs
                        .groupingBy { it.artist }
                        .eachCount()
                        .maxByOrNull { it.value }?.key ?: first.artist

                    val bestYear = groupedSongs.map { it.year }
                        .filter { it > 0 }
                        .maxOrNull() ?: 0

                    Album(
                        id = first.albumId,
                        title = name.ifBlank { "Unknown Album" },
                        artist = mainArtist,
                        coverUri = first.songArtUri,
                        songCount = groupedSongs.size,
                        year = bestYear
                    )
                }
                .sortedBy { it.title }
        }
    }
}

// If unclear, you can ask to me directly (Nauval)