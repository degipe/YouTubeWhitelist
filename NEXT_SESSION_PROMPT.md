# Session 9 Starting Prompt

Ez a 9. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M7 - Publication Preparation

### 1. ProGuard / R8 Finalization
- ProGuard rules review (WebView JavaScript bridges, Serialization, Room, Hilt)
- Release build tesztelés: `./gradlew assembleRelease`
- APK méret ellenőrzés és optimalizáció

### 2. Signing Config
- Release signing keystore generálás
- `signingConfigs` blokk a `build.gradle.kts`-ben
- Keystore path/password a `local.properties`-ben

### 3. App Metadata
- Play Store listing szövegek (title, short/full description, screenshots)
- F-Droid metadata (`fastlane/metadata/android/` struktúra)
- Feature graphic, screenshots generálás

### 4. F-Droid Specifikus
- `fdroid/` metadata dir (Repomaker / Triple-T format)
- Build recipe (`build:` section in metadata)
- Anti-features: `NonFreeNet` (YouTube API)
- Verify no non-FOSS dependencies (Google Auth SDK already removed)

### 5. GitHub Release
- Version bump (versionCode, versionName)
- CHANGELOG.md írás
- GitHub Release draft + APK attachment

### 6. Final Device Testing
- Google Cloud Console OAuth + YouTube API key beállítás (GOOGLE_SETUP.md alapján)
- Valódi eszközön tesztelés (sign-in, whitelist, kid mode, kiosk, sleep, export/import)
- Edge case-ek kézi tesztelés (offline, time limit, playlist detail)

### Megjegyzések

- **Build parancs**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew test`
- **355 teszt** van jelenleg, mind zöld
- **TDD skill használata kötelező!**
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- M1-M6 milestone-ok elkészültek, M7 hátra van
- Google Cloud Console API key/OAuth client ID **még nincs** — runtime teszteléshez szükséges (lásd GOOGLE_SETUP.md)
