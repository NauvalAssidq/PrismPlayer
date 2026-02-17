package org.android.prismplayer.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
private fun getKnobColors(): Triple<Color, Color, Color> {
    return Triple(
        MaterialTheme.colorScheme.surfaceVariant, // Track
        MaterialTheme.colorScheme.surface, // Body
        MaterialTheme.colorScheme.onSurface // Text
    )
}

@Composable
fun KnobComponent(
    label: String,
    value: Float,
    isEnabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 90.dp,
    onValueChange: (Float) -> Unit
) {
    val view = LocalView.current
    val currentPercent = (value * 100).toInt()
    var lastHapticValue by remember { mutableIntStateOf(currentPercent) }
    val (trackColor, bodyColor, textColor) = getKnobColors()
    val primaryColor = accentColor

    val startAngle = 135f
    val sweepAngle = 270f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(size + 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput

                    val center = Offset(this.size.width / 2f, this.size.height / 2f)

                    detectDragGestures(
                        onDragStart = { offset ->
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val touchVec = change.position - center
                            var angle = Math.toDegrees(atan2(touchVec.y.toDouble(), touchVec.x.toDouble())).toFloat()

                            if (angle < 0) angle += 360f
                            var dialAngle = angle - startAngle
                            if (dialAngle < 0) dialAngle += 360f

                            if (dialAngle <= sweepAngle) {
                                val newValue = (dialAngle / sweepAngle).coerceIn(0f, 1f)
                                onValueChange(newValue)
                                val newPercent = (newValue * 100).toInt()
                                if (kotlin.math.abs(newPercent - lastHapticValue) >= 5) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    lastHapticValue = newPercent
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasCenter = Offset(this.size.width / 2, this.size.height / 2)
                val maxRadius = this.size.minDimension / 2
                val strokeWidth = 6.dp.toPx()
                val arcRadius = maxRadius - (strokeWidth / 2)
                val bodyRadius = arcRadius - 8.dp.toPx()

                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(canvasCenter.x - arcRadius, canvasCenter.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                if (isEnabled) {
                    drawArc(
                        color = primaryColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * value,
                        useCenter = false,
                        topLeft = Offset(canvasCenter.x - arcRadius, canvasCenter.y - arcRadius),
                        size = Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(trackColor, bodyColor),
                        center = canvasCenter,
                        radius = bodyRadius
                    ),
                    radius = bodyRadius,
                    center = canvasCenter
                )
                drawCircle(
                    color = trackColor.copy(alpha = 0.5f),
                    radius = bodyRadius,
                    center = canvasCenter,
                    style = Stroke(width = 1.dp.toPx())
                )

                val currentAngleRad = Math.toRadians((startAngle + (sweepAngle * value)).toDouble())
                val indicatorStart = Offset(
                    canvasCenter.x + (bodyRadius * 0.4f * cos(currentAngleRad)).toFloat(),
                    canvasCenter.y + (bodyRadius * 0.4f * sin(currentAngleRad)).toFloat()
                )
                val indicatorEnd = Offset(
                    canvasCenter.x + (bodyRadius * 0.85f * cos(currentAngleRad)).toFloat(),
                    canvasCenter.y + (bodyRadius * 0.85f * sin(currentAngleRad)).toFloat()
                )

                drawLine(
                    color = if(isEnabled) textColor else textColor.copy(alpha = 0.3f),
                    start = indicatorStart,
                    end = indicatorEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            if (isEnabled) {
                Text(
                    text = "${(value * 100).toInt()}",
                    color = textColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = size * 0.25f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isEnabled) primaryColor else textColor.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
fun PreviewTechDial() {
    var dialValue by remember { mutableFloatStateOf(0.4f) }

    Box(Modifier.padding(20.dp)) {
        KnobComponent(
            label = "LFE_DRIVE",
            value = dialValue,
            onValueChange = { dialValue = it }
        )
    }
}