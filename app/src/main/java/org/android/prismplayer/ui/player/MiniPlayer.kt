package org.android.prismplayer.ui.player

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    // Extract Color Logic
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
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 0.dp)
            .height(60.dp)
            .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        backgroundColor,
                        Color(0xFF181818)
                    )
                )
            )
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 12.dp), // Tighter right padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3. FIXED IMAGE SIZE: Fits perfectly inside 60dp height
            AsyncImage(
                model = song.songArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp) // Full height of the player
                    .aspectRatio(1f)
                    .background(Color(0xFF202020))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 4. REFINED TYPOGRAPHY
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium, // Smaller than bodyLarge
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall, // Smaller than bodyMedium
                    color = Color.White.copy(0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying)
                                Icons.Rounded.Pause
                            else
                                Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        null,
                        tint = Color.White.copy(0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.5.dp),
            color = Color.White,
            trackColor = Color.Transparent,
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
            isPlaying = true,
            progress = 0.45f,
            onTogglePlay = {},
            onClick = {}
        )
    }
}