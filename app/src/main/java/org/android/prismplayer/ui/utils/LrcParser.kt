package org.android.prismplayer.ui.utils

data class LyricLine(
    val timestamp: Long,
    val content: String
)

object LrcParser {
    private val regex = Regex("\\[(\\d{2}):(\\d{2}\\.\\d{2,3})\\](.*)")
    fun parse(rawLrc: String?): List<LyricLine> {
        if (rawLrc.isNullOrBlank()) return emptyList()

        return rawLrc.lineSequence()
            .mapNotNull { line ->
                val match = regex.find(line)
                if (match != null) {
                    val (minStr, secStr, text) = match.destructured
                    val minutes = minStr.toLong()
                    val seconds = secStr.toDouble()
                    val totalMillis = (minutes * 60 * 1000) + (seconds * 1000).toLong()
                    LyricLine(totalMillis, text.trim())
                } else {
                    null
                }
            }
            .sortedBy { it.timestamp }
            .toList()
    }
}