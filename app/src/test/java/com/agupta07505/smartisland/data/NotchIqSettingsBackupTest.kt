package com.agupta07505.smartisland.data

import com.agupta07505.smartisland.buddy.model.MascotPalette
import com.agupta07505.smartisland.buddy.model.MascotShape
import com.agupta07505.smartisland.buddy.model.MascotStyle
import com.agupta07505.smartisland.ui.SpringPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** NotchIQ's settings keys ride along upstream's backup and restore (toJson / fromJson). */
class NotchIqSettingsBackupTest {

    @Test
    fun notchIqKeysSurviveABackupRoundTrip() {
        val original = SmartIslandSettings.Default.copy(
            buddyEnabled = true,
            buddyStyle = MascotStyle.entries.last().id,
            buddyShape = MascotShape.entries.last().id,
            buddyPalette = MascotPalette.entries.last().id,
            buddyReacts = false,
            buddyHidesWhenIdle = false,
            springPreset = SpringPreset.Snappy.id,
            kineticGlow = false,
            aodEnabled = true,
            backdropBlur = false,
            secureAppPackages = setOf("com.bank.app", "com.wallet")
        )
        val restored = SmartIslandSettings.fromJson(original.toJson(appVersion = "7.6.0-personal"))
        assertEquals(original.buddyEnabled, restored.buddyEnabled)
        assertEquals(original.buddyStyle, restored.buddyStyle)
        assertEquals(original.buddyShape, restored.buddyShape)
        assertEquals(original.buddyPalette, restored.buddyPalette)
        assertEquals(false, restored.buddyReacts)
        assertEquals(false, restored.buddyHidesWhenIdle)
        assertEquals(SpringPreset.Snappy.id, restored.springPreset)
        assertEquals(false, restored.kineticGlow)
        assertEquals(true, restored.aodEnabled)
        assertEquals(false, restored.backdropBlur)
        assertEquals(setOf("com.bank.app", "com.wallet"), restored.secureAppPackages)
    }

    @Test
    fun aBackupFromUpstreamWithoutNotchIqKeysRestoresTheirDefaults() {
        val upstreamStyle = SmartIslandSettings.Default.toJson()
            .let { org.json.JSONObject(it) }
            .also { root ->
                val s = root.getJSONObject("settings")
                listOf(
                    "buddyEnabled", "buddyStyle", "buddyShape", "buddyPalette", "buddySide", "buddyVisits",
                    "buddyReacts", "buddyHidesWhenIdle", "springPreset", "kineticGlow", "aodEnabled",
                    "backdropBlur", "secureAppPackages"
                ).forEach { s.remove(it) }
            }.toString()
        val restored = SmartIslandSettings.fromJson(upstreamStyle)
        val defaults = SmartIslandSettings.Default
        assertEquals(defaults.buddyEnabled, restored.buddyEnabled)
        assertEquals(defaults.springPreset, restored.springPreset)
        assertEquals(defaults.kineticGlow, restored.kineticGlow)
        assertTrue(restored.secureAppPackages.isEmpty())
    }

    @Test
    fun anUnknownSpringPresetFromAnotherVersionFallsBackToStandard() {
        val json = org.json.JSONObject(SmartIslandSettings.Default.toJson()).also {
            it.getJSONObject("settings").put("springPreset", "from-the-future")
        }.toString()
        assertEquals(SpringPreset.Standard.id, SmartIslandSettings.fromJson(json).springPreset)
    }
}
