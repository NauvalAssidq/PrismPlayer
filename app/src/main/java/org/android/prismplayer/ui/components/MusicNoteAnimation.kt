package org.android.prismplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MusicNoteAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_note")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bounce"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = color.copy(alpha = alpha),
            modifier = Modifier.size(32.dp).offset(y = offsetY.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = color.copy(alpha = alpha),
            modifier = Modifier.size(24.dp).offset(y = (offsetY * 0.8f).dp)
        )
    }
}