package org.android.prismplayer.ui.screens

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.android.prismplayer.ui.components.CustomBottomSheet
import org.android.prismplayer.ui.components.DeleteConfirmationDialog
import org.android.prismplayer.ui.components.DynamicPlaylistCover
import org.android.prismplayer.ui.components.SongListItem
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.android.prismplayer.data.model.Playlist
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.DynamicPlaylistCover // [NEW] Import added
import org.android.prismplayer.ui.components.SongListItem
import org.android.prismplayer.ui.utils.SongArtHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
  playlist: Playlist,
  songs: List<Song>,
  currentSong: Song?,
  isPlaying: Boolean,
  onBack: () -> Unit,
  onPlayPlaylist: (List<Song>) -> Unit,
  onSongClick: (Song) -> Unit,
  onSongMoreClick: (Song) -> Unit, // We will handle "Remove" in the sheet later
  onDeletePlaylist: () -> Unit,
  bottomPadding: Dp
) {
  val coverUris = remember(songs) {
    songs.mapNotNull { it.songArtUri }.distinct().take(4)
  }
  val isFav = playlist.name.equals("Favorites", ignoreCase = true)
  var showDeleteDialog by remember { mutableStateOf(false) }
  val accentColor = MaterialTheme.colorScheme.tertiary

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Column {
        CenterAlignedTopAppBar(
          title = {
            Text(
              "USER_COMPILATION",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp,
              color = accentColor
            )
          },
          navigationIcon = {
            IconButton(onClick = onBack) {
              Icon(Icons.AutoMirrored.Outlined.ArrowBack, "RETURN", tint = MaterialTheme.colorScheme.onBackground)
            }
          },
          actions = {
            IconButton(onClick = { showDeleteDialog = true }) {
              Icon(Icons.Outlined.Delete, "DELETE", tint = MaterialTheme.colorScheme.error)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
      }
    }
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize()) {
      // Ambient Glow Background
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
          .background(
            Brush.verticalGradient(
              colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
            )
          )
      )

      LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 24.dp, bottom = bottomPadding + 24.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        // --- HEADER SECTION ---
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp)
              .height(IntrinsicSize.Min)
          ) {
            Box(
              modifier = Modifier
                .size(140.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                .padding(4.dp),
              contentAlignment = Alignment.Center
            ) {
              DynamicPlaylistCover(
                isFavorite = isFav,
                imageUris = coverUris,
                modifier = Modifier.fillMaxSize()
              )

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.linearGradient(
                      colors = listOf(MaterialTheme.colorScheme.surface.copy(0.1f), Color.Transparent),
                      start = androidx.compose.ui.geometry.Offset(0f, 0f),
                      end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                  )
              )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Metadata
            Column(
              verticalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxHeight().weight(1f)
            ) {
              MetadataField("PLAYLIST_ID", playlist.name.uppercase(), isHeader = true, color = MaterialTheme.colorScheme.onSurface)

              Spacer(Modifier.height(8.dp))

              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MetadataField("CREATED", "LOCAL_USER", color = accentColor)
                MetadataField("FILE_COUNT", "${songs.size} TRACKS")
                MetadataField("TOTAL_LEN", calculateTotalDuration(songs))
              }
            }
          }
        }

        // --- CONTROLS ---
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = { if (songs.isNotEmpty()) onPlayPlaylist(songs) },
              modifier = Modifier.weight(1f).height(52.dp),
              shape = RoundedCornerShape(2.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = MaterialTheme.colorScheme.onTertiary
              ),
              contentPadding = PaddingValues(0.dp),
              enabled = songs.isNotEmpty()
            ) {
              Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(20.dp))
              Spacer(Modifier.width(8.dp))
              Text("INITIALIZE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Box(
              modifier = Modifier
                .width(52.dp)
                .height(52.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f), RoundedCornerShape(2.dp))
                .clickable(enabled = songs.isNotEmpty()) { onPlayPlaylist(songs.shuffled()) },
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
          HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f), modifier = Modifier.padding(bottom = 8.dp))
        }

        if (songs.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "EMPTY_CONTAINER // ADD_FILES",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(0.3f)
              )
            }
          }
        } else {
          itemsIndexed(songs) { index, song ->
            val isActive = currentSong?.id == song.id
            SongListItem(
              song = song,
              isActive = isActive,
              isPlaying = isPlaying,
              index = index + 1,
              showDuration = true,
              onClick = { onSongClick(song) },
              onMoreClick = { onSongMoreClick(song) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
          }
        }
      }
    }
  }

  // Delete Confirmation Bottom Sheet
  CustomBottomSheet(
    visible = showDeleteDialog,
    onDismiss = { showDeleteDialog = false }
  ) {
    DeleteConfirmationDialog(
      presetName = playlist.name,
      onConfirm = {
        onDeletePlaylist()
        showDeleteDialog = false
      },
      onDismiss = { showDeleteDialog = false }
    )
  }
}

@Preview(
  showBackground = true,
  backgroundColor = 0xFF050505, // Dark background to match app theme
  heightDp = 800
)
@Composable
fun PlaylistDetailScreenPreview() {
  val mockPlaylist = Playlist(
    playlistId = 1L,
    name = "Cyberpunk Night Drive",
    createdAt = System.currentTimeMillis()
  )

  val mockSongs = listOf(
    Song(
      id = 1L,
      title = "Midnight City",
      artist = "M83",
      albumName = "Hurry Up, We're Dreaming",
      albumId = 101L,
      duration = 243000L,
      path = "",
      folderName = "",
      dateAdded = 0L,
      songArtUri = null,
      year = 2011,
      trackNumber = 1,
      genre = "Electronic"
    ),
    Song(
      id = 2L,
      title = "Nightcall",
      artist = "Kavinsky",
      albumName = "OutRun",
      albumId = 102L,
      duration = 258000L,
      path = "",
      folderName = "",
      dateAdded = 0L,
      songArtUri = null,
      year = 2013,
      trackNumber = 2,
      genre = "Synthwave"
    ),
    Song(
      id = 3L,
      title = "Resonance",
      artist = "Home",
      albumName = "Odyssey",
      albumId = 103L,
      duration = 212000L,
      path = "",
      folderName = "",
      dateAdded = 0L,
      songArtUri = null,
      year = 2014,
      trackNumber = 3,
      genre = "Vaporwave"
    )
  )

  MaterialTheme {
    PlaylistDetailScreen(
      playlist = mockPlaylist,
      songs = mockSongs,
      currentSong = mockSongs[0],
      isPlaying = true,
      onBack = {},
      onPlayPlaylist = {},
      onSongClick = {},
      onSongMoreClick = {},
      onDeletePlaylist = {},
      bottomPadding = 80.dp
    )
  }
}