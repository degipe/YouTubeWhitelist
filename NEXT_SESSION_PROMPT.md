# Session 20 Starting Prompt

Ez a 20. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Előző Session (19) Összefoglaló

Strategy E (Hybrid + Invidious fallback) implementáció:
- **oEmbed service**: ingyenes video/playlist metadata (no API key, no quota)
- **RSS feed parser**: ingyenes csatorna videó lista (max 15, no API key)
- **Invidious fallback**: nyílt forráskódú YouTube proxy (instance rotation + health tracking)
- **HybridYouTubeRepositoryImpl**: fallback chain (oEmbed/RSS → YouTube API → Invidious)
- **Beépített API kulcs**: `app/build.gradle.kts`-ben, F-Droid build-ekhez is működik
- **ProGuard szabályok**: oEmbed + Invidious DTO-k + OEmbedService
- **Code review javítások**: XXE védelem, thread safety, IOException szűrés
- 413 teszt, mind zöld

## Mi van kész

- App teljesen működőképes (413 teszt, mind zöld)
- Strategy E implementálva: Hybrid + Invidious fallback
- Release APK + AAB
- GitHub Release v1.0.0
- Privacy Policy (GitHub Pages)
- GCP API key: YouTube Data API v3 only restriction (nincs Android restriction)
- OAuth consent screen: Production mode
- Teljes SDLC dokumentáció (7 doksi)
- 7 Play Store screenshot
- Kid mode keresés: csak lokális (0 API quota)
- YouTubeApiRepository interfész változatlan (ViewModelek nem módosultak)

## Mi maradt

1. **Play Store beküldés** (AAB feltöltés, content rating, data safety)
2. **F-Droid RFP** (most már OK — beépített API kulcs + hybrid)
3. **Feature graphic** (1024x500 banner, Play Store kötelező)
4. **App icon** (512x512 PNG, Play Store kötelező)
5. **Device tesztelés** (új hybrid implementáció valós YouTube tartalommal)
6. **YOUTUBE_API_STRATEGY.md** frissítés (Strategy E kiválasztva + implementálva)

### Megjegyzések

- **ADB path**: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`
- **Build parancsok**:
  - APK: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleRelease`
  - AAB: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew bundleRelease`
- **GCP Project**: `youtubewhitelist-486917`
- Kommunikáció magyarul, dokumentáció angolul
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
