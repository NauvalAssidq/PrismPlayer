package org.android.prismplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.data.model.Playlist

@Composable
fun AddToPlaylistSheet(
    playlists: List<Playlist>,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.15f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .navigationBarsPadding()
            .padding(bottom = bottomPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.15f)))
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "SELECT_TARGET_PLAYLIST",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
                fontSize = 11.sp
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
        ) {
            item {
                PlaylistCommandRow(
                    label = "CREATE_NEW_PLAYLIST",
                    icon = Icons.Rounded.Add,
                    isPrimary = true,
                    onClick = onCreateNew
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
            }

            items(playlists) { playlist ->
                val uris = playlist.iconUri?.split("|")?.filter { it.isNotBlank() } ?: emptyList()

                PlaylistCommandRow(
                    label = playlist.name.uppercase(),
                    subLabel = "STORAGE_ID: ${playlist.playlistId}",
                    imageUris = uris, // 2. Pass the list down
                    isFavorite = playlist.name == "Favorites",
                    onClick = { onPlaylistSelected(playlist) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
            }
        }
    }
}

@Composable
private fun PlaylistCommandRow(
    label: String,
    subLabel: String? = null,
    icon: ImageVector? = null,
    imageUris: List<String> = emptyList(),
    isFavorite: Boolean = false,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            if (isPrimary) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.4f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon ?: Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                DynamicPlaylistCover(
                    isFavorite = isFavorite,
                    imageUris = imageUris,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }
            }
        }

        Text(
            text = "[ LOAD ]",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.primary.copy(0.3f),
            fontFamily = FontFamily.Monospace
        )
    }
}