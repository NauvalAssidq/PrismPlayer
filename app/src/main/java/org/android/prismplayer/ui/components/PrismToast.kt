package org.android.prismplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.ui.theme.PrismColor

@Composable
fun PrismToast(message: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)) // Adapts to White/Black border
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Rounded.CheckCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "SYS_SUCCESS // ",
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = message.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Dark Mode")
@Composable
fun PrismToastPreviewDark() {
  MaterialTheme(
    colorScheme = androidx.compose.material3.darkColorScheme(
      primary = Color.White,
      secondary = Color(0xFFD71921),
      surfaceVariant = Color(0xFF1E1E1E),
      onSurface = Color.White,
      onSurfaceVariant = Color.Gray
    )
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.BottomCenter
    ) {
      PrismToast(message = "ADDED TO FAVORITES")
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0, name = "Light Mode")
@Composable
fun PrismToastPreviewLight() {
  MaterialTheme(
    colorScheme = androidx.compose.material3.lightColorScheme(
      primary = Color.Black,
      secondary = Color(0xFFD71921),
      surfaceVariant = Color.White,
      onSurface = Color.Black,
      onSurfaceVariant = Color.Gray
    )
  ) {
    Box(
      modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      PrismToast(message = "ADDED TO FAVORITES")
    }
  }
}