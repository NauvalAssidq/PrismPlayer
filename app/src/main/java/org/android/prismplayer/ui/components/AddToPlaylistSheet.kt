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
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.data.model.Playlist

@Composable
fun AddToPlaylistSheet(
  playlists: List<Playlist>,
  onPlaylistSelected: (Playlist) -> Unit,
  onCreateNew: (String) -> Unit,
  onDismiss: () -> Unit,
  bottomPadding: Dp = 0.dp
) {
  var showCreateDialog by remember { mutableStateOf(false) }
  var newPlaylistName by remember { mutableStateOf("") }

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
    // --- 1. GRIP HEADER (Matches SongOptionSheet) ---
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(26.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) {
          Box(
            modifier = Modifier
              .width(16.dp)
              .height(1.dp)
              .background(MaterialTheme.colorScheme.onSurface.copy(0.2f))
          )
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

    // --- 2. TITLE ---
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        "SELECT_TARGET_PLAYLIST",
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.primary, // Theme Accent
        letterSpacing = 2.sp,
        fontSize = 10.sp
      )
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

    // --- 3. CREATE NEW BUTTON ---
    PlaylistCommandRow(
      label = "CREATE_NEW_PLAYLIST",
      icon = Icons.Rounded.Add,
      isPrimary = true,
      onClick = { showCreateDialog = true }
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

    // --- 4. PLAYLIST LIST ---
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 300.dp)
    ) {
      items(playlists) { playlist ->
        PlaylistCommandRow(
          label = playlist.name.uppercase(),
          subLabel = "ID: ${playlist.playlistId}",
          icon = Icons.Rounded.QueueMusic,
          onClick = { onPlaylistSelected(playlist) }
        )
      }
    }
  }

  // --- DIALOG (Styled to match theme if possible, standard for now) ---
  if (showCreateDialog) {
    AlertDialog(
      onDismissRequest = { showCreateDialog = false },
      title = {
        Text(
          "NEW_PLAYLIST",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        OutlinedTextField(
          value = newPlaylistName,
          onValueChange = { newPlaylistName = it },
          label = { Text("Designation", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (newPlaylistName.isNotBlank()) {
              onCreateNew(newPlaylistName)
              showCreateDialog = false
              newPlaylistName = ""
            }
          }
        ) {
          Text("INITIALIZE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateDialog = false }) {
          Text("CANCEL", fontFamily = FontFamily.Monospace)
        }
      },
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      shape = RoundedCornerShape(2.dp)
    )
  }
}

// --- SUB-COMPONENT (Matches SongOptionSheet CommandRow) ---
@Composable
private fun PlaylistCommandRow(
  label: String,
  subLabel: String? = null,
  icon: ImageVector,
  isPrimary: Boolean = false,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp) // Standard height
      .clickable(onClick = onClick)
      .padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Icon
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.7f),
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(16.dp))

      // Text Column
      Column(verticalArrangement = Arrangement.Center) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold,
          letterSpacing = 1.sp,
          color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.9f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (subLabel != null) {
          Text(
            text = subLabel,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
          )
        }
      }
    }

    // Tech arrow
    Text(
      text = ">>",
      style = MaterialTheme.typography.labelSmall,
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurface.copy(0.2f),
      fontFamily = FontFamily.Monospace
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AddToPlaylistSheetPreview() {
  val mockPlaylists = listOf(
    Playlist(playlistId = 101, name = "Cyberpunk Mix"),
    Playlist(playlistId = 102, name = "Night Drive"),
    Playlist(playlistId = 103, name = "Gym Phonk")
  )

  MaterialTheme {
    AddToPlaylistSheet(
      playlists = mockPlaylists,
      onPlaylistSelected = {},
      onCreateNew = {},
      onDismiss = {},
      bottomPadding = 24.dp
    )
  }
}