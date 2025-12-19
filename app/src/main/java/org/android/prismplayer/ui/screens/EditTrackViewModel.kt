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
import org.android.prismplayer.data.repository.MetadataRepository
import org.android.prismplayer.data.repository.MusicRepository
import org.android.prismplayer.data.source.NativeMetadataSource

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
    private val metadataRepository: MetadataRepository,
    private val application: PrismApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<EditEvent>()
    val events = _eventChannel.receiveAsFlow()

    // Holds the pending operation in case we need to pause for Permissions
    private var pendingOperation: (() -> Unit)? = null

    fun loadSong(songId: Long) {
        _uiState.value = EditUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val dbSong = repository.getSongById(songId)
            if (dbSong != null) {
                // Read fresh tags from disk to ensure accuracy
                val realSong = metadataRepository.readTags(dbSong)
                _uiState.value = EditUiState.Content(realSong)
            } else {
                _uiState.value = EditUiState.Error("Song not found")
            }
        }
    }

    /**
     * SMART ENTRY POINT: Called by UI.
     * Takes raw strings, validates them, and initiates the save process.
     */
    fun onSaveClicked(
        originalSong: Song,
        title: String,
        artist: String,
        album: String,
        yearInput: String,
        genre: String,
        trackInput: String,
        currentArtUri: String?
    ) {
        // 1. Validation Logic (Moved out of Composable)
        val cleanYear = yearInput.filter { it.isDigit() }.toIntOrNull() ?: 0
        val cleanTrack = trackInput.filter { it.isDigit() }.toIntOrNull() ?: 0

        // 2. Logic: Did the Album Art actually change?
        // If the URI is the same as the original, or it looks like the default system URI,
        // we usually don't need to re-write the image bytes unless the user picked a new one.
        val defaultUri = "content://media/external/audio/media/${originalSong.id}/albumart"
        val artToSave = if (currentArtUri != originalSong.songArtUri && currentArtUri != defaultUri) {
            currentArtUri
        } else {
            null // Null means "Don't touch the cover art"
        }

        // 3. Create the sanitized object
        val songToSave = originalSong.copy(
            title = title.trim(),
            artist = artist.trim(),
            albumName = album.trim(),
            year = cleanYear,
            genre = genre.trim(),
            trackNumber = cleanTrack,
            songArtUri = currentArtUri
        )

        executeSave(songToSave, artToSave)
    }

    /**
     * Internal save logic.
     * @param song The song object with updated text fields.
     * @param newImageUri The URI of the *new* image to write to the file. If null, image is skipped.
     */
    private fun executeSave(song: Song, newImageUri: String?) {
        // Store this operation in case we get a Permission Denial and need to retry later
        pendingOperation = { executeSave(song, newImageUri) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Write to File (Text + Optional Image)
                val success = metadataRepository.writeTags(song, newImageUri)

                if (success) {
                    val timestamp = System.currentTimeMillis() / 1000
                    val finalSong = song.copy(dateModified = timestamp)

                    // Update Database & System ID mapping
                    repository.updateSongIdAndMetadata(song.id, song.id, finalSong)

                    _eventChannel.send(EditEvent.SaveSuccess)
                    pendingOperation = null
                } else {
                    _uiState.value = EditUiState.Error("Failed to write tags to file")
                }

            } catch (securityException: SecurityException) {
                handlePermissionError(securityException, song.id)
            } catch (e: Exception) {
                val cause = e.cause
                if (cause is SecurityException) {
                    handlePermissionError(cause, song.id)
                } else {
                    e.printStackTrace()
                    _uiState.value = EditUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun onPermissionGranted() {
        pendingOperation?.invoke()
    }

    private fun handlePermissionError(e: SecurityException, songId: Long) {
        val intentSender = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                MediaStore.createWriteRequest(
                    application.contentResolver,
                    listOf(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId))
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
                val metadataSource = NativeMetadataSource(app)
                return EditTrackViewModel(app.repository, metadataSource, app) as T
            }
        }
    }
}