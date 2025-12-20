package org.android.prismplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.SearchResult
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem
import org.android.prismplayer.ui.theme.PrismPlayerTheme

@Composable
fun SearchScreen(
    query: String,
    results: SearchResult,
    currentSong: Song?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (String) -> Unit,    onArtistClick: (String) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    bottomPadding: Dp
) {
    val focusManager = LocalFocusManager.current

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
            // --- HEADER & INPUT ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "SEARCH_PROTOCOL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                TerminalSearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    onClear = { onQueryChange("") },
                    onDone = { focusManager.clearFocus() }
                )
            }

            FullWidthDivider()

            // --- RESULTS OUTPUT ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                // 1. ARTISTS (Entities)
                if (results.artists.isNotEmpty()) {
                    item { SectionLabel("IDENTIFIED_ENTITIES", "ARTISTS") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(results.artists) { artistName ->
                                val artistImage = remember(artistName, results.songs) {
                                    results.songs.firstOrNull { it.artist == artistName }?.songArtUri
                                }

                                TechArtistCard(
                                    artistName = artistName,
                                    imageUri = artistImage,
                                    onClick = { onArtistClick(artistName) }
                                )
                            }
                        }
                    }
                    item { FullWidthDivider() }
                }

                // 2. ALBUMS (Clusters)
                if (results.albums.isNotEmpty()) {
                    item { SectionLabel("DATA_CLUSTERS", "ALBUMS") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(results.albums) { album ->
                                RawSearchAlbumCard(album, onClick = { onAlbumClick(album.title) })
                            }
                        }
                    }
                    item { FullWidthDivider() }
                }

                // 3. SONGS (Files) - Logic Updated Here
                if (results.songs.isNotEmpty()) {
                    item { SectionLabel("MATCHING_FILES", "${results.songs.size}_FOUND") }
                    items(results.songs, key = { it.id }) { song ->
                        val isCurrent = currentSong?.id == song.id

                        SongListItem(
                            song = song,
                            isActive = isCurrent, // True if IDs match
                            isPlaying = isCurrent && isPlaying, // True if active AND global playing state is true
                            index = null,
                            onClick = { onSongClick(song) },
                            onMoreClick = { onSongMoreClick(song) }
                        )
                        FullWidthDivider()
                    }
                }

                // 4. EMPTY / IDLE STATE
                if (query.isNotEmpty() && results.songs.isEmpty() && results.albums.isEmpty() && results.artists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "NO_MATCHING_RECORDS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "INPUT_QUERY...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary), // Red cursor
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onDone() }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TechArtistCard(
    artistName: String,
    imageUri: String?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Square Wireframe Box
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
                .background(Color(0xFF111111))
        ) {
            if (!imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = artistName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RawSearchAlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
                .background(Color(0xFF111111))
        ) {
            if (!album.coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.coverUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.Album,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = album.title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 10.sp
        )
    }
}

@Composable
fun SectionLabel(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val mockSongs = listOf(
        Song(1L, "Starboy", "The Weeknd", "Starboy", 1L, 230000L, "", "", 0L, null, 2016, 1, "Pop"),
        Song(2L, "Blinding Lights", "The Weeknd", "After Hours", 2L, 200000L, "", "", 0L, null, 2020, 1, "Synth")
    )

    val mockAlbums = listOf(
        Album(1L, "Starboy", "The Weeknd", null, 18, 2016)
    )

    val mockArtists = listOf("The Weeknd")

    val mockResults = SearchResult(
        songs = mockSongs,
        albums = mockAlbums,
        artists = mockArtists
    )

    PrismPlayerTheme {
        SearchScreen(
            query = "weeknd",
            results = mockResults,
            currentSong = mockSongs[0], // Mocking "Starboy" as currently playing
            isPlaying = true, // Mocking playback state as true
            onQueryChange = {},
            onSongClick = {},
            onAlbumClick = {},
            onArtistClick = {},
            onSongMoreClick = {},
            bottomPadding = 80.dp
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun SearchScreenEmptyPreview() {
    val emptyResults = SearchResult(emptyList(), emptyList(), emptyList())

    PrismPlayerTheme {
        SearchScreen(
            query = "nothing",
            results = emptyResults,
            currentSong = null,
            isPlaying = false,
            onQueryChange = {},
            onSongClick = {},
            onAlbumClick = {},
            onArtistClick = {},
            onSongMoreClick = {},
            bottomPadding = 80.dp
        )
    }
}