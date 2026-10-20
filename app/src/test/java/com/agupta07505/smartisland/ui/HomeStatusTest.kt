package com.agupta07505.smartisland.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** The home header's status pill: missing setup beats everything, then on/off. */
class HomeStatusTest {
    @Test fun setupNeededWinsEvenWhenEnabled() {
        assertEquals(HomeStatus.SETUP_NEEDED, homeStatus(canEnable = false, isIslandEnabled = true))
        assertEquals(HomeStatus.SETUP_NEEDED, homeStatus(canEnable = false, isIslandEnabled = false))
    }

    @Test fun activeWhenEnabledAndPermitted() {
        assertEquals(HomeStatus.ACTIVE, homeStatus(canEnable = true, isIslandEnabled = true))
    }

    @Test fun readyWhenPermittedButOff() {
        assertEquals(HomeStatus.READY, homeStatus(canEnable = true, isIslandEnabled = false))
    }
}
