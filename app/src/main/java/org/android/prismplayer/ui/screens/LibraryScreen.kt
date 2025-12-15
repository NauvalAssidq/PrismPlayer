package org.android.prismplayer.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem

enum class LibraryView { SONGS, ALBUMS, ARTISTS }

@Composable
fun LibraryScreen(
    state: HomeState,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory)
) {
    val songs by viewModel.allSongs.collectAsState()
    var currentView by rememberSaveable { mutableStateOf(LibraryView.SONGS) }
    val albums = remember(state.songs) {
        state.songs
            .groupBy { it.albumId }
            .map { (_, songs) -> songs.first() }
    }

    val artists = remember(state.songs) {
        state.songs
            .groupBy { it.artist }
            .map { (_, songs) -> songs.first() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        AuraBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LibraryChip(text = "Songs", selected = currentView == LibraryView.SONGS) { currentView = LibraryView.SONGS }
                LibraryChip(text = "Albums", selected = currentView == LibraryView.ALBUMS) { currentView = LibraryView.ALBUMS }
                LibraryChip(text = "Artists", selected = currentView == LibraryView.ARTISTS) { currentView = LibraryView.ARTISTS }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = currentView, label = "LibrarySwitch") { view ->
                    when (view) {
                        LibraryView.SONGS -> {
                            if (state.songs.isEmpty()) {
                                EmptyStateMessage("No songs found")
                            } else {
                                LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                    items(songs) { song ->
                                        val isCurrent = currentSong?.id == song.id
                                        SongListItem(
                                            song = song,
                                            isActive = isCurrent,
                                            isPlaying = isCurrent && isPlaying,
                                            onClick = {
                                                onSongClick(song, songs)
                                            },
                                            onMoreClick = { onSongMoreClick(song) }
                                        )
                                    }
                                }
                            }
                        }

                        LibraryView.ALBUMS -> {
                            if (albums.isEmpty()) {
                                EmptyStateMessage("No albums found")
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        bottom = 120.dp,
                                        start = 20.dp,
                                        top = 0.dp,
                                        end = 20.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(albums) { song ->
                                        AlbumGridItem(
                                            song = song,
                                            onClick = { onAlbumClick(song.albumId) }
                                        )
                                    }
                                }
                            }
                        }

                        LibraryView.ARTISTS -> {
                            if (artists.isEmpty()) {
                                EmptyStateMessage("No artists found")
                            } else {
                                LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                    items(artists) { song ->
                                        ArtistListItem(artistName = song.artist, onClick = { })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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

@Composable
fun LibraryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(0xFF1DB954)

    val backgroundBrush = if (selected) {
        Brush.verticalGradient(
            colors = listOf(
                accent.copy(alpha = 0.22f),
                accent.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    }

    val borderColor = if (selected) {
        accent.copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    val textColor = if (selected) accent else Color.White

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AlbumGridItem(song: Song, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(0.03f)) // Glassy placeholder
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
        ) {
            if (!song.songArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = song.songArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = song.albumName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistListItem(artistName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.05f))
                .border(1.dp, Color.White.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artistName.take(1).uppercase(),
                color = Color.White.copy(0.5f),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.White.copy(0.3f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
fun LibraryScreenPreview() {
    val mockSongs = listOf(
        Song(
            1, "Midnight City", "M83", "Hurry Up, We're Dreaming", 1, 240000, "", "", 0, null,
            year = 1,
            trackNumber = 1,
            genre = "Pop",
        ),
        Song(
            2, "Starboy", "The Weeknd", "Starboy", 2, 200000, "", "", 0, null,
            year = 1,
            trackNumber = 1,
            genre = "Pop",
        ),
        Song(3, "Reunion", "M83", "Hurry Up, We're Dreaming", 1, 210000, "", "", 0, null, year = 1, trackNumber = 1, genre = "Pop",)
    )

    val mockAlbums = listOf(
        Album (1, "Hurry Up, We're Dreaming", "M83", null, 1, 2011),
        Album (2, "Starboy", "The Weeknd", null, 2, 2016)
    )

    val state = HomeState(false, mockSongs, mockAlbums, null)

    MaterialTheme {
        LibraryScreen(
            state = state,
            currentSong = mockSongs[0],
            isPlaying = true,
            onSongClick = { _, _ -> },
            onAlbumClick = {},
            onSongMoreClick = {},
        )
    }
}