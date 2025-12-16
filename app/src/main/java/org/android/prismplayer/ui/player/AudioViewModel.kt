package org.android.prismplayer.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.ContentUris
import android.media.audiofx.Equalizer
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.EqBand
import org.android.prismplayer.data.model.EqPreset
import org.android.prismplayer.data.model.QueueItem
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.service.PlaybackService
import org.android.prismplayer.ui.utils.EqPreferences
import java.util.Locale

class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private var player: Player? = null

    // Library Pool (For Autoplay)
    private var librarySongs: List<Song> = emptyList()

    // State Flow
    private val _queue = MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue.asStateFlow()

    private val _currentQueueId = MutableStateFlow<String?>(null)

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    // Control
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    // Equalizer
    private var equalizer: Equalizer? = null
    private val eqPrefs = EqPreferences(application)

    private val _eqBands = MutableStateFlow<List<EqBand>>(emptyList())
    val eqBands = _eqBands.asStateFlow()

    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled = _eqEnabled.asStateFlow()

    private val _presets = MutableStateFlow<List<EqPreset>>(emptyList())
    val presets = _presets.asStateFlow()

    private val _currentPresetName = MutableStateFlow("Custom")
    val currentPresetName = _currentPresetName.asStateFlow()

    private var isSeeking = false

    private var progressJob: Job? = null


    init {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                player = controller
                setupPlayerListener(controller)

                _isPlaying.value = controller.isPlaying
                _repeatMode.value = controller.repeatMode
                _isShuffleEnabled.value = controller.shuffleModeEnabled
                controller.currentMediaItem?.let { syncCurrentSongFromMediaItem(it) }

                setupEqualizer(0)

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

    fun setLibrary(songs: List<Song>) {
        librarySongs = songs
    }

    private fun setupPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncCurrentSongFromMediaItem(mediaItem)
                ensureQueueContinuity()
            }
            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffleEnabled.value = shuffleModeEnabled
            }
            override fun onPlaybackStateChanged(state: Int) { }
        })
    }

    private fun ensureQueueContinuity() {
        val p = player ?: return
        val currentQ = _queue.value
        if (currentQ.isEmpty() || librarySongs.isEmpty()) return
        val currentIndex = p.currentMediaItemIndex

        val threshold = currentQ.size - 2

        if (currentIndex >= threshold) {
            val currentId = _currentSong.value?.id
            val candidates = librarySongs.filter { it.id != currentId }

            if (candidates.isNotEmpty()) {
                val newSongs = candidates.asSequence().shuffled().take(5).toList()
                val newItems = newSongs.map { QueueItem(song = it) }
                _queue.value = currentQ + newItems
                p.addMediaItems(newItems.map { it.toMediaItem() })
            }
        }
    }

    private fun syncCurrentSongFromMediaItem(mediaItem: MediaItem?) {
        if (mediaItem == null) return

        val queueId = mediaItem.mediaId

        val item = _queue.value.find { it.queueId == queueId }

        if (item != null) {
            _currentSong.value = item.song
            _currentQueueId.value = item.queueId
        }
    }


    fun playSong(song: Song, contextList: List<Song> = emptyList()) {
        val p = player ?: return

        val safeList = contextList.ifEmpty { listOf(song) }
        val newQueue = safeList.map { QueueItem(song = it) }

        _queue.value = newQueue

        val targetItem = newQueue.firstOrNull { it.song.id == song.id } ?: newQueue.first()
        _currentSong.value = targetItem.song
        _currentQueueId.value = targetItem.queueId

        val mediaItems = newQueue.map { it.toMediaItem() }
        val startIndex = newQueue.indexOf(targetItem).coerceAtLeast(0)

        p.setMediaItems(mediaItems, startIndex, 0L)
        p.prepare()
        p.play()

        if (contextList.isNotEmpty()) {
            setLibrary(contextList)
        }
    }

    fun commitReordering() {
        val p = player ?: return
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return
        val currentQueueId = _currentQueueId.value
        val newIndex = currentQ.indexOfFirst { it.queueId == currentQueueId }.coerceAtLeast(0)
        val currentPos = p.currentPosition
        p.setMediaItems(currentQ.map { it.toMediaItem() }, newIndex, currentPos)
    }

    fun playQueueItem(item: QueueItem) {
        val index = _queue.value.indexOfFirst { it.queueId == item.queueId }

        if (index != -1) {
            player?.seekTo(index, 0L)
            if (player?.isPlaying == false) {
                player?.play()
            }
        }
    }

    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) p.pause() else p.play()
    }

    fun toggleShuffle() {
        val p = player ?: return
        p.shuffleModeEnabled = !p.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val p = player ?: return
        val nextMode = when (p.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        p.repeatMode = nextMode
    }

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
        val dragTime = (duration * fraction).toLong()
        _currentTime.value = dragTime
        _progress.value = fraction
    }

    fun skipNext() {
        player?.let { if (it.hasNextMediaItem()) it.seekToNextMediaItem() }
    }

    fun skipPrev() {
        player?.let {
            if (it.currentPosition > 3000) it.seekTo(0)
            else if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem()
        }
    }

    fun updateQueue(newSongs: List<Song>) {
        val p = player ?: return
        if (newSongs.isEmpty()) return

        val newQueueItems = newSongs.map { QueueItem(song = it) }
        _queue.value = newQueueItems
        val currentId = _currentSong.value?.id
        val newIndex = newSongs.indexOfFirst { it.id == currentId }.coerceAtLeast(0)

        val targetItem = newQueueItems[newIndex]
        _currentSong.value = targetItem.song
        _currentQueueId.value = targetItem.queueId

        p.setMediaItems(newQueueItems.map { it.toMediaItem() }, newIndex, p.currentPosition)
    }

    fun reorderQueue(newQueue: List<QueueItem>) {
        val p = player ?: return
        val currentQ = _queue.value

        _queue.value = newQueue

        if (currentQ.size == newQueue.size && currentQ.toSet() == newQueue.toSet()) {
            val currentQueueId = _currentQueueId.value
            val newIndex = newQueue.indexOfFirst { it.queueId == currentQueueId }.coerceAtLeast(0)
            val currentPos = p.currentPosition

            p.setMediaItems(newQueue.map { it.toMediaItem() }, newIndex, currentPos)
        } else {
            val currentQueueId = _currentQueueId.value
            val newIndex = newQueue.indexOfFirst { it.queueId == currentQueueId }.coerceAtLeast(0)
            p.setMediaItems(newQueue.map { it.toMediaItem() }, newIndex, p.currentPosition)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val currentQ = _queue.value.toMutableList()
        val p = player ?: return

        if (fromIndex in currentQ.indices && toIndex in currentQ.indices) {
            val item = currentQ.removeAt(fromIndex)
            currentQ.add(toIndex, item)
            _queue.value = currentQ
            p.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun addToQueue(song: Song) {
        val newItem = QueueItem(song = song)
        _queue.value = _queue.value + newItem
        player?.addMediaItem(newItem.toMediaItem())
    }

    fun removeSongFromQueue(song: Song) {
        val currentQueue = _queue.value.toMutableList()
        val index = currentQueue.indexOfFirst { it.song.id == song.id }

        if (index != -1) {
            player?.removeMediaItem(index)
            currentQueue.removeAt(index)
            _queue.value = currentQueue
        }
    }

    private fun getMimeType(path: String): String {
        return when (path.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
            "mp3" -> "audio/mpeg"
            "m4a", "aac" -> "audio/mp4"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "ogg", "oga" -> "audio/ogg"
            "opus" -> "audio/opus"
            else -> "audio/mpeg"
        }
    }

    private fun QueueItem.toMediaItem(): MediaItem {
        val mediaUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id
        )

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.albumName)
            .setArtworkUri(song.songArtUri?.toUri())
            .build()

        val mimeType = getMimeType(song.path)

        return MediaItem.Builder()
            .setMediaId(queueId)
            .setUri(mediaUri)
            .setMimeType(mimeType)
            .setMediaMetadata(metadata)
            .build()
    }

    //Equalizer

    fun setupEqualizer(audioSessionId: Int) {
        try {
            if (equalizer != null) return
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq
            val savedEnabledState = eqPrefs.isEqEnabled
            eq.enabled = savedEnabledState
            _eqEnabled.value = savedEnabledState
            loadPresets(equalizer?.numberOfBands?.toInt() ?: 5)

            loadEqBands(eq)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadEqBands(eq: Equalizer) {
        val min = eq.bandLevelRange[0]
        val max = eq.bandLevelRange[1]
        val numBands = eq.numberOfBands

        val bands = mutableListOf<EqBand>()

        for (i in 0 until numBands) {
            val idx = i.toShort()
            val hardwareLevel = eq.getBandLevel(idx)
            val savedLevel = eqPrefs.getBandLevel(idx, hardwareLevel)
            if (savedLevel != hardwareLevel) {
                eq.setBandLevel(idx, savedLevel)
            }

            bands.add(
                EqBand(
                    id = idx,
                    centerFreq = eq.getCenterFreq(idx),
                    level = savedLevel,
                    minLevel = min,
                    maxLevel = max
                )
            )
        }
        _eqBands.value = bands
    }

    fun setEqBandLevel(bandId: Short, level: Short) {
        val currentBands = _eqBands.value.toMutableList()
        val index = currentBands.indexOfFirst { it.id == bandId }
        if (index != -1) {
            currentBands[index] = currentBands[index].copy(level = level)
            _eqBands.value = currentBands
        }
        eqPrefs.saveBandLevel(bandId, level)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                equalizer?.setBandLevel(bandId, level)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleEq(isEnabled: Boolean) {
        equalizer?.enabled = isEnabled
        _eqEnabled.value = isEnabled
        eqPrefs.isEqEnabled = isEnabled
    }

    private fun loadPresets(numBands: Int) {
        val flat = EqPreset("Flat", List(numBands) { 0 })
        val bass = EqPreset("Bass", List(numBands) { if(it < 2) 600.toShort() else 0.toShort() })
        val vocal = EqPreset("Vocal",
            List(numBands) { if(it in 1..3) 500.toShort() else -200.toShort() } as List<Short>)
        val treble = EqPreset("Treble", List(numBands) { if(it > 3) 600.toShort() else 0.toShort() })
        val defaultPresets = listOf(flat, bass, vocal, treble)
        val savedPresets = eqPrefs.loadCustomPresets()
        _presets.value = defaultPresets + savedPresets
    }

    fun saveCustomPreset(name: String) {
        val currentLevels = _eqBands.value.map { it.level }
        val newPreset = EqPreset(name, currentLevels)
        val updatedList = _presets.value + newPreset
        _presets.value = updatedList
        _currentPresetName.value = name
        val defaultNames = setOf("Flat", "Bass", "Vocal", "Treble")
        val customPresetsOnly = updatedList.filter { it.name !in defaultNames }
        eqPrefs.saveCustomPresets(customPresetsOnly)
    }

    fun applyPreset(preset: EqPreset) {
        _currentPresetName.value = preset.name
        preset.bandLevels.forEachIndexed { index, level ->
            if (index < _eqBands.value.size) {
                val bandId = _eqBands.value[index].id
                setEqBandLevel(bandId, level)
            }
        }
    }

    override fun onCleared() {
        equalizer?.release()
        super.onCleared()
    }
}