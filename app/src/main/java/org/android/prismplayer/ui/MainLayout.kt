package org.android.prismplayer.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import org.android.prismplayer.ui.utils.formatTime
import org.android.prismplayer.ui.theme.PrismColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.ui.components.CustomBottomSheet
import org.android.prismplayer.ui.components.PrismNavBar
import org.android.prismplayer.ui.components.PrismTab
import org.android.prismplayer.ui.components.SongOptionSheet
import org.android.prismplayer.ui.player.AudioViewModel
import org.android.prismplayer.ui.player.FullPlayerScreen
import org.android.prismplayer.ui.player.MiniPlayer
import org.android.prismplayer.ui.screens.AlbumDetailScreen
import org.android.prismplayer.ui.screens.AlbumViewModel
import org.android.prismplayer.ui.screens.ArtistScreen
import org.android.prismplayer.ui.screens.ArtistViewModel
import org.android.prismplayer.ui.screens.EqualizerScreen
import org.android.prismplayer.ui.screens.HomeScreen
import org.android.prismplayer.ui.screens.HomeViewModel
import org.android.prismplayer.ui.screens.LibraryScreen
import org.android.prismplayer.ui.screens.SearchScreen
import org.android.prismplayer.ui.screens.SettingsScreen

enum class SheetContext {
    HOME, LIBRARY, ALBUM, ARTIST, SEARCH, PLAYER
}



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainLayout(
    audioViewModel: AudioViewModel,
    homeViewModel: HomeViewModel,
    onEditSong: (Long) -> Unit,
    onReselectFolders: () -> Unit,
    expandPlayer: Boolean,
    onExpandConsumed: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(PrismTab.HOME) }
    var isFullPlayerOpen by remember { mutableStateOf(false) }
    var libraryTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val homeState by homeViewModel.uiState.collectAsState()
    val currentSong by audioViewModel.currentSong.collectAsState()
    val queue by audioViewModel.queue.collectAsState()
    val isPlaying by audioViewModel.isPlaying.collectAsState()
    val progress by audioViewModel.progress.collectAsState()
    val currentTime by audioViewModel.currentTime.collectAsState()
    val repeatMode by audioViewModel.repeatMode.collectAsState()
    val isShuffleEnabled by audioViewModel.isShuffleEnabled.collectAsState()
    var optionsState by remember { mutableStateOf<Pair<Song, SheetContext>?>(null) }
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val searchResults by homeViewModel.searchResults.collectAsState()
    var selectedArtist by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAlbumName by rememberSaveable { mutableStateOf<String?>(null) }

    var isEqualizerOpen by rememberSaveable { mutableStateOf(false) }
    val globalBottomPadding = if (currentSong != null) 178.dp else 88.dp
    val allSongs by homeViewModel.allSongs.collectAsState()
    val backdrop = rememberLayerBackdrop()
    LaunchedEffect(expandPlayer) {
        if (expandPlayer) {
            isFullPlayerOpen = true
            onExpandConsumed()
        }
    }

    LaunchedEffect(allSongs) {
        if (allSongs.isNotEmpty()) {
            audioViewModel.setLibrary(allSongs)
            audioViewModel.setRepository(homeViewModel.repository)
        }
    }

    BackHandler(enabled = isFullPlayerOpen) { isFullPlayerOpen = false }
    BackHandler(enabled = !isFullPlayerOpen && isEqualizerOpen) { isEqualizerOpen = false }
    BackHandler(enabled = !isFullPlayerOpen && !isEqualizerOpen && selectedAlbumName != null) { selectedAlbumName = null }
    BackHandler(enabled = !isFullPlayerOpen && !isEqualizerOpen && selectedAlbumName == null && selectedArtist != null) { selectedArtist = null }
    BackHandler(enabled = !isFullPlayerOpen && !isEqualizerOpen && selectedAlbumName == null && selectedArtist == null && currentTab != PrismTab.HOME) { currentTab = PrismTab.HOME }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {}
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                when (currentTab) {
                    PrismTab.HOME -> HomeScreen(
                        state = homeState,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, list -> audioViewModel.playSong(song, list) },
                        onSeeAllSongs = {
                            libraryTabIndex = 0
                            currentTab = PrismTab.LIBRARY
                        },
                        onOpenAlbums = {
                            libraryTabIndex = 1
                            currentTab = PrismTab.LIBRARY
                        },
                        onOpenArtists = {
                            libraryTabIndex = 2
                            currentTab = PrismTab.LIBRARY
                        },
                        onAlbumClick = { selectedAlbumName = it },
                        onSongMoreClick = { song -> optionsState = song to SheetContext.HOME },
                        onSettingsClick = { currentTab = PrismTab.SETTING },
                        bottomPadding = globalBottomPadding,
                    )

                    PrismTab.SEARCH -> SearchScreen(
                        query = searchQuery,
                        results = searchResults,
                        onQueryChange = { homeViewModel.onSearchQueryChanged(it) },
                        onSongClick = { song ->
                            audioViewModel.playSong(
                                song,
                                searchResults.songs
                            )
                        },
                        onAlbumClick = { selectedAlbumName = it },
                        onArtistClick = { selectedArtist = it },
                        onSongMoreClick = { song -> optionsState = song to SheetContext.SEARCH },
                        bottomPadding = globalBottomPadding,
                        currentSong = currentSong,
                        isPlaying = isPlaying
                    )

                    PrismTab.LIBRARY -> LibraryScreen(
                        state = homeState,
                        songs = allSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, list -> audioViewModel.playSong(song, list) },
                        onAlbumClick = { selectedAlbumName = it },
                        onArtistClick = { selectedArtist = it },
                        onSongMoreClick = { song -> optionsState = song to SheetContext.LIBRARY },
                        bottomPadding = globalBottomPadding,
                        initialPage = libraryTabIndex,
                        onPageChanged = { newIndex ->
                            libraryTabIndex = newIndex
                        },
                    )

                    PrismTab.SETTING -> SettingsScreen(
                        onBack = { currentTab = PrismTab.HOME },
                        onOpenEqualizer = { isEqualizerOpen = true },
                        bottomPadding = globalBottomPadding,
                        onReselectFolders = onReselectFolders
                    )
                }
            }

            if (selectedArtist != null) {
                val owner = LocalViewModelStoreOwner.current
                val defaultExtras = (owner as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras
                    ?: CreationExtras.Empty

                val artistViewModel: ArtistViewModel = viewModel(
                    key = "artist_$selectedArtist",
                    factory = ArtistViewModel.Factory,
                    extras = MutableCreationExtras(defaultExtras).apply {
                        set(DEFAULT_ARGS_KEY, bundleOf("artistName" to selectedArtist))
                    }
                )
                val artistState by artistViewModel.uiState.collectAsState()

                ArtistScreen(
                    state = artistState,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onBack = { selectedArtist = null },
                    onSongClick = { song, list -> audioViewModel.playSong(song, list) },
                    onAlbumClick = { selectedAlbumName = it },
                    onShufflePlay = { list ->
                        if (list.isNotEmpty()) audioViewModel.playSong(
                            list.shuffled().first(), list.shuffled()
                        )
                    },
                    onSongMoreClick = { song -> optionsState = song to SheetContext.ARTIST },
                    bottomPadding = globalBottomPadding,
                )
            }

            if (selectedAlbumName != null) {
                val owner = LocalViewModelStoreOwner.current
                val defaultExtras = (owner as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras
                    ?: CreationExtras.Empty

                val albumViewModel: AlbumViewModel = viewModel(
                    key = "album_$selectedAlbumName",
                    factory = AlbumViewModel.Factory,
                    extras = MutableCreationExtras(defaultExtras).apply {
                        // Pass albumName instead of albumId
                        set(DEFAULT_ARGS_KEY, bundleOf("albumName" to selectedAlbumName))
                    }
                )
                val albumState by albumViewModel.uiState.collectAsState()

                AlbumDetailScreen(
                    albumId = 0L,
                    albumName = albumState.albumName,
                    artistName = albumState.artistName,
                    artUri = albumState.artUri,
                    songs = albumState.songs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onBack = { selectedAlbumName = null },
                    onPlayAlbum = { list -> audioViewModel.playSong(list.first(), list) },
                    onSongClick = { song, list -> audioViewModel.playSong(song, list) },
                    onSongMoreClick = { song -> optionsState = song to SheetContext.ALBUM },
                    bottomPadding = globalBottomPadding
                )
            }

            AnimatedVisibility(
                visible = isEqualizerOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EqualizerScreen(
                    viewModel = audioViewModel,
                    onBack = { isEqualizerOpen = false }
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
                        onToggleRepeat = { audioViewModel.toggleRepeat() },
                        onToggleShuffle = { audioViewModel.toggleShuffle() },
                        onClose = { isFullPlayerOpen = false },
                        onQueueItemClick = { clickedSong -> audioViewModel.playQueueItem(clickedSong) },
                        onRemoveFromQueue = { songToRemove -> audioViewModel.removeSongFromQueue(songToRemove) },
                        onQueueReorder = { from, to ->
                            audioViewModel.moveQueueItem(from, to)
                        },
                        audioViewModel = audioViewModel
                    )
                }
            }

            if (optionsState != null) {
                BackHandler {
                    optionsState = null
                }
                val song = optionsState!!.first
                val source = optionsState!!.second

                CustomBottomSheet(
                    visible = true,
                    onDismiss = { optionsState = null }
                ) {
                    SongOptionSheet(
                        song = song,
                        onPlayNext = { optionsState = null },
                        onAddToQueue = { optionsState = null },
                        onAddToPlaylist = { optionsState = null },
                        onGoToAlbum = if (source != SheetContext.ALBUM) {
                            {
                                val albumName = song.albumName // CHANGE 8: Use Name
                                optionsState = null
                                selectedAlbumName = albumName
                            }
                        } else null,
                        onGoToArtist = if (source != SheetContext.ARTIST) {
                            {
                                val artistName = song.artist
                                optionsState = null
                                selectedAlbumName = null
                                selectedArtist = artistName
                            }
                        } else null,
                        onShare = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "audio/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(song.path))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Song"))
                            optionsState = null
                        },
                        onEdit = {
                            val songId = song.id
                            optionsState = null
                            onEditSong(songId)
                        },
                        bottomPadding = globalBottomPadding
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedVisibility(
                    visible = currentSong != null && !isFullPlayerOpen && !isEqualizerOpen,
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

                AnimatedVisibility(
                    visible = !isFullPlayerOpen && !isEqualizerOpen,
                ) {
                    PrismNavBar(
                        currentTab = currentTab,
                        onTabSelected = {
                            currentTab = it
                            selectedArtist = null
                            selectedAlbumName = null
                        },
                    )
                }
            }
        }
    }
}