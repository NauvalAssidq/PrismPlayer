package org.android.prismplayer.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.repository.MusicRepository

data class AlbumUiState(
    val albumName: String = "",
    val artistName: String = "",
    val artUri: String? = null,
    val songs: List<org.android.prismplayer.data.model.Song> = emptyList(),
    val isLoading: Boolean = true
)

class AlbumViewModel(
    savedStateHandle: SavedStateHandle,
    repository: MusicRepository
) : ViewModel() {

    private val albumId: Long = checkNotNull(savedStateHandle["albumId"]) {
        "albumId is required in SavedStateHandle"
    }

    val uiState: StateFlow<AlbumUiState> =
        repository.getSongsByAlbum(albumId)
            .map { songs ->
                if (songs.isNotEmpty()) {
                    val first = songs.first()
                    AlbumUiState(
                        albumName = first.albumName,
                        artistName = first.artist,
                        artUri = first.songArtUri,
                        songs = songs,
                        isLoading = false
                    )
                } else {
                    AlbumUiState(
                        songs = emptyList(),
                        isLoading = false
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AlbumUiState(isLoading = true)
            )

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                return AlbumViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    repository = app.repository
                ) as T
            }
        }
    }
}
