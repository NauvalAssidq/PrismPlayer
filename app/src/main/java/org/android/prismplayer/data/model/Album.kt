package org.android.prismplayer.data.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val coverUri: String?,
    val songCount: Int,
    val year: Int
)