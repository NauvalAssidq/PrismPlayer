package org.android.prismplayer.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.local.PrismDatabase
import org.android.prismplayer.data.model.EqPreset
import org.android.prismplayer.data.model.Playlist
import org.android.prismplayer.data.model.PlaylistEntry
import org.android.prismplayer.data.model.QueueItem
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository
import org.android.prismplayer.data.repository.PlaylistRepository
import org.android.prismplayer.ui.player.manager.EqManager
import org.android.prismplayer.ui.player.manager.LyricsManager
import org.android.prismplayer.ui.player.manager.QueueManager
import org.android.prismplayer.ui.player.manager.VisualizerManager
import org.android.prismplayer.ui.service.PlaybackService
import org.android.prismplayer.ui.utils.AudioSessionHolder
import org.android.prismplayer.ui.utils.PlaybackSessionStore

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val queueManager = QueueManager()
    private val lyricsManager = LyricsManager()

    private var player: Player? = null
    private var isSeeking = false

    val queue = queueManager.queue
    val currentSong = queueManager.currentSong
    val isPlaying = queueManager.isPlaying
    val repeatMode = queueManager.repeatMode
    val isShuffleEnabled = queueManager.isShuffleEnabled
    val isAutoplayEnabled = queueManager.isAutoplayEnabled

    val eqBands = EqManager.eqBands
    val eqEnabled = EqManager.eqEnabled
    val presets = EqManager.presets
    val currentPresetName = EqManager.currentPresetName
    val bassStrength = EqManager.bassStrength
    val virtStrength = EqManager.virtStrength
    val gainStrength = EqManager.gainStrength

    val lyricState = lyricsManager.lyricsState
    val syncedLyrics = lyricsManager.syncedLyrics

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val visualizerManager = VisualizerManager()
    val visualizerData = visualizerManager.visualizerData

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val database = PrismDatabase.getDatabase(application)
    private val playlistRepository = PlaylistRepository(database.playlistDao())
    private val musicRepository by lazy { (getApplication() as PrismApplication).repository }

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val FAVORITES_NAME = "Favorites"
    private var favoritesPlaylistId: Long? = null

    private val _likedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val likedSongIds: StateFlow<Set<Long>> = _likedSongIds.asStateFlow()

    init {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                player = controller
                setupPlayerListener(controller)
                queueManager.syncQueueFromController(controller)
                queueManager.syncPlayerState(controller)
                controller.currentMediaItem?.let {
                    queueManager.syncCurrentSong(it)
                }

                val store = PlaybackSessionStore(application)
                val savedState = store.getLastSong()

                val currentDuration = controller.duration
                val safeDuration = if (currentDuration > 0 && currentDuration != C.TIME_UNSET) {
                    currentDuration
                } else {
                    savedState?.duration ?: 0L
                }
                _duration.value = safeDuration

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())

        viewModelScope.launch {
            while (isActive) {
                val activePlayer = player
                if (activePlayer != null && activePlayer.isPlaying && !isSeeking) {
                    _currentTime.value = activePlayer.currentPosition
                    val realDuration = activePlayer.duration
                    if (realDuration > 0 && realDuration != C.TIME_UNSET) {
                        _duration.value = realDuration
                    }
                    val calcDuration = _duration.value.coerceAtLeast(1)
                    _progress.value = activePlayer.currentPosition.toFloat() / calcDuration
                }
                delay(100)
            }
        }

        viewModelScope.launch {
            playlistRepository.allPlaylists.collect { list ->
                _playlists.value = list
                val fav = list.find { it.name == FAVORITES_NAME }
                if (fav != null) {
                    if (favoritesPlaylistId != fav.playlistId) {
                        favoritesPlaylistId = fav.playlistId
                        observeFavorites(fav.playlistId)
                    }
                } else {
                    createPlaylist(FAVORITES_NAME)
                }
            }
        }
    }

    fun setRepository(repo: MusicRepository) {
        lyricsManager.setRepository(repo)
    }

    fun setLibrary(songs: List<Song>) {
        queueManager.setLibrary(songs)
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                queueManager.syncPlayerState(player)
                visualizerManager.setPlaying(isPlaying)
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                AudioSessionHolder.updateSessionId(audioSessionId)
                setupVisualizer()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                queueManager.syncCurrentSong(mediaItem)
                val song = queueManager.currentSong.value
                if (song != null) {
                    lyricsManager.initializeLyrics(song, viewModelScope)
                    musicRepository?.let { repo ->
                        viewModelScope.launch {
                            repo.recordPlay(song)
                        }
                    }
                } else {
                    lyricsManager.reset()
                }
                queueManager.checkAutoPlay(player)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                queueManager.syncPlayerState(player)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                queueManager.syncPlayerState(player)
            }
        })
    }

    fun playSong(song: Song, contextList: List<Song> = emptyList()) {
        player?.let { queueManager.playSong(it, song, contextList) }
        musicRepository?.let { repo ->
            viewModelScope.launch {
                repo.recordPlay(song)
            }
        }
    }

    fun addToQueue(song: Song) {
        player?.let { queueManager.addToQueue(it, song) }
    }

    fun playNext(song: Song) {
        player?.let { queueManager.playNext(it, song) }
    }

    fun removeSongFromQueue(song: Song) {
        player?.let { queueManager.removeSongFromQueue(it, song) }
    }

    fun playQueueItem(item: QueueItem) {
        player?.let { queueManager.playQueueItem(it, item) }
    }

    fun moveQueueItem(from: Int, to: Int) {
        player?.let { queueManager.moveQueueItem(it, from, to) }
    }

    fun togglePlayPause() { player?.let { queueManager.togglePlayPause(it) } }
    fun toggleShuffle() { player?.let { queueManager.toggleShuffle(it) } }
    fun toggleRepeat() { player?.let { queueManager.toggleRepeat(it) } }
    fun skipNext() { player?.let { queueManager.skipNext(it) } }
    fun skipPrev() { player?.let { queueManager.skipPrev(it) } }
    fun toggleAutoplay() = queueManager.toggleAutoplay()

    fun seekTo(fraction: Float) {
        val p = player ?: return
        isSeeking = true
        val duration = p.duration.coerceAtLeast(1)
        val targetTime = (duration * fraction).toLong()
        _progress.value = fraction
        _currentTime.value = targetTime
        p.seekTo(targetTime)
        viewModelScope.launch {
            delay(500)
            isSeeking = false
        }
    }

    fun updateDragProgress(fraction: Float) {
        isSeeking = true
        val duration = player?.duration?.coerceAtLeast(1) ?: 1L
        _currentTime.value = (duration * fraction).toLong()
        _progress.value = fraction
    }

    fun toggleEq(enabled: Boolean) = EqManager.toggleEq(enabled)
    fun setBassStrength(value: Float) = EqManager.setBassStrength(value)
    fun setVirtStrength(value: Float) = EqManager.setVirtStrength(value)
    fun setGainStrength(value: Float) = EqManager.setGainStrength(value)
    fun setEqBandLevel(bandId: Short, level: Short) = EqManager.setBandLevelUserAction(bandId, level, viewModelScope)
    fun saveCustomPreset(name: String) = EqManager.saveCustomPreset(name)
    fun deleteCustomPreset(preset: EqPreset) = EqManager.deleteCustomPreset(preset)
    fun applyPreset(preset: EqPreset) = EqManager.applyPreset(preset, viewModelScope)

    fun setupEqualizer(audioSessionId: Int) {}

    fun initializeLyrics(song: Song) = lyricsManager.initializeLyrics(song, viewModelScope)

    fun fetchLyricsOnline() {
        val song = currentSong.value ?: return
        lyricsManager.fetchLyricsOnline(song, viewModelScope)
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun createPlaylistWithSongs(name: String, songs: List<Song>) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name)
            songs.forEach { song ->
                playlistRepository.addSongToPlaylist(playlistId, song.id)
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long, allSongs: List<Song>): kotlinx.coroutines.flow.Flow<List<Song>> {
        return playlistRepository.getSongsInPlaylist(playlistId, allSongs)
    }

    fun getPlaylistCovers(playlistId: Long, allSongs: List<Song>): kotlinx.coroutines.flow.Flow<List<String>> {
        return playlistRepository.getPlaylistCovers(playlistId, allSongs)
    }

    fun deletePlaylist(playlist: Playlist, onShowToast: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
            onShowToast?.invoke("PLAYLIST DELETED: ${playlist.name.uppercase()}")
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long, onShowToast: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
            onShowToast?.invoke("TRACK REMOVED FROM PLAYLIST")
        }
    }

    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean {
        return playlistRepository.isSongInPlaylist(playlistId, songId)
    }

    private var favoritesObserverJob: kotlinx.coroutines.Job? = null

    private fun observeFavorites(playlistId: Long) {
        favoritesObserverJob?.cancel()
        favoritesObserverJob = viewModelScope.launch {
            database.playlistDao().getEntriesForPlaylist(playlistId).collect { entries ->
                _likedSongIds.value = entries.map { it.songId }.toSet()
            }
        }
    }

    fun toggleLike(song: Song, onShowToast: ((String) -> Unit)? = null) {
        val favId = favoritesPlaylistId ?: return
        val isLiked = _likedSongIds.value.contains(song.id)

        viewModelScope.launch {
            if (isLiked) {
                playlistRepository.removeSongFromPlaylist(favId, song.id)
                onShowToast?.invoke("REMOVED FROM FAVORITES")
            } else {
                database.playlistDao().addSongToPlaylist(
                    PlaylistEntry(
                        playlistId = favId,
                        songId = song.id
                    )
                )
                onShowToast?.invoke("ADDED TO FAVORITES")
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun setupVisualizer() {
        val sessionId = player?.audioSessionId ?: 0
        if (sessionId == 0) return

        val permission = ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.RECORD_AUDIO
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            visualizerManager.start(sessionId)
            visualizerManager.setPlaying(player?.isPlaying == true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        visualizerManager.stop()
    }
}