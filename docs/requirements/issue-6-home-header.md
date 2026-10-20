# Issue #6: home header rename and status pill

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/6

## Requirements

1. The home header and the launcher label read **NotchIQ** instead of "Smart Island Personal".
2. The status pill (Active / Ready / Setup needed) sits **below** the app name and subtitle, not beside it, so the name is not crowded or clipped.

## Acceptance criteria

- [x] `app_name` is `NotchIQ` in every locale, and the launcher label shows NotchIQ (strings checked; debug build installed on device).
- [x] The status pill is placed under the app name and subtitle (code change; debug build installed on device).
- [ ] No overlap or clipping at the largest system font size. **Not verified.**
- [ ] No overlap or clipping in landscape. **Not verified.**
- [ ] The pill stays tappable (`onHealthClick`) and keeps its colours. Not separately verified.
- [x] About screen, README and changelog updated where they quote the name.
- [x] Unit tests pass (2051 pass, including 3 new in `HomeStatusTest`).
- [x] Lint is clean.
- [x] Debug build installed on a device.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/ui/SmartIslandHomeScreen.kt`: header layout and `homeStatus()` use.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/AboutSection.kt`: About title.
- `app/src/main/res/values/strings.xml`, `values-en`, `values-es`, `values-ja`, `values-pt-rBR`, `values-zh-rCN`: `app_name`, master switch and About strings.
- `app/src/test/java/com/agupta07505/smartisland/ui/HomeStatusTest.kt`: new, 3 tests.
- `CHANGELOG.md`: Unreleased entry.

## Tests

- `./gradlew testDebugUnitTest`: 2051 pass (2048 before this change, plus 3 in `HomeStatusTest`).
- `./gradlew lintDebug`: clean.
