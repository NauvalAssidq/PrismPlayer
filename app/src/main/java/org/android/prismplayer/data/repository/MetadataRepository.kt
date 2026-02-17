package org.android.prismplayer.data.repository

import org.android.prismplayer.data.model.Song

interface MetadataRepository {
    /**
     * Writes metadata (Text + Image) to the audio file safely.
     */
    suspend fun writeTags(song: Song, pickedArtUri: String?): Boolean

    /**
     * Reads metadata directly from the file disk.
     */
    suspend fun readTags(song: Song): Song
}