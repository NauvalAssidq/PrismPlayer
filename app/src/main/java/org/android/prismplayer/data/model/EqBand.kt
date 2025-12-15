package org.android.prismplayer.data.model

data class EqBand(
    val id: Short,
    val centerFreq: Int,
    var level: Short,
    val minLevel: Short,
    val maxLevel: Short
) {
    val label: String
        get() {
            val hz = centerFreq / 1000
            return if (hz < 1000) "$hz Hz" else "${hz / 1000} kHz"
        }
}