package org.android.prismplayer.ui.utils

import android.R.attr.id
import android.content.ContentUris
import android.net.Uri

object SongArtHelper {
    private val ART_CONTENT_URI = Uri.parse("content://media/external/audio/media/$id/albumart")

    fun getUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(ART_CONTENT_URI, albumId)
    }
}