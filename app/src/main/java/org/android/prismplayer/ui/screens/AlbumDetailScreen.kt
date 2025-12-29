package org.android.prismplayer.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.SongListItem
import org.android.prismplayer.ui.utils.PrismaColorUtils
import org.android.prismplayer.ui.utils.rememberImmersiveColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
        albumId: Long,
        albumName: String,
        artistName: String,
        artUri: String?,
        songs: List<Song>,
        currentSong: Song?,
        isPlaying: Boolean,
        onBack: () -> Unit,
        onPlayAlbum: (List<Song>) -> Unit,
        onSongClick: (Song, List<Song>) -> Unit,
        onSongMoreClick: (Song) -> Unit,
        bottomPadding: Dp
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(artUri) {
        if (!artUri.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context).data(artUri).allowHardware(false).build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                bitmap = result.drawable.toBitmap()
            }
        }
    }

    val rawColor = rememberImmersiveColor(bitmap)

    val accentColor by
            animateColorAsState(
                    targetValue =
                            remember(rawColor) {
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

    Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                            title = {
                                Text(
                                        "ARCHIVE_VIEWER",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp,
                                        color = MaterialTheme.colorScheme.primary
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                            Icons.AutoMirrored.Outlined.ArrowBack,
                                            "RETURN",
                                            tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {}) {
                                    Icon(
                                            Icons.Outlined.MoreVert,
                                            "OPTIONS",
                                            tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            colors =
                                    TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.background
                                    )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                }
            }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(300.dp)
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    accentColor.copy(alpha = 0.15f),
                                                                    Color.Transparent
                                                            )
                                            )
                                    )
            )

            LazyColumn(
                    contentPadding =
                            PaddingValues(
                                    top = padding.calculateTopPadding() + 24.dp,
                                    bottom = bottomPadding + 24.dp
                            ),
                    modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                                modifier =
                                        Modifier.size(140.dp)
                                                .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.onSurface.copy(
                                                                0.2f
                                                        )
                                                )
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(4.dp)
                        ) {
                            if (artUri != null) {
                                AsyncImage(
                                        model = artUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                )
                            }

                            Box(
                                    modifier =
                                            Modifier.fillMaxSize()
                                                    .background(
                                                            Brush.linearGradient(
                                                                    colors =
                                                                            listOf(
                                                                                    MaterialTheme
                                                                                            .colorScheme
                                                                                            .onSurface
                                                                                            .copy(
                                                                                                    0.1f
                                                                                            ),
                                                                                    Color.Transparent
                                                                            ),
                                                                    start =
                                                                            androidx.compose.ui
                                                                                    .geometry
                                                                                    .Offset(0f, 0f),
                                                                    end =
                                                                            androidx.compose.ui
                                                                                    .geometry
                                                                                    .Offset(
                                                                                            Float.POSITIVE_INFINITY,
                                                                                            Float.POSITIVE_INFINITY
                                                                                    )
                                                            )
                                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxHeight().weight(1f)
                        ) {
                            MetadataField(
                                    label = "ALBUM_ID",
                                    value = albumName.uppercase(),
                                    isHeader = true
                            )

                            Spacer(Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                MetadataField(
                                        "ARTIST_KEY",
                                        artistName.uppercase(),
                                        color = accentColor
                                )
                                MetadataField("FILE_COUNT", "${songs.size} TRACKS")
                                MetadataField("TOTAL_LEN", calculateTotalDuration(songs))
                            }
                        }
                    }
                }

                item {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                                onClick = { onPlayAlbum(songs) },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = accentColor,
                                                contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("INITIALIZE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Box(
                                modifier =
                                        Modifier.width(52.dp)
                                                .height(52.dp)
                                                .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.onSurface.copy(
                                                                0.2f
                                                        ),
                                                        RoundedCornerShape(2.dp)
                                                )
                                                .clickable { onPlayAlbum(songs.shuffled()) },
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    Icons.Outlined.Shuffle,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                                    modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(0.1f),
                            modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(songs) { index, song ->
                    val isActive = currentSong?.id == song.id

                    if (index == 0) {
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                    "TRACK_MANIFEST",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                            )
                        }
                    }

                    SongListItem(
                            song = song,
                            isActive = isActive,
                            isPlaying = isPlaying,
                            index = index + 1,
                            showDuration = true,
                            onClick = { onSongClick(song, songs) },
                            onMoreClick = { onSongMoreClick(song) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                }
            }
        }
    }
}

@Composable
fun MetadataField(
        label: String,
        value: String,
        isHeader: Boolean = false,
        color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (isHeader) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
            )
            Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
            )
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    modifier = Modifier.width(80.dp)
            )

            Box(
                    modifier =
                            Modifier.padding(end = 12.dp)
                                    .width(1.dp)
                                    .height(10.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
            )

            Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
            )
        }
    }
}

private fun calculateTotalDuration(songs: List<Song>): String {
    val totalSeconds = songs.sumOf { it.duration } / 1000
    val minutes = totalSeconds / 60
    return "${minutes} MIN"
}

@Preview(showBackground = true, backgroundColor = 0xFF050505, heightDp = 800)
@Composable
fun PreviewAlbumDetail() {
    val mockSongs =
            listOf(
                    Song(
                            1,
                            "Grenade",
                            "Bruno Mars",
                            "Doo-Wops & Hooligans",
                            1,
                            240000,
                            "",
                            "",
                            0,
                            null,
                            2010,
                            1,
                            "Pop"
                    ),
                    Song(
                            2,
                            "The Lazy Song",
                            "Bruno Mars",
                            "Doo-Wops & Hooligans",
                            2,
                            180000,
                            "",
                            "",
                            0,
                            null,
                            2010,
                            2,
                            "Pop"
                    )
            )

    MaterialTheme {
        AlbumDetailScreen(
                albumId = 1,
                albumName = "Doo-Wops & Hooligans",
                artistName = "Bruno Mars",
                artUri = null,
                songs = mockSongs,
                currentSong = mockSongs[0],
                isPlaying = true,
                onBack = {},
                onPlayAlbum = {},
                onSongClick = { _, _ -> },
                onSongMoreClick = {},
                bottomPadding = 120.dp
        )
    }
}
