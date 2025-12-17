package org.android.prismplayer.data.model

sealed class LyricsState {
    object Idle : LyricsState()
    object Loading : LyricsState()
    data class Success(val staticLyrics: String, val isSynced: Boolean) : LyricsState()
    data class Error(val message: String) : LyricsState()
}