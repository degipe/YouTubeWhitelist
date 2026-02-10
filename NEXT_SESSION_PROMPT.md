# Session 14 Starting Prompt

Ez a 14. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: Store Submission + Final Polish

### 1. GitHub Release

- `git tag v1.0.0` + push tag
- GitHub Release létrehozása CHANGELOG.md tartalommal
- Signed APK csatolása a release-hez

### 2. Privacy Policy

- Privacy Policy oldal létrehozása (GitHub Pages vagy static page)
- Tartalom: no data collection, all data stays on device, YouTube API usage, no analytics
- Link hozzáadása az About screen-hez

### 3. Play Store Screenshots

- Készíts 4-6 screenshot-ot a főbb képernyőkről (emulator vagy valós eszköz)
- Feature graphic (1024x500) ha szükséges

### 4. API Key Restriction

- GCP Console: API Key korlátozása YouTube Data API v3-ra
- Android app restriction: package name (`io.github.degipe.youtubewhitelist`) + SHA-1 fingerprint
- OAuth consent screen: publish for production (jelenleg Testing mode)

### 5. F-Droid Submission

- Verify reproducible builds
- Submit RFP issue on F-Droid GitLab (https://gitlab.com/fdroid/rfp)
- AntiFeatures: NonFreeNet (YouTube API usage)

### 6. Play Store Submission

- Upload AAB: `app/build/outputs/bundle/release/app-release.aab`
- Content rating questionnaire
- Store listing (STORE_LISTING.md tartalma)
- Screenshots + feature graphic

### Megjegyzések

- **ADB path**: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`
- **Build parancsok**:
  - APK: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleRelease`
  - AAB: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew bundleRelease`
- **378+ teszt** van, mind zöld
- **Release builds**: APK 2.4 MB, AAB 5.2 MB
- **GCP Project**: `youtubewhitelist-486917`
- **OAuth**: Testing mode (test user configured in GCP Console)
- **Sleep mode**: Háttér timer, KidHome + VideoPlayer overlay, max 600 perc
- **Channel video search**: YouTube Search API (max 3 csatorna, 100 unit/keresés, 10k napi limit)
- Kommunikáció magyarul, dokumentáció angolul
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
