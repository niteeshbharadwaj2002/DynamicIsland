package com.agupta07505.smartisland.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlCenterRulesTest {
    private val ids = listOf("wifi", "flashlight", "dnd", "settings")

    @Test
    fun anIdlePillOpensTheControlCenterOnATap() = assertTrue(ControlCenterRules.tapOpens(shortcutsEnabled = true, cardsOnIsland = 0))

    @Test
    fun aTapWithACardOnTheIslandStillOpensTheCard() = assertFalse(ControlCenterRules.tapOpens(true, 1))

    @Test
    fun withShortcutsOffATapDoesNothingNew() = assertFalse(ControlCenterRules.tapOpens(false, 0))

    @Test
    fun unlockedShowsEveryTileInOrder() = assertEquals(ids, ControlCenterRules.visibleIds(ids, locked = false))

    @Test
    fun lockedShowsOnlyTheSafeTiles() =
        assertEquals(listOf("flashlight", "dnd"), ControlCenterRules.visibleIds(ids, locked = true))

    @Test
    fun lockedHidesSearchAndTheLauncher() {
        assertFalse(ControlCenterRules.showsLauncher(locked = true))
        assertTrue(ControlCenterRules.showsLauncher(locked = false))
    }
}
