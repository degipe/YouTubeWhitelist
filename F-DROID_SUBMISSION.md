# F-Droid RFP (Request for Packaging) — Step by Step Guide

## 1. GitLab Account

Az F-Droid GitLab-on működik: https://gitlab.com/fdroid/rfp

**Probléma**: GitHub-os bejelentkezésnél "422: Email has already been taken" errort kaptál.
Ez azt jelenti, hogy **már van GitLab accountod** ezzel az email címmel (valószínűleg régen regisztráltál, vagy auto-created).

**Megoldás**:
1. Menj ide: https://gitlab.com/users/password/new
2. Add meg az email címedet (ami a GitHub accountodhoz is tartozik)
3. Kapsz egy password reset emailt
4. Állíts be új jelszót
5. Jelentkezz be ezzel az email + jelszó kombinációval

## 2. RFP Issue Létrehozása

1. Jelentkezz be GitLab-ra
2. Menj ide: https://gitlab.com/fdroid/rfp/-/issues/new
3. **Title**: `YouTubeWhitelist - Safe YouTube for Kids`
4. **Description**: Másold be az alábbi szöveget ⬇️

## 3. Issue Body (ezt másold be)

```
### App name
YouTubeWhitelist

### App website / Source code link
https://github.com/degipe/YouTubeWhitelist

### F-Droid / IzzyOnDroid link
N/A (new submission)

### Description
Free, open-source whitelist-based YouTube client for kids. Parents browse YouTube and whitelist specific channels, videos, and playlists. Kids only see approved content — no algorithm, no recommendations, no surprises.

**Features:**
- Parent Mode: Browse YouTube freely and build whitelists
- Kid Mode: Clean, distraction-free interface with only approved content
- PIN Protection: Secure parent access with brute-force protection
- Multiple Profiles: Separate whitelists for each child
- Daily Time Limits: Per-profile configurable watch time limits
- Sleep Mode: Timer-based playback with gradual volume fade-out
- Watch Statistics: Daily, weekly, and monthly tracking
- Kiosk Mode: Screen pinning keeps kids inside the app
- Export/Import: JSON backup and restore
- Search: Within whitelisted content

**Privacy:**
- 100% client-side — no backend server
- No ads, no tracking, no analytics
- All data stored locally on device

### License
GPLv3

### Additional information

**Package name:** `io.github.degipe.youtubewhitelist`
**Min SDK:** 26 (Android 8.0)
**Target SDK:** 35
**Build system:** Gradle (Kotlin DSL)

**AntiFeatures:**
- `NonFreeNet` — Uses YouTube Data API v3 (Google network service)

**Notes:**
- App requires a YouTube Data API v3 key (configured via `local.properties`)
- OAuth 2.0 uses Chrome Custom Tabs (no Google Play Services dependency)
- Fastlane metadata structure is included in the repo (`fastlane/metadata/android/`)
- Changelogs follow fastlane convention (`fastlane/metadata/android/en-US/changelogs/`)
- Release signing keystore is NOT in the repo (reproducible builds may need additional configuration)

**Build instructions:**
```
JAVA_HOME=/path/to/jdk17 ./gradlew assembleRelease
```

Requires `YOUTUBE_API_KEY` and `GOOGLE_CLIENT_ID` in `local.properties`.
```

## 4. Beküldés Után

- Az F-Droid önkéntesek átnézik a kérelmet
- Lehet, hogy kérdeznek (pl. build reprodukálhatóság, API key kezelés)
- A feldolgozás heteket-hónapokat vehet igénybe
- Ha elfogadják, felveszik az F-Droid repóba

## 5. Megjegyzések

- Az `NonFreeNet` antifeature miatt az app megjelenik az F-Droid-ban, de jelölve lesz
- A YouTube API key nélkül az app nem működik — ezt az F-Droid builderek is tudni fogják
- A fastlane metadata struktúra megkönnyíti az F-Droid számára a store listing átvételét
