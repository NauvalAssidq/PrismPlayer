package org.android.prismplayer.ui.utils

import android.content.Context
import android.content.SharedPreferences
import org.android.prismplayer.data.model.EqPreset

class EqPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prism_eq_prefs", Context.MODE_PRIVATE)

    var isEqEnabled: Boolean
        get() = prefs.getBoolean("eq_enabled", false)
        set(value) = prefs.edit().putBoolean("eq_enabled", value).apply()

    fun getBandLevel(bandId: Short, defaultLevel: Short): Short {
        return prefs.getInt("band_$bandId", defaultLevel.toInt()).toShort()
    }

    fun saveBassLevel(level: Short) {
        prefs.edit().putInt("BASS_LEVEL", level.toInt()).apply()
    }

    fun getBassLevel(): Short {
        return prefs.getInt("BASS_LEVEL", 0).toShort()
    }

    fun saveVirtLevel(level: Short) {
        prefs.edit().putInt("VIRT_LEVEL", level.toInt()).apply()
    }

    fun getVirtLevel(): Short {
        return prefs.getInt("VIRT_LEVEL", 0).toShort()
    }

    fun saveGainLevel(level: Int) {
        prefs.edit().putInt("GAIN_LEVEL", level).apply()
    }

    fun getGainLevel(): Int {
        return prefs.getInt("GAIN_LEVEL", 0)
    }

    fun saveBandLevel(bandId: Short, level: Short) {
        prefs.edit().putInt("band_$bandId", level.toInt()).apply()
    }

    fun saveLastPresetName(name: String) {
        prefs.edit().putString("LAST_PRESET_NAME", name).apply()
    }

    fun getLastPresetName(): String {
        return prefs.getString("LAST_PRESET_NAME", "Custom") ?: "Custom"
    }

    fun saveCustomPresets(presets: List<EqPreset>) {
        val serialized = presets.joinToString("|") { preset ->
            val levelsString = preset.bandLevels.joinToString(",")
            "${preset.name}:$levelsString"
        }
        prefs.edit().putString("custom_presets", serialized).apply()
    }

    fun loadCustomPresets(): List<EqPreset> {
        val serialized = prefs.getString("custom_presets", "") ?: ""
        if (serialized.isBlank()) return emptyList()

        return serialized.split("|").mapNotNull { entry ->
            try {
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val name = parts[0]
                    val levels = parts[1].split(",").map { it.toShort() }
                    EqPreset(name, levels)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}