package org.android.prismplayer.ui.player.manager

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.android.prismplayer.data.model.EqBand
import org.android.prismplayer.data.model.EqPreset
import org.android.prismplayer.ui.utils.EqPreferences

object EqManager {
    private var eqPrefs: EqPreferences? = null

    // Hardware Effects
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _eqBands = MutableStateFlow<List<EqBand>>(emptyList())
    val eqBands: StateFlow<List<EqBand>> = _eqBands.asStateFlow()

    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _presets = MutableStateFlow<List<EqPreset>>(emptyList())
    val presets: StateFlow<List<EqPreset>> = _presets.asStateFlow()

    private val _currentPresetName = MutableStateFlow("Custom")
    val currentPresetName: StateFlow<String> = _currentPresetName.asStateFlow()

    private val _bassStrength = MutableStateFlow(0f)
    val bassStrength: StateFlow<Float> = _bassStrength.asStateFlow()

    private val _virtStrength = MutableStateFlow(0f)
    val virtStrength: StateFlow<Float> = _virtStrength.asStateFlow()

    private val _gainStrength = MutableStateFlow(0f)
    val gainStrength: StateFlow<Float> = _gainStrength.asStateFlow()

    fun setupEqualizer(context: Context, audioSessionId: Int) {
        try {
            if (equalizer != null) return

            eqPrefs = EqPreferences(context)
            val prefs = eqPrefs!!

            val eq = Equalizer(0, audioSessionId)
            val enhancer = LoudnessEnhancer(audioSessionId)
            val bass = BassBoost(0, audioSessionId)
            val virt = Virtualizer(0, audioSessionId)

            val savedEnabledState = prefs.isEqEnabled
            loadPresets(prefs, eq.numberOfBands.toInt())
            loadEqBands(prefs, eq)
            _currentPresetName.value = prefs.getLastPresetName()

            val savedBass = prefs.getBassLevel()
            val savedVirt = prefs.getVirtLevel()
            val savedGain = prefs.getGainLevel()

            if (bass.strengthSupported) bass.setStrength(savedBass)
            if (virt.strengthSupported) virt.setStrength(savedVirt)
            enhancer.setTargetGain(if (savedEnabledState) savedGain else 0)

            _bassStrength.value = savedBass / 1000f
            _virtStrength.value = savedVirt / 1000f
            _gainStrength.value = savedGain / 800f

            eq.enabled = savedEnabledState
            enhancer.enabled = savedEnabledState
            bass.enabled = savedEnabledState
            virt.enabled = savedEnabledState

            equalizer = eq
            loudnessEnhancer = enhancer
            bassBoost = bass
            virtualizer = virt
            _eqEnabled.value = savedEnabledState

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassStrength(value: Float) {
        val strength = (value * 1000).toInt().toShort()
        _bassStrength.value = value

        bassBoost?.let {
            if (it.strengthSupported) it.setStrength(strength)
        }
        eqPrefs?.saveBassLevel(strength)
    }

    fun setVirtStrength(value: Float) {
        val strength = (value * 1000).toInt().toShort()
        _virtStrength.value = value

        virtualizer?.let {
            if (it.strengthSupported) it.setStrength(strength)
        }
        eqPrefs?.saveVirtLevel(strength)
    }

    fun setGainStrength(value: Float) {
        val maxGain = 800
        val gainMb = (value * maxGain).toInt()
        _gainStrength.value = value

        if (_eqEnabled.value) {
            loudnessEnhancer?.setTargetGain(gainMb)
        }
        eqPrefs?.saveGainLevel(gainMb)
    }

    fun toggleEq(isEnabled: Boolean) {
        val prefs = eqPrefs ?: return

        equalizer?.enabled = isEnabled
        bassBoost?.enabled = isEnabled
        virtualizer?.enabled = isEnabled
        loudnessEnhancer?.enabled = isEnabled

        val savedGain = prefs.getGainLevel()
        loudnessEnhancer?.setTargetGain(if (isEnabled) savedGain else 0)

        _eqEnabled.value = isEnabled
        prefs.isEqEnabled = isEnabled
    }

    private fun loadEqBands(prefs: EqPreferences, eq: Equalizer) {
        val min = eq.bandLevelRange[0]
        val max = eq.bandLevelRange[1]
        val numBands = eq.numberOfBands
        val bands = mutableListOf<EqBand>()
        for (i in 0 until numBands) {
            val idx = i.toShort()
            val hardwareLevel = eq.getBandLevel(idx)
            val savedLevel = prefs.getBandLevel(idx, hardwareLevel)
            if (savedLevel != hardwareLevel) {
                try { eq.setBandLevel(idx, savedLevel) } catch (e: Exception) {}
            }
            bands.add(EqBand(idx, eq.getCenterFreq(idx), savedLevel, min, max))
        }
        _eqBands.value = bands
    }

    private fun loadPresets(prefs: EqPreferences, numBands: Int) {
        val flat = EqPreset("Flat", List(numBands) { 0 })
        val bass = EqPreset("Bass", List(numBands) { if(it < 2) 600.toShort() else 0.toShort() })
        val vocal = EqPreset("Vocal", List(numBands) { if(it in 1..3) 500.toShort() else -200.toShort() } as List<Short>)
        val treble = EqPreset("Treble", List(numBands) { if(it > 3) 600.toShort() else 0.toShort() })
        val defaultPresets = listOf(flat, bass, vocal, treble)
        val savedPresets = prefs.loadCustomPresets()
        _presets.value = defaultPresets + savedPresets
    }


    fun setBandLevelUserAction(bandId: Short, level: Short, scope: CoroutineScope) {
        val prefs = eqPrefs ?: return
        val currentBands = _eqBands.value.toMutableList()
        val index = currentBands.indexOfFirst { it.id == bandId }
        if (index != -1) {
            currentBands[index] = currentBands[index].copy(level = level)
            _eqBands.value = currentBands
        }
        prefs.saveBandLevel(bandId, level)
        if (_currentPresetName.value != "Custom") {
            _currentPresetName.value = "Custom"
            prefs.saveLastPresetName("Custom")
        }
        scope.launch(Dispatchers.IO) {
            try { equalizer?.setBandLevel(bandId, level) } catch (e: Exception) { }
        }
    }

    fun saveCustomPreset(name: String) {
        val prefs = eqPrefs ?: return
        val currentLevels = _eqBands.value.map { it.level }
        val newPreset = EqPreset(name, currentLevels)
        val currentList = _presets.value.toMutableList()
        currentList.removeAll { it.name == name }
        currentList.add(newPreset)
        _presets.value = currentList
        _currentPresetName.value = name
        prefs.saveLastPresetName(name)
        val defaultNames = setOf("Flat", "Bass", "Vocal", "Treble")
        val customPresetsOnly = currentList.filter { it.name !in defaultNames }
        prefs.saveCustomPresets(customPresetsOnly)
    }

    fun deleteCustomPreset(preset: EqPreset) {
        val prefs = eqPrefs ?: return
        val defaultNames = setOf("Flat", "Bass", "Vocal", "Treble")
        if (preset.name in defaultNames) return
        val currentList = _presets.value.toMutableList()
        currentList.remove(preset)
        _presets.value = currentList
        if (_currentPresetName.value == preset.name) {
            _currentPresetName.value = "Custom"
            prefs.saveLastPresetName("Custom")
        }
        val customPresetsOnly = currentList.filter { it.name !in defaultNames }
        prefs.saveCustomPresets(customPresetsOnly)
    }

    fun applyPreset(preset: EqPreset, scope: CoroutineScope) {
        val prefs = eqPrefs ?: return
        _currentPresetName.value = preset.name
        prefs.saveLastPresetName(preset.name)
        preset.bandLevels.forEachIndexed { index, level ->
            if (index < _eqBands.value.size) {
                val bandId = _eqBands.value[index].id
                val currentBands = _eqBands.value.toMutableList()
                currentBands[index] = currentBands[index].copy(level = level)
                _eqBands.value = currentBands
                prefs.saveBandLevel(bandId, level)
                scope.launch(Dispatchers.IO) {
                    try { equalizer?.setBandLevel(bandId, level) } catch (e: Exception) { }
                }
            }
        }
    }

    fun release() {
        equalizer?.release()
        loudnessEnhancer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        loudnessEnhancer = null
        bassBoost = null
        virtualizer = null
    }
}