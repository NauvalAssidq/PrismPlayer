package org.android.prismplayer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.model.Album
import org.android.prismplayer.data.model.SearchResult
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository

data class HomeState(
    val isLoading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val errorMessage: String? = null,
    val totalSongCount: Int = 0,
    val totalAlbumCount: Int = 0
)

class HomeViewModel(
    val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    val allSongs = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAlbums = repository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<SearchResult> = combine(
        allSongs,
        allAlbums,
        _searchQuery
    ) { songs, albums, query ->
        if (query.isBlank()) {
            SearchResult()
        } else {
            val trimmedQuery = query.trim()
            val matchedSongs = songs.filter {
                it.title.contains(trimmedQuery, true) || it.artist.contains(trimmedQuery, true)
            }.take(10)

            val matchedAlbums = albums.filter {
                it.title.contains(trimmedQuery, true) || it.artist.contains(trimmedQuery, true)
            }.take(4)

            val matchedArtists = (matchedSongs.map { it.artist } + matchedAlbums.map { it.artist })
                .distinct()
                .filter { it.contains(trimmedQuery, true) }
                .take(3)

            SearchResult(
                songs = matchedSongs,
                albums = matchedAlbums,
                artists = matchedArtists
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchResult()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            combine(
                repository.getQuickPlaySongs(),
                allAlbums,
                allSongs
            ) { songs, albums, fullSongList ->
                HomeState(
                    isLoading = false,
                    songs = songs,
                    albums = albums.take(10),
                    errorMessage = if (songs.isEmpty() && albums.isEmpty()) "No music found." else null,
                    totalSongCount = fullSongList.size,
                    totalAlbumCount = albums.size
                )
            }
                .catch { exception ->
                    _uiState.value = HomeState(
                        isLoading = false,
                        errorMessage = "Failed to load data: ${exception.message}"
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                return HomeViewModel(
                    repository = application.repository
                ) as T
            }
        }
    }
}