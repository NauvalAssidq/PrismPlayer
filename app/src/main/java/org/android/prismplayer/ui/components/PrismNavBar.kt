package org.android.prismplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class PrismTab {
    HOME, SEARCH, LIBRARY, SETTING
}

@Composable
fun PrismNavBar(
    currentTab: PrismTab,
    onTabSelected: (PrismTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 18.dp, top = 6.dp)
            .height(60.dp)
            .shadow(
                elevation = 30.dp,
                spotColor = Color.Black.copy(0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121212).copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(0.03f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.
            fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { }
                )
            },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Rounded.Home,
                isSelected = currentTab == PrismTab.HOME,
                onClick = { onTabSelected(PrismTab.HOME) }
            )

            NavBarItem(
                icon = Icons.Rounded.Search,
                isSelected = currentTab == PrismTab.SEARCH,
                onClick = { onTabSelected(PrismTab.SEARCH) }
            )

            NavBarItem(
                icon = Icons.Rounded.LibraryMusic,
                isSelected = currentTab == PrismTab.LIBRARY,
                onClick = { onTabSelected(PrismTab.LIBRARY) }
            )

            NavBarItem(
                icon = Icons.Rounded.Settings,
                isSelected = currentTab == PrismTab.SETTING,
                onClick = { onTabSelected(PrismTab.SETTING) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1DB954) else Color.White.copy(0.4f),
        label = "color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(
                        elevation = 20.dp,
                        spotColor = Color(0xFF1DB954),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = false, widthDp = 360, heightDp = 100)
@Composable
fun PreviewPrismNavBar() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)),
        contentAlignment = Alignment.BottomCenter
    ) {
        PrismNavBar(currentTab = PrismTab.HOME, onTabSelected = {})
    }
}