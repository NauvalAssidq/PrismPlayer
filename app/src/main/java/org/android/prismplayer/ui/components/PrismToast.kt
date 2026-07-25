package org.android.prismplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.ui.theme.DotFont

enum class ToastType {
    SUCCESS, WARN, ERROR, INFO
}

@Composable
fun PrismToast(
    message: String,
    type: ToastType = ToastType.SUCCESS,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    durationMs: Long = 2000L
) {
    val progress = remember { Animatable(1f) }

    LaunchedEffect(message) {
        progress.snapTo(1f)
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = durationMs.toInt(), easing = LinearEasing)
        )
    }

    val shape = CutCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    val accentColor = when (type) {
        ToastType.SUCCESS -> MaterialTheme.colorScheme.secondary
        ToastType.WARN -> Color(0xFFFFB703)
        ToastType.ERROR -> Color(0xFFD71921)
        ToastType.INFO -> MaterialTheme.colorScheme.primary
    }

    val typeLabel = when (type) {
        ToastType.SUCCESS -> "SYS_OK"
        ToastType.WARN -> "SYS_WARN"
        ToastType.ERROR -> "SYS_ERR"
        ToastType.INFO -> "SYS_INFO"
    }

    val iconVector = when (type) {
        ToastType.SUCCESS -> Icons.Outlined.Check
        ToastType.WARN -> Icons.Outlined.ReportProblem
        ToastType.ERROR -> Icons.Outlined.Terminal
        ToastType.INFO -> Icons.Outlined.Info
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, accentColor.copy(alpha = 0.6f), shape)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$typeLabel //",
                        fontFamily = DotFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!actionLabel.isNullOrBlank() && onAction != null) {
                    Text(
                        text = "[ $actionLabel ]",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = accentColor,
                        modifier = Modifier
                            .clickable(onClick = onAction)
                            .padding(start = 8.dp)
                    )
                }
            }

            // Animated shrinking progress line at bottom of full-width top banner
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .height(2.dp)
                    .background(accentColor)
            )
        }
    }
}