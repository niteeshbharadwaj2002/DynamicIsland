# Issue #15: Support & Feedback links point to NotchIQ

Issue: https://github.com/niteeshbharadwaj2002/NotchIQ/issues/15

## Requirements

1. **Star on GitHub** opens the NotchIQ repository, not the upstream Smart Island repository.
2. **Join Telegram Community** is removed from Support & Feedback and from the welcome dialog. No Telegram link remains in the app.
3. **Request a Feature** and **Report a Bug** open the NotchIQ issue forms (`feature_request.md` and `bug_report.md`), so reports reach this project.
4. **License** opens the NotchIQ repository's `LICENSE` (GPL-3.0, the same licence as upstream).
5. The About screen's repository links (stars, forks, issues, license, privacy, terms and open source) point at NotchIQ.
6. The credit to the original author stays. Nothing else points upstream.

## Decisions

1. **One source for the links:** `util/ProjectLinks.kt` holds the repository URL and every link built from it. The three screens use it, so the links cannot drift apart again.
2. **Telegram removed everywhere:** the Support row, the welcome dialog button and the `join_telegram` and `join_telegram_desc` strings are gone from every locale. The `telegram` and `community` settings search keywords are dropped. The support card description (`card_support_requests_desc`) no longer names Telegram, in every locale.
3. **License row:** points at NotchIQ's own `LICENSE`, not the upstream file.
4. **About credits kept:** the original author's rows (GitHub profile, LinkedIn, Instagram and mail) and the buddy artwork credit to Vorssaint stay as credits. The artwork credit is kept because its licence requires attribution.
5. **Release notes untouched:** `docs/release-notes/v8.0.0-personal.md` is a published release and still says what it said at release time.

## Acceptance criteria

- [x] Star on GitHub opens the NotchIQ repo (code: `SupportSection.kt` uses `ProjectLinks.REPO`).
- [x] No Telegram option on Support & Feedback or the welcome dialog (code: rows and button removed; strings removed from all locales).
- [x] Request a Feature opens the NotchIQ feature-request form (code: `ProjectLinks.FEATURE_REQUEST`; the template file exists, checked by `ProjectLinksTest`).
- [x] Report a Bug opens the NotchIQ bug-report form (code: `ProjectLinks.BUG_REPORT`; device details are still prefilled as before). Not opened on a device.
- [x] No link on these screens points at `agupta07505/SmartIsland`, except the credits (checked by `ProjectLinksTest`).
- [x] Unused strings removed; settings search keywords for Telegram and community removed (`SettingsSearchIndex.kt`).
- [ ] Unit tests and lint pass. The tests and lint were run by the author, not in this documentation pass. (Gradle was not run for this pass.)
- [x] `Rule_Book.md` (section 11.4), `CHANGELOG.md` and this document updated.

## Files changed

- `app/src/main/java/com/agupta07505/smartisland/util/ProjectLinks.kt`: new, the single source for project links.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/SupportSection.kt`: Telegram row removed; the other links use `ProjectLinks`.
- `app/src/main/java/com/agupta07505/smartisland/ui/sections/AboutSection.kt`: repository links use `ProjectLinks`; credits unchanged.
- `app/src/main/java/com/agupta07505/smartisland/ui/SmartIslandHomeScreen.kt`: welcome dialog Star button uses `ProjectLinks.REPO`; Telegram button removed.
- `app/src/main/java/com/agupta07505/smartisland/ui/search/SettingsSearchIndex.kt`: `telegram` and `community` keywords dropped.
- `app/src/main/res/values/strings.xml`, `values-en`, `values-es`, `values-ja`, `values-pt-rBR`, `values-zh-rCN`: `join_telegram` and `join_telegram_desc` removed; `card_support_requests_desc` no longer names Telegram (the Spanish value also had a broken quote, now fixed).
- `app/src/test/java/com/agupta07505/smartisland/util/ProjectLinksTest.kt`: new, 3 tests.
- `Rule_Book.md`: section 11.4 bullet on project links.
- `CHANGELOG.md`: Unreleased entry.
- `docs/requirements/issue-15-support-links.md`: this document.

## Tests

- `./gradlew testDebugUnitTest`: passes (per the author; not re-run here).
- `./gradlew lintDebug`: passes (per the author; not re-run here).
- Device check (Star, Request a Feature, Report a Bug and License open the right pages in a browser) is still open.
