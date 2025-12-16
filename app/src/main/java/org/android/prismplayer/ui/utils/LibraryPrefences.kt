package org.android.prismplayer.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment

class LibraryPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prism_library_prefs", Context.MODE_PRIVATE)
    private val KEY_FOLDERS = "saved_music_folders"

    fun getSavedFolders(): List<String> {
        val savedSet = prefs.getStringSet(KEY_FOLDERS, null)
        if (savedSet != null) {
            return savedSet.toList().sorted()
        }

        return listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        )
    }

    fun saveFolders(paths: List<String>) {
        prefs.edit().putStringSet(KEY_FOLDERS, paths.toSet()).apply()
    }
}