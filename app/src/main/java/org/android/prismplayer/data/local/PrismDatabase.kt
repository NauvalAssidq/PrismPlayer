package org.android.prismplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.android.prismplayer.data.model.PlayHistory
import org.android.prismplayer.data.model.Song

@Database(
    entities = [Song::class, PlayHistory::class],
    version = 1,
    exportSchema = false
)
abstract class PrismDatabase : RoomDatabase() {
    abstract fun statsDao(): StatsDao

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
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}