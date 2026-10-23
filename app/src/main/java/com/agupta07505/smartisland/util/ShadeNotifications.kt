package com.agupta07505.smartisland.util

import android.app.PendingIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The reply a shade notification offers: the intent to fire and the key its text goes under. */
data class ShadeReply(val pendingIntent: PendingIntent, val remoteInputKey: String)

/** One notification of the shade as the notification center lists it (Rule_Book §4.17). */
data class ShadeItem(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val clearable: Boolean,
    val reply: ShadeReply? = null
)

/** One app's notifications, newest first. */
data class ShadeGroup(val packageName: String, val appLabel: String, val items: List<ShadeItem>)

/**
 * The shade's live notifications for the swipe-down notification center, kept by the notification listener service
 * (same debounced refresh as [NotificationCountTracker]) and read by the panel. The actions go back through the
 * service, which owns the notification access.
 */
object ShadeNotifications {
    private val state = MutableStateFlow<List<ShadeItem>>(emptyList())
    val items: StateFlow<List<ShadeItem>> = state

    @Volatile var dismiss: ((String) -> Unit)? = null
    @Volatile var dismissAll: (() -> Unit)? = null
    @Volatile var open: ((String) -> Unit)? = null

    fun update(list: List<ShadeItem>) {
        if (state.value != list) state.value = list
    }

    fun clear() {
        state.value = emptyList()
        dismiss = null
        dismissAll = null
        open = null
    }

    /**
     * A swipe down on the pill opens the notification center only on the idle pill: with a card on the island the
     * same pull keeps expanding that card (Rule_Book §4.17).
     */
    fun opensOnSwipe(downward: Boolean, cardsOnIsland: Int): Boolean = downward && cardsOnIsland == 0

    /** Apps newest first, each app's notifications newest first. Pure, so it is unit-testable. */
    fun group(items: List<ShadeItem>): List<ShadeGroup> =
        items.groupBy { it.packageName }
            .map { (pkg, list) ->
                ShadeGroup(pkg, list.first().appLabel, list.sortedByDescending { it.postTime })
            }
            .sortedByDescending { it.items.first().postTime }

    /**
     * What the panel may show while the device is locked (Rule_Book §8.1): the app and how many, never the sender
     * or the text, and no reply. Unlocked, the items pass through untouched.
     */
    fun forLockState(items: List<ShadeItem>, locked: Boolean): List<ShadeItem> =
        if (!locked) items else items.map { it.copy(title = "", text = "", reply = null) }
}
