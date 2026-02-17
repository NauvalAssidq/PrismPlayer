package org.android.prismplayer.ui.player.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.LyricsEntity
import org.android.prismplayer.data.model.LyricsState
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository
import org.android.prismplayer.ui.utils.LrcParser
import org.android.prismplayer.ui.utils.LyricLine

class LyricsManager {
    private val _lyricsState = MutableStateFlow<LyricsState>(LyricsState.Idle)
    val lyricsState: StateFlow<LyricsState> = _lyricsState.asStateFlow()

    private val _syncedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val syncedLyrics: StateFlow<List<LyricLine>> = _syncedLyrics.asStateFlow()

    private var repository: MusicRepository? = null

    fun setRepository(repo: MusicRepository) {
        this.repository = repo
    }

    fun reset() {
        _lyricsState.value = LyricsState.Idle
        _syncedLyrics.value = emptyList()
    }

    fun initializeLyrics(song: Song, scope: CoroutineScope) {
        scope.launch {
            val repo = repository ?: return@launch
            val cached = repo.getCachedLyrics(song.id)

            if (cached != null) {
                parseAndSetLyrics(cached)
            } else {
                reset()
            }
        }
    }

    fun fetchLyricsOnline(song: Song, scope: CoroutineScope) {
        scope.launch {
            _lyricsState.value = LyricsState.Loading

            val repo = repository
            if (repo == null) {
                _lyricsState.value = LyricsState.Error("Repo missing")
                return@launch
            }

            val result = repo.fetchLyrics(song)

            if (result != null) {
                parseAndSetLyrics(result)
            } else {
                _lyricsState.value = LyricsState.Error("Lyrics not found")
            }
        }
    }

    private fun parseAndSetLyrics(entity: LyricsEntity) {
        val parsedLines = LrcParser.parse(entity.syncedLyrics)
        _syncedLyrics.value = parsedLines

        if (parsedLines.isNotEmpty()) {
            _lyricsState.value = LyricsState.Success(
                staticLyrics = entity.plainLyrics ?: "No text lyrics",
                isSynced = true
            )
        } else {
            _lyricsState.value = LyricsState.Success(
                staticLyrics = entity.plainLyrics ?: "No lyrics found",
                isSynced = false
            )
        }
    }
}