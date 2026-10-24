/*
 * Smart Island (2026)
 * Copyright Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 */

package com.agupta07505.smartisland.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agupta07505.smartisland.model.IslandNotification

/** The green "unread" dot at the pill's right end. */
private val AlertDotColor = Color(0xFF34D399)

/** Overlapping circular icons, oldest at the back, for a folded group. */
@Composable
internal fun StackedAlertIcons(icons: List<Bitmap>, size: Dp = 22.dp, overlap: Dp = 9.dp) {
    Box(modifier = Modifier.size(width = size + overlap * (icons.size - 1).coerceAtLeast(0), height = size)) {
        icons.forEachIndexed { index, bitmap ->
            Image(
                bitmap = remember(bitmap) { bitmap.asImageBitmap() },
                contentDescription = null,
                modifier = Modifier
                    .offset(x = overlap * index)
                    .size(size)
                    // A black ring separates each icon from the one behind it.
                    .background(Color.Black, CircleShape)
                    .padding(1.5.dp)
                    .clip(CircleShape)
            )
        }
    }
}

/**
 * The alert pill's right-hand side, symbols only (Rule_Book §3.5): the green unread dot, or the message count
 * for a folded group. The sender and preview stay in the expanded card; the pill carries no text that scrolls.
 */
@Composable
internal fun AlertPillBadge(notification: IslandNotification) {
    if (notification.groupCount > 1) {
        Text(
            text = notification.groupCount.toString(),
            color = AlertDotColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    } else {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AlertDotColor))
    }
}

/** A small dot that breathes, for a state with no number yet: ringing, connecting, a sensor in use, a torch on. */
@Composable
internal fun PulsingDot(color: Color, size: Dp = 8.dp) {
    val alpha by rememberInfiniteTransition(label = "pillDot").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pillDotAlpha"
    )
    Box(modifier = Modifier.size(size).clip(CircleShape).background(color.copy(alpha = alpha)))
}

/**
 * The folded group's left-hand badge: the stacked icons when the alerts brought bitmaps, otherwise
 * overlapping lettered circles (one per message, at most three) so a group never looks like a single alert.
 */
@Composable
internal fun StackedAlertBadges(notification: IslandNotification, color: Color, size: Dp = 22.dp, overlap: Dp = 9.dp) {
    if (notification.groupIcons.size > 1) {
        StackedAlertIcons(icons = notification.groupIcons, size = size, overlap = overlap)
        return
    }
    val count = notification.groupCount.coerceIn(2, 3)
    val letter = notification.appName.firstOrNull()?.uppercase() ?: "S"
    Box(modifier = Modifier.size(width = size + overlap * (count - 1), height = size)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .offset(x = overlap * index)
                    .size(size)
                    .background(Color.Black, CircleShape)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(text = letter, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
