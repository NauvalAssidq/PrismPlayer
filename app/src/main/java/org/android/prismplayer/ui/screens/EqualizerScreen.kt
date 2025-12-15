package org.android.prismplayer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.data.model.EqBand
import org.android.prismplayer.data.model.EqPreset
import org.android.prismplayer.ui.components.SavePresetDialog
import org.android.prismplayer.ui.player.AudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: AudioViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.setupEqualizer(0)
    }

    val bands by viewModel.eqBands.collectAsState()
    val isEnabled by viewModel.eqEnabled.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val currentPresetName by viewModel.currentPresetName.collectAsState()

    EqualizerScreenContent(
        bands = bands,
        isEnabled = isEnabled,
        presets = presets,
        currentPresetName = currentPresetName,
        onBack = onBack,
        onToggleEnabled = { viewModel.toggleEq(it) },
        onApplyPreset = { viewModel.applyPreset(it) },
        onSetBandLevel = { id, level -> viewModel.setEqBandLevel(id, level) },
        onSavePreset = { name -> viewModel.saveCustomPreset(name) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqualizerScreenContent(
    bands: List<EqBand>,
    isEnabled: Boolean,
    presets: List<EqPreset>,
    currentPresetName: String?,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onApplyPreset: (EqPreset) -> Unit,
    onSetBandLevel: (bandId: Short, level: Short) -> Unit,
    onSavePreset: (String) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        AuraBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Equalizer", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, null)
                        }
                    },
                    actions = {
                        val accent = Color(0xFF1DB954)

                        val backgroundBrush = if (isEnabled) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.22f),
                                    accent.copy(alpha = 0.10f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            )
                        }

                        val borderColor = if (isEnabled) {
                            accent.copy(alpha = 0.45f)
                        } else {
                            Color.White.copy(alpha = 0.12f)
                        }

                        IconButton(
                            onClick = { onToggleEnabled(!isEnabled) },
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(backgroundBrush)
                                .border(1.dp, borderColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PowerSettingsNew,
                                contentDescription = null,
                                tint = if (isEnabled) accent else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(0.5f),
                    modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 12.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PresetChip(
                            text = "New +",
                            selected = false,
                            isAction = true,
                            onClick = { showSaveDialog = true }
                        )
                    }

                    items(presets) { preset ->
                        PresetChip(
                            text = preset.name,
                            selected = preset.name == currentPresetName,
                            onClick = { onApplyPreset(preset) }
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                if (bands.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1DB954))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bands.forEach { band ->
                            EqSliderItem(
                                band = band,
                                isEnabled = isEnabled,
                                onValueChange = { level -> onSetBandLevel(band.id, level) }
                            )
                        }
                    }
                }
            }
        }

        if (showSaveDialog) {
            SavePresetDialog(
                onDismiss = { showSaveDialog = false },
                onSave = { name ->
                    onSavePreset(name)
                    showSaveDialog = false
                }
            )
        }
    }
}


@Composable
fun PresetChip(
    text: String,
    selected: Boolean,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    val accent = Color(0xFF1DB954)

    val backgroundBrush = when {
        selected -> {
            Brush.verticalGradient(
                colors = listOf(
                    accent.copy(alpha = 0.22f),
                    accent.copy(alpha = 0.10f)
                )
            )
        }
        isAction -> {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        }
        else -> {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.03f)
                )
            )
        }
    }

    val borderColor = when {
        selected -> accent.copy(alpha = 0.45f)
        else -> Color.White.copy(alpha = 0.12f)
    }

    val textColor = when {
        selected -> accent
        isAction -> Color.White.copy(alpha = 0.9f)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun EqSliderItem(
    band: EqBand,
    isEnabled: Boolean,
    onValueChange: (Short) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        Text(
            text = "${(band.level / 100)}dB",
            color = if (isEnabled) Color.White else Color.White.copy(0.3f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        VerticalSlider(
            value = band.level.toFloat(),
            min = band.minLevel.toFloat(),
            max = band.maxLevel.toFloat(),
            isEnabled = isEnabled,
            onValueChange = { onValueChange(it.toInt().toShort()) },
            modifier = Modifier
                .height(260.dp)
                .width(44.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = band.label,
            color = if (isEnabled) Color.White.copy(0.8f) else Color.White.copy(0.3f),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun VerticalSlider(
    value: Float,
    min: Float,
    max: Float,
    isEnabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size -> canvasHeight = size.height.toFloat() }
            .pointerInput(isEnabled, canvasHeight) {
                if (!isEnabled || canvasHeight == 0f) return@pointerInput

                fun updateValue(touchY: Float) {
                    val percentFromBottom = 1f - (touchY / canvasHeight)
                    val range = max - min
                    val newValue = min + (range * percentFromBottom)
                    onValueChange(newValue.coerceIn(min, max))
                }

                detectDragGestures(
                    onDragStart = { updateValue(it.y) },
                    onDrag = { change, _ ->
                        change.consume()
                        updateValue(change.position.y)
                    }
                )
            }
    ) {
        val range = max - min
        val normalizedValue = ((value - min) / range).coerceIn(0f, 1f)

        val barWidth = 4.dp.toPx()
        val thumbSize = 14.dp.toPx()
        val cornerRadius = CornerRadius(barWidth / 2)
        val height = size.height
        val width = size.width

        val activeColor = if (isEnabled) Color(0xFF1DB954) else Color(0xFF404040)
        val inactiveColor = Color.White.copy(0.05f)

        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset((width - barWidth) / 2, 0f),
            size = Size(barWidth, height),
            cornerRadius = cornerRadius
        )

        val fillHeight = height * normalizedValue
        val topPos = height - fillHeight

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(activeColor, activeColor.copy(alpha = 0.5f)),
                startY = topPos,
                endY = height
            ),
            topLeft = Offset((width - barWidth) / 2, topPos),
            size = Size(barWidth, fillHeight),
            cornerRadius = cornerRadius
        )

        val thumbY = height - (normalizedValue * height)

        if (isEnabled) {
            drawCircle(
                color = activeColor.copy(alpha = 0.2f),
                radius = thumbSize * 1.5f,
                center = Offset(width / 2, thumbY)
            )
        }

        drawCircle(
            color = if (isEnabled) Color.White else Color.White.copy(0.3f),
            radius = thumbSize / 2,
            center = Offset(width / 2, thumbY)
        )
    }
}

@Composable
private fun AuraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1DB954).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.5f, -100f),
                radius = width * 1.3f
            ),
            center = Offset(width * 0.5f, -100f),
            radius = width * 1.3f
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
private fun EqualizerScreenPreview_Enabled() {
    val numBands = 5

    val freqs = listOf(60, 230, 910, 3600, 14000)

    val fakeBands = listOf(
        EqBand(id = 0, level = 0,    minLevel = -1500, maxLevel = 1500, centerFreq = freqs[0]),
        EqBand(id = 1, level = 500,  minLevel = -1500, maxLevel = 1500, centerFreq = freqs[1]),
        EqBand(id = 2, level = -300, minLevel = -1500, maxLevel = 1500, centerFreq = freqs[2]),
        EqBand(id = 3, level = 900,  minLevel = -1500, maxLevel = 1500, centerFreq = freqs[3]),
        EqBand(id = 4, level = -700, minLevel = -1500, maxLevel = 1500, centerFreq = freqs[4]),
    )

    // Same presets logic you showed (but for Short list)
    val flat = EqPreset("Flat", List(numBands) { 0.toShort() })
    val bass = EqPreset("Bass", List(numBands) { if (it < 2) 600.toShort() else 0.toShort() })
    val vocal = EqPreset("Vocal", List(numBands) { if (it in 1..3) 500.toShort() else (-200).toShort() })
    val treble = EqPreset("Treble", List(numBands) { if (it > 3) 600.toShort() else 0.toShort() })

    val fakePresets = listOf(flat, bass, vocal, treble)

    EqualizerScreenContent(
        bands = fakeBands,
        isEnabled = true,
        presets = fakePresets,
        currentPresetName = "Bass",
        onBack = {},
        onToggleEnabled = {},
        onApplyPreset = {},
        onSetBandLevel = { _, _ -> },
        onSavePreset = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
private fun EqualizerScreenPreview_Disabled() {
    val numBands = 5
    val freqs = listOf(60, 230, 910, 3600, 14000)

    val fakeBands = freqs.mapIndexed { i, f ->
        EqBand(
            id = i.toShort(),
            level = 0,
            minLevel = -1500,
            maxLevel = 1500,
            centerFreq = f
        )
    }

    val flat = EqPreset("Flat", List(numBands) { 0.toShort() })

    EqualizerScreenContent(
        bands = fakeBands,
        isEnabled = false,
        presets = listOf(flat),
        currentPresetName = "Flat",
        onBack = {},
        onToggleEnabled = {},
        onApplyPreset = {},
        onSetBandLevel = { _, _ -> },
        onSavePreset = {}
    )
}
