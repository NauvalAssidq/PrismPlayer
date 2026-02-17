package org.android.prismplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DuplicateSongDialog(
  playlistName: String,
  songTitle: String,
  onDismiss: () -> Unit
) {
  // Blinking status light animation
  val infiniteTransition = rememberInfiniteTransition(label = "status_light")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 1f, targetValue = 0.2f,
    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), // Faster blink for error
    label = "alpha"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = true
    )
  ) {
    // Container: "Terminal Window" Style
    Column(
      modifier = Modifier
        .width(320.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(2.dp))
    ) {
      // 1. HEADER STRIP
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "SYSTEM_ALERT // DUPLICATE",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.error // Error Color for header
        )

        // Live Status Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "HALTED",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
          )
          Spacer(Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .size(6.dp)
              .alpha(alpha)
              .background(MaterialTheme.colorScheme.error, CircleShape)
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))

      // 2. ERROR DETAILS AREA
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(
          text = "> ERROR_DETAILS:",
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.2f))
            .padding(12.dp)
        ) {
          Text(
            text = "Entry '$songTitle' already exists in index '$playlistName'.\n\nWrite operation aborted to prevent data redundancy.",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
            lineHeight = 16.sp
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. FOOTER ACTIONS
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "[ CLOSE ]",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
              .clickable { onDismiss() }
              .padding(8.dp)
          )
        }
      }
    }
  }
}

@Preview(showBackground = false)
@Composable
fun DuplicateSongDialogPreview() {
  MaterialTheme(
    colorScheme = darkColorScheme(
      primary = Color.White,
      secondary = Color(0xFFD71921),
      error = Color(0xFFFF5555),
      outline = Color.White.copy(0.5f),
      surface = Color(0xFF1E1E1E),
      surfaceVariant = Color(0xFF2D2D2D),
      background = Color(0xFF121212),
      onSurface = Color.White,
      onSurfaceVariant = Color.Gray,
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(0.8f)),
      contentAlignment = Alignment.Center
    ) {
      DuplicateSongDialog(
        playlistName = "Cyberpunk Mix",
        songTitle = "Midnight City",
        onDismiss = {}
      )
    }
  }
}