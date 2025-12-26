package org.android.prismplayer.ui.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.android.prismplayer.MainActivity
import org.android.prismplayer.R
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.player.manager.EqManager
import org.android.prismplayer.ui.utils.AudioSessionHolder
import org.android.prismplayer.ui.utils.PlaybackSessionStore
import org.android.prismplayer.ui.widget.PrismWidgetProvider

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var sessionStore: PlaybackSessionStore

    // CACHE: Lightweight reference to avoid rebuilding queue from Player
    private var currentQueue: List<Song> = emptyList()

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        sessionStore = PlaybackSessionStore(this)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        AudioSessionHolder.updateSessionId(player.audioSessionId)
        EqManager.setupEqualizer(applicationContext, player.audioSessionId)

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                AudioSessionHolder.updateSessionId(audioSessionId)
                EqManager.release()
                EqManager.setupEqualizer(applicationContext, audioSessionId)
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("PlaybackService", "Error: ${error.message}")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (player.duration != C.TIME_UNSET) saveCurrentState()
                updateWidgetUI()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) saveCurrentState()
                updateWidgetUI()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    saveCurrentState()
                    updateWidgetUI()
                }
            }
        })

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_FULL_PLAYER", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .build()
                .apply {
                    setSmallIcon(R.mipmap.ic_launcher)
                }
        )

        restoreSession()
        updateWidgetUI()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PrismWidgetProvider.ACTION_PLAY_PAUSE -> {
                if (player.playbackState == Player.STATE_IDLE && player.mediaItemCount > 0) player.prepare()
                if (player.isPlaying) player.pause() else player.play()
            }
            PrismWidgetProvider.ACTION_NEXT -> if (player.hasNextMediaItem()) player.seekToNext()
            PrismWidgetProvider.ACTION_PREV -> if (player.hasPreviousMediaItem()) player.seekToPrevious()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun restoreSession() {
        val lastState = sessionStore.getLastPlaybackState()

        if (lastState.queue.isNotEmpty()) {
            currentQueue = lastState.queue
            val mediaItems = currentQueue.map { song ->
                val artUri = if (!song.songArtUri.isNullOrEmpty()) Uri.parse(song.songArtUri) else null

                MediaItem.Builder()
                    .setUri(Uri.parse(song.path))
                    .setMediaId(song.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(artUri)
                            .build()
                    )
                    .build()
            }

            var startIndex = lastState.currentIndex
            var startPos = lastState.positionMs

            if (startIndex < 0 || startIndex >= mediaItems.size) {
                startIndex = 0
            }

            if (startPos < 0) {
                startPos = 0
            }

            player.setMediaItems(mediaItems, startIndex, startPos)
            player.prepare()
        } else {
            val lastSong = sessionStore.getLastSong()
            if (lastSong != null) {
                currentQueue = listOf(lastSong)
                val artUri = if (!lastSong.songArtUri.isNullOrEmpty()) Uri.parse(lastSong.songArtUri) else null

                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(lastSong.path ?: ""))
                    .setMediaId(lastSong.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(lastSong.title)
                            .setArtist(lastSong.artist)
                            .setArtworkUri(artUri)
                            .build()
                    )
                    .build()
                player.setMediaItem(mediaItem)

                val lastPos = sessionStore.getLastPlaybackState().positionMs
                if (lastPos > 0) player.seekTo(lastPos)
            }
        }
    }

    private fun saveCurrentState() {
        val mediaItem = player.currentMediaItem ?: return
        val metadata = mediaItem.mediaMetadata

        val validDuration = if (player.duration != C.TIME_UNSET && player.duration > 0) {
            player.duration
        } else {
            sessionStore.getLastSong()?.duration ?: 0L
        }

        val currentSong = Song(
            id = mediaItem.mediaId.toLongOrNull() ?: -1L,
            title = metadata.title.toString(),
            artist = metadata.artist.toString(),
            path = mediaItem.localConfiguration?.uri.toString(),
            songArtUri = metadata.artworkUri?.toString() ?: "",
            duration = validDuration,
            albumName = "", albumId = 0, folderName = "", dateAdded = 0,
            year = 0, trackNumber = 0, dateModified = 0, genre = ""
        )

        val freshQueue = ArrayList<Song>()
        for (i in 0 until player.mediaItemCount) {
            val item = player.getMediaItemAt(i)
            val meta = item.mediaMetadata

            var safeId = item.mediaId.toLongOrNull() ?: -1L
            if (safeId == -1L) {
                safeId = item.localConfiguration?.uri.toString().hashCode().toLong()
            }

            freshQueue.add(Song(
                id = safeId,
                title = meta.title.toString(),
                artist = meta.artist.toString(),
                path = item.localConfiguration?.uri.toString(),
                songArtUri = meta.artworkUri?.toString() ?: "",
                duration = 0L,
                albumName = "", albumId = 0, folderName = "", dateAdded = 0,
                year = 0, trackNumber = 0, dateModified = 0, genre = ""
            ))
        }

        sessionStore.saveCurrentSong(currentSong, 0, 0)
        sessionStore.savePosition(player.currentPosition)
        sessionStore.saveQueueState(freshQueue, player.currentMediaItemIndex, player.shuffleModeEnabled, player.repeatMode)
        currentQueue = freshQueue
    }

    private fun updateWidgetUI() {
        val mediaItem = player.currentMediaItem
        val metadata = mediaItem?.mediaMetadata
        val title = metadata?.title?.toString() ?: ""
        val artist = metadata?.artist?.toString() ?: ""
        var bitmap: Bitmap? = null
        var duration = player.duration


        if (duration <= 0 || duration == C.TIME_UNSET) {
            duration = sessionStore.getLastSong()?.duration ?: 0L
        }

        if (metadata?.artworkData != null) {
            bitmap = BitmapFactory.decodeByteArray(metadata.artworkData, 0, metadata.artworkData!!.size)
        } else if (metadata?.artworkUri != null) {
            try {
                contentResolver.openInputStream(metadata.artworkUri!!)?.use {
                    bitmap = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception)
            { /* Silent fail */ }
        }

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } else if (bitmap!!.width > 256 || bitmap!!.height > 256) {
            bitmap = Bitmap.createScaledBitmap(bitmap!!, 256, 256, true)
        }

        PrismWidgetProvider.pushUpdate(applicationContext, title, artist, player.isPlaying, bitmap)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.let {
            it.pause()
            it.stop()
        }
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        EqManager.release()
        super.onDestroy()
    }
}