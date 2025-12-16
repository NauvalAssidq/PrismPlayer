package org.android.prismplayer.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp // Import Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem
import java.util.Calendar

@Composable
fun HomeScreen(
    state: HomeState,
    currentSong: Song? = null,
    isPlaying: Boolean = false,
    bottomPadding: Dp, // <--- 1. New Parameter for dynamic padding
    onSongClick: (Song, List<Song>) -> Unit,
    onSeeAllSongs: () -> Unit = {},
    onOpenAlbums: () -> Unit = {},
    onOpenArtists: () -> Unit = {},
    onAlbumClick: (Long) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        AuraBackground()
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            // We ignore 'paddingValues' here because we want manual control
            // over the bottom padding, and top padding is handled by statusBarsPadding()

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1DB954))
                    }
                }

                state.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.errorMessage, color = Color.Red)
                    }
                }

                else -> {
                    HomeDashboardContent(
                        songs = state.songs,
                        albums = state.albums,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        bottomPadding = bottomPadding, // <--- 2. Pass it down
                        onSongClick = onSongClick,
                        onSeeAllSongs = onSeeAllSongs,
                        onOpenAlbums = onOpenAlbums,
                        onOpenArtists = onOpenArtists,
                        onAlbumClick = onAlbumClick,
                        onSongMoreClick = onSongMoreClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDashboardContent(
    songs: List<Song>,
    albums: List<Album>,
    currentSong: Song?,
    isPlaying: Boolean,
    bottomPadding: Dp, // <--- 3. Receive it here
    onSongClick: (Song, List<Song>) -> Unit,
    onSeeAllSongs: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onSettingsClick: () -> Unit

) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = bottomPadding),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(0.1f),
                                            Color.White.copy(0.03f)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(0.1f), CircleShape)
                                .clickable(onClick = onSettingsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = Color.White.copy(0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    item { CategoryPill(text = "Albums", onClick = onOpenAlbums) }
                    item { CategoryPill(text = "Artists", onClick = onOpenArtists) }
                }
            }
        }

        if (albums.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Played Albums", onSeeAll = onOpenAlbums)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(albums) { album ->
                        AlbumCard(album, onClick = { onAlbumClick(album.id) })
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }

        if (songs.isNotEmpty()) {
            item {
                SectionHeader(title = "Newest Songs", onSeeAll = onSeeAllSongs)
            }
            items(songs.take(10)) { song ->
                val isCurrent = currentSong?.id == song.id

                SongListItem(
                    song = song,
                    isActive = isCurrent,
                    isPlaying = isCurrent && isPlaying,
                    index = null,
                    onClick = {
                        onSongClick(song, songs)
                    },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
        }
    }
}


@Composable
fun CategoryPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(0.1f),
                        Color.White.copy(0.03f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        TextButton(onClick = onSeeAll) {
            Text("See All", color = Color(0xFF1DB954))
        }
    }
}

@Composable
private fun AlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF252525))
        ) {
            if (!album.coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = album.coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Rounded.Album,
                    null,
                    tint = Color.White.copy(0.2f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AuraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1DB954).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, -100f),
                radius = width * 1.3f
            ),
            center = Offset(width * 0.5f, -100f),
            radius = width * 1.3f
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
fun HomeScreenPreview() {
    val mockSongs = listOf(
        Song(
            id = 1,
            title = "Midnight City",
            artist = "M83",
            albumName = "Hurry Up, We're Dreaming",
            albumId = 0,
            duration = 240_000L,
            path = "",
            folderName = "Music",
            dateAdded = 0L,
            songArtUri = null,
            year = 1993,
            genre = "Rock",
            trackNumber = 12
        )
    )
    val mockAlbums = listOf(
        Album(1, "Hurry Up, We're Dreaming", "M83", null, 1, 2011)
    )

    val mockState = HomeState(
        isLoading = false,
        songs = mockSongs,
        albums = mockAlbums,
        errorMessage = null
    )

    MaterialTheme {
        HomeScreen(
            state = mockState,
            currentSong = mockSongs[0],
            onAlbumClick = {},
            onSettingsClick = {},
            isPlaying = false,
            onSeeAllSongs = {},
            onOpenAlbums = {},
            onOpenArtists = {},
            onSongMoreClick = {},
            onSongClick = { _, _ -> },
            bottomPadding = 100.dp
        )
    }
}