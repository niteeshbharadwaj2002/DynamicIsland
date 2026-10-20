/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.agupta07505.smartisland.buddy.MascotRuntime
import com.agupta07505.smartisland.buddy.model.MascotSide
import com.agupta07505.smartisland.buddy.model.MascotTrack
import com.agupta07505.smartisland.buddy.model.MascotVisitKind
import com.agupta07505.smartisland.buddy.runtime.MascotEvents
import com.agupta07505.smartisland.buddy.runtime.MascotVisitScheduler
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.util.CameraCutoutDetector

/**
 * What the overlay needs to seat the buddy (Mascot / Buddy) on the collapsed pill. The buddy only lives on the
 * portrait pill: in landscape the pill is anchored beside a lateral cutout and has no wing to rest in, so it is off.
 */
internal class BuddyPillState(
    /** The pill's width with room for a resting buddy (the base width when it needs none). */
    val widthDp: Float,
    /** The buddy is on, resting and visible on an idle pill. */
    val onPill: Boolean,
    /** A cameo or countdown watch brought it out over a busy pill, in one wing. */
    val aside: Boolean,
    val asideLeft: Boolean,
    val track: MascotTrack
)

/**
 * These settings with the pill widened for a resting buddy (see [PillLayout.effectiveWidthDp]); a resting buddy also
 * overrides "Hide when idle" (it tucks itself away after a while instead). The same object when nothing changes.
 * Applied once, in [IslandViewModel.displaySettings], so the overlay view, the window and the touch region agree.
 */
internal fun SmartIslandSettings.withBuddyPill(
    holeWidthDp: Float?,
    notificationCount: Int,
    tucked: Boolean,
    isLandscape: Boolean
): SmartIslandSettings {
    if (!buddyEnabled || isLandscape) return this
    val widthDp = PillLayout.effectiveWidthDp(this, holeWidthDp, notificationCount, tucked)
    return if (widthDp == width && !hideWhenIdle) this else copy(width = widthDp, hideWhenIdle = false)
}

@Composable
internal fun rememberBuddyPill(
    settings: SmartIslandSettings,
    notificationCount: Int,
    isLandscape: Boolean
): BuddyPillState {
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val cutout = remember(context, configuration) { CameraCutoutDetector.detect(context) }
    val holeWidthDp = if (cutout.hasHardwareCutout) cutout.rawWidthDp else null
    val buddyHere = settings.buddyEnabled && !isLandscape
    // [settings] already carries the widened pill (IslandViewModel.displaySettings).
    val widthDp = settings.width
    val onPill = buddyHere && PillLayout.buddyOnPill(settings, notificationCount)

    val visit by MascotRuntime.controller.visit.collectAsState()
    val aside = buddyHere && visit?.kind?.takesOnlyItsWing == true
    // A countdown is watched from the camera's left, over the timer's mark, whichever side the buddy rests on.
    val asideLeft = when (visit?.kind) {
        is MascotVisitKind.Countdown, MascotVisitKind.Retreat -> true
        else -> settings.buddySideValue == MascotSide.Left
    }

    // The buddy hears about events even while the island is busy, so a reaction can wait for the rest.
    LaunchedEffect(buddyHere, settings.buddyReacts) {
        MascotRuntime.controller.reacts = buddyHere && settings.buddyReacts
        if (!buddyHere) return@LaunchedEffect
        MascotEvents.moments.collect { MascotRuntime.controller.post(it) }
    }
    // Now and then it strolls through the pill, and a tucked buddy is called back out. Skipped under Battery
    // Saver, with Reduce Motion, and whenever the pill is busy (the controller only starts a visit at rest).
    val visitFrequency = settings.buddyVisitFrequency
    val reduceMotion = rememberReduceMotion()
    LaunchedEffect(buddyHere, visitFrequency, reduceMotion) {
        if (!buddyHere || reduceMotion) return@LaunchedEffect
        val power = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
        while (true) {
            val wait = MascotVisitScheduler.nextDelaySec(visitFrequency) ?: return@LaunchedEffect
            kotlinx.coroutines.delay((wait * 1000f).toLong())
            if (power?.isPowerSaveMode == true) continue
            val controller = MascotRuntime.controller
            if (controller.tucked.value) controller.untuck(MascotVisitScheduler.greeting())
            else controller.startVisit(MascotVisitKind.Lap, MascotVisitScheduler.greeting())
        }
    }

    val track = remember(widthDp, settings.height, settings.xOffset, buddyHere, settings.buddySide, cutout) {
        MascotTrack.forPill(
            pillWidthDp = widthDp,
            pillHeightDp = settings.height,
            sizeDp = PillLayout.buddySizeDp(settings.height),
            holeWidthDp = holeWidthDp,
            // Where the hole sits in the pill: the pill's middle, plus how far the hole is from where the pill is.
            holeCenterDp = holeWidthDp?.let { widthDp / 2f + (cutout.rawCenterOffsetDp - settings.xOffset) },
            side = settings.buddySideValue
        )
    }
    return BuddyPillState(widthDp, onPill, aside, asideLeft, track)
}
