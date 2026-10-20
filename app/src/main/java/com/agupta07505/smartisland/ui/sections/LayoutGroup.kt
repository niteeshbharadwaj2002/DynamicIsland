/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import androidx.annotation.StringRes
import com.agupta07505.smartisland.R

/**
 * The groups of the Layout screen (portrait), in the order they are shown. [searchKey] is the row key used by the
 * settings search to scroll to (and open) the group; [titleRes] is the group's only visible text.
 */
enum class LayoutGroup(val searchKey: String, @StringRes val titleRes: Int) {
    QuickPresets("layout.presets", R.string.layout_group_presets),
    ExemptApps("layout.exempt_apps", R.string.layout_group_exempt_apps),
    SizeAndPosition("layout.size_position", R.string.layout_group_size_position),
    CameraCutout("layout.camera_cutout", R.string.camera_cutout_title),
    Shadow("layout.shadow", R.string.layout_group_shadow),
    MotionPhysics("layout.motion", R.string.layout_group_motion),
    KineticGlow("layout.kinetic_glow", R.string.motion_glow_title),
    BackdropBlur("layout.backdrop_blur", R.string.motion_blur_title)
}
