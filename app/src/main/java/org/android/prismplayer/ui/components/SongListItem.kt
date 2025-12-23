package org.android.prismplayer.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.theme.PrismPlayerTheme
import org.android.prismplayer.ui.utils.SongArtHelper

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
    val nothingRed = Color(0xFFD71921)
    val prismWhite = Color(0xFFFFFFFF)
    val prismLightGrey = Color(0xFF808080)
    val prismDarkGrey = Color(0xFF121212)

    val textColor = if (isActive) nothingRed else prismWhite
    val subTextColor = if (isActive) nothingRed.copy(0.7f) else prismLightGrey
    val bgColor = if (isActive) prismDarkGrey.copy(0.5f) else Color.Transparent

    val pulseAlpha = if (isActive && isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        alpha
    } else {
        1f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (index != null) {
            Box(modifier = Modifier.width(32.dp)) {
                Text(
                    text = if (isActive) ">>" else String.format("%02d", index),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) nothingRed else prismLightGrey.copy(0.5f),
                    modifier = Modifier.graphicsLayer { alpha = if (isActive && isPlaying) pulseAlpha else 1f }
                )
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF111111))
        ) {
            if (!song.songArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            SongArtHelper.getUri(song.id)
                                .buildUpon()
                                .appendQueryParameter("t", song.dateModified.toString())
                                .build()
                        )
                        .crossfade(false)
                        .size(108, 108)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = if (isActive) 0.5f else 1f }
                )
            } else {
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = prismLightGrey.copy(0.3f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (isActive) {
                if (isPlaying) {
                    AnimatedEqualizer(nothingRed)
                } else {
                    StaticEqualizer(nothingRed)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.artist.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = subTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (!isActive) {
                    Text(
                        text = " // ${formatDuration(song.duration)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = prismLightGrey.copy(0.4f)
                    )
                }
            }
        }

        if (isActive) {
            Text(
                text = if (isPlaying) "RUNNING" else "PAUSED",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = nothingRed,
                modifier = Modifier.graphicsLayer { alpha = if (isPlaying) pulseAlpha else 1f }
            )
        } else {
            IconButton(onClick = onMoreClick) {
                Icon(
                    Icons.Rounded.MoreVert,
                    null,
                    tint = prismLightGrey,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedEqualizer(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar1)
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar2)
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(bar3)
                .background(color)
        )
    }
}

@Composable
private fun StaticEqualizer(color: Color) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(0.4f, 0.8f, 0.6f).forEach { level ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(level)
                    .background(color)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = false)
@Composable
fun SongListItemPreview() {
    PrismPlayerTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
        ) {
            Column {
                SongListItem(
                    song = Song(
                        id = 1L, title = "Starboy", artist = "The Weeknd", duration = 230_000L,
                        albumName = "Starboy", albumId = 0, path = "", folderName = "",
                        dateAdded = 0, songArtUri = "content://media/external/audio/albumart/1",
                        year = 2016, trackNumber = 1, genre = "Pop", dateModified = 0L
                    ),
                    isActive = true,
                    isPlaying = true,
                    index = 1,
                    onClick = {}
                )
                SongListItem(
                    song = Song(
                        id = 2L, title = "I Feel It Coming", artist = "The Weeknd", duration = 250_000L,
                        albumName = "Starboy", albumId = 0, path = "", folderName = "",
                        dateAdded = 0, songArtUri = null, year = 2016, trackNumber = 2,
                        genre = "Pop", dateModified = 0L
                    ),
                    isActive = false,
                    isPlaying = false,
                    index = 2,
                    onClick = {}
                )
                SongListItem(
                    song = Song(
                        id = 3L, title = "Party Monster", artist = "The Weeknd", duration = 245_000L,
                        albumName = "Starboy", albumId = 0, path = "", folderName = "",
                        dateAdded = 0, songArtUri = null, year = 2016, trackNumber = 3,
                        genre = "Pop", dateModified = 0L
                    ),
                    isActive = true,
                    isPlaying = false,
                    index = 3,
                    onClick = {}
                )
            }
        }
    }
}