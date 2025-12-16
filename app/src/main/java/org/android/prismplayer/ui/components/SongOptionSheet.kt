package org.android.prismplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.android.prismplayer.data.model.Song

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
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF151515))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = bottomPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.2f))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF252525))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(8.dp))
                ) {
                    if (!song.songArtUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(song.songArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Rounded.MusicNote,
                            null,
                            tint = Color.White.copy(0.2f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(0.08f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            if (onPlayNext != null) OptionItem(Icons.Rounded.PlaylistPlay, "Play Next", onPlayNext)
            if (onAddToQueue != null) OptionItem(Icons.Rounded.Queue, "Add to Queue", onAddToQueue)
            if (onAddToPlaylist != null) OptionItem(Icons.Rounded.PlaylistAdd, "Add to Playlist", onAddToPlaylist)

            if (onGoToAlbum != null) OptionItem(Icons.Rounded.Album, "Go to Album", onGoToAlbum)
            if (onGoToArtist != null) OptionItem(Icons.Rounded.Person, "Go to Artist", onGoToArtist)

            if (onEdit != null || onShare != null) {
                HorizontalDivider(
                    color = Color.White.copy(0.08f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                if (onEdit != null) OptionItem(Icons.Rounded.Edit, "Edit Info", onEdit)
                if (onShare != null) OptionItem(Icons.Rounded.Share, "Share", onShare)
            }
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(0.9f)
        )
    }
}

@Preview(showBackground = false)
@Composable
fun SongOptionSheetPreview() {
    MaterialTheme{
        val mockSongs = Song(
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
        
        SongOptionSheet(
            song = mockSongs,
            bottomPadding = 60.dp,
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