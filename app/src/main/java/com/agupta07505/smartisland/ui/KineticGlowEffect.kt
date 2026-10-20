/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.max

/** Frames of stillness after which the glow sampler goes back to sleep (Rule_Book 2.3). */
private const val KINETIC_GLOW_SETTLE_FRAMES = 6

/** Light blue, the reference's primary colour (Rule_Book 2.3). */
private val KineticGlowColor = Color(0xFF3186FF)

/**
 * The glow's current blur radius in dp (Rule_Book 2.3). Samples how fast the island's size ([widthDp], [heightDp])
 * changes, per frame, only while it changes: the coroutine sleeps on a snapshot read while the island rests and
 * halts after [KINETIC_GLOW_SETTLE_FRAMES] frames of stillness. The maths is [KineticGlow.blurRadius]. Off with the
 * setting, Reduce Motion or a dark screen. The value is meant to be read while drawing, so it never recomposes.
 */
@Composable
internal fun rememberKineticGlow(enabled: Boolean, widthDp: () -> Float, heightDp: () -> Float): State<Float> {
    val radius = remember { mutableFloatStateOf(0f) }
    val reduceMotion = rememberReduceMotion()
    val screenOn = LocalScreenOn.current
    LaunchedEffect(enabled, reduceMotion, screenOn) {
        radius.floatValue = 0f
        if (!enabled || reduceMotion || !screenOn) return@LaunchedEffect
        var lastW = widthDp()
        var lastH = heightDp()
        while (true) {
            snapshotFlow { widthDp() != lastW || heightDp() != lastH }.first { it }
            var lastNanos = withFrameNanos { it }
            var stillFrames = 0
            while (stillFrames < KINETIC_GLOW_SETTLE_FRAMES) {
                val nanos = withFrameNanos { it }
                val w = widthDp()
                val h = heightDp()
                val dt = (nanos - lastNanos) / 1_000_000_000f
                if (dt > 0f) {
                    val speed = max(abs(w - lastW), abs(h - lastH)) / dt
                    radius.floatValue = KineticGlow.blurRadius(speed)
                    stillFrames = if (w == lastW && h == lastH) stillFrames + 1 else 0
                }
                lastNanos = nanos
                lastW = w
                lastH = h
            }
            radius.floatValue = 0f
        }
    }
    return radius
}

/**
 * Draws the glow behind the shell, outside its bounds (so it goes before the shell's clip): contiguous soft bands
 * around the rounded rectangle, from its edge outward, each fainter than the one inside it (a quadratic falloff),
 * so it looks the same on every Android version (no hardware shadow-layer limits) and shows no gaps or rings.
 */
internal fun Modifier.kineticGlow(radiusDp: () -> Float, cornerDp: () -> Dp): Modifier = drawBehind {
    val radiusPx = radiusDp().dp.toPx()
    if (radiusPx > 0f) drawKineticGlow(radiusPx, cornerDp().toPx())
}

private fun DrawScope.drawKineticGlow(radiusPx: Float, cornerPx: Float) {
    val steps = 14
    val band = radiusPx / steps
    for (i in steps downTo 1) {
        val centre = band * (i - 0.5f)
        val falloff = 1f - (i - 0.5f) / steps
        drawRoundRect(
            color = KineticGlowColor.copy(alpha = 0.40f * falloff * falloff),
            topLeft = Offset(-centre, -centre),
            size = Size(size.width + 2 * centre, size.height + 2 * centre),
            cornerRadius = CornerRadius(cornerPx + centre, cornerPx + centre),
            style = Stroke(width = band * 1.05f)
        )
    }
}
