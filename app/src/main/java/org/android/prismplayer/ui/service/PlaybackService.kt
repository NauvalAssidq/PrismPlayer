package org.android.prismplayer.ui.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
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

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val renderersFactory = DefaultRenderersFactory(this)
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

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
                android.util.Log.e("PlaybackService", "Playback error: ${error.message}", error)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                saveCurrentState()
                updateWidgetUI()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) saveCurrentState()
                updateWidgetUI()
            }
        })

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_FULL_PLAYER", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .build()
            .apply {
                setSmallIcon(R.mipmap.ic_launcher)
            }

        setMediaNotificationProvider(notificationProvider)
        restoreSession()
        updateWidgetUI()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PrismWidgetProvider.ACTION_PLAY_PAUSE -> {
                if (player.playbackState == Player.STATE_IDLE && player.mediaItemCount > 0) {
                    player.prepare()
                }
                if (player.isPlaying) player.pause() else player.play()
            }
            PrismWidgetProvider.ACTION_NEXT -> {
                if (player.hasNextMediaItem()) player.seekToNext()
            }
            PrismWidgetProvider.ACTION_PREV -> {
                if (player.hasPreviousMediaItem()) player.seekToPrevious()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun restoreSession() {
        val store = PlaybackSessionStore(this)
        val lastSong = store.getLastSong()

        if (lastSong != null && player.mediaItemCount == 0) {
            val songPath = lastSong.path ?: ""

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(songPath))
                .setMediaId(lastSong.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(lastSong.title)
                        .setArtist(lastSong.artist)
                        .setArtworkUri(
                            if (!lastSong.songArtUri.isNullOrEmpty()) Uri.parse(lastSong.songArtUri) else null
                        )
                        .build()
                )
                .build()

            player.setMediaItem(mediaItem)

            val lastState = store.getLastPlaybackState()
            if (lastState.positionMs > 0) {
                player.seekTo(lastState.positionMs)
            }

            player.prepare()
        }
    }

    private fun saveCurrentState() {
        val mediaItem = player.currentMediaItem ?: return
        val metadata = mediaItem.mediaMetadata
        val currentPath = mediaItem.localConfiguration?.uri.toString()

        val song = Song(
            id = mediaItem.mediaId.toLongOrNull() ?: -1L,
            title = metadata.title.toString(),
            artist = metadata.artist.toString(),
            path = currentPath,
            songArtUri = metadata.artworkUri?.toString() ?: "",
            duration = player.duration.takeIf { it > 0 } ?: 0L,
            albumName = "",
            albumId = 0,
            folderName = "",
            dateAdded = 0,
            year = 0,
            trackNumber = 0,
            dateModified = 0,
            genre = ""
        )

        val store = PlaybackSessionStore(this)
        store.saveCurrentSong(song, 0, 0)
        store.savePosition(player.currentPosition)
    }

    private fun updateWidgetUI() {
        val mediaItem = player.currentMediaItem
        val metadata = mediaItem?.mediaMetadata
        val title = metadata?.title?.toString() ?: "" // Empty string handles "Idle" better in my Widget Provider
        val artist = metadata?.artist?.toString() ?: ""
        var bitmap: android.graphics.Bitmap? = null

        if (metadata?.artworkData != null) {
            bitmap = BitmapFactory.decodeByteArray(metadata.artworkData, 0, metadata.artworkData!!.size)
        }
        if (bitmap == null && metadata?.artworkUri != null) {
            try {
                val inputStream = contentResolver.openInputStream(metadata.artworkUri!!)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) { e.printStackTrace() }
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        }
        if (bitmap != null && (bitmap.width > 256 || bitmap.height > 256)) {
            bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 256, 256, true)
        }

        PrismWidgetProvider.pushUpdate(
            applicationContext,
            title,
            artist,
            player.isPlaying,
            bitmap
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            player.pause()
            player.stop()
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