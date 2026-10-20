/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import com.agupta07505.smartisland.data.SmartIslandNotificationRepository
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The rulebook (Rule_Book 1 and 3) as the view model really runs it: the default alert engine sits between the
 * stored cards and what the island shows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IslandViewModelAlertRulesTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** The engine runs on the test's virtual clock, so seven seconds really are seven virtual seconds. */
    private fun newViewModel(notifRepo: SmartIslandNotificationRepository) =
        IslandViewModel(
            mockk<SmartIslandSettingsRepository>(relaxed = true),
            notifRepo,
            clock = { 1_000_000L + testDispatcher.scheduler.currentTime }
        )

    private fun chat(key: String, pkg: String = "pkg.$key", time: Long = 1_000L) = IslandNotification(
        key = key, packageName = pkg, appName = pkg, title = "Sam", text = "hi", timeMillis = time
    )

    /** A Background-tier card that stays on the island by itself (a live activity does not idle out in portrait). */
    private fun music() = IslandNotification(
        key = "music", packageName = "pkg.music", appName = "Music", title = "Song", text = "Artist",
        mode = IslandMode.LiveActivity, timeMillis = 1L
    )

    @Test
    fun theIslandNeverShowsMoreThanTheMainPillAndTheCompanion() = runTest {
        val notifRepo = SmartIslandNotificationRepository()
        val viewModel = newViewModel(notifRepo)
        notifRepo.postNotification(music())
        notifRepo.postNotification(chat("a"))
        notifRepo.postNotification(chat("b"))
        testDispatcher.scheduler.runCurrent()
        assertEquals(2, viewModel.visibleNotifications.value.size)
    }

    @Test
    fun aMessageTakesTheMainPillForSevenSecondsThenHandsItBackToTheMusic() = runTest {
        val notifRepo = SmartIslandNotificationRepository()
        val viewModel = newViewModel(notifRepo)
        notifRepo.postNotification(music())
        testDispatcher.scheduler.runCurrent()
        notifRepo.postNotification(chat("a"))
        testDispatcher.scheduler.runCurrent()
        assertEquals(listOf("a", "music"), viewModel.visibleNotifications.value.map { it.key })

        testDispatcher.scheduler.advanceTimeBy(6_900L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(listOf("a", "music"), viewModel.visibleNotifications.value.map { it.key })

        testDispatcher.scheduler.advanceTimeBy(200L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(listOf("music"), viewModel.visibleNotifications.value.map { it.key })
    }

    @Test
    fun messagesFromOneAppFoldIntoOneCountedEntry() = runTest {
        val notifRepo = SmartIslandNotificationRepository()
        val viewModel = newViewModel(notifRepo)
        notifRepo.postNotification(chat("m1", pkg = "pkg.chat", time = 1_000L))
        notifRepo.postNotification(chat("m2", pkg = "pkg.chat", time = 2_000L))
        notifRepo.postNotification(chat("m3", pkg = "pkg.chat", time = 3_000L))
        testDispatcher.scheduler.runCurrent()
        val shown = viewModel.visibleNotifications.value
        assertEquals(1, shown.size)
        assertEquals(3, shown.single().groupCount)
        assertTrue(shown.single().memberKeys.containsAll(listOf("m1", "m2", "m3")))
    }

    @Test
    fun anOpenCardHoldsTheSevenSecondClock() = runTest {
        val notifRepo = SmartIslandNotificationRepository()
        val viewModel = newViewModel(notifRepo)
        notifRepo.postNotification(chat("a"))
        testDispatcher.scheduler.runCurrent()
        viewModel.expand()
        testDispatcher.scheduler.runCurrent()
        // The card auto-collapses after its own countdown, but the message must outlive the seven seconds while open.
        testDispatcher.scheduler.advanceTimeBy(4_000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(listOf("a"), viewModel.visibleNotifications.value.map { it.key })
    }
}
