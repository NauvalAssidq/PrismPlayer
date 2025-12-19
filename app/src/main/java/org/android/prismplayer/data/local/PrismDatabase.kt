package org.android.prismplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.android.prismplayer.data.dao.LyricsDao
import org.android.prismplayer.data.dao.SongDao
import org.android.prismplayer.data.model.LyricsEntity // ✅ Import this
import org.android.prismplayer.data.model.PlayHistory
import org.android.prismplayer.data.model.Song

@Database(
    entities = [Song::class, PlayHistory::class, LyricsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PrismDatabase : RoomDatabase() {
    abstract fun statsDao(): SongDao
    abstract fun lyricsDao(): LyricsDao

    companion object {
        @Volatile
        private var INSTANCE: PrismDatabase? = null

        fun getDatabase(context: Context): PrismDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrismDatabase::class.java,
                    "prism_music_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}