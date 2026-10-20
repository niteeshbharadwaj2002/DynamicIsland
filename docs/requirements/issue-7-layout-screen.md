# Issue #7: Layout screen grouping and description removal

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/7

## Requirements

1. Every Layout screen setting uses **one grouped style** (one rounded card per group, consistent header, row height and spacing), with no mixed bordered-card, plain-row and highlighted styles.
2. Related settings are **grouped** in the order the issue lists: Quick presets, Exempt apps, Size and position, Camera cutout, Shadow, Motion physics, Kinetic glow. Backdrop blur, Animation speed, Cutout merge style and Idle widgets follow the same style. Groups are **collapsed by default**. The Compact Pill / Notch switch sits **above the seven numbered groups**, because the issue listed seven groups and did not place the switch.
3. **Explanatory description text is removed** from the groups and the settings inside them. Titles only. Values that carry information (selected preset, check mark, toggle state) stay. Settings search still finds each setting by its title.

## Acceptance criteria

- [ ] All Layout settings use the same grouped style, with no mixed card, plain-row or highlighted styles. Not visually checked on device.
- [ ] The seven groups are present, in the stated order, collapsed by default. Not visually checked on device.
- [ ] No explanatory description or subtitle lines remain on the Layout screen. Not visually checked on device.
- [x] Settings search finds these settings by title (`SettingsSearchIndexTest`, unit tests).
- [ ] Accessibility: removed descriptions are replaced by `contentDescription` or state descriptions where a control needs one. TalkBack not checked.
- [ ] No overlap or clipping at the largest font size. **Not verified.**
- [ ] No overlap or clipping in light and dark theme. **Not verified.**
- [ ] No overlap or clipping in landscape. **Not verified.**
- [x] Unit tests pass (2052 on the branch; 2055 after merging `origin/main`, which adds the 3 `HomeStatusTest` tests).
- [x] Lint is clean.
- [x] Debug build installed on a device.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/ui/sections/LayoutGroup.kt`: new shared `CollapsibleGroup` and `SegmentedChoiceRow` widgets.
- `app/src/main/java/com/agupta07505/smartisland/ui/components/GroupedList.kt`: grouped-list style shared by the Layout screen.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/PositionsSection.kt`: Layout screen and its groups.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/AnimationSpeedSection.kt`, `IdleWidgetsSection.kt`, `LandscapeSection.kt`, `MotionSection.kt`, `ExemptAppsCard.kt`, `IslandFontPicker.kt`: moved to the shared style and description text removed.
- `app/src/main/java/com/agupta07505/smartisland/ui/search/SettingsSearchIndex.kt`: indexes every Layout group by title.
- `app/src/main/res/values/strings_layout_rides.xml`: new `layout_group_*` strings.
- `app/src/test/java/com/agupta07505/smartisland/ui/search/SettingsSearchIndexTest.kt`: updated for removed descriptions.
- `app/src/test/java/com/agupta07505/smartisland/ui/sections/LayoutGroupTest.kt`: new.
- `CHANGELOG.md`: Unreleased entry and follow-ups.

## Known follow-ups

- The new `layout_group_*` strings have no translations yet and fall back to English.
- The Landscape tab and the Exempt apps rows still use hardcoded English labels.

## Tests

- `./gradlew testDebugUnitTest`: 2052 pass on the branch. After merging `origin/main`, 2055 pass (2052 plus 3 in `HomeStatusTest`), 0 failures.
- `./gradlew lintDebug`: clean.
- Debug build installed on a device. Visual checks (largest font, light and dark theme, landscape, TalkBack) are still open.
