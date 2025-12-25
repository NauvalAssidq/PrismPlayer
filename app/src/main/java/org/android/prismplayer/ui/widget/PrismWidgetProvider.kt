package org.android.prismplayer.ui.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.palette.graphics.Palette
import org.android.prismplayer.MainActivity
import org.android.prismplayer.R
import org.android.prismplayer.ui.service.PlaybackService
import org.android.prismplayer.ui.utils.PlaybackSessionStore

class PrismWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // 1. Initialize Store
        val store = PlaybackSessionStore(context)
        val lastMetadata = store.getLastMetadata()

        for (appWidgetId in appWidgetIds) {
            // 2. Check if we have history to restore
            if (lastMetadata.title != "PRISM PLAYER" && lastMetadata.title.isNotEmpty()) {
                // RESTORED STATE (Paused) -> Opens Player
                restoreLastState(context, appWidgetManager, appWidgetId, lastMetadata)
            } else {
                // EMPTY STATE (Fresh) -> Opens Home
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY_PAUSE, ACTION_NEXT, ACTION_PREV -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = intent.action
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    companion object {
        private const val TAG = "PrismWidget"
        const val ACTION_PLAY_PAUSE = "org.android.prismplayer.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "org.android.prismplayer.ACTION_NEXT"
        const val ACTION_PREV = "org.android.prismplayer.ACTION_PREV"

        const val ACTION_OPEN_PLAYER = "org.android.prismplayer.OPEN_PLAYER"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_prism_player)

            views.setTextViewText(R.id.widget_title, "PRISM PLAYER")
            views.setTextViewText(R.id.widget_artist, "READY")
            views.setViewVisibility(R.id.widget_no_sig_text, View.VISIBLE)

            val defaultBg = createGradientBitmap(Color.BLACK, Color.BLACK)
            views.setImageViewBitmap(R.id.widget_bg_gradient, defaultBg)

            val defaultAccent = 0xFFD71921.toInt()
            updateCorners(views, defaultAccent)

            val openIntent = Intent(context, MainActivity::class.java)
            val openPending = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.widget_bg_gradient, openPending)
            views.setOnClickPendingIntent(R.id.widget_title, openPending)
            views.setOnClickPendingIntent(R.id.widget_artist, openPending)
            views.setOnClickPendingIntent(R.id.widget_album_art, openPending)
            views.setOnClickPendingIntent(R.id.widget_no_sig_text, openPending)

            views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingIntent(context, ACTION_PREV))
            views.setOnClickPendingIntent(R.id.widget_btn_play, getPendingIntent(context, ACTION_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingIntent(context, ACTION_NEXT))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


        private fun restoreLastState(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            metadata: PlaybackSessionStore.LastMetadata
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_prism_player)

            // 1. Set Text
            views.setTextViewText(R.id.widget_title, metadata.title.uppercase())
            views.setTextViewText(R.id.widget_artist, metadata.artist.uppercase())
            views.setViewVisibility(R.id.widget_no_sig_text, View.GONE)

            // 2. Set Art (Using URI directly is faster for cold start)
            if (metadata.artUri != null) {
                views.setImageViewUri(R.id.widget_album_art, Uri.parse(metadata.artUri))
            } else {
                views.setImageViewResource(R.id.widget_album_art, android.R.drawable.ic_menu_gallery)
            }

            // 3. Set Colors (From Storage - No Palette calculation needed!)
            val bgBitmap = createGradientBitmap(metadata.bgColor, 0xFF000000.toInt())
            views.setImageViewBitmap(R.id.widget_bg_gradient, bgBitmap)
            updateCorners(views, metadata.accentColor)

            // 4. Set Controls (Paused state)
            views.setImageViewResource(R.id.widget_btn_play, android.R.drawable.ic_media_play)
            views.setInt(R.id.widget_btn_play, "setColorFilter", Color.WHITE)

            // 5. Intent: OPEN PLAYER (Request Code 100)
            val playerIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_PLAYER
                putExtra("EXPAND_PLAYER", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val playerPending = PendingIntent.getActivity(
                context, 100, playerIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.widget_bg_gradient, playerPending)
            views.setOnClickPendingIntent(R.id.widget_title, playerPending)
            views.setOnClickPendingIntent(R.id.widget_artist, playerPending)
            views.setOnClickPendingIntent(R.id.widget_album_art, playerPending)

            views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingIntent(context, ACTION_PREV))
            views.setOnClickPendingIntent(R.id.widget_btn_play, getPendingIntent(context, ACTION_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingIntent(context, ACTION_NEXT))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        @SuppressLint("RemoteViewLayout")
        fun pushUpdate(context: Context, title: String, artist: String, isPlaying: Boolean, albumArt: Bitmap?, song: org.android.prismplayer.data.model.Song? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, PrismWidgetProvider::class.java))
            if (ids.isEmpty()) return

            if (title.isEmpty()) {
                for (id in ids) updateAppWidget(context, appWidgetManager, id)
                return
            }

            // --- 1. CALCULATE COLORS FIRST ---
            var accentColor = 0xFFD71921.toInt()
            var bgColor = 0xFF000000.toInt()

            if (albumArt != null) {
                val palette = Palette.from(albumArt).generate()
                accentColor = palette.getLightVibrantColor(palette.getVibrantColor(0xFFD71921.toInt()))
                bgColor = palette.getVibrantColor(palette.getDominantColor(0xFF000000.toInt()))
            }

            // --- 2. SAVE TO STORAGE (For next reboot) ---
            if (song != null) {
                val store = PlaybackSessionStore(context)
                store.saveCurrentSong(song, accentColor, bgColor)
            }

            // --- 3. RENDER WIDGET ---
            val views = RemoteViews(context.packageName, R.layout.widget_prism_player)
            views.setTextViewText(R.id.widget_title, title.uppercase())
            views.setTextViewText(R.id.widget_artist, artist.uppercase())

            if (albumArt != null) {
                views.setImageViewBitmap(R.id.widget_album_art, albumArt)
                views.setViewVisibility(R.id.widget_no_sig_text, View.GONE)
            } else {
                views.setImageViewResource(R.id.widget_album_art, android.R.drawable.ic_menu_gallery)
                views.setViewVisibility(R.id.widget_no_sig_text, View.VISIBLE)
            }

            val bgBitmap = createGradientBitmap(bgColor, 0xFF000000.toInt())
            views.setImageViewBitmap(R.id.widget_bg_gradient, bgBitmap)
            updateCorners(views, accentColor)

            val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            views.setImageViewResource(R.id.widget_btn_play, playIcon)
            views.setInt(R.id.widget_btn_play, "setColorFilter", Color.WHITE)

            // Intent: OPEN PLAYER
            val playerIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_PLAYER
                putExtra("EXPAND_PLAYER", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val playerPending = PendingIntent.getActivity(
                context, 100, playerIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.widget_bg_gradient, playerPending)
            views.setOnClickPendingIntent(R.id.widget_title, playerPending)
            views.setOnClickPendingIntent(R.id.widget_artist, playerPending)
            views.setOnClickPendingIntent(R.id.widget_album_art, playerPending)

            views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingIntent(context, ACTION_PREV))
            views.setOnClickPendingIntent(R.id.widget_btn_play, getPendingIntent(context, ACTION_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingIntent(context, ACTION_NEXT))

            appWidgetManager.updateAppWidget(ids, views)
        }

        private fun updateCorners(views: RemoteViews, color: Int) {
            views.setInt(R.id.widget_corner_tr, "setColorFilter", color)
            views.setInt(R.id.widget_corner_tl, "setColorFilter", color)
            views.setInt(R.id.widget_corner_br, "setColorFilter", color)
            views.setInt(R.id.widget_corner_bl, "setColorFilter", color)
        }

        private fun getPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, PrismWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun createGradientBitmap(startColor: Int, endColor: Int): Bitmap {
            val width = 400
            val height = 100
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val shader = LinearGradient(0f, 0f, width.toFloat(), 0f, startColor, endColor, Shader.TileMode.CLAMP)
            val paint = Paint().apply { this.shader = shader }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            return bitmap
        }
    }
}