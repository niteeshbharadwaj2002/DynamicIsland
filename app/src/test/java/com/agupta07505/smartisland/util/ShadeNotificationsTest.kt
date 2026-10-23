package com.agupta07505.smartisland.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadeNotificationsTest {
    private fun item(key: String, pkg: String, time: Long, title: String = "T$key", text: String = "x$key", clearable: Boolean = true) =
        ShadeItem(key, pkg, "App $pkg", title, text, time, clearable)

    @Test
    fun groupsAreByAppNewestAppFirstAndNewestItemFirst() {
        val groups = ShadeNotifications.group(
            listOf(item("a1", "a", 10), item("b1", "b", 30), item("a2", "a", 20), item("c1", "c", 5))
        )
        assertEquals(listOf("b", "a", "c"), groups.map { it.packageName })
        assertEquals(listOf("a2", "a1"), groups[1].items.map { it.key })
    }

    @Test
    fun noNotificationsMeansNoGroups() = assertTrue(ShadeNotifications.group(emptyList()).isEmpty())

    @Test
    fun lockedItemsCarryNoTitleOrText() {
        val masked = ShadeNotifications.forLockState(listOf(item("a1", "a", 1, title = "Mum", text = "Call me")), locked = true)
        assertEquals("", masked.single().title)
        assertEquals("", masked.single().text)
        assertEquals("App a", masked.single().appLabel)
        assertEquals(null, masked.single().reply)
    }

    @Test
    fun unlockedItemsPassThroughUntouched() {
        val items = listOf(item("a1", "a", 1, title = "Mum", text = "Call me"))
        assertEquals(items, ShadeNotifications.forLockState(items, locked = false))
    }

    @Test
    fun swipeDownOpensTheCenterOnlyOnTheIdlePill() {
        assertTrue(ShadeNotifications.opensOnSwipe(downward = true, cardsOnIsland = 0))
        assertFalse(ShadeNotifications.opensOnSwipe(downward = true, cardsOnIsland = 1))
        assertFalse(ShadeNotifications.opensOnSwipe(downward = false, cardsOnIsland = 0))
    }
}
