package org.android.prismplayer.data.model

data class SearchResult(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<String> = emptyList()
)