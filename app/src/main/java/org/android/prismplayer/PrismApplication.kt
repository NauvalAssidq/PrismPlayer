package org.android.prismplayer

import android.app.Application
import org.android.prismplayer.data.local.PrismDatabase
import org.android.prismplayer.data.repository.MusicRepository


class PrismApplication : Application() {
    val database by lazy { PrismDatabase.getDatabase(this) }

    val repository by lazy {
        MusicRepository(
            context = this,
            statsDao = database.statsDao(),
            lyricsDao = database.lyricsDao()
        )
    }
}