package org.android.prismplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.utils.rememberImmersiveColor

@Composable
fun SongOptionSheet(
    song: Song,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Logic: Extract Color
    LaunchedEffect(song) {
        if (!song.songArtUri.isNullOrBlank()) {
            val request = ImageRequest.Builder(context)
                .data(song.songArtUri)
                .allowHardware(false)
                .scale(Scale.FILL)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                bitmap = result.drawable.toBitmap()
            }
        }
    }

    val dominantColor by animateColorAsState(
        targetValue = rememberImmersiveColor(bitmap),
        animationSpec = tween(1000),
        label = "sheetColor"
    )

    // --- THE SHELL (This replaces ModalBottomSheet's default look) ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 1. Top Corners Rounded
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            // 2. Base Background (Dark Grey)
            .background(Color(0xFF121212))
            // 3. Subtle Border for "Glass" feel
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
    ) {
        // --- AURA GRADIENT ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp) // Height of the glow
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.2f), // Top glow
                            Color.Transparent // Fade to bottom
                        )
                    )
                )
        )

        // --- CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Drag Pill (Visual Indicator)
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

            // Song Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Art Box
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF252525))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                ) {
                    if (!song.songArtUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(song.songArtUri)
                                .setParameter("uniq_key", song.hashCode())
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

            // Options
            OptionItem(Icons.Rounded.PlaylistPlay, "Play Next", onPlayNext)
            OptionItem(Icons.Rounded.Queue, "Add to Queue", onAddToQueue)
            OptionItem(Icons.Rounded.PlaylistAdd, "Add to Playlist", onAddToPlaylist)
            OptionItem(Icons.Rounded.Album, "Go to Album", onGoToAlbum)

            HorizontalDivider(
                color = Color.White.copy(0.08f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            OptionItem(Icons.Rounded.Edit, "Edit Info", onEdit)
            OptionItem(Icons.Rounded.Share, "Share", onShare)
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