# Issue #10: Pill symbols only

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/10

## Requirements

1. The collapsed pill and the companion bubble show **symbols and short values only**: no words, no status text and no scrolling or marquee text, for every event type.
2. Calls show the **phone icon (or avatar) on the left** and only the **call time on the right**. While ringing or connecting there is no duration yet, so the right side shows a **pulsing dot** instead of "Ringing" / "Calling" / "Connecting".
3. Every other pill type is checked against the same rule. Messages show the app icon plus the unread dot, or the **message count** for a folded group.
4. Full text stays in the **expanded** card. TalkBack keeps the full words through content descriptions, even though nothing is shown.
5. Pill width is fixed per mode, so it does not depend on text length (`Rule_Book.md` §2.1 and `PillLayout`).

## Decisions

These settle the open questions in the issue.

1. **Nothing numeric to show (messages, and similar):** the symbol alone is enough. A single message shows the app icon and the green unread dot. A grouped message pill shows the app icon and the count, so the dot is only used when there is no number.
2. **Ringing or connecting call:** a pulsing dot in the call colour on the right (`PulsingDot`). It is replaced by the mm:ss timer once the call connects.
3. **TalkBack:** keeps the full words. The privacy dot has a semantics `contentDescription`, and the other pills keep their descriptions.
4. **Grouped message pill:** shows a count number (`groupCount`), not symbols only.

Per pill, the right side is now:

- Flashlight: the "ON" text is replaced by a pulsing yellow dot.
- Live activity: "N min", a check icon when arrived, or a pulsing dot. No "Active" word.
- Navigation: the distance only (e.g. "200 m"), or a pulsing dot when there is none.
- Camera or microphone privacy: a pulsing dot in the sensor colour, instead of "Camera in use" and similar.
- Alarm: keeps the time (a time value is allowed).
- Battery %, hotspot count, timers, stopwatch and Bluetooth: unchanged (they were already a symbol plus a number).

## Acceptance criteria

- [x] No text or marquee/moving text in the collapsed pill or companion bubble for any event type (code: `AlertPill.kt`, `IslandCollapsedContent.kt`; marquee removed). Not checked on device.
- [x] Calls show the phone symbol on the left and the call time on the right; ringing and connecting show a pulsing dot, no words (`IslandCollapsedContent.kt`).
- [x] Every other event type is checked against the same rule. The per-pill list is in the Decisions section above and in `Rule_Book.md` §3.5.
- [x] TalkBack still announces the full meaning. Content descriptions are kept in code. Not tested with TalkBack on device.
- [ ] Pill width stays within the width budget, with no clipping at the largest font size or in landscape. The width is now a fixed 34dp for messages and privacy (`PillLayout.ALERT_BADGE_WING_DP`), capped at 280dp (`MAX_ALERT_PILL_WIDTH_DP`). **Not verified on device.**
- [x] `Rule_Book.md` updated (new §3.5 and the sections that quoted pill text).
- [ ] Unit tests adjusted (`PillLayoutTest`) and `testDebugUnitTest` and lint pass. The test file is updated, but the tests and lint were **not run** in this documentation pass.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/ui/AlertPill.kt`: `AlertPillText`, `alertPillLabel` and the marquee removed; new `AlertPillBadge` (dot or group count) and `PulsingDot`.
- `app/src/main/java/com/agupta07505/smartisland/ui/IslandCollapsedContent.kt`: call, flashlight, live activity, navigation and privacy pills show symbols and values only.
- `app/src/main/java/com/agupta07505/smartisland/ui/expanded/PrivacyIndicatorExpanded.kt`: the pill shows a pulsing dot in the sensor colour instead of the label.
- `app/src/main/java/com/agupta07505/smartisland/ui/PillLayout.kt`: message and privacy wing is a fixed `ALERT_BADGE_WING_DP` (34dp) instead of 6.2dp per character.
- `app/src/test/java/com/agupta07505/smartisland/ui/PillLayoutTest.kt`: updated for the fixed wing.
- `Rule_Book.md`: §2.1, §3.5 (new), §4.1, §4.7, §4.9, §4.14, §4.15, §4.16, §8.1.
- `CHANGELOG.md`: Unreleased entry.
- `docs/requirements/issue-10-symbol-only-pill.md`: this document.

## Tests

- `./gradlew testDebugUnitTest`: not run in this documentation pass.
- `./gradlew lintDebug`: not run in this documentation pass.
- Device checks (largest font, landscape, light and dark theme, TalkBack, a call from ringing to connected) are still open.
