package com.agupta07505.smartisland.util

import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import com.agupta07505.smartisland.util.PrivacyIndicatorAlert.DotForm
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivacyDotFormTest {
    private fun alert(category: String) = IslandNotification(
        key = PrivacyIndicatorAlert.KEY, packageName = "p", appName = "", title = "", text = "", timeMillis = 0L,
        category = category, mode = IslandMode.PrivacyIndicator
    )

    private fun form(category: String) = PrivacyIndicatorAlert.dotFormOf(PrivacyIndicatorAlert.sensorsOf(alert(category)))

    @Test
    fun cameraOnlyIsTheGreenCircle() = assertEquals(DotForm.Camera, form(PrivacyIndicatorAlert.CATEGORY_CAMERA))

    @Test
    fun microphoneOnlyIsTheOrangeCircle() = assertEquals(DotForm.Microphone, form(PrivacyIndicatorAlert.CATEGORY_MICROPHONE))

    @Test
    fun bothIsTheSplitCircle() = assertEquals(DotForm.Split, form(PrivacyIndicatorAlert.CATEGORY_BOTH))

    @Test
    fun anythingElseDrawsNothing() {
        assertEquals(DotForm.None, form("alarm"))
        assertEquals(DotForm.None, PrivacyIndicatorAlert.dotFormOf(PrivacyIndicatorAlert.sensorsOf(null)))
    }

    @Test
    fun theCircleFollowsTheUsageLive() {
        fun formFor(usage: PrivacyUsage) = PrivacyIndicatorAlert.build(usage, "own", { it }, 0L)
            ?.let { PrivacyIndicatorAlert.dotFormOf(PrivacyIndicatorAlert.sensorsOf(it)) } ?: DotForm.None
        val both = PrivacyUsage().with(PrivacySensor.Camera, "a", true).with(PrivacySensor.Microphone, "b", true)
        assertEquals(DotForm.Split, formFor(both))
        val micOnly = both.with(PrivacySensor.Camera, "a", false)
        assertEquals(DotForm.Microphone, formFor(micOnly))
        assertEquals(DotForm.None, formFor(micOnly.with(PrivacySensor.Microphone, "b", false)))
    }
}
