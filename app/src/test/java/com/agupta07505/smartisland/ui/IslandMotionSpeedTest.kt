package com.agupta07505.smartisland.ui

import com.agupta07505.smartisland.util.AnimationSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** NotchIQ's spring presets set the island's motion; upstream's animation-speed setting scales them. */
class IslandMotionSpeedTest {

    @Test
    fun theSpeedSettingScalesEveryPhysicalPresetsStiffnessButNotItsDampingRatio() {
        SpringPreset.entries.filter { it.isPhysical }.forEach { preset ->
            val base = islandSpringStiffness(false, preset)
            assertEquals(preset.stiffness, base, 0f)
            val relaxed = AnimationSpeed.RELAXED.userStiffness(base)
            val normal = AnimationSpeed.NORMAL.userStiffness(base)
            val snappy = AnimationSpeed.SNAPPY.userStiffness(base)
            assertTrue(preset.id, relaxed < normal && normal < snappy)
            assertEquals(preset.id, normal, base, 0f)
            // The scale is applied to the stiffness only; the damping ratio comes from the preset.
            assertEquals(preset.id, preset.dampingRatio, islandSpringDamping(false, preset), 0f)
        }
    }

    @Test
    fun standardKeepsUpstreamsPortraitAndLandscapeTuningAndStillScalesWithTheSpeedSetting() {
        assertEquals(400f, islandSpringStiffness(false, SpringPreset.Standard), 0f)
        assertEquals(280f, islandSpringStiffness(true, SpringPreset.Standard), 0f)
        assertEquals(0.8f, islandSpringDamping(false, SpringPreset.Standard), 0f)
        assertEquals(0.86f, islandSpringDamping(true, SpringPreset.Standard), 0f)
        assertTrue(AnimationSpeed.SNAPPY.userStiffness(400f) > AnimationSpeed.NORMAL.userStiffness(400f))
    }

    @Test
    fun theOfflineBuildOffersNoWeatherWidgetButKeepsTheOtherIdleWidgetKinds() {
        val kinds = com.agupta07505.smartisland.model.IdleWidgetKind.selectable()
        val hidden = com.agupta07505.smartisland.model.IdleWidgetKind.offlineHidden
        assertTrue(hidden.isNotEmpty() && hidden.none { it in kinds })
        assertTrue(kinds.containsAll(com.agupta07505.smartisland.model.IdleWidgetKind.values().filter { it !in hidden }))
    }
}
