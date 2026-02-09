# Session 8 Starting Prompt

Ez a 8. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M6 - Testing, Bugfix, Optimization + hiányzó feature-ök

### 1. Kiosk Mode (M3 hiány)
- Kid mode-ból ne lehessen kilépni az alkalmazásból
- Android kiosk/lock task mode integráció
- Csak a Parent PIN-nel lehet kilépni

### 2. Playlist Detail Screen (M3 hiány)
- Playlist tartalomlistázás (playlist items lekérdezés YouTube API-ból)
- Videó lejátszás playlist-ből
- Placeholder click handler cseréje valódi navigációra

### 3. Google Sign-In valódi integráció
- Google Cloud Console projekt + OAuth client ID beállítás
- YouTube Data API v3 engedélyezés
- Mock GoogleSignInManager cseréje valódi implementációra
- Runtime tesztelés valódi API kulccsal

### 4. Testing + Bugfix
- Instrumented tesztek (Compose UI testing, Espresso)
- End-to-end flow tesztek
- ProGuard/R8 tesztelés release build-en
- Edge case-ek tesztelése (üres adatok, hálózati hibák, offline működés)

### 5. Optimization
- Room query optimalizáció (EXPLAIN QUERY PLAN)
- Image caching stratégia (Coil disk cache beállítás)
- Memory profiling (WebView, bitmap-ek)
- Cold start idő optimalizáció

### Megjegyzések

- **Build parancs**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew test`
- **316 teszt** van jelenleg, mind zöld
- **TDD skill használata kötelező!**
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- Google Cloud Console YouTube API key **még nincs** — runtime teszteléshez szükséges
- M1-M5 milestone-ok elkészültek, M6-M7 hátra van
