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
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
    onSongMoreClick: (Song) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onShufflePlay: (List<Song>) -> Unit,
    bottomPadding: Dp
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
                contentPadding = PaddingValues(bottom = bottomPadding + 20.dp)
            ) {
                item {
                    ArtistHeroHeader(
                        artistName = state.artistName,
                        artUri = state.heroArtUri,
                        albumCount = state.albums.size,
                        songCount = state.songs.size,
                        onBack = onBack,
                        onPlayClick = {
                            if (state.songs.isNotEmpty())
                                onSongClick(state.songs.first(), state.songs)
                        },
                        onShuffleClick = { onShufflePlay(state.songs) }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                if (state.albums.isNotEmpty()) {
                    item { SectionTitle("Discography") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.albums) { album ->
                                GlassAlbumCard(album, onClick = { onAlbumClick(album.id) })
                            }
                        }
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }

                if (state.songs.isNotEmpty()) {
                    item { SectionTitle("Added Tracks") }
                    items(state.songs) { song ->
                        val isCurrent = currentSong?.id == song.id
                        SongListItem(
                            song = song,
                            isActive = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            index = null,
                            onClick = { onSongClick(song, state.songs) },
                            onMoreClick = { onSongMoreClick(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistHeroHeader(
    artistName: String,
    artUri: String?,
    albumCount: Int,
    songCount: Int,
    onBack: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit
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
                    .blur(50.dp)
                    .drawDarkOverlay()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF151515))
                    .drawDarkOverlay()
            )
        }

        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.Black.copy(0.3f))
                .border(1.dp, Color.White.copy(0.1f), CircleShape)
        ) {
            Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // Row: Avatar + Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF202020))
                        .border(2.dp, Color.White.copy(0.2f), CircleShape)
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
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(Modifier.width(20.dp))

                Column {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "$albumCount Albums • $songCount Songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Play All", fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(0.1f))
                        .clickable { onShuffleClick() }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Shuffle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassAlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF202020))
        ) {
            AsyncImage(
                model = album.coverUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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
            text = album.year.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(0.5f)
        )
    }
}



private fun Modifier.drawDarkOverlay(): Modifier = this.then(
    Modifier.background(
        Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(0.3f),
                Color.Black.copy(0.7f),
                Color(0xFF050505)
            )
        )
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
fun ArtistScreenPreview() {
    val now = System.currentTimeMillis() / 1000

    val mockSongs = listOf(
        Song(
            id = 1L,
            title = "Starboy",
            artist = "The Weeknd",
            albumName = "Starboy",
            duration = 230_000L,
            path = "",
            albumId = 1L,
            folderName = "Music",
            dateAdded = now - 10_000,
            songArtUri = null,
            year = 2016,
            trackNumber = 1,
            genre = "Pop",
            dateModified = now - 5_000
        ),
        Song(
            id = 2L,
            title = "Blinding Lights",
            artist = "The Weeknd",
            albumName = "After Hours",
            duration = 200_000L,
            path = "",
            albumId = 2L,
            folderName = "Music",
            dateAdded = now - 9_000,
            songArtUri = null,
            year = 2020,
            trackNumber = 9,
            genre = "Synthwave",
            dateModified = now - 4_000
        ),
        Song(
            id = 3L,
            title = "Save Your Tears",
            artist = "The Weeknd",
            albumName = "After Hours",
            duration = 215_000L,
            path = "",
            albumId = 2L,
            folderName = "Music",
            dateAdded = now - 8_000,
            songArtUri = null,
            year = 2020,
            trackNumber = 11,
            genre = "Pop",
            dateModified = now - 3_000
        )
    )

    val mockAlbums = listOf(
        Album(
            id = 1L,
            title = "Starboy",
            artist = "The Weeknd",
            year = 2016,
            coverUri = null,
            songCount = 1
        ),
        Album(
            id = 2L,
            title = "After Hours",
            artist = "The Weeknd",
            year = 2020,
            coverUri = null,
            songCount = 2
        ),
        Album(
            id = 3L,
            title = "Dawn FM",
            artist = "The Weeknd",
            year = 2022,
            coverUri = null,
            songCount = 0
        )
    )

    val mockState = ArtistUiState(
        artistName = "The Weeknd",
        heroArtUri = null,
        albums = mockAlbums,
        songs = mockSongs,
        isLoading = false
    )

    MaterialTheme {
        ArtistScreen(
            state = mockState,
            currentSong = mockSongs.first(),
            isPlaying = true,
            onBack = {},
            onSongClick = { _, _ -> },
            onSongMoreClick = {},
            onAlbumClick = {},
            onShufflePlay = {},
            bottomPadding = 80.dp
        )
    }
}
