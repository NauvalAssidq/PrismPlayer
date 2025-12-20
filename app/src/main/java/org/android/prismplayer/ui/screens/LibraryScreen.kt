package org.android.prismplayer.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.ArtistListItem
import org.android.prismplayer.ui.components.SongListItem

@Composable
fun LibraryScreen(
    state: HomeState,
    currentSong: Song?,
    songs: List<Song>,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (String) -> Unit,
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

    // Data Processing (Kept efficient)
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // --- HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "DATABASE_VIEWER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "LOCAL_LIBRARY",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 32.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0A0A0A)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabSegment("SONGS", pagerState.currentPage == 0, modifier = Modifier.weight(1f)) {
                    scope.launch { pagerState.animateScrollToPage(0) }
                }
                VerticalDivider()
                TabSegment("ALBUMS", pagerState.currentPage == 1, modifier = Modifier.weight(1f)) {
                    scope.launch { pagerState.animateScrollToPage(1) }
                }
                VerticalDivider()
                TabSegment("ARTISTS", pagerState.currentPage == 2, modifier = Modifier.weight(1f)) {
                    scope.launch { pagerState.animateScrollToPage(2) }
                }
            }

            Spacer(Modifier.height(24.dp))

            Divider(color = Color.White.copy(0.1f))

            // --- PAGER CONTENT ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> { // SONGS LIST
                        if (songs.isEmpty()) {
                            EmptyStateMessage("NO_AUDIO_FILES_DETECTED")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = bottomPadding),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(songs, key = { it.id }) { song ->
                                    val isCurrent = currentSong?.id == song.id
                                    SongListItem(
                                        song = song,
                                        isActive = isCurrent,
                                        isPlaying = isCurrent && isPlaying,
                                        onClick = { onSongClick(song, songs) },
                                        onMoreClick = { onSongMoreClick(song) }
                                    )
                                    Divider(color = Color.White.copy(0.1f))
                                }
                            }
                        }
                    }

                    1 -> { // ALBUMS GRID
                        if (albums.isEmpty()) {
                            EmptyStateMessage("NO_DATA_BLOCKS_FOUND")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(
                                    start = 24.dp, end = 24.dp, top = 24.dp, bottom = bottomPadding
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(albums, key = { it.id }) { song ->
                                    RawAlbumGridItem(
                                        song = song,
                                        onClick = { onAlbumClick(song.albumName) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        if (artists.isEmpty()) {
                            EmptyStateMessage("NO_ARTIST_METADATA")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = bottomPadding),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(artists, key = { it }) { artistName ->
                                    val imageUri = remember(artistName) {
                                        songs.firstOrNull { it.artist == artistName }?.songArtUri
                                    }

                                    ArtistListItem(
                                        artistName = artistName,
                                        imageUri = imageUri,
                                        onClick = { onArtistClick(artistName) }
                                    )

                                    Divider(color = Color.White.copy(0.1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun TabSegment(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ERROR // $message",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun RawAlbumGridItem(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Square Wireframe Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
                .background(Color(0xFF111111))
        ) {
            if (!song.songArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.songArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Rounded.Album,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = song.albumName.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 10.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryPreview() {
    val mockSongs = listOf(
        Song(1, "Midnight City", "M83", "Hurry Up", 1, 240000, "", "", 0, null, 2011, 1, "Rock")
    )
    val state = HomeState(false, mockSongs, emptyList(), null)

    MaterialTheme {
        LibraryScreen(
            state = state,
            currentSong = null,
            songs = mockSongs,
            isPlaying = false,
            onSongClick = { _, _ -> },
            onAlbumClick = {},
            onSongMoreClick = {},
            onArtistClick = {},
            bottomPadding = 80.dp,
            onPageChanged = {}
        )
    }
}