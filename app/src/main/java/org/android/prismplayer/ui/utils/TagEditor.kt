package org.android.prismplayer.ui.utils

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.kyant.taglib.Picture
import com.kyant.taglib.PropertyMap
import com.kyant.taglib.TagLib
import org.android.prismplayer.data.model.Song
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object TagEditor {

    fun writeTags(context: Context, song: Song, pickedArtUri: String?): Boolean {
        val ext = song.path.substringAfterLast('.', "mp3")
        val tempFile = File(context.cacheDir, "edit_buffer_${System.currentTimeMillis()}.$ext")

        try {
            val originalUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)

            context.contentResolver.openInputStream(originalUri)?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            } ?: return false

            val currentProperties = try {
                ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    val nativeFd = pfd.dup().detachFd()
                    TagLib.getMetadata(nativeFd)?.propertyMap?.toMutableMap()
                }
            } catch (e: Exception) { null } ?: mutableMapOf()

            // 3. Update Text Data
            if (song.title.isNotBlank()) currentProperties["TITLE"] = arrayOf(song.title)
            if (song.artist.isNotBlank()) currentProperties["ARTIST"] = arrayOf(song.artist)
            if (song.albumName.isNotBlank()) currentProperties["ALBUM"] = arrayOf(song.albumName)
            if (song.genre.isNotBlank()) currentProperties["GENRE"] = arrayOf(song.genre)
            if (song.year > 0) currentProperties["DATE"] = arrayOf(song.year.toString())
            if (song.trackNumber > 0) currentProperties["TRACKNUMBER"] = arrayOf(song.trackNumber.toString())

            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
                val nativeFd = pfd.dup().detachFd()
                if (!TagLib.savePropertyMap(nativeFd, currentProperties as PropertyMap)) {
                    throw Exception("Native Save Failed: Text")
                }
                try { pfd.fileDescriptor.sync() } catch (e: Exception) {}
            }

            if (!pickedArtUri.isNullOrEmpty()) {
                val bitmap = getBitmapFromUri(context, Uri.parse(pickedArtUri))
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    val mimeType = getMimeType(context, Uri.parse(pickedArtUri)) ?: "image/jpeg"
                    val format = if (mimeType == "image/png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

                    bitmap.compress(format, 90, stream)

                    val newPic = Picture(
                        data = stream.toByteArray(),
                        description = "Cover",
                        pictureType = "Front Cover",
                        mimeType = mimeType
                    )

                    ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
                        val nativeFd = pfd.dup().detachFd()
                        TagLib.savePictures(nativeFd, arrayOf(newPic))
                        try { pfd.fileDescriptor.sync() } catch (e: Exception) {}
                    }
                }
            }

            try {
                context.contentResolver.openFileDescriptor(originalUri, "rwt")?.use { targetPfd ->
                    FileOutputStream(targetPfd.fileDescriptor).use { output ->
                        FileInputStream(tempFile).use { input -> input.copyTo(output) }
                        output.flush()
                        output.channel.force(true)
                        targetPfd.fileDescriptor.sync()
                    }
                }
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) throw e
                if (e is SecurityException) throw e

                context.contentResolver.openOutputStream(originalUri, "wt")?.use { output ->
                    FileInputStream(tempFile).use { input -> input.copyTo(output) }
                }
            }

            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, song.title)
                put(MediaStore.Audio.Media.ARTIST, song.artist)
                put(MediaStore.Audio.Media.ALBUM, song.albumName)
                put(MediaStore.Audio.Media.YEAR, song.year)
                put(MediaStore.Audio.Media.TRACK, song.trackNumber)
                put(MediaStore.Audio.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                put(MediaStore.Audio.Media.SIZE, tempFile.length())
            }
            context.contentResolver.update(originalUri, values, null, null)
            MediaScannerConnection.scanFile(context, arrayOf(song.path), null, null)

            return true

        } catch (e: RecoverableSecurityException) {
            throw e
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    fun readTags(context: Context, song: Song): Song {
        val file = File(song.path)
        if (!file.exists()) return song

        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                val nativeFd = pfd.dup().detachFd()
                val metadata = TagLib.getMetadata(nativeFd) ?: return song
                val map = metadata.propertyMap

                song.copy(
                    title = map["TITLE"]?.firstOrNull()?.ifBlank { song.title } ?: song.title,
                    artist = map["ARTIST"]?.firstOrNull()?.ifBlank { song.artist } ?: song.artist,
                    albumName = map["ALBUM"]?.firstOrNull()?.ifBlank { song.albumName } ?: song.albumName,
                    year = map["DATE"]?.firstOrNull()?.toIntOrNull() ?: song.year,
                    trackNumber = map["TRACKNUMBER"]?.firstOrNull()?.toIntOrNull() ?: song.trackNumber
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            song
        }
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) { null }
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        return try {
            if (uri.scheme == "content") {
                context.contentResolver.getType(uri)
            } else {
                val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
            }
        } catch (e: Exception) { "image/jpeg" }
    }
}