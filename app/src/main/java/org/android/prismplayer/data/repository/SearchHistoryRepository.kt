package org.android.prismplayer.data.repository

import kotlinx.coroutines.flow.Flow
import org.android.prismplayer.data.dao.SearchHistoryDao
import org.android.prismplayer.data.model.SearchHistory

class SearchHistoryRepository(private val searchHistoryDao: SearchHistoryDao) {
    val recentSearches: Flow<List<SearchHistory>> = searchHistoryDao.getRecentSearchHistory()

    suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            val trimmed = query.trim()
            searchHistoryDao.deleteSearchQuery(trimmed)
            searchHistoryDao.insertSearchQuery(SearchHistory(query = trimmed))
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteSearchQuery(query)
    }

    suspend fun clearHistory() {
        searchHistoryDao.clearSearchHistory()
    }
}
