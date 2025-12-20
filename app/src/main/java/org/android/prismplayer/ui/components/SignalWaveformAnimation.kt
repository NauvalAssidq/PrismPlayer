package org.android.prismplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SignalWaveformAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(32.dp)
    ) {
        repeat(5) { i ->
            val duration = 600 + (i * 100)
            val heightAnim by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp * heightAnim) // Height varies based on animation
                    .background(color.copy(alpha = 0.8f))
            )

            if (i < 4) Spacer(Modifier.width(6.dp))
        }
    }
}