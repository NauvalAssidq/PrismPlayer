package org.android.prismplayer.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.AlbumCard
import org.android.prismplayer.ui.components.SongListItem
import org.android.prismplayer.ui.utils.PrismaColorUtils
import org.android.prismplayer.ui.utils.rememberImmersiveColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    state: ArtistUiState,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onAlbumClick: (String) -> Unit,
    onShufflePlay: (List<Song>) -> Unit,
    bottomPadding: Dp
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(state.heroArtUri) {
        if (!state.heroArtUri.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(state.heroArtUri)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                bitmap = result.drawable.toBitmap()
            }
        }
    }

    val rawColor = rememberImmersiveColor(bitmap)

    val accentColor by animateColorAsState(
        targetValue = remember(rawColor) {
            val base = PrismaColorUtils.adjustForAccent(rawColor)
            if (base.luminance() < 0.3f) {
                base.copy(alpha = 0.9f).compositeOver(Color.White)
            } else {
                base
            }
        },
        animationSpec = tween(1000),
        label = "accentGlow"
    )

    // MATCHING HEADER LOGIC: Wrapped in Scaffold
    Scaffold(
        containerColor = Color(0xFF050505),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "ARTIST_DOSSIER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, "RETURN", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Outlined.MoreVert, "OPTIONS", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF050505)
                    )
                )
                Divider(color = Color.White.copy(0.1f))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = accentColor,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(), // Respect Scaffold padding
                        bottom = bottomPadding + 20.dp
                    )
                ) {
                    item {
                        ArtistDataHeader(
                            artistName = state.artistName,
                            artUri = state.heroArtUri,
                            albumCount = state.albums.size,
                            songCount = state.songs.size,
                            accentColor = accentColor,
                            // onBack and header title logic moved to TopBar
                            onPlayClick = {
                                if (state.songs.isNotEmpty())
                                    onSongClick(state.songs.first(), state.songs)
                            },
                            onShuffleClick = { onShufflePlay(state.songs) }
                        )
                        FullWidthDivider()
                    }

                    if (state.albums.isNotEmpty()) {
                        item { SectionLabel("DISCOGRAPHY", "DATA_BLOCKS") }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.albums) { album ->
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
                        item { FullWidthDivider() }
                    }

                    if (state.songs.isNotEmpty()) {
                        item { SectionLabel("TRACK_MANIFEST", "${state.songs.size}_ENTRIES") }
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
                            FullWidthDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistDataHeader(
    artistName: String,
    artUri: String?,
    albumCount: Int,
    songCount: Int,
    accentColor: Color,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(1.dp, Color.White.copy(0.2f))
                    .background(Color(0xFF0A0A0A))
            ) {
                if (artUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.Person,
                        null,
                        tint = Color.White.copy(0.2f),
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    text = artistName.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "$albumCount ALBUMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.7f)
                    )
                    Text(
                        text = "//",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.3f)
                    )
                    Text(
                        text = "$songCount TRACKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.7f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("INITIALIZE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Button(
                onClick = onShuffleClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(4.dp)),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.Shuffle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("RANDOMIZE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
fun ArtistScreenPreview() {
    val now = System.currentTimeMillis() / 1000

    val mockSongs = listOf(
        Song(1L, "Starboy", "The Weeknd", "Starboy", 1L, 230_000L, "", "Music", now, null, 2016, 1, "Pop"),
        Song(2L, "Blinding Lights", "The Weeknd", "After Hours", 2L, 200_000L, "", "Music", now, null, 2020, 9, "Synth")
    )

    val mockAlbums = listOf(
        Album(1L, "Starboy", "The Weeknd", null, 1, 2016),
        Album(2L, "After Hours", "The Weeknd", null, 2, 2020)
    )

    val mockState = ArtistUiState("The Weeknd", null, mockSongs, mockAlbums, "", isLoading = false)

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