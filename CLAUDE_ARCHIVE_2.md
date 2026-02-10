# YouTubeWhitelist - Session Archive 2 (Sessions 11-20)

### Session 11 - 2026-02-09: M7 - Google Cloud Setup + AAB Build

**Objectives**: Set up Google Cloud Console credentials for runtime use, build AAB for Play Store, prepare for device testing.

**Completed**:
- **Google Cloud Console Setup (via browser automation)**:
  - Created new GCP project: `YouTubeWhitelist` (ID: `youtubewhitelist-486917`)
  - Enabled YouTube Data API v3
  - Created API Key (unrestricted — restrict later for production)
  - Configured OAuth consent screen (External, Testing mode)
  - Created OAuth 2.0 Client ID (Web application type, redirect URI: `http://localhost/callback`)
  - Added test user to OAuth consent screen
  - Updated `local.properties` with real API key and OAuth client ID

- **AAB Build for Play Store**:
  - Verified `bundleRelease` task works out of the box (Android Gradle Plugin)
  - Built both APK (2.4 MB) and AAB (5.2 MB) with real API credentials
  - APK for F-Droid + GitHub Release, AAB for Google Play Store

- **Sideloading Tutorial**:
  - Step-by-step guide for USB debugging + adb install from macOS
  - WiFi ADB alternative for Android 11+
  - adb path: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`

- **Archive**: Session 6 archived to CLAUDE_ARCHIVE_1.md (now contains sessions 1-6)

**Decisions Made**:
- Both APK and AAB in release pipeline (APK for F-Droid/GitHub, AAB for Play Store)
- API Key created without restrictions initially — add Android app restriction after testing
- OAuth consent screen in Testing mode — publish for production later

**Files Modified**:
- `local.properties` (real YOUTUBE_API_KEY + GOOGLE_CLIENT_ID)

**Test Stats**: 355 tests, all green (no new tests — infrastructure session)

**Notes**:
- GCP Project ID: `youtubewhitelist-486917`
- OAuth consent screen is in Testing mode — only test users can sign in
- API Key should be restricted to YouTube Data API v3 + Android app after device testing confirms it works
- Release builds: `app/build/outputs/apk/release/app-release.apk` (2.4 MB), `app/build/outputs/bundle/release/app-release.aab` (5.2 MB)

**Next Session Focus**: Real device testing (sideload APK, test full user flow), bug fixes if needed, Play Store screenshots, GitHub Release + tag, F-Droid submission, Privacy Policy page.
