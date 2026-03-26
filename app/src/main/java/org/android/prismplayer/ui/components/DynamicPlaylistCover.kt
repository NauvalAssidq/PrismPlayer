package org.android.prismplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.android.prismplayer.data.model.Playlist

@Composable
fun PlaylistItem(
    playlist: Playlist,
    artUris: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val uniqueCovers = remember(artUris) {
        artUris.filter { it.isNotBlank() }.distinct()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                // SCENARIO A: 4 or more unique covers -> The Prism 2x2 Grid
                uniqueCovers.size >= 4 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.weight(1f)) {
                            GridCoverImage(uri = uniqueCovers[0], modifier = Modifier.weight(1f))
                            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = MaterialTheme.colorScheme.background)
                            GridCoverImage(uri = uniqueCovers[1], modifier = Modifier.weight(1f))
                        }
                        Divider(modifier = Modifier.fillMaxWidth().height(1.dp), color = MaterialTheme.colorScheme.background)
                        Row(modifier = Modifier.weight(1f)) {
                            GridCoverImage(uri = uniqueCovers[2], modifier = Modifier.weight(1f))
                            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = MaterialTheme.colorScheme.background)
                            GridCoverImage(uri = uniqueCovers[3], modifier = Modifier.weight(1f))
                        }
                    }
                    // Dark overlay to unify the chaotic colors
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                }

                // SCENARIO B: 1 to 3 unique covers -> Just show the first one full size
                uniqueCovers.isNotEmpty() -> {
                    GridCoverImage(uri = uniqueCovers[0], modifier = Modifier.fillMaxSize())
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                }

                // SCENARIO C: Empty playlist or no art -> Fallback Icon
                else -> {
                    Icon(
                        Icons.Rounded.FolderSpecial, // Changed to folder to fit the OS vibe
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- TEXT DATA ---
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ID: ${playlist.playlistId} // TRACKS: ${artUris.size}", // Show track count!
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    }
}

// Helper component for the grid images to keep code clean
@Composable
private fun GridCoverImage(uri: String, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .size(100) // Downsample aggressively to save RAM! It's a tiny 24x24dp square anyway.
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxHeight()
    )
}