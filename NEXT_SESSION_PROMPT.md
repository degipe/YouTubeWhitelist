# Session 12 Starting Prompt

Ez a 12. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: Real Device Testing + Store Submission

### 1. Device Testing Results

- Az előző session végén az APK sideloading tutorial elkészült
- Teszteld az APK-t valódi eszközön és jelentsd a bugokat
- Teljes user flow tesztelés:
  - Sign-in (WebView OAuth — `degi.peter@gmail.com` test user)
  - PIN setup + entry
  - Profile creation
  - YouTube browsing + whitelist management
  - Kid mode (grid, channel detail, video player)
  - Search within whitelisted content
  - Playlist detail
  - Sleep mode (timer, volume fade)
  - Time limits
  - Export/Import
  - Kiosk mode (screen pinning)
  - About screen + Ko-fi link

### 2. Bug Fixes

- Fix any issues found during device testing

### 3. API Key Restriction

- After confirming API works: restrict API key to YouTube Data API v3 + Android apps
- Add package name (`io.github.degipe.youtubewhitelist`) + SHA-1 fingerprint

### 4. Play Store Submission

- Screenshots (phone + tablet if applicable)
- Feature graphic (1024x500)
- Privacy policy URL (GitHub Pages or README link)
- Content rating questionnaire
- Upload AAB: `app/build/outputs/bundle/release/app-release.aab`

### 5. GitHub Release

- `git tag v1.0.0`
- GitHub Release with CHANGELOG content
- Attach signed APK

### 6. F-Droid Submission

- Verify reproducible builds
- Submit RFP issue on F-Droid GitLab
- AntiFeatures: NonFreeNet

### Megjegyzések

- **ADB path**: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`
- **Build parancsok**:
  - APK: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleRelease`
  - AAB: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew bundleRelease`
- **355 teszt** van, mind zöld
- **Release builds**: APK 2.4 MB, AAB 5.2 MB
- **GCP Project**: `youtubewhitelist-486917`
- **OAuth**: Testing mode, test user: `degi.peter@gmail.com`
- Kommunikáció magyarul, dokumentáció angolul
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
