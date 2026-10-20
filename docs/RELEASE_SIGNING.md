# Release Signing With GitHub Actions

NotchIQ builds its release APK in `.github/workflows/release.yml`. The keystore is never committed: the
workflow decodes it from a repository secret into the runner's temporary folder, signs with it, and discards it.

## 1. Keep Signing Files Private

Never commit these files (they are listed in `.gitignore`):

- `*.jks`
- `*.keystore`
- `*_base64.txt`
- `keystore.properties`
- passwords, aliases, tokens, or secret notes

## 2. Create The Keystore (once)

If you do not have a keystore yet, create one with the JDK `keytool`:

```bash
keytool -genkeypair -v -keystore notchiq.jks -keyalg RSA -keysize 2048 -validity 10000 -alias notchiq
```

Keep at least two secure backups of the `.jks` file and its password. If you lose the signing key, you cannot
publish updates that install over builds signed with it.

## 3. Generate The Base64 Secret

Encode the keystore as a single line of base64 text.

Windows (PowerShell), using the helper in `scripts/`:

```powershell
.\scripts\export-keystore-base64.ps1 -KeystorePath .\notchiq.jks
```

macOS / Linux:

```bash
base64 -i notchiq.jks | tr -d '\n' > notchiq_base64.txt
```

Open the resulting file locally and copy its full content. Do not commit it.

## 4. Add The GitHub Repository Secrets

In GitHub, open:

```text
Repository -> Settings -> Secrets and variables -> Actions -> New repository secret
```

Add these two secrets:

| Secret name | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Full content of the base64 file from step 3 |
| `ANDROID_KEYSTORE_PASSWORD` | The keystore password (also used as the key password) |

The workflow detects the key alias from the keystore automatically.

## 5. What Happens Without The Secrets

Signing is optional. The release workflow checks both secrets before building:

- **Both secrets set:** the release APK is signed and the workflow verifies the signature with `apksigner`.
- **Either secret missing:** the workflow prints a warning and builds an **unsigned** APK named
  `NotchIQ-vX.Y.Z-personal-unsigned.apk`. Unsigned APKs cannot be installed over a signed build, and the
  GitHub release says so.

## 6. Build A Release

Manual build (no GitHub release is published):

1. Open the repository on GitHub and go to `Actions`.
2. Select `Release`.
3. Click `Run workflow`.

Tagged release: the tag must match `versionName` in `app/build.gradle.kts`, otherwise the run fails.

```bash
git tag v7.6.0-personal
git push origin v7.6.0-personal
```

A tag starting with `v` builds the APK and publishes it to a GitHub release. Pushes to `main` and `update`
only run the build and test workflow and never publish a release.

## 7. Outputs

The workflow uploads one release APK, named from `versionName`:

```text
NotchIQ-v7.6.0-personal.apk            (signed)
NotchIQ-v7.6.0-personal-unsigned.apk   (no signing secrets)
```

The APK is also available as the `NotchIQ-release` artifact in the workflow run.
