package org.android.prismplayer.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.utils.SongArtHelper

@Composable
fun SongOptionSheet(
    song: Song,
    bottomPadding: Dp = 0.dp,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onGoToAlbum: (() -> Unit)? = null,
    onGoToArtist: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F0F))
            .border(
                width = 1.dp,
                color = Color.White.copy(0.15f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .padding(bottom = bottomPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(Color(0xFF151515)),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(1.dp)
                            .background(Color.White.copy(0.2f))
                    )
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(0.1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, Color.White.copy(0.2f))
                    .background(Color(0xFF050505))
            ) {
                if (!song.songArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                SongArtHelper.getUri(song.id)
                                    .buildUpon()
                                    .appendQueryParameter("t", song.dateModified.toString())
                                    .build()
                            )
                            .crossfade(false)
                            .size(96, 96)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "NO_SIG",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color.White.copy(0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MetadataLine("TITLE", song.title.uppercase())
                MetadataLine("ARTIST", song.artist.uppercase())
                MetadataLine("LENGTH", formatDurationTech(song.duration))
            }
        }

        HorizontalDivider(color = Color.White.copy(0.1f))

        if (onPlayNext != null) CommandRow("PRIORITY_NEXT", Icons.Outlined.PlaylistPlay, onPlayNext)
        if (onAddToQueue != null) CommandRow("ENQUEUE", Icons.Outlined.Queue, onAddToQueue)
        if (onAddToPlaylist != null) CommandRow("ADD_TO_PLAYLIST", Icons.Outlined.PlaylistAdd, onAddToPlaylist)
        if (onPlayNext != null || onAddToQueue != null) {
            HorizontalDivider(color = Color.White.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))
        }

        if (onGoToAlbum != null) CommandRow("OPEN_ALBUM", Icons.Outlined.Album, onGoToAlbum)
        if (onGoToArtist != null) CommandRow("OPEN_ARTIST", Icons.Outlined.Person, onGoToArtist)

        if (onEdit != null || onShare != null) {
            HorizontalDivider(color = Color.White.copy(0.05f), modifier = Modifier.padding(horizontal = 24.dp))
        }

        if (onEdit != null) CommandRow("MODIFY_TAGS", Icons.Outlined.Edit, onEdit)
        if (onShare != null) CommandRow("TRANSMIT", Icons.Outlined.Share, onShare)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- SUB-COMPONENTS ---

@Composable
private fun MetadataLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.White.copy(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, // Theme Accent
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CommandRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = Color.White.copy(0.9f)
            )
        }

        Text(
            text = ">>",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = Color.White.copy(0.2f)
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatDurationTech(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SongOptionSheetPreview() {
    val mockSong = Song(
        id = 1,
        title = "Midnight City",
        artist = "M83",
        albumName = "Hurry Up, We're Dreaming",
        albumId = 0,
        duration = 240_000L,
        path = "",
        folderName = "Music",
        dateAdded = 0L,
        songArtUri = null,
        year = 1993,
        genre = "Rock",
        trackNumber = 12
    )

    MaterialTheme {
        SongOptionSheet(
            song = mockSong,
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onEdit = {},
            onShare = {}
        )
    }
}