# Issue #11: Privacy indicator as a coloured circle

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/11

## Requirements

1. The privacy indicator is a **pulsing filled circle**, with one colour per sensor state:
   - camera only: **green** (`PrivacyColors.camera`, `0xFF00C853`);
   - microphone only: **orange** (`PrivacyColors.microphone`, `0xFFFF9100`);
   - camera and microphone: a circle **half green, half orange** (left half camera, right half microphone).
2. **Companion bubble** (the small circle beside the pill): the circle alone, 12dp.
3. **Main pill** (when the privacy event owns the pill): the camera and/or microphone **icon on the left** and the **circle on the right**, 10dp.
4. The circle appears as soon as a sensor starts being used, updates live when one stops (both, then one colour, then gone), and disappears when neither is in use.
5. TalkBack gets a content description for all three states: "Camera in use", "Microphone in use", "Camera and microphone in use".
6. The mapping is pure and unit-tested. The expanded card still names the app using each sensor.

## Decisions

These settle the open questions in the issue.

1. **Pill or companion:** the privacy event is Tier 1 (Persistent, `Rule_Book.md` §3.1). It takes the main pill when nothing more important owns it, and otherwise it is the companion circle.
2. **Shape:** a filled circle with a gentle pulse (alpha 0.55 to 1.0, 900ms each way, `PrivacyIndicatorDot`). No ring.
3. **Colours:** keep the colours already in code, which follow Android's own indicator (`PrivacyColors`). No new hex values.
4. **When the dot shows:** only while a sensor is actively in use (AppOps `startWatchingActive`, Android 11+ reference). An app that merely holds the camera or microphone in the background does not show the circle.
5. **Mapping:** `PrivacyIndicatorAlert.DotForm {None, Camera, Microphone, Split}` via `dotFormOf(sensors)` in `util/PrivacyIndicator.kt`. `PrivacyIndicatorDot` draws the form.

## Acceptance criteria

- [x] Green circle for camera only; orange for microphone only; half green and half orange for both (code: `PrivacyIndicatorDot`, `PrivacyIndicatorAlert.dotFormOf`; `PrivacyDotFormTest`).
- [x] Companion bubble shows the circle alone; the main pill shows the symbol on the left and the circle on the right (code: `IslandSecondary.kt`, `IslandCollapsedContent.kt`, `PrivacyIndicatorCollapsedGlyph`, `PrivacyIndicatorCollapsedRight`). Not checked on device.
- [x] Updates live when camera or microphone start or stop, and disappears when neither is in use (code: `PrivacyIndicatorAlert.build`; `PrivacyDotFormTest.theCircleFollowsTheUsageLive`).
- [x] Accessible: content descriptions for all three states (code: `PrivacyIndicatorDot`, `PrivacyIndicatorExpanded`). Not tested with TalkBack on device.
- [x] `Rule_Book.md` updated (§3.5 table, §4.15, §4.16, §6) and unit tests added for the colour and form mapping (`PrivacyDotFormTest`, 5 tests).
- [x] Unit tests and lint pass (2061 unit tests pass, lint clean, debug APK builds; reported from the build run for this change).
- [ ] Verified on a device: the circle, the split form, live updates and the TalkBack wording still need a real-device check.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/util/PrivacyIndicator.kt`: `DotForm` and `dotFormOf`.
- `app/src/main/java/com/agupta07505/smartisland/ui/expanded/PrivacyIndicatorExpanded.kt`: `PrivacyIndicatorDot` (pulsing circle, split form, content description); the pill's right side uses it.
- `app/src/main/java/com/agupta07505/smartisland/ui/IslandSecondary.kt`: the companion bubble shows the circle alone (12dp).
- `app/src/test/java/com/agupta07505/smartisland/util/PrivacyDotFormTest.kt`: new, 5 tests.
- `Rule_Book.md`: §3.5 (privacy row), §4.15, §4.16, §6 (test list).
- `CHANGELOG.md`: Unreleased entry.
- `docs/requirements/issue-11-privacy-indicator-dots.md`: this document.

## Tests

- `./gradlew testDebugUnitTest`: 2061 tests pass.
- `./gradlew lintDebug`: clean.
- Debug APK builds.
- Device checks (the three circle forms, live updates, light and dark theme, TalkBack, largest font) are still open.
