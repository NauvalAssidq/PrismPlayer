package org.android.prismplayer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["songId"]), Index(value = ["timestamp"])]
)
data class PlayHistory(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val songId: Long,
    val artistName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val playDuration: Long
)