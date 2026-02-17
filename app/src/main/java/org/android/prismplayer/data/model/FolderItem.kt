package org.android.prismplayer.data.model

data class FolderItem(
    val name: String,
    val path: String,
    val count: Int,
    var isSelected: Boolean
)
