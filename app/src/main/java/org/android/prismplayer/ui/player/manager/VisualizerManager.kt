package org.android.prismplayer.ui.player.manager

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot

class VisualizerManager {

    // Holds the raw data for the UI
    data class VisualizerData(
        val fft: FloatArray = FloatArray(0)
    )

    private val _visualizerData = MutableStateFlow(VisualizerData())
    val visualizerData = _visualizerData.asStateFlow()

    private var visualizer: Visualizer? = null
    private var isEnabled = false

    fun start(audioSessionId: Int) {
        if (visualizer != null || audioSessionId == 0) return

        try {
            visualizer = Visualizer(audioSessionId).apply {
                // Set capture size (High quality)
                captureSize = Visualizer.getCaptureSizeRange()[1]

                // IMPORTANT: Measurement Mode Peak/RMS
                measurementMode = Visualizer.MEASUREMENT_MODE_NONE

                // Listener
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, rate: Int) {}

                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, rate: Int) {
                        if (fft != null && isEnabled) {
                            val processed = processFft(fft)
                            _visualizerData.value = VisualizerData(fft = processed)
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)

                enabled = true
            }
            isEnabled = true
        } catch (e: Exception) {
            android.util.Log.e("VisualizerManager", "Failed to start: ${e.message}")
        }
    }

    fun stop() {
        isEnabled = false
        _visualizerData.value = VisualizerData() // Clear UI
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
    }

    fun setPlaying(isPlaying: Boolean) {
        // Just gate the data flow, don't destroy the session
        isEnabled = isPlaying
        if (!isPlaying) {
            _visualizerData.value = VisualizerData() // Clear bars on pause
        }
    }

    private fun processFft(fft: ByteArray): FloatArray {
        val n = fft.size
        val magnitudes = FloatArray(n / 2)

        for (i in 0 until n / 2) {
            // Complex number conversion (Real + Imaginary)
            // The byte array is structured as [Real, Imaginary, Real, Imaginary...]
            // We verify index to prevent crashes
            val rIndex = 2 * i
            val iIndex = 2 * i + 1

            if (iIndex < fft.size) {
                val real = fft[rIndex].toFloat()
                val imaginary = fft[iIndex].toFloat()

                // Calculate magnitude and normalize (0.0 to 1.0)
                // 128f is the theoretical max, but we boost it slightly
                val magnitude = hypot(real, imaginary) / 128f
                magnitudes[i] = magnitude.coerceIn(0f, 1f)
            }
        }
        return magnitudes
    }
}