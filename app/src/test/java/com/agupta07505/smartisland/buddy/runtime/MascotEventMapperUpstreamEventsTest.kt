package com.agupta07505.smartisland.buddy.runtime

import com.agupta07505.smartisland.model.IslandMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The buddy has no reaction (yet) for the cards upstream 7.6.0 added around NotchIQ's: wired headphones and USB,
 * the Face ID scan, a hotspot and audio recording. They must pass through the mapper silently, and the cards it
 * does know (a stopwatch is not one) must keep reacting while those are on screen.
 */
class MascotEventMapperUpstreamEventsTest {
    private fun note(key: String, mode: IslandMode, title: String = "", text: String = "", time: Long = 0L) =
        NoteSnapshot(key, mode, title, text, timeMillis = time)

    private val mapper = MascotEventMapper()

    @Test
    fun upstreamOnlyCardsProduceNoMoments() {
        listOf(
            note("hp", IslandMode.Bluetooth, "Headphones connected"),
            note("usb", IslandMode.Bluetooth, "USB connected"),
            note("hotspot", IslandMode.Hotspot, "Hotspot", "On"),
            note("rec", IslandMode.AudioRecording, "Recording"),
            note("stopwatch", IslandMode.Stopwatch, "Stopwatch")
        ).forEach { card ->
            assertEquals(card.key, emptyList<MascotMoment>(), mapper.onChange(listOf(card), 10.0, 1_000L))
            mapper.reset()
        }
    }

    @Test
    fun aTimerStillReactsWhenAnUpstreamCardSitsBesideIt() {
        val moments = mapper.onChange(
            listOf(note("t", IslandMode.Timer, time = 60_000L), note("hotspot", IslandMode.Hotspot, "Hotspot", "On")),
            10.0, 1_000L
        )
        assertEquals(listOf(MascotMoment.TimerStarted), moments)
    }

    @Test
    fun aTimerThatRunsOutWhileAHotspotIsUpRingsTheTimeIsUpMoment() {
        val timer = note("t", IslandMode.Timer, time = 5_000L)
        val hotspot = note("hotspot", IslandMode.Hotspot, "Hotspot", "On")
        mapper.onChange(listOf(timer, hotspot), 10.0, 1_000L)
        assertEquals(listOf(MascotMoment.TimeIsUp), mapper.onChange(listOf(hotspot), 11.0, 5_000L))
    }
}
