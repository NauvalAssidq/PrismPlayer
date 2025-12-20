package org.android.prismplayer.ui.player

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.utils.PrismaColorUtils
import org.android.prismplayer.ui.utils.rememberDominantColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current

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

    val rawColor = rememberDominantColor(bitmap)
    val backgroundColor = remember(rawColor) { PrismaColorUtils.adjustForBackground(rawColor) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.9f),
                        Color(0xFF121212)
                    )
                )
            )
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0A0A0A))
                    .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = song.songArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (bitmap == null) 0.5f else 1f)
                )

                if (bitmap == null) {
                    Text(
                        text = "NO_SIG",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = Color.White.copy(0.3f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = song.artist.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace, // Tech font
                    color = Color.White.copy(0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(Color.White.copy(0.15f))
            )

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        null,
                        tint = Color.White.copy(0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp),
            color = Color.White,
            trackColor = Color.White.copy(0.1f),
            drawStopIndicator = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewMiniPlayer() {
    val mockSong = Song(
        id = 1L,
        title = "Midnight City",
        artist = "M83",
        albumName = "Hurry Up, We're Dreaming",
        albumId = 0L,
        duration = 240000L,
        path = "",
        folderName = "",
        dateAdded = 0L,
        songArtUri = null,
        year = 0,
        trackNumber = 0
    )

    Box(modifier = Modifier.padding(16.dp)) {
        MiniPlayer(
            song = mockSong,
            isPlaying = false,
            progress = 0.45f,
            onTogglePlay = {},
            onClick = {}
        )
    }
}