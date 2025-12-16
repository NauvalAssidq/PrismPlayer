package org.android.prismplayer.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.utils.AlbumArtHelper

@Composable
fun SongListItem(
    song: Song,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    index: Int? = null,
    showDuration: Boolean = false,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    val textColor = if (isActive) Color(0xFF1DB954) else Color.White
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val stableArtUri = remember(song.albumId) {
        AlbumArtHelper.getUri(song.albumId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (index != null) 20.dp else 16.dp,
                vertical = if (index != null) 12.dp else 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (index != null) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isActive) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF1DB954),
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { if (isPlaying) alpha = pulseAlpha }
                    )
                } else {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.5f)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF252525))
            ) {
                if (!song.songArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = stableArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF1DB954),
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { alpha = pulseAlpha }
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (isActive) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = if (isActive) Color(0xFF1DB954) else Color.White.copy(0.2f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                            .graphicsLayer { if (isActive) alpha = pulseAlpha }
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (index == null) Spacer(Modifier.height(4.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showDuration) {
            Spacer(Modifier.width(16.dp))
            Text(
                text = formatDuration(song.duration),
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) Color(0xFF1DB954) else Color.White.copy(0.4f)
            )
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                Icons.Rounded.MoreVert,
                null,
                tint = Color.White.copy(0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Preview(showBackground = false)
@Composable
fun SongListItemPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050505))
        ) {
            SongListItem(
                song = Song(
                    id = 1L,
                    title = "Starboy",
                    artist = "The Weeknd",
                    duration = 230_000L,
                    albumName = "Starboy",
                    albumId = 10L,
                    path = "/storage/emulated/0/Music/TheWeeknd/Starboy.mp3",
                    folderName = "Music",
                    dateAdded = System.currentTimeMillis() / 1000,
                    songArtUri = null,
                    year = 2016,
                    trackNumber = 1,
                    genre = "Pop",
                    dateModified = System.currentTimeMillis() / 1000
                ),
                isActive = true,
                isPlaying = true,
                index = 1,
                showDuration = true,
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}