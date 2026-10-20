/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutGroupTest {
    @Test
    fun groupsAreInTheOrderOfTheLayoutScreen() {
        assertEquals(
            listOf(
                LayoutGroup.QuickPresets,
                LayoutGroup.ExemptApps,
                LayoutGroup.SizeAndPosition,
                LayoutGroup.CameraCutout,
                LayoutGroup.Shadow,
                LayoutGroup.MotionPhysics,
                LayoutGroup.KineticGlow,
                LayoutGroup.BackdropBlur
            ),
            LayoutGroup.entries
        )
    }

    @Test
    fun searchKeysAreUniqueAndUseTheLayoutPrefix() {
        val keys = LayoutGroup.entries.map { it.searchKey }
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.all { it.startsWith("layout.") })
    }
}
