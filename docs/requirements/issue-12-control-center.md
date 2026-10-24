# Issue #12: Control center from the idle pill

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/12

## Requirements

1. A **tap** on the collapsed idle pill (no card on the island, shortcuts enabled in settings) opens the **control center**. A long press still opens it too.
2. The control center reuses the existing expanded launcher expansion (`IslandViewModel.openAppsLauncher`, reached through the pill callback).
3. Its contents are NotchIQ's idle dashboard: a search field, battery and Bluetooth status badges, and 2-column quick-control tiles (Wi-Fi, Flashlight, Do Not Disturb, Island Settings). The launcher pager (pinned apps, recents, Utilities page with the toggle grid) is its app area.
4. The **lockscreen** shows only the safe tiles (Flashlight, Do Not Disturb). Search and the app launcher are hidden until unlock.
5. Toggles that Android 16 restricts for normal apps (Wi-Fi, Bluetooth, mobile data) keep opening the system panel, or toggle directly with Shizuku or `WRITE_SECURE_SETTINGS`.
6. Close with an outside tap, a swipe up, or back, as on the expanded island.

## Decisions

These were approved by the user.

1. **Toggles:** the existing `UtilityType` grid, with no new toggles.
2. **Tap with a card on the island:** the tap opens the card, as before. The control center only opens from an idle pill.
3. **Scope:** the idle pill only. The control center is not reachable from the expanded card or the landscape corner pill yet.
4. **Lockscreen:** safe tiles only (`ControlCenterRules.LOCK_SAFE_IDS`).
5. **Style:** the existing grouped dashboard in the NotchIQ palette.
6. **Height:** the measured-height clamp for the empty-island branch is raised from 260dp to 380dp, so the dashboard and launcher fit.

## Implementation

- `util/ControlCenterRules.kt`: `tapOpens(shortcutsEnabled, cardsOnIsland)`, `LOCK_SAFE_IDS`, `visibleIds(ids, locked)` and `showsLauncher(locked)`.
- `ui/IslandMainGestures.kt`: the idle-pill tap calls `onOpenAppsLauncher` when `tapOpens` is true. The earlier branches (idle widget slots, stored-card reveal, landscape idle dot, hidden-pill reveal) keep priority.
- `ui/expanded/IslandExpandedContent.kt`: the empty-island branch draws the `ControlCenter` composable, which hosts the launcher pager. The clamp is 380dp.
- `ui/expanded/IdleDashboard.kt`: the dashboard takes `locked` and filters the tiles with `ControlCenterRules`.

## Acceptance criteria

- [x] A tap on the idle pill with shortcuts on and no card opens the control center (code: `IslandMainGestures`, `ControlCenterRules.tapOpens`).
- [x] A tap with a card on the island still opens that card (`ControlCenterRulesTest`).
- [x] With shortcuts off, a tap does nothing new (`ControlCenterRulesTest`).
- [x] A long press still opens the control center (existing long-press path, unchanged).
- [x] The control center shows the search field, status badges, quick-control tiles and the launcher pager (code: `IdleDashboard`, `IslandExpandedContent`).
- [x] The lockscreen shows only the safe tiles and hides search and the launcher (`ControlCenterRulesTest`, code: `IdleDashboard(locked = ...)`).
- [x] The height clamp is 380dp (code: `IslandExpandedContent`).
- [x] Every Layout group is found by its summary line in settings search (`SettingsSearchIndexTest`, which also closes the gap from issue 16).
- [ ] The tap opens the control center on a device. **Not verified on device.**
- [ ] The larger dashboard height and window sizing render correctly on a device. **Not verified on device.**
- [ ] Lockscreen tile filtering works on a real locked device. **Not verified on device.**
- [ ] The dashboard renders as intended on a device. It was previously reachable only through a per-card Empty path. **Not verified on device.**
- [ ] Toggle behaviour on Android 16 (system panel, or direct with Shizuku) on a device. **Not verified on device.**

## Follow-ups (not in this issue)

- Brightness and volume sliders in the control center.
- Opening the control center from the expanded card.
- The landscape corner pill.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/util/ControlCenterRules.kt`: new.
- `app/src/main/java/com/agupta07505/smartisland/ui/IslandMainGestures.kt`: the idle-pill tap opens the control center.
- `app/src/main/java/com/agupta07505/smartisland/ui/expanded/IdleDashboard.kt`: `locked` flag and tile filtering.
- `app/src/main/java/com/agupta07505/smartisland/ui/expanded/IslandExpandedContent.kt`: the `ControlCenter` composable and the 380dp clamp.
- `app/src/test/java/com/agupta07505/smartisland/util/ControlCenterRulesTest.kt`: new, 6 tests.
- `app/src/test/java/com/agupta07505/smartisland/ui/search/SettingsSearchIndexTest.kt`: new case, every Layout group is found by its summary line.
- `Rule_Book.md`: §3.4 (tap behaviour), §4.5 (control center) and §6 (test list).
- `CHANGELOG.md`: Unreleased entry.
- `docs/requirements/issue-16-layout-group-summaries.md`: the search item is ticked.
- `docs/requirements/issue-12-control-center.md`: this document.

## Tests

- `./gradlew testDebugUnitTest`: 2078 tests pass.
- `./gradlew lintDebug`: clean.
- `./gradlew assembleDebug`: builds.
- Device checks (the tap, dashboard size and window sizing, lockscreen filtering, toggles on Android 16) are still open.
