package org.android.prismplayer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository

class RecentlyPlayedViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    val recentlyPlayedSongs: StateFlow<List<Song>> = repository.getRecentlyPlayedSongs(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun logSongPlayed(song: Song) {
        viewModelScope.launch {
            repository.recordPlay(song)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                return RecentlyPlayedViewModel(app.repository) as T
            }
        }
    }
}
