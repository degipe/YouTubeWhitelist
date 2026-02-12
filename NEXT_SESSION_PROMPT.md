# Session 25 Starting Prompt

Ez a 25. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Előző Session (24) Összefoglaló

F-Droid RFP újra beadása a helyes repo-ba:

- **F-Droid RFP #3586**: https://gitlab.com/fdroid/rfp/-/issues/3586 (helyes repo: `fdroid/rfp`)
- Régi #3794 (`fdroid/fdroiddata`) lezárva — rossz repo volt

## Mi van kész

- App teljesen működőképes (~401 teszt, mind zöld)
- v1.1.0 release build (APK + AAB)
- Strategy E (Hybrid + Invidious fallback) implementálva
- Channel lazy loading + Room cache + helyi keresés — emulátoron verifikálva
- GitHub Release v1.0.0 + v1.1.0
- F-Droid RFP #3586 beadva (fdroid/rfp, helyes repo)
- Play Console fiók létrehozva (device verification pending)
- Privacy Policy (GitHub Pages)
- GCP API key: YouTube Data API v3 only restriction
- OAuth consent screen: Production mode
- Teljes SDLC dokumentáció (7 doksi, frissítve v1.1.0-ra)
- 7 Play Store screenshot + feature graphic + app icon
- Fastlane changelogs (EN + HU)
- Play Store Submission Guide frissítve v1.1.0-ra

## Mi maradt

1. **Play Store device verification** — fizikai Google-ös Android telefon beszerzése, majd:
   - Device verification elvégzése
   - Phone number verification
   - Identity verification (személyi/útlevél)
2. **Play Store app submission** — a verification után:
   - App létrehozás, store listing, content rating, data safety
   - AAB feltöltés, publish
3. **F-Droid RFP #3586 nyomon követés** — várni az F-Droid maintainerek válaszát (fdroid/rfp)

### Megjegyzések

- **Emulator AVD**: átállítva Play Store image-re (`google_apis_playstore`, Android 34)
- **ADB path**: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`
- **Build parancsok**:
  - APK: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleRelease`
  - AAB: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew bundleRelease`
- **GCP Project**: `youtubewhitelist-486917`
- **Play Console fiók ID**: 4768413512690805008
- **F-Droid repo-k**: `fdroid/rfp` (RFP), `fdroid/fdroiddata` (build recipes) — ne keverd össze!
- Kommunikáció magyarul, dokumentáció angolul
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
