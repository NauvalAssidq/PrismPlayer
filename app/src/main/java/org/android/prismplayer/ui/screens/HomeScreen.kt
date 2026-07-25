package org.android.prismplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.AlbumCard
import org.android.prismplayer.ui.components.SongListItem

@Composable
fun HomeScreen(
    state: HomeState,
    currentSong: Song? = null,
    isPlaying: Boolean = false,
    bottomPadding: Dp,
    onSongClick: (Song, List<Song>) -> Unit,
    onSeeAllSongs: () -> Unit = {},
    onOpenAlbums: () -> Unit = {},
    onOpenArtists: () -> Unit = {},
    onAlbumClick: (String) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onSettingsClick: () -> Unit,
    recentlyPlayedSongs: List<Song> = emptyList()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                val displaySongs = remember(state.songs) { state.songs.take(5) }
                val displayAlbums = remember(state.albums) { state.albums.take(5) }
                val displayRecentlyPlayed = remember(recentlyPlayedSongs) { recentlyPlayedSongs.take(9) }
                val songCount = state.totalSongCount
                val albumCount = state.totalAlbumCount

                LazyColumn(
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item(key = "header") {
                        DashboardHeader(onSettingsClick)
                        FullWidthDivider()
                    }

                    item(key = "stats") {
                        SystemStatsRow(totalSongs = songCount, totalAlbums = albumCount)
                        FullWidthDivider()
                    }

                    // 1. RECENTLY PLAYED (DYNAMIC GRID UP TO 9 SONGS: 3x1, 2x2, 3x2, 3x3)
                    if (displayRecentlyPlayed.isNotEmpty()) {
                        item(key = "recently_played_label") {
                            SectionLabel("RECENTLY_PLAYED", "PLAY_LOGS")
                        }
                        item(key = "recently_played_grid") {
                            val columns = if (displayRecentlyPlayed.size == 4) 2 else 3
                            val rows = displayRecentlyPlayed.chunked(columns)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { song ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                RecentlyPlayedCard(
                                                    song = song,
                                                    onClick = { onSongClick(song, displayRecentlyPlayed) }
                                                )
                                            }
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        item(key = "recently_played_divider") {
                            FullWidthDivider()
                        }
                    }

                    // 2. NEW ENTRIES (LIMITED TO 5 SONGS MAX)
                    if (displaySongs.isNotEmpty()) {
                        item(key = "songs_label") {
                            SectionLabel("NEW_ENTRIES", "AUDIO_FILES")
                            FullWidthDivider()
                        }

                        items(items = displaySongs, key = { it.id }) { song ->
                            val isCurrent = currentSong?.id == song.id
                            SongListItem(
                                song = song,
                                isActive = isCurrent,
                                isPlaying = isCurrent && isPlaying,
                                index = null,
                                showDuration = true,
                                onClick = { onSongClick(song, state.songs) },
                                onMoreClick = { onSongMoreClick(song) }
                            )
                            FullWidthDivider()
                        }
                    }

                    // 3. RECENT MOUNTS (ALBUMS) MOVED ALL THE WAY DOWN BELOW NEW ENTRIES
                    if (displayAlbums.isNotEmpty()) {
                        item(key = "albums_label") {
                            SectionLabel("RECENT_MOUNTS", "ALBUMS")
                        }
                        item(key = "albums_list") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(items = displayAlbums, key = { it.id }) { album ->
                                    AlbumCard(
                                        title = album.title,
                                        artist = album.artist,
                                        coverUri = album.coverUri,
                                        onClick = { onAlbumClick(album.title) },
                                        fixedWidth = 140.dp
                                    )
                                }
                            }
                        }
                        item(key = "albums_divider") {
                            FullWidthDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedCard(
    song: Song,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.2f))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Audiotrack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Gradient overlay for bottom-left text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(0.85f)),
                        startY = 60f
                    )
                )
        )

        // Song Title inside album cover bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = song.title.uppercase(),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist.uppercase(),
                style = TextStyle(
                    color = Color.White.copy(0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DashboardHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "THINGS_SYSTEM_OS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DASHBOARD",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = org.android.prismplayer.ui.theme.DotFont,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun SystemStatsRow(totalSongs: Int, totalAlbums: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatBox(label = "TOTAL_TRACKS", value = "$totalSongs")
        StatBox(label = "TOTAL_MOUNTS", value = "$totalAlbums")
        StatBox(label = "SYSTEM_STATUS", value = "ONLINE")
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FullWidthDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
}