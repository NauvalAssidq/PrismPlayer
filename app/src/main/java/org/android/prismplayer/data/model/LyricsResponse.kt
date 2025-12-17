package org.android.prismplayer.data.model

import com.google.gson.annotations.SerializedName

data class LyricsResponse(
    val id: Int,
    val name: String,
    val artistName: String,
    val albumName: String?,
    val duration: Double,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?
)