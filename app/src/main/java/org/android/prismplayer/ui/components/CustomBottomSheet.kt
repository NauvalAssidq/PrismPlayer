package org.android.prismplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CustomBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {},
                        onDragEnd = {},
                        onDragCancel = {},
                        onVerticalDrag = { _, _ -> }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Release) {
                                onDismiss()
                            }
                        }
                    }
                }
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Start from bottom
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it }, // Exit to bottom
            animationSpec = tween(300)
        ),
    ) {
        var offsetY by remember { mutableFloatStateOf(0f) }
        val animatedOffset = remember { Animatable(0f) }

        LaunchedEffect(offsetY) {
            animatedOffset.snapTo(offsetY)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, animatedOffset.value.roundToInt()) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetY + dragAmount).coerceAtLeast(0f)
                                offsetY = newOffset
                            },
                            onDragEnd = {
                                if (offsetY > 150f) {
                                    onDismiss()
                                } else {
                                    scope.launch {
                                        animatedOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f)
                                        )
                                        offsetY = 0f
                                    }
                                }
                            }
                        )
                    }
            ) {
                content()
            }
        }
    }
}