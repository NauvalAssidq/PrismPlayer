package org.android.prismplayer.data.model

import java.util.UUID

data class QueueItem(
    val queueId: String = UUID.randomUUID().toString(),
    val song: Song
)