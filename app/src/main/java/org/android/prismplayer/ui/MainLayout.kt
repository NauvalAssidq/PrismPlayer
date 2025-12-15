package org.android.prismplayer.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.CustomBottomSheet
import org.android.prismplayer.ui.components.PrismNavBar
import org.android.prismplayer.ui.components.PrismTab
import org.android.prismplayer.ui.components.SongOptionSheet
import org.android.prismplayer.ui.player.AudioViewModel
import org.android.prismplayer.ui.player.FullPlayerScreen
import org.android.prismplayer.ui.player.MiniPlayer
import org.android.prismplayer.ui.screens.HomeScreen
import org.android.prismplayer.ui.screens.HomeViewModel
import org.android.prismplayer.ui.screens.LibraryScreen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainLayout(
    audioViewModel: AudioViewModel,
    homeViewModel: HomeViewModel,
    onOpenAlbum: (Long) -> Unit,
    onEditSong: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(PrismTab.HOME) }
    var isFullPlayerOpen by remember { mutableStateOf(false) }
    val homeState by homeViewModel.uiState.collectAsState()
    val currentSong by audioViewModel.currentSong.collectAsState()
    val queue by audioViewModel.queue.collectAsState()
    val isPlaying by audioViewModel.isPlaying.collectAsState()
    val progress by audioViewModel.progress.collectAsState()
    val currentTime by audioViewModel.currentTime.collectAsState()
    val repeatMode by audioViewModel.repeatMode.collectAsState()
    val isShuffleEnabled by audioViewModel.isShuffleEnabled.collectAsState()
    var songForOptions by remember { mutableStateOf<Song?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(homeViewModel.allSongs) {
        audioViewModel.setLibrary(homeViewModel.allSongs.value)
    }

    Scaffold(
        containerColor = Color(0xFF050505),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {}
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    PrismTab.HOME -> HomeScreen(
                        state = homeState,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, list ->
                            audioViewModel.playSong(song, list)
                        },
                        onSeeAllSongs = { currentTab = PrismTab.LIBRARY },
                        onOpenAlbums = { currentTab = PrismTab.LIBRARY },
                        onOpenArtists = { currentTab = PrismTab.LIBRARY },
                        onAlbumClick = onOpenAlbum,
                        onSongMoreClick = { song -> songForOptions = song },
                        onSettingsClick = onSettingsClick
                    )

                    PrismTab.SEARCH -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Search Coming Soon", color = Color.White)
                        }
                    }

                    PrismTab.LIBRARY -> {
                        LibraryScreen(
                            state = homeState,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = { song, list ->
                                audioViewModel.playSong(song, list)
                            },
                            onSongMoreClick = { song -> songForOptions = song },
                            onAlbumClick = onOpenAlbum
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedVisibility(
                    visible = currentSong != null && !isFullPlayerOpen,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    if (currentSong != null) {
                        MiniPlayer(
                            song = currentSong!!,
                            isPlaying = isPlaying,
                            progress = progress,
                            onTogglePlay = { audioViewModel.togglePlayPause() },
                            onSkipNext = { audioViewModel.skipNext() },
                            onClick = { isFullPlayerOpen = true }
                        )
                    }
                }

                PrismNavBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }

            AnimatedVisibility(
                visible = isFullPlayerOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                if (currentSong != null) {
                    FullPlayerScreen(
                        song = currentSong!!,
                        queue = queue,
                        isPlaying = isPlaying,
                        progress = progress,
                        currentTime = formatTime(currentTime),
                        totalTime = formatTime(currentSong!!.duration),
                        repeatMode = repeatMode,
                        isShuffleEnabled = isShuffleEnabled,
                        onPlayPause = { audioViewModel.togglePlayPause() },
                        onNext = { audioViewModel.skipNext() },
                        onPrev = { audioViewModel.skipPrev() },
                        onSeek = { frac -> audioViewModel.seekTo(frac) },
                        onToggleRepeat = { audioViewModel.toggleRepeat() },
                        onToggleShuffle = { audioViewModel.toggleShuffle() },
                        onClose = { isFullPlayerOpen = false },
                        onQueueItemClick = { clickedSong ->
                            audioViewModel.playQueueItem(clickedSong)
                        },
                        onRemoveFromQueue = { songToRemove ->
                            audioViewModel.removeSongFromQueue(songToRemove)
                        },
                        onQueueReorder = { newList ->
                            audioViewModel.updateQueue(newList)
                        },
                        audioViewModel = audioViewModel
                    )
                }
            }

            if (songForOptions != null) {
                CustomBottomSheet(
                    visible = true,
                    onDismiss = { songForOptions = null }
                ) {
                    SongOptionSheet(
                        song = songForOptions!!,
                        onPlayNext = {
                            songForOptions = null
                        },
                        onAddToQueue = {
                            songForOptions = null
                        },
                        onAddToPlaylist = {
                            songForOptions = null
                        },
                        onGoToAlbum = {
                            val albumId = songForOptions!!.albumId
                            songForOptions = null
                            onOpenAlbum(albumId)
                        },
                        onShare = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "audio/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(songForOptions!!.path))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Song"))
                            songForOptions = null
                        },
                        onEdit = {
                            val songId = songForOptions!!.id
                            songForOptions = null
                            onEditSong(songId)
                        },
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}