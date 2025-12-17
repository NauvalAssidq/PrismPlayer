package org.android.prismplayer.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.SearchResult
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem

@Composable
fun SearchScreen(
    query: String,
    results: SearchResult,
    onQueryChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (String) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    bottomPadding: Dp
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        SearchAuraBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onClear = { onQueryChange("") },
                onDone = { focusManager.clearFocus() }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                if (results.artists.isNotEmpty()) {
                    item { SectionTitle("Artists") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(results.artists) { artistName ->
                                val artistImage = remember(artistName, results.songs) {
                                    results.songs.firstOrNull { it.artist == artistName }?.songArtUri
                                }

                                ArtistCard(
                                    artistName = artistName,
                                    imageUri = artistImage,
                                    onClick = { onArtistClick(artistName) }
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }

                if (results.albums.isNotEmpty()) {
                    item { SectionTitle("Albums") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(results.albums) { album ->
                                SearchAlbumCard(album, onClick = { onAlbumClick(album.id) })
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }

                if (results.songs.isNotEmpty()) {
                    item { SectionTitle("Songs") }
                    items(results.songs) { song ->
                        SongListItem(
                            song = song,
                            isActive = false,
                            isPlaying = false,
                            index = null,
                            onClick = { onSongClick(song) },
                            onMoreClick = { onSongMoreClick(song) }
                        )
                    }
                }

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
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(0.2f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text("No results found", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    val accent = Color(0xFF1DB954)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.03f)
        )
    )

    val borderColor = Color.White.copy(alpha = 0.12f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(50.dp)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, CircleShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search artists, songs...",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(accent),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onDone() }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }

        Spacer(Modifier.width(8.dp))
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
fun ArtistCard(
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
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF202020))
                .border(1.dp, Color.White.copy(0.1f), CircleShape), // Added subtle border
            contentAlignment = Alignment.Center
        ) {
            if (!imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SearchAlbumCard(album: Album, onClick: () -> Unit) {
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
                .clip(RoundedCornerShape(12.dp))
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
private fun SearchAuraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1DB954).copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(width * 0.8f, -100f),
                radius = width * 1.2f
            ),
            center = Offset(width * 0.8f, -100f),
            radius = width * 1.2f
        )
    }
}