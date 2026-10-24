# Issue #13: Swipe down opens the notification center

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/13

## Requirements

1. A swipe **down** on the portrait idle pill (no card on the island) expands the island onto the Notification Center.
2. The panel lists the shade's live notifications, grouped by app. Apps are ordered by their newest notification, and each app's items are newest first. The app's own notifications and group summaries are excluded.
3. Each item has the existing actions: tap opens the app, a cross dismisses it (if clearable), "Clear all" cancels every clearable item, and "Reply" sends through the notification's own reply action (only when it offers one).
4. Locked device: masked. Each app shows only a count ("N notifications"). No sender, no text, no reply and no Clear all (`Rule_Book.md` §8.1).
5. Close with a tap outside, a swipe up or back, the same as the expanded island.
6. With a card on the island, the downward pull still expands that card as before.
7. The swipe uses the existing pill swipe arming (gesture sensitivity applies) and needs pill swipe actions on (Settings > Gestures).

## Decisions

These were approved by the user.

1. **Live list, not history.** The panel shows the shade as it is now. The history screen is not used.
2. **Idle pill only.** A card on the island keeps its existing downward pull.
3. **Lockscreen:** masked. Opening, replying and Clear all need an unlock.
4. **Actions:** open, dismiss, clear all and reply.
5. **Landscape corner pill:** not included.

Code: `util/ShadeNotifications.kt` (`opensOnSwipe` is the routing rule, `forLockState` the masking).

## Acceptance criteria

- [x] A swipe down on the idle island opens the Notification Center with the live notifications (code: `IslandMainGestures.commitPillSwipe`, `IslandExpandedContent.kt`). Not checked on device.
- [x] The existing actions (open, dismiss, clear all, reply) and the lockscreen masking work inside it (code: `NotificationCenter.kt`, `SmartIslandNotificationListenerService.kt`). Not checked on device.
- [ ] Closing gestures work (tap outside, swipe up, back), and there is no conflict with tap and sideways swipe. **Not verified on device.**
- [x] The gesture uses pill swipe arming, so the gesture sensitivity setting applies (code: `commitPillSwipe`).
- [x] Unit tests for the gesture routing (`ShadeNotificationsTest.swipeDownOpensTheCenterOnlyOnTheIdlePill`); `Rule_Book.md` updated (§3.4, §4.17, §6); `testDebugUnitTest` and lint pass.

## Not verified on device

- The open and close gestures, and that they do not conflict with the tap and sideways swipe.
- Reply sending from the panel, on a real notification with a `RemoteInput` action.
- Clear all and the cross dismiss, on a live shade.
- Lockscreen masking in the panel.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/util/ShadeNotifications.kt`: new. `ShadeItem`, `ShadeGroup`, `ShadeReply`, grouping, lock masking, swipe routing and the action hooks.
- `app/src/main/java/com/agupta07505/smartisland/service/SmartIslandNotificationListenerService.kt`: `publishShadeItems` from the debounced `refreshCountEntries`, `openShadeNotification`, hooks set on connect and cleared on disconnect.
- `app/src/main/java/com/agupta07505/smartisland/ui/expanded/NotificationCenter.kt`: new. `IdlePanel` enum, `LocalIdlePanel` and the panel.
- `app/src/main/java/com/agupta07505/smartisland/ui/IslandViewModel.kt`: `idlePanel` flow, `openNotificationCenter()`.
- `app/src/main/java/com/agupta07505/smartisland/ui/IslandCallbacks.kt`, `IslandOverlayView.kt`, `OverlayIsland.kt`: `onOpenNotificationCenter` threaded through.
- `app/src/main/java/com/agupta07505/smartisland/ui/IslandMainGestures.kt`: the swipe down route in `commitPillSwipe`.
- `app/src/main/java/com/agupta07505/smartisland/ui/expanded/IslandExpandedContent.kt`: shows the panel for `IdlePanel.Notifications`.
- `app/src/main/res/values/strings_notification_center.xml`: new strings (English; locales fall back).
- `app/src/test/java/com/agupta07505/smartisland/util/ShadeNotificationsTest.kt`: new, 5 tests.
- `Rule_Book.md`: §3.4 (swipe down on the idle pill), §4.17 (new), §6 (test list).
- `CHANGELOG.md`: Unreleased entry.
- `README.md`: gestures line.
- `docs/requirements/issue-13-notification-center.md`: this document.

## Tests

- `./gradlew testDebugUnitTest`: passes, including the 5 `ShadeNotificationsTest` tests (grouping order, empty, lock masking, unlocked pass-through, swipe routing).
- `./gradlew lintDebug`: passes.
- Debug build: builds.
- Device checks are still open (see above).
