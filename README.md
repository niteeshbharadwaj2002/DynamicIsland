<h1 align="center">NotchIQ</h1>

<p align="center">
  <strong>A personal, offline Dynamic Island for Android. Notifications, music, calls, timers and a small mascot live around your camera cutout.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-7.6.0--personal-6366f1?style=for-the-badge&labelColor=1e1e2e" alt="Version 7.6.0-personal" />
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&labelColor=1e1e2e&logo=android&logoColor=white" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/License-GNU%20GPLv3-3b82f6?style=for-the-badge&labelColor=1e1e2e&logo=gnu&logoColor=white" alt="GNU GPLv3" />
</p>

<p align="center">
  <a href="#what-it-is">What it is</a> &bull;
  <a href="#what-it-does">What it does</a> &bull;
  <a href="#offline-by-design">Offline</a> &bull;
  <a href="#privacy--permissions">Privacy</a> &bull;
  <a href="#building-from-source">Build</a> &bull;
  <a href="#credits">Credits</a>
</p>

---

## What it is

NotchIQ is a personal fork of [Smart Island](https://github.com/agupta07505/SmartIsland) **7.6.0** by Animesh Gupta. It keeps the upstream feature set and adds three areas of its own:

- **Buddy (the mascot).** A small character that lives on the collapsed pill, reacts to events and makes short visits when things are quiet. Buddy is ported from Vorssaint (GPL-3.0-or-later), and the About screen credits it.
- **Island UI revamp.** A two-slot alert engine decides what the main pill and the companion bubble show, with spring presets, a kinetic glow and a size-class pill layout.
- **HIG app UI.** The settings app uses Apple Human Interface Guidelines-style grouped lists, typography and switches.

Everything runs on the device. There is no account, no analytics and no cloud.

---

## What it does

Notifications, music, calls, charging, timers, stopwatches, navigation, downloads, hotspot, screen and voice recording and the camera or microphone privacy dots all appear in the island. A tap opens each one into a card. When nothing is going on, idle widgets (clock, date, battery, next alarm, net speed, unread count, Do Not Disturb) take turns in the same spot.

- **Alerts:** messages open into a card with actions and inline reply. Alerts from the same app fold into one entry, and a card can be dismissed by swiping.
- **Live activities:** music with album art and a waveform, calls with a duration timer, timers and stopwatches from common clock apps, navigation and ride cards (Uber, Ola, Rapido), and system events such as charging, Bluetooth with earbud batteries, USB, headphones, hotspot and flashlight.
- **Per-event control:** 32 events, each with its own switch, auto-expand option and lock screen setting, and portrait and landscape can be set separately.
- **Gestures:** swipes on the collapsed pill, and tap, swipe and hold actions on expanded cards. Each direction can be remapped or turned off.
- **Quick launch:** a launcher window with pinned apps, recents and a utilities grid (Wi-Fi, Bluetooth, hotspot, flashlight, timer, recorders and more).
- **Looks:** Material 3 with Material You colours on Android 12 and later, custom colours and fonts, animation speed presets and a settings search.
- **Backup and restore** of settings to a file.
- **Languages:** English, Spanish, Japanese, Brazilian Portuguese and Simplified Chinese. Strings that have no translation yet fall back to English.

Translations live in `app/src/main/res/values-<locale>/`. Contributions are welcome; see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## Offline by design

NotchIQ makes no network calls. The decisions behind that:

- **No `INTERNET` permission** in the manifest. The app cannot send or receive data from the network.
- **No update checker.** The upstream GitHub update check and its download screen are removed. Updates come from building the source or installing a release APK you trust.
- **Weather and wind widgets are hidden.** They need a network lookup, so the widget pickers do not offer them in this build.
- **Calendar is read locally.** The date widget can show your next calendar event (`READ_CALENDAR`). No location permission is requested.

---

## Privacy & permissions

NotchIQ asks for what it needs and nothing more:

- **Overlay and notification access** to draw the island and read alerts, media and timers.
- **Foreground service and battery optimisation exemption** so Android keeps it alive.
- **Usage access and Shizuku** (optional) for one-tap setup and the privacy indicator's app names.
- **Calendar** for the next-event widget, and **Bluetooth** for connection and earbud battery alerts.
- **Notification history** is stored locally in a database that is not shared with anything else. Notification text never leaves the device.

See [PRIVACY.md](PRIVACY.md) and [SECURITY.md](SECURITY.md).

---

## Building from source

You need the Android SDK (API 26 to 36), JDK 17 and the bundled Gradle wrapper.

```bash
git clone https://github.com/niteeshbharadwaj2002/NotchIQ.git
cd NotchIQ
chmod +x gradlew            # or run: sh ./gradlew
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On Windows use `gradlew.bat`. Tests and lint run with:

```bash
./gradlew testDebugUnitTest lintDebug
```

The app is written in Kotlin with Jetpack Compose and Material 3, uses Hilt for injection, keeps settings in DataStore and notification history in SQLite. Gradle dependency verification is on (`gradle/verification-metadata.xml`).

Release signing is optional and described in [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md). Without the signing secrets the release workflow produces an unsigned APK.

The development checklist for the port onto upstream 7.6.0 is in [docs/UPDATE_CHECKLIST.md](docs/UPDATE_CHECKLIST.md).

---

## Documentation

- [CHANGELOG.md](CHANGELOG.md): version history, including the `7.6.0-personal` entry.
- [Rule_Book.md](Rule_Book.md): the island's rules, including the alert tiers, timing and expansion behaviour.
- [CLAUDE.md](CLAUDE.md): notes for working on the alert engine and motion code.

---

## Credits

- **Smart Island** is by [Animesh Gupta](https://github.com/agupta07505). NotchIQ is built on Smart Island 7.6.0.
- **Buddy** is ported from Vorssaint (GPL-3.0-or-later).
- Bundled font licences are in [THIRD_PARTY_FONTS.md](THIRD_PARTY_FONTS.md) and [NOTICE.md](NOTICE.md).

---

## License

NotchIQ is licensed under the [GNU General Public License v3.0](LICENSE), the same licence as Smart Island. The upstream copyright notice is kept in the source headers.
