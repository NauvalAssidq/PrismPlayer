package org.android.prismplayer.ui.screens

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.AlbumGridItem
import org.android.prismplayer.ui.components.ArtistListItem
import org.android.prismplayer.ui.components.SongListItem

@Composable
fun LibraryScreen(
    state: HomeState,
    currentSong: Song?,
    songs: List<Song>,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    bottomPadding: Dp,
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 3 }
    )
    val scope = rememberCoroutineScope()
    val albums = remember(songs) {
        songs
            .groupBy { "${it.albumName.trim()}|${it.artist.trim()}" }
            .map { (_, list) -> list.first() }
            .sortedBy { it.albumName.lowercase() }
    }

    val artists = remember(songs) {
        songs
            .map { it.artist.trim() }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
        }
    }

    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPage && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(initialPage)
        }
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
                LibraryChip(
                    text = "Songs",
                    selected = pagerState.currentPage == 0
                ) { scope.launch { pagerState.animateScrollToPage(0) } }

                LibraryChip(
                    text = "Albums",
                    selected = pagerState.currentPage == 1
                ) { scope.launch { pagerState.animateScrollToPage(1) } }

                LibraryChip(
                    text = "Artists",
                    selected = pagerState.currentPage == 2
                ) { scope.launch { pagerState.animateScrollToPage(2) } }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        if (songs.isEmpty()) {
                            EmptyStateMessage("No songs found")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = bottomPadding),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(songs) { song ->
                                    val isCurrent = currentSong?.id == song.id
                                    SongListItem(
                                        song = song,
                                        isActive = isCurrent,
                                        isPlaying = isCurrent && isPlaying,
                                        onClick = { onSongClick(song, songs) },
                                        onMoreClick = { onSongMoreClick(song) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        if (albums.isEmpty()) {
                            EmptyStateMessage("No albums found")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 0.dp,
                                    bottom = bottomPadding
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
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

                    2 -> {
                        if (artists.isEmpty()) {
                            EmptyStateMessage("No artists found")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = bottomPadding),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(artists) { artistName ->
                                    val imageUri = remember(artistName) {
                                        songs.firstOrNull { it.artist == artistName }?.songArtUri
                                    }

                                    ArtistListItem(
                                        artistName = artistName,
                                        imageUri = imageUri,
                                        onClick = { onArtistClick(artistName) }
                                    )
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
        Album(1, "Hurry Up, We're Dreaming", "M83", null, 1, 2011),
        Album (2, "Starboy", "The Weeknd", null, 2, 2016)
    )

    val state = HomeState(false, mockSongs, mockAlbums, null)

    MaterialTheme {
        LibraryScreen(
            songs = mockSongs,
            state = state,
            currentSong = mockSongs[0],
            isPlaying = true,
            onSongClick = { _, _ -> },
            onAlbumClick = {},
            onSongMoreClick = {},
            onArtistClick = {},
            bottomPadding = 120.dp,
            onPageChanged = {}
        )
    }
}