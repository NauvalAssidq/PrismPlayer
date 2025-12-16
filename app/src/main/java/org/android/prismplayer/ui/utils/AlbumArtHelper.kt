package org.android.prismplayer.ui.utils

import android.content.ContentUris
import android.net.Uri

object AlbumArtHelper {
    private val ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart")

    fun getUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
    }
}