package com.agupta07505.smartisland.util

/** The tap and lockscreen rules of the control center (Rule_Book §4.5). Pure, so they are unit-testable. */
object ControlCenterRules {
    /** Tiles that work without unlocking: they change a setting and leave no app or network page behind. */
    val LOCK_SAFE_IDS: Set<String> = setOf("flashlight", "dnd")

    /**
     * A tap on the collapsed pill opens the control center only when nothing else claims it: the feature is on and
     * the island holds no card (with a card, the tap opens that card as before).
     */
    fun tapOpens(shortcutsEnabled: Boolean, cardsOnIsland: Int): Boolean = shortcutsEnabled && cardsOnIsland == 0

    /** The tile ids shown for the lock state: all of them unlocked, only the safe ones while locked. */
    fun visibleIds(ids: List<String>, locked: Boolean): List<String> =
        if (locked) ids.filter { it in LOCK_SAFE_IDS } else ids

    /** Search and the app launcher start apps, so they wait for an unlock. */
    fun showsLauncher(locked: Boolean): Boolean = !locked
}
