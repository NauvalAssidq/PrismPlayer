package org.android.prismplayer.ui.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import org.android.prismplayer.data.model.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.StandardArtwork
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object TagEditor {

    fun writeTags(context: Context, song: Song, pickedArtUri: String?) {
        if (song.path.isBlank()) throw IllegalArgumentException("Invalid File Path")

        val originalUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id
        )

        val ext = song.path.substringAfterLast('.', "mp3")
        val tempFile = File.createTempFile("temp_edit_${song.id}_", ".$ext", context.cacheDir)

        try {
            context.contentResolver.openInputStream(originalUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Failed to open input stream")

            val audioFile = AudioFileIO.read(tempFile)
            val tag = audioFile.tagOrCreateAndSetDefault

            if (song.title.isNotBlank()) tag.setField(FieldKey.TITLE, song.title)
            if (song.artist.isNotBlank()) tag.setField(FieldKey.ARTIST, song.artist)
            if (song.albumName.isNotBlank()) tag.setField(FieldKey.ALBUM, song.albumName)
            if (song.artist.isNotBlank()) tag.setField(FieldKey.ALBUM_ARTIST, song.artist)
            if (song.genre.isNotBlank()) tag.setField(FieldKey.GENRE, song.genre)
            if (song.year > 0) tag.setField(FieldKey.YEAR, song.year.toString())
            if (song.trackNumber > 0) tag.setField(FieldKey.TRACK, song.trackNumber.toString())

            if (!pickedArtUri.isNullOrEmpty()) {
                val originalBitmap = getBitmapFromUri(context, pickedArtUri.toUri())

                if (originalBitmap != null) {
                    val resizedBitmap = resizeBitmap(originalBitmap, 1000)

                    tag.deleteArtworkField()

                    val stream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val byteArray = stream.toByteArray()

                    val artwork = StandardArtwork()
                    artwork.binaryData = byteArray
                    artwork.mimeType = "image/jpeg"
                    artwork.pictureType = 3
                    artwork.isLinked = false

                    tag.setField(artwork)
                }
            }

            audioFile.commit()

            context.contentResolver.openOutputStream(originalUri, "wt")?.use { output ->
                FileInputStream(tempFile).use { input ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Failed to open output stream")

            MediaScannerConnection.scanFile(context, arrayOf(song.path), null) { _, _ -> }

        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    fun readTags(context: Context, song: Song): Song {
        val file = File(song.path)
        if (!file.exists()) return song

        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag

            song.copy(
                title = tag.getFirst(FieldKey.TITLE).ifBlank { song.title },
                artist = tag.getFirst(FieldKey.ARTIST).ifBlank { song.artist },
                albumName = tag.getFirst(FieldKey.ALBUM).ifBlank { song.albumName },
                genre = tag.getFirst(FieldKey.GENRE).ifBlank { song.genre },
                year = tag.getFirst(FieldKey.YEAR).toIntOrNull() ?: song.year,
                trackNumber = tag.getFirst(FieldKey.TRACK).toIntOrNull() ?: song.trackNumber
            )
        } catch (e: Exception) {
            song
        }
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resizeBitmap(source: Bitmap, maxSize: Int): Bitmap {
        var width = source.width
        var height = source.height
        if (width <= maxSize && height <= maxSize) return source

        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}