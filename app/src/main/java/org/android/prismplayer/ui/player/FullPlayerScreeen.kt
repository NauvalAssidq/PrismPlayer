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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
    val durationLong by audioViewModel.duration.collectAsState()

    val formattedTotalTime = remember(durationLong) {
        val totalSeconds = durationLong / 1000
        val mm = totalSeconds / 60
        val ss = totalSeconds % 60
        "%02d:%02d".format(mm, ss)
    }

    FullPlayerContent(
        song = song,
        queue = queue,
        isPlaying = isPlaying,
        progress = progress,
        currentTime = currentTime,
        totalTime = formattedTotalTime,
        repeatMode = repeatMode,
        isShuffleEnabled = isShuffleEnabled,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onPrev = onPrev,
        onToggleRepeat = onToggleRepeat,
        onToggleShuffle = onToggleShuffle,
        onClose = onClose,
        onQueueItemClick = onQueueItemClick,
        onRemoveFromQueue = onRemoveFromQueue,
        onQueueReorder = onQueueReorder,
        onSeek = { audioViewModel.seekTo(it) },
        onSeekDrag = { audioViewModel.updateDragProgress(it) },
        lyricsContent = { glowColor, onCloseLyrics ->
            LyricSheet(
                viewModel = audioViewModel,
                glowColor = glowColor,
                onClose = onCloseLyrics
            )
        }
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FullPlayerContent(
    song: Song,
    queue: List<QueueItem>,
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
    onQueueReorder: (Int, Int) -> Unit,
    onSeek: (Float) -> Unit,
    onSeekDrag: (Float) -> Unit,
    lyricsContent: @Composable (Color, () -> Unit) -> Unit
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    // Slightly adjust art size for the new bottom deck
    val artWidthFraction = if (config.screenHeightDp < 700) 0.75f else 0.82f

    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }

    BackHandler(enabled = showQueue || showLyrics) {
        showQueue = false
        showLyrics = false
    }

    // --- COLOR LOGIC ---
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(song.songArtUri) {
        bitmap = null
        if (!song.songArtUri.isNullOrEmpty() && !song.songArtUri.startsWith("dummy")) {
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
        animationSpec = tween(1000),
        label = "color"
    )

    val glowColor = remember(rawColor) { PrismaColorUtils.adjustForAccent(rawColor) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.65f),
                            glowColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = with(LocalResources.current.displayMetrics) { widthPixels / 2f },
                            y = with(LocalResources.current.displayMetrics) { heightPixels * 0.45f }
                        ),
                        radius = with(LocalResources.current.displayMetrics) { widthPixels * 0.9f }
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SESSION_ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = glowColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                    Text(
                        "PRISM_OS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Placeholder for balance, or Menu if needed
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. MONITOR (Album Art)
            Box(
                modifier = Modifier
                    .width(screenWidth * artWidthFraction)
                    .aspectRatio(1f)
                    .background(Color(0xFF0A0A0A))
                    .border(1.dp, Color.White.copy(0.15f))
                    .padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!song.songArtUri.isNullOrEmpty() && !song.songArtUri.startsWith("dummy")) {
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
                                .background(Color(0xFF151515)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.MusicNote,
                                    null,
                                    tint = Color.White.copy(0.2f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. METADATA DECK
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = glowColor,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { isLiked = !isLiked },
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, Color.White.copy(0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) glowColor else Color.White.copy(0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. SCRUBBER
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        currentTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        totalTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.3f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                Slider(
                    value = progress,
                    onSeeking = onSeekDrag,
                    onValueChange = onSeek
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. TRANSPORT CONTROLS (Round)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Outlined.Shuffle,
                        null,
                        tint = if (isShuffleEnabled) glowColor else Color.White.copy(0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onPrev) {
                    Icon(Icons.Outlined.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Outlined.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = onToggleRepeat) {
                    val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat
                    val tint = if (repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(0.3f) else glowColor
                    Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. FUNCTION DECK (Bottom Pads)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeckKey(
                    label = "QUEUE",
                    icon = Icons.Outlined.QueueMusic,
                    isActive = showQueue,
                    activeColor = glowColor,
                    modifier = Modifier.weight(1f),
                    onClick = { showQueue = true }
                )

                DeckKey(
                    label = "LYRICS",
                    icon = Icons.Outlined.Subject,
                    isActive = showLyrics,
                    activeColor = glowColor,
                    modifier = Modifier.weight(1f),
                    onClick = { showLyrics = true }
                )

                // MORE KEY (Replaces the top menu)
                DeckKey(
                    label = "OPTS",
                    icon = Icons.Outlined.MoreHoriz,
                    isActive = false,
                    activeColor = glowColor,
                    modifier = Modifier.weight(0.6f),
                    onClick = { /* TODO: Bottom Sheet for More */ }
                )
            }

            // Bottom safe area spacing
            Spacer(Modifier.height(8.dp))
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
                onItemClick = onQueueItemClick,
                onMove = onQueueReorder,
            )
        }

        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            lyricsContent(glowColor) { showLyrics = false }
        }
    }
}

@Composable
fun DeckKey(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .border(
                width = 1.dp,
                color = if (isActive) activeColor else Color.White.copy(0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isActive) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else Color.White.copy(0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isActive) activeColor else Color.White.copy(0.7f),
                letterSpacing = 1.sp
            )
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(4.dp)
                    .background(activeColor, CircleShape)
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF050505,
    heightDp = 800
)
@Composable
fun FullPlayerScreenPreview() {
    val mockSong = Song(
        id = 1L,
        title = "Midnight City",
        artist = "M83",
        albumName = "Hurry Up, We're Dreaming",
        albumId = 1L,
        duration = 243000,
        path = "",
        folderName = "",
        dateAdded = 0L,
        songArtUri = "dummy",
        year = 2011,
        trackNumber = 1,
        dateModified = 0L,
        genre = "Synth-pop"
    )

    MaterialTheme {
        FullPlayerContent(
            song = mockSong,
            queue = emptyList(),
            isPlaying = true,
            progress = 0.4f,
            currentTime = "1:35",
            totalTime = "4:03",
            repeatMode = Player.REPEAT_MODE_OFF,
            isShuffleEnabled = false,
            onPlayPause = {},
            onNext = {},
            onPrev = {},
            onToggleRepeat = {},
            onToggleShuffle = {},
            onClose = {},
            onQueueItemClick = {},
            onRemoveFromQueue = {},
            onQueueReorder = { _, _ -> },
            onSeek = {},
            onSeekDrag = {},
            lyricsContent = { color, _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.Black.copy(0.9f))
                        .border(1.dp, color, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Lyrics Placeholder", color = Color.White)
                }
            }
        )
    }
}