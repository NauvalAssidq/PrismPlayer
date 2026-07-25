package org.android.prismplayer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "playlist_entries",
  foreignKeys = [
    ForeignKey(
      entity = Playlist::class,
      parentColumns = ["playlistId"],
      childColumns = ["playlistId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["playlistId"]),
    Index(value = ["playlistId", "songId"], unique = true)
  ]
)
data class PlaylistEntry(
  @PrimaryKey(autoGenerate = true) val entryId: Long = 0,
  val playlistId: Long,
  val songId: Long,
  val dateAdded: Long = System.currentTimeMillis()
)