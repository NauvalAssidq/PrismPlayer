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
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic // [NEW] Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily // [NEW]
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
import org.android.prismplayer.data.model.Playlist // [NEW]
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.AlbumCard
import org.android.prismplayer.ui.components.ArtistListItem
import org.android.prismplayer.ui.components.SongListItem

@Composable
fun LibraryScreen(
    state: HomeState,
    currentSong: Song?,
    songs: List<Song>,
    // [NEW] Playlist Data
    playlists: List<Playlist>,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (String) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    // [NEW] Playlist Click
    onPlaylistClick: (Playlist) -> Unit,
    bottomPadding: Dp,
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 4 } // [UPDATED] Increased from 3 to 4
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

            // --- TAB ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabSegment(
                    text = "SONGS",
                    icon = Icons.Rounded.Audiotrack,
                    isSelected = pagerState.currentPage == 0,
                    modifier = Modifier.weight(1f)
                ) { scope.launch { pagerState.animateScrollToPage(0) } }

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))

                TabSegment(
                    text = "ALBUMS",
                    icon = Icons.Rounded.Album,
                    isSelected = pagerState.currentPage == 1,
                    modifier = Modifier.weight(1f)
                ) { scope.launch { pagerState.animateScrollToPage(1) } }

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))

                TabSegment(
                    text = "ARTISTS",
                    icon = Icons.Rounded.Person,
                    isSelected = pagerState.currentPage == 2,
                    modifier = Modifier.weight(1f)
                ) { scope.launch { pagerState.animateScrollToPage(2) } }

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))

                TabSegment(
                    text = "LISTS",
                    icon = Icons.Rounded.QueueMusic,
                    isSelected = pagerState.currentPage == 3,
                    modifier = Modifier.weight(1f)
                ) { scope.launch { pagerState.animateScrollToPage(3) } }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> { // SONGS
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
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                                }
                            }
                        }
                    }

                    1 -> { // ALBUMS
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
                                    AlbumCard(
                                        title = song.albumName,
                                        artist = song.artist,
                                        coverUri = song.songArtUri,
                                        onClick = { onAlbumClick(song.albumName) },
                                        fixedWidth = null
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // ARTISTS
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

                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                                }
                            }
                        }
                    }

                    3 -> { // [NEW] PLAYLISTS
                        if (playlists.isEmpty()) {
                            EmptyStateMessage("NO_PLAYLIST_DATA")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = bottomPadding),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(playlists, key = { it.playlistId }) { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        onClick = { onPlaylistClick(playlist) }
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
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
fun TabSegment(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.1f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Favorite,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ID: ${playlist.playlistId} // USER_CREATED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
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

@Preview(showBackground = true)
@Composable
fun LibraryPreview() {
    val mockSongs = listOf(
        Song(1, "Midnight City", "M83", "Hurry Up", 1, 240000, "", "", 0, null, 2011, 1, "Rock")
    )
    val mockPlaylists = listOf(
        Playlist(1, "Gym Mix"),
        Playlist(2, "Night Drive")
    )
    val state = HomeState(false, mockSongs, emptyList(), null)

    MaterialTheme {
        LibraryScreen(
            state = state,
            currentSong = null,
            songs = mockSongs,
            playlists = mockPlaylists,
            isPlaying = false,
            onSongClick = { _, _ -> },
            onAlbumClick = {},
            onSongMoreClick = {},
            onArtistClick = {},
            onPlaylistClick = {},
            bottomPadding = 80.dp,
            onPageChanged = {}
        )
    }
}