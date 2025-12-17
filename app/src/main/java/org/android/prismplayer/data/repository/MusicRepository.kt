package org.android.prismplayer.data.repository

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.android.prismplayer.data.dao.StatsDao
import org.android.prismplayer.data.dao.LyricsDao
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.LyricsEntity
import org.android.prismplayer.data.model.PlayHistory
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.network.RetrofitClient
import java.io.File

class MusicRepository(
    private val context: Context,
    private val statsDao: StatsDao,
    private val lyricsDao: LyricsDao
) {

    fun getAllSongs(): Flow<List<Song>> = statsDao.getAllSongs()

    suspend fun importSongsFromFolders(folderPaths: List<String>) = withContext(Dispatchers.IO) {
        val songsToInsert = mutableListOf<Song>()
        val authorizedPaths = folderPaths.map { it.trimEnd('/') }
        val genreMap = HashMap<Long, String>()
        try {
            val genreProjection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
            context.contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genreProjection, null, null, null
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
                while (c.moveToNext()) {
                    val genreId = c.getLong(idCol)
                    val genreName = c.getString(nameCol) ?: continue
                    val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                    context.contentResolver.query(membersUri, arrayOf(MediaStore.Audio.Media._ID), null, null, null)?.use { mc ->
                        val mIdCol = mc.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        while (mc.moveToNext()) genreMap[mc.getLong(mIdCol)] = genreName
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.DATE_MODIFIED
            )
            context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val pathCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val yearCol = c.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackCol = c.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val dateModCol = c.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)

                while (c.moveToNext()) {
                    val path = c.getString(pathCol)
                    val duration = c.getLong(durCol)
                    val isInside = authorizedPaths.any { path.startsWith(it) }
                    val isAudio = path.substringAfterLast('.', "").lowercase() in setOf("mp3", "m4a", "flac", "wav", "opus", "ogg", "aac")

                    if (isInside && isAudio && duration >= 0 && File(path).exists()) {
                        val id = c.getLong(idCol)
                        val genre = genreMap[id] ?: "Unknown"
                        songsToInsert.add(
                            Song(
                                id = id,
                                title = c.getString(titleCol) ?: File(path).nameWithoutExtension,
                                artist = c.getString(artistCol).let { if (it == "<unknown>") "Unknown Artist" else it } ?: "Unknown",
                                albumName = c.getString(albumCol) ?: "Unknown Album",
                                albumId = c.getLong(albumIdCol),
                                duration = duration,
                                path = path,
                                folderName = File(path).parentFile?.name ?: "Unknown",
                                dateAdded = c.getLong(dateCol),
                                songArtUri = "content://media/external/audio/albumart/${c.getLong(albumIdCol)}",
                                year = if (yearCol != -1) c.getInt(yearCol) else 0,
                                trackNumber = if (trackCol != -1) c.getInt(trackCol) else 0,
                                dateModified = c.getLong(dateModCol),
                                genre = genre
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        if (songsToInsert.isNotEmpty()) {
            statsDao.deleteAllSongs()
            statsDao.insertSongs(songsToInsert)
        }
    }


    fun getAlbums(): Flow<List<Album>> {
        return statsDao.getAllSongs().map { songs ->
            songs
                .groupBy { it.albumName.trim() }
                .map { (name, groupedSongs) ->
                    val first = groupedSongs.first()

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

    fun getQuickPlaySongs(): Flow<List<Song>> = statsDao.getRecentlyAddedSongs()

    fun getTotalListeningHours(): Flow<Float> = statsDao.getPlayCount().map { (it * 3.5f) / 60f }

    fun getTopGenre(): Flow<String> = statsDao.getTopArtist().map { if (!it.isNullOrBlank()) "Vibe of $it" else "Eclectic" }

    suspend fun recordPlay(song: Song) {
        statsDao.logPlay(PlayHistory(songId = song.id, artistName = song.artist, playDuration = song.duration, timestamp = System.currentTimeMillis()))
    }

    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = statsDao.getSongsByAlbum(albumId)

    suspend fun updateSong(song: Song) = statsDao.updateSong(song)

    suspend fun getSongById(id: Long): Song? {
        val localSong = statsDao.getSongById(id)
        if (localSong != null) return localSong
        return getSongFromMediaStore(id)
    }

    private suspend fun getSongFromMediaStore(songId: Long): Song? = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(songId.toString())

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val genre = getGenreForSong(songId)

                    return@use Song(
                        id = songId,
                        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist",
                        albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album",
                        albumId = albumId,
                        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                        path = path,
                        folderName = File(path).parentFile?.name ?: "Unknown",
                        dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)),
                        songArtUri = "content://media/external/audio/albumart/$albumId",
                        year = cursor.runCatching { getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)) }.getOrElse { 0 },
                        trackNumber = cursor.runCatching { getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)) }.getOrElse { 0 },
                        dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)),
                        genre = genre
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun getGenreForSong(songId: Long): String {
        try {
            val genreUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
            context.contentResolver.query(genreUri, arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME), null, null, null)?.use { genreCursor ->
                while (genreCursor.moveToNext()) {
                    val genreId = genreCursor.getLong(0)
                    val genreName = genreCursor.getString(1)
                    val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                    context.contentResolver.query(membersUri, arrayOf(MediaStore.Audio.Media._ID), "${MediaStore.Audio.Media._ID} = ?", arrayOf(songId.toString()), null)?.use {
                        if (it.moveToFirst()) return genreName
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return "Unknown"
    }

    suspend fun getCachedLyrics(songId: Long): LyricsEntity? {
        return withContext(Dispatchers.IO) {
            lyricsDao.getLyrics(songId)
        }
    }

    suspend fun fetchLyrics(song: Song): LyricsEntity? {
        return withContext(Dispatchers.IO) {
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

    suspend fun updateSongIdAndMetadata(
        oldId: Long,
        finalId: Long,
        updatedSong: Song
    ) = withContext(Dispatchers.IO) {

        val finalSong = updatedSong.copy(
            id = finalId,
            songArtUri = updatedSong.songArtUri
                ?: "content://media/external/audio/albumart/${updatedSong.albumId}"
        )

        statsDao.deleteSongById(oldId)
        statsDao.insertSongs(listOf(finalSong))
    }
}