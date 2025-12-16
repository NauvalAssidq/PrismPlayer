package org.android.prismplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem

@Composable
fun ArtistScreen(
    state: ArtistUiState,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onShufflePlay: (List<Song>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF1DB954),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ArtistHeader(
                        artistName = state.artistName,
                        artUri = state.heroArtUri,
                        stats = "${state.albums.size} Albums • ${state.songs.size} Songs",
                        onBack = onBack
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { onShufflePlay(state.songs) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DB954),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Rounded.Shuffle, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Shuffle", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (state.songs.isNotEmpty())
                                    onSongClick(state.songs.first(), state.songs)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF252525),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Play")
                        }
                    }
                }

                if (state.albums.isNotEmpty()) {
                    item {
                        Text(
                            "Albums",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.albums) { album ->
                                ArtistAlbumCard(album, onClick = { onAlbumClick(album.id) })
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }

                if (state.songs.isNotEmpty()) {
                    item {
                        Text(
                            "Popular Songs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    items(state.songs) { song ->
                        val isCurrent = currentSong?.id == song.id
                        SongListItem(
                            song = song,
                            isActive = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            index = null,
                            onClick = { onSongClick(song, state.songs) },
                            onMoreClick = { /* Handle More */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistHeader(
    artistName: String,
    artUri: String?,
    stats: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        if (artUri != null) {
            AsyncImage(
                model = artUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .drawForegroundGradient()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF151515))
                    .drawForegroundGradient()
            )
        }

        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(0.3f), CircleShape)
        ) {
            Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF202020))
                    .border(2.dp, Color.White.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (artUri != null) {
                    AsyncImage(
                        model = artUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Rounded.Person,
                        null,
                        tint = Color.White.copy(0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = artistName,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stats,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.7f)
            )
        }
    }
}

@Composable
fun ArtistAlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = album.coverUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF202020))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.year.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

private fun Modifier.drawForegroundGradient(): Modifier = this.then(
    Modifier.background(
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF050505).copy(alpha = 0.6f),
                Color(0xFF050505)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    )
)