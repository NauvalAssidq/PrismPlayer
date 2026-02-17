package org.android.prismplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
  @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
  val name: String,
  val createdAt: Long = System.currentTimeMillis(),
  val iconUri: String? = null
)