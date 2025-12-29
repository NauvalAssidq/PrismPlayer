package org.android.prismplayer.data.source

import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.android.prismplayer.data.model.Song
import java.io.File

/**
 * Handles all interactions with the Android System (MediaStore, File System, Scanning).
 * Team Note: Edit this file if you are fixing scanning bugs or MediaStore queries.
 */
class SystemAudioSource(private val context: Context) {

    suspend fun scanAndFetchSongs(folderPaths: List<String>): List<Song> = withContext(Dispatchers.IO) {
        // 1. Force the system to scan the files in our folders first
        val filesToScan = folderPaths.flatMap { folder ->
            File(folder).walkTopDown()
                .filter { it.isFile && it.extension.lowercase() in setOf("mp3", "m4a", "flac", "wav", "opus", "ogg", "aac") }
                .map { it.absolutePath }
                .toList()
        }.toTypedArray()

        MediaScannerConnection.scanFile(context, filesToScan, null, null)
        delay(500) // Give MediaStore a moment to index

        // 2. Prepare for query
        val authorizedPaths = folderPaths.map { it.trimEnd('/') }
        val genreMap = fetchGenreMap()
        val songsFound = mutableListOf<Song>()

        // 3. Query MediaStore
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.DATE_MODIFIED
        )

        try {
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
                        songsFound.add(
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
                                // Updated to the stable URI format you requested
                                songArtUri = "content://media/external/audio/media/$id/albumart",
                                year = if (yearCol != -1) c.getInt(yearCol) else 0,
                                trackNumber = if (trackCol != -1) c.getInt(trackCol) else 0,
                                dateModified = c.getLong(dateModCol),
                                genre = genreMap[id] ?: "Unknown"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        return@withContext songsFound
    }

    suspend fun fetchSingleSongFromSystem(songId: Long): Song? = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.DATE_MODIFIED
        )

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Audio.Media._ID} = ?",
                arrayOf(songId.toString()),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    // Fetch genre for this single song
                    val genre = fetchGenreForSingleSong(songId)

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
                        // Updated URI format
                        songArtUri = "content://media/external/audio/media/$songId/albumart",
                        year = cursor.runCatching { getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)) }.getOrElse { 0 },
                        trackNumber = cursor.runCatching { getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)) }.getOrElse { 0 },
                        dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)),
                        genre = genre
                    )
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return@withContext null
    }

    private fun fetchGenreMap(): Map<Long, String> {
        val map = HashMap<Long, String>()
        try {
            val genreProjection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
            context.contentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genreProjection, null, null, null)?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
                while (c.moveToNext()) {
                    val genreId = c.getLong(idCol)
                    val genreName = c.getString(nameCol) ?: continue
                    val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                    context.contentResolver.query(membersUri, arrayOf(MediaStore.Audio.Media._ID), null, null, null)?.use { mc ->
                        val mIdCol = mc.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        while (mc.moveToNext()) map[mc.getLong(mIdCol)] = genreName
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return map
    }

    private fun fetchGenreForSingleSong(songId: Long): String {
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
        } catch (e: Exception) { /* Ignore */ }
        return "Unknown"
    }


// Refactored from using the albumArtUri to songArtUri by accessing it via filepath

    suspend fun scanAudioFolders(): List<org.android.prismplayer.data.model.FolderItem> {
        return withContext(Dispatchers.IO) {
            val folderMap = mutableMapOf<String, Int>()
            val projection = arrayOf(MediaStore.Audio.Media.DATA)
            val selection = "${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%' AND ${MediaStore.Audio.Media.DURATION} >= 10000"
            try {
                context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)?.use { cursor ->
                    val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    while (cursor.moveToNext()) {
                        val fullPath = cursor.getString(pathCol)
                        if (File(fullPath).exists()) {
                            File(fullPath).parentFile?.absolutePath?.let { folderMap[it] = folderMap.getOrDefault(it, 0) + 1 }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            folderMap.map { (path, count) ->
                val name = File(path).name
                val isImportant = name.contains("Music", true) || name.contains("Download", true) || count > 5
                org.android.prismplayer.data.model.FolderItem(name, path, count, isImportant)
            }.sortedByDescending { it.count }
        }
    }
}