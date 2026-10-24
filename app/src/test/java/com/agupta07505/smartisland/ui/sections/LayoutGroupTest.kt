/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import java.io.File
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

    @Test
    fun everyGroupHasItsOwnSummaryLine() {
        val ids = LayoutGroup.entries.map { it.summaryRes }
        assertTrue(ids.all { it != 0 })
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun summariesAreShortSingleLines() {
        val xml = File("src/main/res/values/strings_layout_rides.xml").readText()
        val summaries = Regex("""<string name="layout_summary_[a-z_]+">([^<]*)</string>""").findAll(xml).map { it.groupValues[1] }.toList()
        // One per group, plus the form-factor row, Animation speed, Cutout merge style, Idle widgets and two landscape groups.
        assertTrue(summaries.size >= LayoutGroup.entries.size + 6)
        summaries.forEach {
            assertTrue(it, it.isNotBlank() && '\n' !in it && it.length <= 60)
            assertTrue(it, !it.endsWith("."))
        }
    }
}
