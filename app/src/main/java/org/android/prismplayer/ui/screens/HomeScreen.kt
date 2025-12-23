package org.android.prismplayer.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.AlbumCard
import org.android.prismplayer.ui.components.SongListItem
import org.android.prismplayer.ui.theme.PrismPlayerTheme

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
                val displaySongs = remember(state.songs) { state.songs.take(10) }
                val displayAlbums = remember(state.albums) { state.albums.take(5) }
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

                    item(key = "commands") {
                        CommandGrid(
                            onOpenAlbums = onOpenAlbums,
                            onOpenArtists = onOpenArtists,
                            onSeeAllSongs = onSeeAllSongs
                        )
                        FullWidthDivider()
                    }

                    if (displayAlbums.isNotEmpty()) {
                        item(key = "albums_label") {
                            SectionLabel("RECENT_MOUNTS", "DATA_BLOCKS")
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
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(onSettingsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // FIX: Respects device notch/status bar height
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 6.dp) // Consistent 24dp spacing
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRISM OS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )

        }

        Text(
            text = "AUDIO\nCONSOLE",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 48.sp
        )
    }
}

@Composable
fun SystemStatsRow(totalSongs: Int, totalAlbums: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBadge("INDEXED_FILES", "$totalSongs")
        StatBadge("DATA_BLOCKS", "$totalAlbums")
        StatBadge("SYS", "ONLINE")
    }
}

@Composable
fun StatBadge(label: String, value: String) {
    Row(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CommandGrid(
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit,
    onSeeAllSongs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0A0A0A)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StripButton(
                label = "SONGS",
                icon = Icons.Rounded.Audiotrack,
                modifier = Modifier.weight(1f),
                onClick = onSeeAllSongs
            )

            VerticalDivider()

            StripButton(
                label = "ALBUMS",
                icon = Icons.Rounded.Album,
                modifier = Modifier.weight(1f),
                onClick = onOpenAlbums
            )

            VerticalDivider()

            StripButton(
                label = "ARTISTS",
                icon = Icons.Rounded.Person,
                modifier = Modifier.weight(1f),
                onClick = onOpenArtists
            )
        }
    }
}

@Composable
fun StripButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = 14.dp)
            .background(Color.White.copy(alpha = 0.2f))
    )
}

@Composable
fun FullWidthDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.1f),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

//Deprecated but saved for debugging

//@Composable
//fun RawAlbumCard(album: Album, onClick: () -> Unit) {
//    Column(
//        modifier = Modifier
//            .width(140.dp)
//            .clickable { onClick() }
//    ) {
//        Box(
//            modifier = Modifier
//                .size(140.dp)
//                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
//                .background(Color(0xFF111111))
//        ) {
//            if (!album.coverUri.isNullOrBlank()) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(album.coverUri)
//                        .crossfade(false)
//                        .memoryCacheKey(album.coverUri)
//                        .diskCacheKey(album.coverUri)
//                        .build(),
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
//            } else {
//                Column(
//                    modifier = Modifier.align(Alignment.Center),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        Icons.Rounded.Album,
//                        null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.size(32.dp)
//                    )
//                    Spacer(Modifier.height(8.dp))
//                    Text(
//                        "NO_DATA",
//                        style = MaterialTheme.typography.labelSmall,
//                        fontSize = 8.sp,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(8.dp)
//                    .background(Color.White.copy(0.85f))
//                    .padding(horizontal = 3.dp, vertical = 2.dp)
//                    .height(12.dp),
//                horizontalArrangement = Arrangement.spacedBy(1.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                val bars = listOf(1, 2, 1, 1, 3, 1, 2, 1, 2, 3, 1)
//                bars.forEach { w ->
//                    Box(
//                        modifier = Modifier
//                            .width(w.dp)
//                            .fillMaxHeight()
//                            .background(Color.Black)
//                    )
//                }
//            }
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        Text(
//            text = album.title.uppercase(),
//            style = MaterialTheme.typography.labelLarge,
//            color = MaterialTheme.colorScheme.primary,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//        Text(
//            text = album.artist.uppercase(),
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val mockSongs = listOf(
        Song(1, "Midnight City", "M83", "Hurry Up", 0, 240000, "", "", 0, null, 2011, 1, "Rock")
    )
    val mockAlbums = listOf(Album(1, "Hurry Up", "M83", null, 1, 2011))

    val mockState = HomeState(false, mockSongs, mockAlbums, null)

    PrismPlayerTheme {
        HomeScreen(
            state = mockState,
            currentSong = mockSongs.first(),
            isPlaying = true,
            bottomPadding = 80.dp,
            onSongClick = { _, _ -> },
            onSeeAllSongs = {},
            onOpenAlbums = {},
            onOpenArtists = {},
            onAlbumClick = {},
            onSongMoreClick = {},
            onSettingsClick = {},
        )
    }
}