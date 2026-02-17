package org.android.prismplayer.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.EqPreset
import org.android.prismplayer.data.model.QueueItem
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository
import org.android.prismplayer.ui.player.manager.LyricsManager
import org.android.prismplayer.ui.player.manager.QueueManager
import org.android.prismplayer.ui.player.manager.EqManager
import org.android.prismplayer.ui.player.manager.VisualizerManager
import org.android.prismplayer.ui.service.PlaybackService
import org.android.prismplayer.ui.utils.AudioSessionHolder
import org.android.prismplayer.ui.utils.PlaybackSessionStore
import androidx.media3.common.C
import org.android.prismplayer.data.local.PrismDatabase
import org.android.prismplayer.data.repository.PlaylistRepository
import org.android.prismplayer.data.model.Playlist

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    // --- Managers ---
    private val queueManager = QueueManager()
    private val lyricsManager = LyricsManager()

    // --- Player Reference ---
    private var player: Player? = null
    private var isSeeking = false

    // --- State Delegation (UI observes these) ---
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


    // --- Progress State (Kept in VM for loop efficiency) ---
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // --- Visualizer ---
    private val visualizerManager = VisualizerManager()
    val visualizerData = visualizerManager.visualizerData

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val database = PrismDatabase.getDatabase(application)
    private val playlistRepository = PlaylistRepository(database.playlistDao())

    // --- [NEW] Playlist State ---
    // Expose playlists as a StateFlow for the UI to observe
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
            while (true) {
                if (player?.isPlaying == true && !isSeeking) {
                    _currentTime.value = player?.currentPosition ?: 0L

                    val realDuration = player?.duration ?: C.TIME_UNSET
                    if (realDuration > 0) {
                        _duration.value = realDuration
                    }

                    val calcDuration = _duration.value.coerceAtLeast(1)
                    _progress.value = (player?.currentPosition?.toFloat() ?: 0f) / calcDuration
                }
                delay(100)
            }
        }

        viewModelScope.launch {
            playlistRepository.allPlaylists.collect { playlists ->
                val fav = playlists.find { it.name == FAVORITES_NAME }
                if (fav != null) {
                    favoritesPlaylistId = fav.playlistId
                    observeFavorites(fav.playlistId)
                } else {
                    createPlaylist(FAVORITES_NAME)
                }
            }
        }

        viewModelScope.launch {
            playlistRepository.allPlaylists.collect { list ->
                _playlists.value = list
            }
        }
    }

    // --- Configuration ---
    fun setRepository(repo: MusicRepository) {
        lyricsManager.setRepository(repo)
    }

    fun setLibrary(songs: List<Song>) {
        queueManager.setLibrary(songs)

        if (player == null || player?.mediaItemCount == 0) {
            if (songs.isNotEmpty()) {
                // queueManager.setQueue(songs)
            }
        } else {
            // Log: "Service is active, skipping library default load to preserve state"
        }
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

                // Lyrics reset on song change
                val song = queueManager.currentSong.value
                if (song != null) {
                    lyricsManager.initializeLyrics(song, viewModelScope)
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

    // --- Forwarded Actions ---

    // Queue Actions
    fun playSong(song: Song, contextList: List<Song> = emptyList()) {
        player?.let { queueManager.playSong(it, song, contextList) }
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

    // Controls
    fun togglePlayPause() { player?.let { queueManager.togglePlayPause(it) } }
    fun toggleShuffle() { player?.let { queueManager.toggleShuffle(it) } }
    fun toggleRepeat() { player?.let { queueManager.toggleRepeat(it) } }
    fun skipNext() { player?.let { queueManager.skipNext(it) } }
    fun skipPrev() { player?.let { queueManager.skipPrev(it) } }
    fun toggleAutoplay() = queueManager.toggleAutoplay()

    // Seeking
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

    fun setupEqualizer(audioSessionId: Int) {
    }

    // Lyrics Actions
    fun initializeLyrics(song: Song) = lyricsManager.initializeLyrics(song, viewModelScope)
    fun fetchLyricsOnline() {
        val song = currentSong.value ?: return
        lyricsManager.fetchLyricsOnline(song, viewModelScope)
    }

    // Playlist Action
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
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

    // Optional: Delete playlist
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean {
        return playlistRepository.isSongInPlaylist(playlistId, songId)
    }

    private fun observeFavorites(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.getSongsInPlaylist(playlistId, emptyList()) // Pass empty list just to get IDs flow if your repo supports it, OR:
            // We need a way to just get IDs from the DAO for efficiency,
            // but for now, let's use the existing flow and map it.
            // *NOTE: You might need to update Repository to allow passing 'allSongs' or use a simpler DAO call.*
            // For now, let's assume we pass the current library:

            // BETTER APPROACH: Let's just watch the DB entries directly in DAO (if possible),
            // but sticking to your Repository:
            database.playlistDao().getEntriesForPlaylist(playlistId).collect { entries ->
                _likedSongIds.value = entries.map { it.songId }.toSet()
            }
        }
    }

    fun toggleLike(song: Song) {
        val favId = favoritesPlaylistId ?: return
        val isLiked = _likedSongIds.value.contains(song.id)

        viewModelScope.launch {
            if (isLiked) {
                playlistRepository.removeSongFromPlaylist(favId, song.id)
            } else {
                database.playlistDao().addSongToPlaylist(
                    org.android.prismplayer.data.model.PlaylistEntry(
                        playlistId = favId,
                        songId = song.id
                    )
                )
            }
        }
    }

    // Visualizer
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
            // Sync state immediately
            visualizerManager.setPlaying(player?.isPlaying == true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        visualizerManager.stop()
    }
}