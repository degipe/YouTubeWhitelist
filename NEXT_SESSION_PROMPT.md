# Session 19 Starting Prompt

Ez a 19. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Előző Session (18) Összefoglaló

F-Droid kompatibilitás és API stratégia elemzés:
- F-Droid inclusion policy átnézve — FLOSS szempontból OK, de API kulcs probléma van (F-Droid nem regisztrál kulcsokat)
- YouTube API-free endpointok kutatva: oEmbed (video/playlist metadata), RSS feed (channel videók), közvetlen thumbnail URL-ek
- **YouTube Search API eltávolítva a kid mode-ból** — ez volt a quota fogyasztás 95%-a (100-300 unit/keresés)
- Kid search most csak lokális Room DB-ben keres (whitelist-elt elemek cím/csatornacím alapján)
- GCP API key restriction javítva: Android restriction eltávolítva (kislány telefonján 403-at okozott), API restriction (YouTube Data API v3) maradt
- Részletes stratégia dokumentum: `/tmp/YOUTUBE_API_STRATEGY.md` (NEM publikálandó)

## Döntésre vár

Az API stratégia még nincs eldöntve. A felhasználó gondolkodik rajta. A `/tmp/YOUTUBE_API_STRATEGY.md` tartalmazza az 5 stratégiát:
- **A:** Hybrid (oEmbed+RSS + API csak keresésre) — keresés már ki lett véve!
- **B:** Zero-API (semmi API kulcs, csak oEmbed+RSS)
- **C:** Beépített API kulcs + hybrid optimalizáció
- **D:** Invidious proxy
- **E:** Hybrid + Invidious fallback

A keresés eltávolítása után az API kulcs már csak ezekre kell:
1. `channels.list` — channel metadata + @handle feloldás (1 unit)
2. `videos.list` — video metadata (1 unit) → kiváltható oEmbed-del
3. `playlists.list` — playlist metadata (1 unit) → kiváltható oEmbed-del
4. `playlistItems.list` — channel/playlist videó lista (1 unit) → részben kiváltható RSS-sel (max 15 videó)

## Mi van kész

- App teljesen működőképes (373+ teszt, mind zöld)
- Release APK + AAB
- GitHub Release v1.0.0
- Privacy Policy (GitHub Pages)
- GCP API key: YouTube Data API v3 only restriction (nincs Android restriction)
- OAuth consent screen: Production mode
- Teljes SDLC dokumentáció (7 doksi)
- 7 Play Store screenshot
- Kid mode keresés: csak lokális (0 API quota)

## Mi maradt

1. **API stratégia döntés** (felhasználó dönt) → implementáció
2. **Play Store beküldés** (AAB feltöltés, content rating, data safety)
3. **F-Droid RFP** (API stratégia után)
4. **Feature graphic** (1024x500 banner, Play Store kötelező)
5. **App icon** (512x512 PNG, Play Store kötelező)

### Megjegyzések

- **ADB path**: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`
- **Build parancsok**:
  - APK: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleRelease`
  - AAB: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew bundleRelease`
- **GCP Project**: `youtubewhitelist-486917`
- Kommunikáció magyarul, dokumentáció angolul
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
