package org.android.prismplayer.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.android.prismplayer.data.dao.LyricsDao
import org.android.prismplayer.data.dao.SongDao
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.LyricsEntity
import org.android.prismplayer.data.model.PlayHistory
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.source.LocalLibrarySource
import org.android.prismplayer.data.source.LyricsSource
import org.android.prismplayer.data.source.SystemAudioSource

class MusicRepository(
    private val context: Context,
    private val songDao: SongDao,
    private val lyricsDao: LyricsDao
) {
    // Sources (Initialized here to avoid changing constructor signature for now)
    private val systemSource = SystemAudioSource(context)
    private val localSource = LocalLibrarySource(songDao)
    private val lyricsSource = LyricsSource(lyricsDao)

    // Main Library Functions

    fun getAllSongs(): Flow<List<Song>> = localSource.getAllSongs()

    suspend fun importSongsFromFolders(folderPaths: List<String>) {
        val songs = systemSource.scanAndFetchSongs(folderPaths)
        localSource.refreshLibrary(songs)
    }

    fun getAlbums(): Flow<List<Album>> = localSource.getAlbumsStream()

    // Stats & History

    fun getQuickPlaySongs(): Flow<List<Song>> = localSource.getRecentSongs()

    fun getTotalListeningHours(): Flow<Float> = localSource.getListeningHours()

    fun getTopGenre(): Flow<String> = localSource.getTopGenreDescription()

    suspend fun recordPlay(song: Song) {
        val history = PlayHistory(
            songId = song.id,
            artistName = song.artist,
            playDuration = song.duration,
            timestamp = System.currentTimeMillis()
        )
        localSource.logPlay(history)
    }

    // Individual Song Operations

    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = localSource.getSongsByAlbum(albumId)
    suspend fun updateSong(song: Song) = localSource.updateSong(song)
    suspend fun getSongById(id: Long): Song? {
        return localSource.getSongById(id) ?: systemSource.fetchSingleSongFromSystem(id)
    }

    fun getSongsByAlbumName(name: String): Flow<List<Song>> = localSource.getSongsByAlbumName(name)

    suspend fun updateSongIdAndMetadata(oldId: Long, finalId: Long, updatedSong: Song) {
        val finalSong = updatedSong.copy(
            id = finalId,
            songArtUri = "content://media/external/audio/media/$finalId/albumart"
        )
        localSource.replaceSongEntry(oldId, finalSong)
    }

    suspend fun getCachedLyrics(songId: Long): LyricsEntity? = lyricsSource.getCached(songId)

    suspend fun fetchLyrics(song: Song): LyricsEntity? = lyricsSource.fetchFromNetwork(song)
}