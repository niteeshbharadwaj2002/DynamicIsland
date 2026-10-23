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
 * settings search to scroll to (and open) the group; [titleRes] is its title and [summaryRes] the one line under it.
 */
enum class LayoutGroup(val searchKey: String, @StringRes val titleRes: Int, @StringRes val summaryRes: Int) {
    QuickPresets("layout.presets", R.string.layout_group_presets, R.string.layout_summary_presets),
    ExemptApps("layout.exempt_apps", R.string.layout_group_exempt_apps, R.string.layout_summary_exempt_apps),
    SizeAndPosition("layout.size_position", R.string.layout_group_size_position, R.string.layout_summary_size_position),
    CameraCutout("layout.camera_cutout", R.string.camera_cutout_title, R.string.layout_summary_camera_cutout),
    Shadow("layout.shadow", R.string.layout_group_shadow, R.string.layout_summary_shadow),
    MotionPhysics("layout.motion", R.string.layout_group_motion, R.string.layout_summary_motion),
    KineticGlow("layout.kinetic_glow", R.string.motion_glow_title, R.string.layout_summary_kinetic_glow),
    BackdropBlur("layout.backdrop_blur", R.string.motion_blur_title, R.string.layout_summary_backdrop_blur)
}
