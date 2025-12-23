package org.android.prismplayer.ui.player.manager

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.android.prismplayer.data.model.QueueItem
import org.android.prismplayer.data.model.Song
import java.util.Locale

class QueueManager {
    // --- State ---
    private val _queue = MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentQueueId = MutableStateFlow<String?>(null)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isAutoplayEnabled = MutableStateFlow(false)
    val isAutoplayEnabled: StateFlow<Boolean> = _isAutoplayEnabled.asStateFlow()

    private var librarySongs: List<Song> = emptyList()

    // --- Configuration ---
    fun setLibrary(songs: List<Song>) {
        librarySongs = songs

        if (_queue.value.isNotEmpty() && songs.isNotEmpty()) {
            val enrichedQueue = _queue.value.map { item ->
                val fullSong = songs.find { it.id == item.song.id }
                if (fullSong != null) item.copy(song = fullSong) else item
            }
            _queue.value = enrichedQueue

            val currentId = _currentQueueId.value
            if (currentId != null) {
                val currentItem = enrichedQueue.find { it.queueId == currentId }
                if (currentItem != null) _currentSong.value = currentItem.song
            }
        }
    }

    fun toggleAutoplay() {
        _isAutoplayEnabled.value = !_isAutoplayEnabled.value
    }

    // --- Player Sync Logic ---
    fun syncPlayerState(player: Player) {
        _isPlaying.value = player.isPlaying
        _repeatMode.value = player.repeatMode
        _isShuffleEnabled.value = player.shuffleModeEnabled
    }

    fun syncCurrentSong(mediaItem: MediaItem?) {
        if (mediaItem == null) return

        val queueId = mediaItem.mediaId
        val item = _queue.value.find { it.queueId == queueId }

        if (item != null) {
            _currentSong.value = item.song
            _currentQueueId.value = item.queueId
        }
    }

    fun syncQueueFromController(controller: MediaController) {
        val restoredQueue = mutableListOf<QueueItem>()
        for (i in 0 until controller.mediaItemCount) {
            val mediaItem = controller.getMediaItemAt(i)
            val extraId = mediaItem.mediaMetadata.extras?.getString("SONG_ID")?.toLongOrNull()
            val uriId = mediaItem.requestMetadata.mediaUri?.lastPathSegment?.toLongOrNull()
            val songId = extraId ?: uriId ?: 0L
            val cachedSong = librarySongs.find { it.id == songId }
            val song = if (cachedSong != null) {
                cachedSong
            } else {
                Song(
                    id = songId,
                    title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown",
                    artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
                    albumName = mediaItem.mediaMetadata.albumTitle?.toString() ?: "",
                    songArtUri = mediaItem.mediaMetadata.artworkUri?.toString(),
                    path = mediaItem.requestMetadata.mediaUri?.toString() ?: "",
                    duration = 0L,
                    albumId = 0L,
                    folderName = "",
                    dateAdded = 0L,
                    year = mediaItem.mediaMetadata.recordingYear ?: 0,
                    trackNumber = mediaItem.mediaMetadata.trackNumber ?: 0,
                    genre = mediaItem.mediaMetadata.genre?.toString() ?: "",
                    dateModified = 0L
                )
            }
            restoredQueue.add(QueueItem(queueId = mediaItem.mediaId, song = song))
        }
        _queue.value = restoredQueue
    }

    fun checkAutoPlay(player: Player) {
        if (!_isAutoplayEnabled.value) return

        val currentQ = _queue.value
        if (currentQ.isEmpty() || librarySongs.isEmpty()) return

        val currentIndex = player.currentMediaItemIndex
        val threshold = currentQ.size - 2

        if (currentIndex >= threshold) {
            val currentId = _currentSong.value?.id
            val candidates = librarySongs.filter { it.id != currentId }

            if (candidates.isNotEmpty()) {
                val batchSize = currentQ.size.coerceIn(5, 50)
                val newSongs = candidates.shuffled().take(batchSize)
                val newItems = newSongs.map { QueueItem(song = it) }
                _queue.value = currentQ + newItems
                player.addMediaItems(newItems.map { it.toMediaItem() })
            }
        }
    }

    // --- Actions ---
    fun playSong(player: Player, song: Song, contextList: List<Song> = emptyList()) {
        val safeList = contextList.ifEmpty { listOf(song) }
        val newQueue = safeList.map { QueueItem(song = it) }
        _queue.value = newQueue

        val targetIndex = newQueue.indexOfFirst { it.song.id == song.id }.coerceAtLeast(0)
        val targetItem = newQueue[targetIndex]

        _currentSong.value = targetItem.song
        _currentQueueId.value = targetItem.queueId

        val mediaItems = newQueue.map { it.toMediaItem() }
        player.setMediaItems(mediaItems, targetIndex, 0L)
        player.prepare()
        player.play()

        if (contextList.isNotEmpty()) {
            setLibrary(contextList)
        }
    }

    fun playNext(player: Player, song: Song) {
        val newItem = QueueItem(song = song)
        val currentIndex = player.currentMediaItemIndex
        val nextIndex = currentIndex + 1

        val currentQ = _queue.value.toMutableList()
        currentQ.add(nextIndex.coerceAtMost(currentQ.size), newItem)
        _queue.value = currentQ

        player.addMediaItem(nextIndex.coerceAtMost(player.mediaItemCount), newItem.toMediaItem())
    }

    fun addToQueue(player: Player, song: Song) {
        val newItem = QueueItem(song = song)
        _queue.value = _queue.value + newItem
        player.addMediaItem(newItem.toMediaItem())
    }

    fun removeSongFromQueue(player: Player, song: Song) {
        val currentQueue = _queue.value.toMutableList()
        val index = currentQueue.indexOfFirst { it.song.id == song.id }

        if (index != -1) {
            player.removeMediaItem(index)
            currentQueue.removeAt(index)
            _queue.value = currentQueue
        }
    }

    fun moveQueueItem(player: Player, fromIndex: Int, toIndex: Int) {
        val currentQ = _queue.value.toMutableList()
        if (fromIndex in currentQ.indices && toIndex in currentQ.indices) {
            val item = currentQ.removeAt(fromIndex)
            currentQ.add(toIndex, item)
            _queue.value = currentQ
            player.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun playQueueItem(player: Player, item: QueueItem) {
        val index = _queue.value.indexOfFirst { it.queueId == item.queueId }
        if (index != -1) {
            player.seekTo(index, 0L)
            if (!player.isPlaying) player.play()
        }
    }

    fun togglePlayPause(player: Player) {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun toggleShuffle(player: Player) {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun toggleRepeat(player: Player) {
        val nextMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = nextMode
        _repeatMode.value = nextMode
    }

    fun skipNext(player: Player) {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun skipPrev(player: Player) {
        if (player.currentPosition > 3000) player.seekTo(0)
        else if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
    }

    // --- Helpers ---
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
        val mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        val extras = Bundle().apply {
            putString("SONG_ID", song.id.toString())
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.albumName)
            .setArtworkUri(song.songArtUri?.toUri())
            .setExtras(extras)
            .build()

        return MediaItem.Builder()
            .setMediaId(queueId)
            .setUri(mediaUri)
            .setMimeType(getMimeType(song.path))
            .setMediaMetadata(metadata)
            .build()
    }
}