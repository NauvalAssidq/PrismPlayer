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
                controller.currentMediaItem?.let { queueManager.syncCurrentSong(it) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())

        viewModelScope.launch {
            while (true) {
                if (player?.isPlaying == true && !isSeeking) {
                    _currentTime.value = player?.currentPosition ?: 0L
                    val duration = player?.duration?.coerceAtLeast(1) ?: 1L
                    _progress.value = (player?.currentPosition?.toFloat() ?: 0f) / duration
                }
                delay(100)
            }
        }

    }

    // --- Configuration ---
    fun setRepository(repo: MusicRepository) {
        lyricsManager.setRepository(repo)
    }

    fun setLibrary(songs: List<Song>) {
        queueManager.setLibrary(songs)
    }

    // --- Listener ---
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