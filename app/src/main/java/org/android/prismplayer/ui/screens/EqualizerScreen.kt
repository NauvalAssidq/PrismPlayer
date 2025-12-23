package org.android.prismplayer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.data.model.EqBand
import org.android.prismplayer.data.model.EqPreset
import org.android.prismplayer.ui.components.DeleteConfirmationDialog
import org.android.prismplayer.ui.components.KnobComponent
import org.android.prismplayer.ui.components.SavePresetDialog
import org.android.prismplayer.ui.player.AudioViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ColorPrimaryRed = Color(0xFFD71921)
private val ColorGrid = Color(0xFF1A1A1A)
private val ColorBackground = Color(0xFF050505)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: AudioViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.setupEqualizer(0) }

    val bands by viewModel.eqBands.collectAsState()
    val isEnabled by viewModel.eqEnabled.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val currentPresetName by viewModel.currentPresetName.collectAsState()

    val bassBoost by viewModel.bassStrength.collectAsState()
    val virtualizer by viewModel.virtStrength.collectAsState()
    val loudnessGain by viewModel.gainStrength.collectAsState()

    EqualizerScreenContent(
        bands = bands,
        isEnabled = isEnabled,
        presets = presets,
        currentPresetName = currentPresetName,
        bassBoost = bassBoost,
        virtualizer = virtualizer,
        loudnessGain = loudnessGain,
        onBack = onBack,
        onToggleEnabled = { viewModel.toggleEq(it) },
        onApplyPreset = { viewModel.applyPreset(it) },
        onSetBandLevel = { id, level -> viewModel.setEqBandLevel(id, level) },
        onSavePreset = { name -> viewModel.saveCustomPreset(name) },
        onDeletePreset = { preset -> viewModel.deleteCustomPreset(preset) },
        onBassChange = { viewModel.setBassStrength(it) },
        onVirtChange = { viewModel.setVirtStrength(it) },
        onGainChange = { viewModel.setGainStrength(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqualizerScreenContent(
    bands: List<EqBand>,
    isEnabled: Boolean,
    presets: List<EqPreset>,
    currentPresetName: String?,
    bassBoost: Float,
    virtualizer: Float,
    loudnessGain: Float,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onApplyPreset: (EqPreset) -> Unit,
    onSetBandLevel: (bandId: Short, level: Short) -> Unit,
    onSavePreset: (String) -> Unit,
    onDeletePreset: (EqPreset) -> Unit,
    onBassChange: (Float) -> Unit,
    onVirtChange: (Float) -> Unit,
    onGainChange: (Float) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<EqPreset?>(null) }
    val defaultPresets = listOf("Flat", "Bass", "Vocal", "Treble")

    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "AUDIO_PROCESSOR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ColorBackground
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, "RETURN", tint = Color.White)
                        }
                    },
                    actions = {
                        val powerColor = if (isEnabled) ColorPrimaryRed else Color.White.copy(0.3f)
                        IconButton(
                            onClick = { onToggleEnabled(!isEnabled) },
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .border(1.dp, powerColor, CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PowerSettingsNew,
                                contentDescription = "POWER",
                                tint = powerColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
                Divider(color = Color.White.copy(0.1f))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        TechPresetChip(
                            text = "+ NEW",
                            selected = false,
                            onClick = { showSaveDialog = true },
                            isAction = true
                        )
                    }
                    items(presets) { preset ->
                        val isCustom = preset.name !in defaultPresets
                        TechPresetChip(
                            text = preset.name.uppercase(),
                            selected = preset.name == currentPresetName,
                            onClick = { onApplyPreset(preset) },
                            onDelete = if (isCustom) { { presetToDelete = preset } } else null
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 24.dp)
                    .border(1.dp, ColorGrid, RoundedCornerShape(4.dp))
                    .background(Color(0xFF080808))
            ) {
                WaveformGraph(
                    bands = bands,
                    isEnabled = isEnabled,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(24.dp))

            if (bands.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("NO_SIGNAL", color = Color.White.copy(0.3f))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bands.forEach { band ->
                        TechFader(
                            band = band,
                            isEnabled = isEnabled,
                            onValueChange = { level -> onSetBandLevel(band.id, level) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Divider(color = Color.White.copy(0.05f))

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KnobComponent(
                    label = "SUB_BASS",
                    value = bassBoost,
                    isEnabled = isEnabled,
                    size = 80.dp,
                    onValueChange = onBassChange
                )
                KnobComponent(
                    label = "STEREO",
                    value = virtualizer,
                    isEnabled = isEnabled,
                    size = 80.dp,
                    onValueChange = onVirtChange
                )
                KnobComponent(
                    label = "GAIN",
                    value = loudnessGain,
                    isEnabled = isEnabled,
                    size = 80.dp,
                    onValueChange = onGainChange
                )
            }
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(onDismiss = { showSaveDialog = false }, onSave = { showSaveDialog = false; onSavePreset(it) })
    }
    if (presetToDelete != null) {
        DeleteConfirmationDialog(presetName = presetToDelete!!.name, onConfirm = { onDeletePreset(presetToDelete!!); presetToDelete = null }, onDismiss = { presetToDelete = null })
    }
}

@Composable
fun WaveformGraph(
    bands: List<EqBand>,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(4.dp))) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        val barCount = 60
        val barWidth = width / barCount

        for (i in 0 until barCount) {
            val x = i * barWidth
            val distanceFromCenter = Math.abs(i - barCount/2f) / (barCount/2f)
            val randomAmp = Random.nextFloat() * (1f - distanceFromCenter) * height * 0.8f

            drawLine(
                color = Color.White.copy(alpha = if(isEnabled) 0.15f else 0.05f),
                start = Offset(x, centerY - (randomAmp/2)),
                end = Offset(x, centerY + (randomAmp/2)),
                strokeWidth = barWidth * 0.6f,
                cap = StrokeCap.Round
            )
        }

        if (bands.isNotEmpty()) {
            val path = Path()
            val stepX = width / (bands.size + 1)

            val points = bands.mapIndexed { index, band ->
                val x = stepX * (index + 1)
                val normalized = (band.level - band.minLevel).toFloat() / (band.maxLevel - band.minLevel)
                val y = height - (normalized * height)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                path.moveTo(0f, points.first().y)
                path.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val cx1 = (p1.x + p2.x) / 2
                    val cy1 = p1.y
                    val cx2 = (p1.x + p2.x) / 2
                    val cy2 = p2.y
                    path.cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
                }

                path.lineTo(width, points.last().y)

                drawPath(
                    path = path,
                    color = if(isEnabled) ColorPrimaryRed.copy(0.2f) else Color.Transparent,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = path,
                    color = if(isEnabled) ColorPrimaryRed else Color.White.copy(0.1f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        drawLine(
            color = Color.White.copy(0.1f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Composable
fun TechPresetChip(
    text: String,
    selected: Boolean,
    isAction: Boolean = false,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val activeColor = ColorPrimaryRed
    val inactiveColor = Color.White.copy(0.6f)
    val inactiveBorder = Color.White.copy(0.2f)
    val borderColor = if (selected) activeColor else if (isAction) inactiveBorder else inactiveBorder
    val textColor = if (selected) activeColor else if (isAction) Color.White else inactiveColor
    val bgColor = if (selected) activeColor.copy(0.1f) else Color.Transparent
    val iconColor = if (selected) activeColor else inactiveColor

    Row(
        modifier = Modifier
            .height(36.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        if (onDelete != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "DELETE",
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun TechFader(
    band: EqBand,
    isEnabled: Boolean,
    onValueChange: (Short) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        val dbValue = (band.level / 100)
        Text(
            text = if (dbValue > 0) "+${dbValue}dB" else "${dbValue}dB",
            color = if (isEnabled) ColorPrimaryRed else Color.White.copy(0.3f),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )

        Spacer(Modifier.height(12.dp))

        VerticalFaderTrack(
            value = band.level.toFloat(),
            min = band.minLevel.toFloat(),
            max = band.maxLevel.toFloat(),
            isEnabled = isEnabled,
            onValueChange = { onValueChange(it.toInt().toShort()) },
            modifier = Modifier
                .height(200.dp)
                .width(40.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = band.label,
            color = if (isEnabled) Color.White.copy(0.8f) else Color.White.copy(0.3f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun VerticalFaderTrack(
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
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val zeroY = height * (1f - ((0 - min) / range))

        drawLine(
            color = Color.White.copy(0.1f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, height),
            strokeWidth = 2.dp.toPx()
        )

        val thumbY = height - (normalizedValue * height)
        val thumbHeight = 12.dp.toPx()
        val thumbWidth = 24.dp.toPx()
        val thumbColor = if (isEnabled) Color.White else Color.White.copy(0.3f)

        drawRect(
            color = ColorBackground,
            topLeft = Offset(centerX - thumbWidth/2, thumbY - thumbHeight/2),
            size = Size(thumbWidth, thumbHeight)
        )
        drawRect(
            color = thumbColor,
            topLeft = Offset(centerX - thumbWidth/2, thumbY - thumbHeight/2),
            size = Size(thumbWidth, thumbHeight),
            style = Stroke(width = 2.dp.toPx())
        )

        if (isEnabled) {
            drawLine(
                color = ColorPrimaryRed.copy(0.5f),
                start = Offset(centerX, zeroY),
                end = Offset(centerX, thumbY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
private fun PrismEqualizerPreview() {
    val numBands = 5
    val freqs = listOf(60, 230, 910, 3600, 14000)
    val fakeBands = freqs.mapIndexed { i, f ->
        val level = when(i) {
            0 -> 800
            1 -> 400
            2 -> 0
            3 -> 500
            4 -> 1000
            else -> 0
        }
        EqBand(id = i.toShort(), level = level.toShort(), minLevel = -1500, maxLevel = 1500, centerFreq = f)
    }

    val flat = EqPreset("FLAT", emptyList())
    val myCustom = EqPreset("CYBER_BASS", emptyList())

    MaterialTheme {
        EqualizerScreenContent(
            bands = fakeBands,
            isEnabled = true,
            presets = listOf(flat, myCustom),
            currentPresetName = "CYBER_BASS",
            bassBoost = 0.75f,
            virtualizer = 0.3f,
            loudnessGain = 0.5f,
            onBack = {},
            onToggleEnabled = {},
            onApplyPreset = {},
            onSetBandLevel = { _, _ -> },
            onSavePreset = {},
            onDeletePreset = {},
            onBassChange = {},
            onVirtChange = {},
            onGainChange = {}
        )
    }
}