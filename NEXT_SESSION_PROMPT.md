# Session 11 Starting Prompt

Ez a 11. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M7 Completion - Device Testing + Store Submission

### 1. Google Cloud Console Setup

- YouTube Data API v3 key létrehozása
- OAuth 2.0 "Web application" client ID létrehozása
- `local.properties` frissítése valódi kulcsokkal
- Teszt: sign-in flow, API calls

### 2. Real Device Testing

- Install release APK valódi eszközre
- Teljes user flow tesztelés:
  - Sign-in (WebView OAuth)
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

### 3. Play Store Submission Prep

- Screenshots (phone + tablet if applicable)
- Feature graphic (1024x500)
- App icon (512x512)
- Content rating questionnaire
- Privacy policy URL (GitHub Pages or README link)

### 4. F-Droid Submission

- Verify reproducible builds
- Submit to F-Droid via GitLab merge request or RFP issue
- Verify AntiFeatures: NonFreeNet

### 5. GitHub Release

- `git tag v1.0.0`
- GitHub Release with CHANGELOG content
- Attach signed APK

### Megjegyzések

- **Build parancs**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleRelease`
- **355 teszt** van, mind zöld
- **Release APK**: 2.4 MB, R8 minified, signed
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- M1-M7 milestone-ok lényegében elkészültek
- Google Cloud Console API key/OAuth client ID **szükséges** — runtime teszteléshez (lásd GOOGLE_SETUP.md)
