package org.android.prismplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.android.prismplayer.ui.theme.PrismColor
import org.android.prismplayer.ui.theme.PrismPlayerTheme

enum class PrismTab {
    HOME,
    SEARCH,
    LIBRARY,
    SETTING
}

@Composable
fun PrismNavBar(currentTab: PrismTab, onTabSelected: (PrismTab) -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 24.dp, top = 6.dp)
                            .height(64.dp)
                            .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            NavBarItem(
                    icon = Icons.Outlined.Dashboard,
                    label = "DASH",
                    isSelected = currentTab == PrismTab.HOME,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(PrismTab.HOME) }
            )

            VerticalSeparator()

            NavBarItem(
                    icon = Icons.AutoMirrored.Outlined.ManageSearch,
                    label = "FIND",
                    isSelected = currentTab == PrismTab.SEARCH,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(PrismTab.SEARCH) }
            )

            VerticalSeparator()

            NavBarItem(
                    icon = Icons.Outlined.Dns,
                    label = "DATA",
                    isSelected = currentTab == PrismTab.LIBRARY,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(PrismTab.LIBRARY) }
            )

            VerticalSeparator()

            NavBarItem(
                    icon = Icons.Outlined.Tune,
                    label = "CONF",
                    isSelected = currentTab == PrismTab.SETTING,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(PrismTab.SETTING) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
        icon: ImageVector,
        label: String,
        isSelected: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    val inactiveColor = if (isSystemInDarkTheme()) PrismColor.NavBarDark else PrismColor.NavBarLight
    val backgroundColor by
            animateColorAsState(
                    targetValue =
                            if (isSelected) MaterialTheme.colorScheme.secondary else inactiveColor,
                    label = "bg"
            )
    val contentColor by
            animateColorAsState(
                    targetValue =
                            if (isSelected) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.primary,
                    label = "content"
            )
    val scale by animateFloatAsState(if (isSelected) 0.95f else 1f, label = "press")

    Box(
            modifier =
                    modifier.fillMaxHeight()
                            .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                            ) { onClick() }
                            .background(backgroundColor),
            contentAlignment = Alignment.Center
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scale)
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun VerticalSeparator() {
    Box(
            modifier =
                    Modifier.width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Preview(showBackground = false, widthDp = 400, heightDp = 100)
@Composable
fun PreviewPrismNavBar() {
    PrismPlayerTheme {
        Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.BottomCenter
        ) { PrismNavBar(currentTab = PrismTab.HOME, onTabSelected = {}) }
    }
}
