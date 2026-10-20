package com.agupta07505.smartisland.alert

import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import com.agupta07505.smartisland.model.IslandPriority
import com.agupta07505.smartisland.model.isSameAppSibling
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The seam between NotchIQ's alert engine (Rule_Book 1 and 3: two slots, folding, tiers) and upstream's
 * priority model (`IslandPriority`) and same-app grouping (`SameAppCards`): the engine's order must agree
 * with upstream's ranks where both apply, and where the two disagree (more than two cards, siblings) the
 * engine wins.
 */
class AlertEngineUpstreamSeamTest {

    private fun alert(
        key: String,
        pkg: String,
        mode: IslandMode = IslandMode.Notification,
        category: String? = "msg",
        title: String = "Sam",
        time: Long = 0L
    ) = IslandNotification(
        key = key, packageName = pkg, appName = pkg, title = title, text = "hi",
        timeMillis = time, category = category, mode = mode
    )

    private val call = alert("call", "com.android.dialer", IslandMode.IncomingCall, "call")
    private val music = alert("music", "com.spotify.music", IslandMode.Music, "transport")
    private val msgA = alert("a", "com.whatsapp", time = 1L)
    private val msgB = alert("b", "com.whatsapp", time = 2L)

    @Test
    fun theEngineAndUpstreamPriorityAgreeThatACallOutranksMusicAndAMessage() {
        assertTrue(IslandPriority.rank(call) < IslandPriority.rank(music))
        assertTrue(IslandPriority.rank(call) < IslandPriority.rank(msgA))
        val visible = AlertEngine().evaluate(listOf(music, msgA, call), nowMs = 10L).visible
        assertEquals("call", visible.first().key)
    }

    @Test
    fun upstreamSeesTwoSameAppMessagesAsSiblingsButTheEngineFoldsThemIntoOneCounted_entry() {
        assertTrue(isSameAppSibling(msgA, msgB))
        val visible = AlertEngine().evaluate(listOf(msgA, msgB), nowMs = 10L).visible
        assertEquals(1, visible.size)
        assertEquals(2, visible.single().groupCount)
        assertEquals(listOf("a", "b").toSet(), visible.single().memberKeys.toSet())
    }

    @Test
    fun theEngineCapsTheIslandAtTwoSlotsWhereUpstreamWouldCycleThreeOrMoreCards() {
        val others = alert("c", "com.telegram", time = 3L)
        val visible = AlertEngine().evaluate(listOf(call, music, others), nowMs = 10L).visible
        assertEquals(2, visible.size)
        assertEquals("call", visible[0].key)
    }

    @Test
    fun aTransparentEngineLeavesUpstreamsOrderingToTheViewModel() {
        val transparent = AlertEngine(maxActive = Int.MAX_VALUE, foldGroups = false)
        val visible = transparent.evaluate(listOf(msgA, msgB, call), nowMs = 10L).visible
        assertEquals(3, visible.size)
        assertEquals(setOf("a", "b", "call"), visible.map { it.key }.toSet())
    }
}
