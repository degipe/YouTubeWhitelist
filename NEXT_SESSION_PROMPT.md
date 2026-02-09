# Session 10 Starting Prompt

Ez a 10. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M7 - Publication Preparation (folytatás)

### 1. ProGuard / R8 Finalization
- ProGuard rules review (WebView JavaScript bridges, Serialization, Room, Hilt)
- Release build tesztelés: `./gradlew assembleRelease`
- APK méret ellenőrzés és optimalizáció

### 2. Signing Config
- Release signing keystore generálás
- `signingConfigs` blokk a `build.gradle.kts`-ben
- Keystore path/password a `local.properties`-ben

### 3. F-Droid Metadata
- `fastlane/metadata/android/` struktúra (Triple-T format)
- `en-US/full_description.txt`, `short_description.txt`, `title.txt`
- `hu-HU/` magyar fordítás
- Anti-features: `NonFreeNet` (YouTube API)
- Verify no non-FOSS dependencies

### 4. GitHub Release
- Version bump (versionCode, versionName)
- CHANGELOG.md írás
- GitHub Release draft + APK attachment

### 5. Final Device Testing
- Google Cloud Console OAuth + YouTube API key beállítás (GOOGLE_SETUP.md alapján)
- Valódi eszközön tesztelés (sign-in, whitelist, kid mode, kiosk, sleep, export/import)
- Edge case-ek kézi tesztelés (offline, time limit, playlist detail)

### Megjegyzések

- **Build parancs**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew test`
- **355 teszt** van jelenleg, mind zöld
- **TDD skill használata kötelező!**
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- M1-M6 milestone-ok elkészültek, M7 van hátra
- Ko-fi donation integráció kész (About screen, README, STORE_LISTING.md)
- Google Cloud Console API key/OAuth client ID **még nincs** — runtime teszteléshez szükséges (lásd GOOGLE_SETUP.md)
