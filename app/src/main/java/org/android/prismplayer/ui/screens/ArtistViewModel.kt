package org.android.prismplayer.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository

data class ArtistUiState(
    val artistName: String = "",
    val heroArtUri: String? = null,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val totalDuration: String = "",
    val isLoading: Boolean = true
)

class ArtistViewModel(
    savedStateHandle: SavedStateHandle,
    repository: MusicRepository
) : ViewModel() {

    private val artistName: String = checkNotNull(savedStateHandle["artistName"]) {
        "artistName is required"
    }

    val uiState: StateFlow<ArtistUiState> = combine(
        repository.getAllSongs(),
        repository.getAlbums()
    ) { allSongs, allAlbums ->
        val artistSongs = allSongs.filter { it.artist.equals(artistName, ignoreCase = true) }
        val artistAlbums = allAlbums.filter { it.artist.equals(artistName, ignoreCase = true) }
        val heroImage = artistAlbums.firstOrNull { it.coverUri != null }?.coverUri
            ?: artistSongs.firstOrNull { it.songArtUri != null }?.songArtUri
        val totalMs = artistSongs.sumOf { it.duration }
        val durationStr = formatDurationHours(totalMs)

        ArtistUiState(
            artistName = artistName,
            heroArtUri = heroImage,
            songs = artistSongs.sortedByDescending { it.year },
            albums = artistAlbums,
            totalDuration = durationStr,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtistUiState(isLoading = true)
    )

    private fun formatDurationHours(ms: Long): String {
        val hours = ms / (1000 * 60 * 60)
        val mins = (ms % (1000 * 60 * 60)) / (1000 * 60)
        return if (hours > 0) "${hours}h ${mins}m" else "${mins} mins"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                return ArtistViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    repository = app.repository
                ) as T
            }
        }
    }
}