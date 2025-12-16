package org.android.prismplayer.ui.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import org.android.prismplayer.data.model.QueueItem
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.Slider
import org.android.prismplayer.ui.utils.PrismaColorUtils
import org.android.prismplayer.ui.utils.rememberDominantColor

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FullPlayerScreen(
    song: Song,
    queue: List<QueueItem> = emptyList(),
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    repeatMode: Int,
    isShuffleEnabled: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onClose: () -> Unit,
    onQueueItemClick: (QueueItem) -> Unit,
    onRemoveFromQueue: (Song) -> Unit,
    onQueueReorder: (Int, Int) -> Unit = { _, _ -> },
    audioViewModel: AudioViewModel,
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp

    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    BackHandler(enabled = showQueue || showLyrics) {
        showQueue = false
        showLyrics = false
    }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(song.songArtUri) {
        bitmap = null
        if (!song.songArtUri.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(song.songArtUri)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                bitmap = result.drawable.toBitmap()
            }
        }
    }

    val rawColor by animateColorAsState(
        targetValue = rememberDominantColor(bitmap),
        animationSpec = tween(1000)
    )

    val glowColor = remember(rawColor) {
        PrismaColorUtils.adjustForAccent(rawColor)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume clicks */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.6f),
                            glowColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = with(LocalResources.current.displayMetrics) { widthPixels / 2f },
                            y = 0f
                        ),
                        radius = with(LocalResources.current.displayMetrics) { heightPixels * 0.7f }
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.6f),
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Library",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.MoreVert, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(screenWidth * 0.85f)
                    .aspectRatio(1f)
                    .shadow(50.dp, RoundedCornerShape(32.dp), spotColor = glowColor)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF121212))
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(32.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!song.songArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.songArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1A1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            null,
                            tint = Color.White.copy(0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.Transparent)))
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Slider(
                    value = progress,
                    onSeeking = { frac ->
                        audioViewModel.updateDragProgress(frac)
                    },
                    onValueChange = { frac ->
                        audioViewModel.seekTo(frac)
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(currentTime, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
                    Text(totalTime, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF202020).copy(0.5f))
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(32.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            null,
                            tint = if (isShuffleEnabled) glowColor else Color.White.copy(0.6f)
                        )
                    }

                    IconButton(onClick = onPrev, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Rounded.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = glowColor)
                            .clip(RoundedCornerShape(24.dp))
                            .background(glowColor)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Rounded.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    IconButton(onClick = onToggleRepeat) {
                        val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                        val tint = if (repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(0.6f) else glowColor
                        Icon(icon, null, tint = tint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconButton(onClick = { showQueue = true }) {
                        Icon(
                            Icons.Rounded.QueueMusic,
                            null,
                            tint = if (showQueue) glowColor else Color.White.copy(0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { /* TODO: Toggle Love */ }) {
                        Icon(
                            Icons.Rounded.FavoriteBorder,
                            null,
                            tint = Color.White.copy(0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                IconButton(onClick = { showLyrics = true }) {
                    Icon(
                        Icons.Rounded.Lyrics,
                        null,
                        tint = if (showLyrics) glowColor else Color.White.copy(0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showQueue,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            QueueSheet(
                queue = queue,
                currentSong = song,
                glowColor = glowColor,
                onClose = { showQueue = false },
                onRemove = onRemoveFromQueue,
                onItemClick = onQueueItemClick as (QueueItem) -> Unit,
                onMove = onQueueReorder,
            )
        }

        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xFF121212))
                    .clickable { showLyrics = false }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 20) showLyrics = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Lyrics Coming Soon", color = Color.White)
            }
        }
    }
}