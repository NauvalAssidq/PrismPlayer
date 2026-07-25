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
import org.android.prismplayer.data.model.SearchHistory
import org.android.prismplayer.data.repository.SearchHistoryRepository

class SearchHistoryViewModel(
    private val repository: SearchHistoryRepository
) : ViewModel() {

    val recentSearches: StateFlow<List<SearchHistory>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            repository.addSearchQuery(query)
        }
    }

    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                val searchHistoryDao = app.database.searchHistoryDao()
                val repository = SearchHistoryRepository(searchHistoryDao)
                return SearchHistoryViewModel(repository) as T
            }
        }
    }
}
