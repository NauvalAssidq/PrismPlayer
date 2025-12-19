package org.android.prismplayer.ui.screens

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.android.prismplayer.PrismApplication
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.repository.MusicRepository
import org.android.prismplayer.ui.utils.TagEditor

sealed class EditUiState {
    object Loading : EditUiState()
    data class Content(val song: Song) : EditUiState()
    data class Error(val message: String) : EditUiState()
}

sealed class EditEvent {
    data class RequestPermission(val intentSender: IntentSender) : EditEvent()
    object SaveSuccess : EditEvent()
}

class EditTrackViewModel(
    private val repository: MusicRepository,
    private val application: PrismApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<EditEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var pendingSong: Song? = null

    fun loadSong(songId: Long) {
        _uiState.value = EditUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val dbSong = repository.getSongById(songId)
            if (dbSong != null) {
                val realSong = TagEditor.readTags(application, dbSong)
                _uiState.value = EditUiState.Content(realSong)
            } else {
                _uiState.value = EditUiState.Error("Song not found")
            }
        }
    }

    fun saveSong(song: Song) {
        pendingSong = song

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = TagEditor.writeTags(application, song, song.songArtUri)

                if (success) {
                    val timestamp = System.currentTimeMillis() / 1000
                    val updatedSong = song.copy(dateModified = timestamp)
                    repository.updateSongIdAndMetadata(song.id, song.id, updatedSong)

                    _eventChannel.send(EditEvent.SaveSuccess)
                    pendingSong = null
                } else {
                    _uiState.value = EditUiState.Error("Failed to write tags")
                }

            } catch (securityException: SecurityException) {
                handlePermissionError(securityException, song)
            } catch (e: Exception) {
                val cause = e.cause
                if (cause is SecurityException) {
                    handlePermissionError(cause, song)
                } else {
                    e.printStackTrace()
                    _uiState.value = EditUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun onPermissionGranted() {
        pendingSong?.let {
            saveSong(it)
        }
    }

    private fun handlePermissionError(e: SecurityException, song: Song) {
        val intentSender = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                MediaStore.createWriteRequest(
                    application.contentResolver,
                    listOf(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id))
                ).intentSender
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
            }
            else -> null
        }

        if (intentSender != null) {
            viewModelScope.launch {
                _eventChannel.send(EditEvent.RequestPermission(intentSender))
            }
        } else {
            _uiState.value = EditUiState.Error("Permission required but cannot be requested")
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as PrismApplication
                return EditTrackViewModel(app.repository, app) as T
            }
        }
    }
}