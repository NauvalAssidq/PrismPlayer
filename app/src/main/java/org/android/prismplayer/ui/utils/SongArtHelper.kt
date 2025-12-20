package org.android.prismplayer.ui.utils

import android.content.ContentUris
import android.net.Uri

object SongArtHelper {
    private val BASE_MEDIA_URI = Uri.parse("content://media/external/audio/media")

    fun getUri(songId: Long): Uri {
        // Result: content://media/external/audio/media/{songId}/albumart
        return ContentUris.withAppendedId(BASE_MEDIA_URI, songId)
            .buildUpon()
            .appendPath("albumart")
            .build()
    }
}