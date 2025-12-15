package org.android.prismplayer.ui.components

import androidx.compose.animation.core.Animatable // IMPERATIVE ANIMATION
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onSeeking: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    trackHeight: Dp = 4.dp,
    thumbSize: Dp = 12.dp,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(0.2f)
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val thumbPosition = remember { Animatable(value) }

    LaunchedEffect(value) {
        if (!isDragging) {
            thumbPosition.animateTo(value, animationSpec = tween(100))
        }
    }

    val visualProgress = if (isDragging) thumbPosition.value else thumbPosition.value

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .onSizeChanged { canvasWidth = it.width.toFloat() }
            .pointerInput(canvasWidth) {
                detectTapGestures { offset ->
                    if (canvasWidth > 0) {
                        val newProgress = (offset.x / canvasWidth).coerceIn(0f, 1f)
                        scope.launch { thumbPosition.snapTo(newProgress) }

                        onSeeking(newProgress)
                        onValueChange(newProgress)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
            .pointerInput(canvasWidth) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        scope.launch {
                            thumbPosition.stop()
                            val startVal = (offset.x / canvasWidth).coerceIn(0f, 1f)
                            thumbPosition.snapTo(startVal)
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChange(thumbPosition.value)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val newProgress = (change.position.x / canvasWidth).coerceIn(0f, 1f)
                        scope.launch { thumbPosition.snapTo(newProgress) }
                        onSeeking(newProgress)
                    }
                )
            }
    ) {
        val centerY = size.height / 2
        val trackH = trackHeight.toPx()
        val cornerRadius = CornerRadius(trackH / 2)
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(0f, centerY - trackH / 2),
            size = Size(size.width, trackH),
            cornerRadius = cornerRadius
        )

        val activeWidth = size.width * visualProgress
        drawRoundRect(
            color = activeColor,
            topLeft = Offset(0f, centerY - trackH / 2),
            size = Size(activeWidth, trackH),
            cornerRadius = cornerRadius
        )

        val currentThumbRadius = if (isDragging) thumbSize.toPx() else (thumbSize.toPx() * 0.7f)

        drawCircle(
            color = activeColor,
            radius = currentThumbRadius,
            center = Offset(activeWidth, centerY)
        )

        if (isDragging) {
            drawCircle(
                color = activeColor.copy(alpha = 0.3f),
                radius = currentThumbRadius * 2f,
                center = Offset(activeWidth, centerY)
            )
        }
    }
}