# Issue #16: One-line summary under each Layout group

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/16

## Requirements

1. Every group on the Layout screen shows **one short gray line** under its title. The line is always visible in the collapsed row.
2. The line is **one line only**. Long text is ellipsized and never wraps, including at the largest font size.
3. The same wording is used in **Portrait and Landscape**, except where the Landscape behaviour differs (Idle cutout and Cutout with content have their own lines).
4. The copy lives in `res/values/strings_layout_rides.xml` as `layout_summary_*`. Other locales fall back to English until they are translated.
5. Settings search matches the summary text as well as the title.

## Decisions

These settle the open questions in the issue.

1. **Placement:** the summary sits **under the title in the collapsed row**, not only once the group is expanded. A user can tell what a group does without opening it.
2. **Landscape:** the Landscape groups use the same wording as Portrait. The two Landscape-only groups (Idle cutout, Cutout with content) get their own lines, because their behaviour is different.
3. **Length:** each summary is at most 60 characters, with no trailing period. A unit test enforces this, so the copy stays on one line in normal widths.
4. **Compact Pill / iPhone Notch Mode:** the segmented control row gets the form-factor summary too, so every group on the screen has one.

## Wording

| Group | Summary (`layout_summary_*`) |
|---|---|
| Compact Pill / iPhone Notch Mode (`form_factor`) | Choose how the island looks around the camera |
| Quick presets (`presets`) | One-tap sizes for the pill |
| Exempt apps (`exempt_apps`) | Apps where the island stays hidden |
| Size and position (`size_position`) | Set the pill's width, height and place on screen |
| Camera cutout (`camera_cutout`) | Fit the pill around your camera hole |
| Shadow (`shadow`) | Soft shadow behind the pill |
| Motion physics (`motion`) | How the island springs open and closed |
| Kinetic glow (`kinetic_glow`) | A glow that follows the island's motion |
| Backdrop blur (`backdrop_blur`) | Blur what is behind the expanded island |
| Animation speed (`anim_speed`) | How fast the island animates |
| Cutout merge style (`merge_style`) | How the pill joins the camera cutout |
| Idle widgets (`idle_widgets`) | What the island shows when nothing is happening |
| Landscape: Idle cutout (`land_idle`) | How the pill sits around the camera when idle |
| Landscape: Cutout with content (`land_content`) | How the pill grows when something is showing |

## Acceptance criteria

- [x] Every group on the Layout screen, in Portrait and Landscape, has a one-line summary (code: `CollapsibleGroup`, `SegmentedChoiceRow`, `GroupedRow` and `SwitchRow` take a summary; `LayoutGroup.summaryRes`). Not checked on device.
- [ ] No summary wraps or clips at the largest font size. Long text is ellipsized (`maxLines = 1` with `TextOverflow.Ellipsis` in code). **Not verified on device.**
- [ ] Works in light and dark theme with enough contrast. **Not verified on device.**
- [ ] TalkBack reads the title and then the summary. **Not checked.**
- [x] New strings are added for English. Other locales fall back to English (no per-locale `layout_summary_*` strings exist).
- [ ] Settings search matches summary text and `SettingsSearchIndexTest` is updated. The search index matches summary text in code (`SettingsSearchIndex.kt`), but `SettingsSearchIndexTest` has **not** been extended for summaries.
- [x] `Rule_Book.md` is updated (section 11.3). Unit tests (2066 pass) and lint pass.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/ui/components/GroupedList.kt`: `CollapsibleGroup` and `SegmentedChoiceRow` take a `summary`; `GroupedRow` and `SwitchRow` take `subtitleMaxLines`.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/LayoutGroup.kt`: each group has a `summaryRes`.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/PositionsSection.kt`, `MotionSection.kt`, `AnimationSpeedSection.kt`, `IdleWidgetsSection.kt`, `LandscapeSection.kt`: pass the summary for each group.
- `app/src/main/java/com/agupta07505/smartisland/ui/search/SettingsSearchIndex.kt`: the search description is the summary text.
- `app/src/main/res/values/strings_layout_rides.xml`: the `layout_summary_*` strings.
- `app/src/test/java/com/agupta07505/smartisland/ui/sections/LayoutGroupTest.kt`: two new tests (unique summaries; short single-line copy with no trailing period).
- `Rule_Book.md`: section 11.3.
- `docs/requirements/issue-7-layout-screen.md`: note that the titles-only rule is superseded for group titles.
- `CHANGELOG.md`: Unreleased entry.

## Tests

- `./gradlew testDebugUnitTest`: 2066 pass, 0 failures.
- `./gradlew lintDebug`: clean.
- `./gradlew assembleDebug`: builds.
- Device checks (largest font, light and dark theme, landscape, TalkBack) are still open.
