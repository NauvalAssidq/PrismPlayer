package org.android.prismplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.core.net.toUri

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val albumName: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val folderName: String,
    val dateAdded: Long,
    val songArtUri: String?,
    val year: Int,
    val trackNumber: Int,
    val genre: String = "Unknown",
    val dateModified: Long = 0
)