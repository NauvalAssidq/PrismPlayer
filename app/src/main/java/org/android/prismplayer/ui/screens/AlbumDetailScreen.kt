package org.android.prismplayer.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem
import org.android.prismplayer.ui.utils.PrismaColorUtils
import org.android.prismplayer.ui.utils.rememberImmersiveColor

@Composable
fun AlbumDetailScreen(
    albumId: Long,
    albumName: String,
    artistName: String,
    artUri: String?,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAlbum: (List<Song>) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onSongMoreClick: (Song) -> Unit,

    ) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(artUri) {
        if (!artUri.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(artUri)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                bitmap = result.drawable.toBitmap()
            }
        }
    }

    val rawColor = rememberImmersiveColor(bitmap)

    val backgroundColor by animateColorAsState(
        targetValue = remember(rawColor) { PrismaColorUtils.adjustForBackground(rawColor) },
        animationSpec = tween(1000),
        label = "bgGlow"
    )

    val accentColor by animateColorAsState(
        targetValue = remember(rawColor) { PrismaColorUtils.adjustForAccent(rawColor) },
        animationSpec = tween(1000),
        label = "accentGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.7f),
                            backgroundColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(0.2f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .background(Color.Black.copy(0.2f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Rounded.MoreVert, null, tint = Color.White)
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .shadow(40.dp, RoundedCornerShape(16.dp), spotColor = accentColor)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF202020))
                        ) {
                            if (artUri != null) {
                                AsyncImage(
                                    model = artUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(0.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${songs.size} Songs • ${calculateTotalDuration(songs)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(0.4f),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { onPlayAlbum(songs) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1DB954),
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(
                                    start = 12.dp,
                                    end = 24.dp,
                                    top = 16.dp,
                                    bottom = 16.dp
                                ),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Play", fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { onPlayAlbum(songs.shuffled()) },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.White.copy(0.1f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Shuffle, null, tint = Color.White)
                            }
                        }
                    }
                }

                itemsIndexed(songs) { index, song ->
                    val isActive = currentSong?.id == song.id
                    SongListItem(
                        song = song,
                        isActive = isActive,
                        isPlaying = isPlaying,
                        index = index + 1,
                        showDuration = true,
                        onClick = { onSongClick(song, songs) },
                        onMoreClick = { onSongMoreClick(song) }
                    )
                }

                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}


private fun calculateTotalDuration(songs: List<Song>): String {
    val totalSeconds = songs.sumOf { it.duration } / 1000
    val minutes = totalSeconds / 60
    return "${minutes} min"
}

@SuppressLint("DefaultLocale")
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFF050505,
    heightDp = 800
)
@Composable
fun PreviewAlbumDetail() {
    val mockSongs = listOf(
        Song(1, "Midnight City", "M83", "Hurry Up, We're Dreaming", 1, 240000, "", "", 0, null, year = 1, trackNumber = 1, genre = "Pop"),
        Song(2, "Wait", "M83", "Hurry Up, We're Dreaming", 1, 180000, "", "", 0, null, year = 1, trackNumber = 1, genre = "Pop"),
        Song(3, "Reunion", "M83", "Hurry Up, We're Dreaming", 1, 210000, "", "", 0, null, year = 1, trackNumber = 1, genre = "Pop"),
        Song(4, "Steve McQueen", "M83", "Hurry Up, We're Dreaming", 1, 200000, "", "", 0, null, year = 1, trackNumber = 1, genre = "Pop")
    )

    MaterialTheme {
        AlbumDetailScreen(
            albumId = 1,
            albumName = "Hurry Up, We're Dreaming",
            artistName = "M83",
            artUri = null,
            songs = mockSongs,
            currentSong = mockSongs[0],
            isPlaying = true,
            onBack = {},
            onPlayAlbum = {},
            onSongClick = { _, _ -> },
            onSongMoreClick = {}
        )
    }
}