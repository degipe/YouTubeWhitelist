# YouTubeWhitelist - Claude Code Project Guide

## Project Overview
Whitelist-based YouTube client for kids. Android app (Kotlin + Jetpack Compose) that lets parents whitelist specific YouTube channels, videos, and playlists. Two modes: parent mode (full YouTube browsing + whitelist management) and kid mode (only whitelisted content visible). 100% client-side, no backend, GPLv3.

## Tech Stack
- **Platform**: Android (API 26+), Kotlin
- **UI**: Jetpack Compose, Material Design 3
- **Architecture**: MVVM + Clean Architecture, multi-module Gradle
- **DI**: Hilt
- **Database**: Room (SQLite)
- **Network**: Retrofit + OkHttp (YouTube Data API v3)
- **Video**: YouTube IFrame Player API
- **Auth**: WebView OAuth 2.0 (F-Droid compatible, no Google Play Services SDK)
- **Serialization**: Kotlinx Serialization
- **Background**: WorkManager
- **Testing**: JUnit, MockK, Truth, Turbine, Espresso, Compose Testing, Robolectric

## Module Structure
```
:app                    - Main module, Activity, navigation
:feature:parent         - Parent mode UI (dashboard, WebView browser, whitelist manager)
:feature:kid            - Kid mode UI (grid, search, player)
:feature:sleep          - Sleep mode (timer, fade-out, dark UI)
:core:common            - Shared utilities, theme, common composables
:core:data              - Repositories, data sources
:core:database          - Room DAOs, entities
:core:network           - YouTube API client (Retrofit)
:core:auth              - Google Sign-In, token management
:core:export            - JSON export/import logic
```

## Package Convention
`io.github.degipe.youtubewhitelist.<module>.<layer>`

## Development Principles
- **Language**: Communicate in Hungarian, documentation in English
- **Quality over speed**: Always choose thoroughness over shortcuts
- **Test-driven**: Write tests first, implementation second
- **Ask if uncertain**: Never assume, always clarify
- **Session-based**: Development proceeds in sessions, each documented

## Session Workflow
At end of each session:
1. Update CLAUDE.md with session log
2. Update NEXT_SESSION_PROMPT.md with next session's starting prompt
3. Push everything to git

## Archive Rules (after session 5)
- CLAUDE.md: always contains only the latest 5 session logs
- CLAUDE_ARCHIVE_X.md: max 10 session logs per archive file
- ARCHITECTURE.md: index of all archives, kept up to date

## PRD Reference
Full PRD: `YouTubeWhitelist_PRD_v1.1.docx` in project root

## Key PRD Milestones
- M1 (Wk 1-4): Infrastructure - project setup, OAuth, PIN, Room DB, basic navigation
- M2 (Wk 5-8): Parent mode - WebView, URL parsing, whitelist CRUD, YouTube API
- M3 (Wk 9-12): Kid mode - grid, channel view, player, search, kiosk mode
- M4 (Wk 13-14): Sleep mode - timer, fade-out, dark UI
- M5 (Wk 15-16): Multi-profile, time limits, stats, export/import
- M6 (Wk 17-18): Testing, bugfix, optimization, beta
- M7 (Wk 19-20): Publication (Play Store, F-Droid, GitHub)

---

## Session Logs

### Session 8 - 2026-02-09: M6 - Missing Features, Edge Case Tests, Optimization

**Objectives**: Complete deferred M3 features (Playlist Detail Screen, Kiosk Mode), replace mock Google Sign-In with F-Droid-compatible WebView OAuth, edge case tests, optimization.

**Completed**:
- **Phase 1: Playlist Detail Screen (TDD)**:
  - `PlaylistVideo` domain model in core:data
  - `getPlaylistItems(playlistId)` added to `YouTubeApiRepository` interface + impl (PlaylistItemDto → PlaylistVideo mapping, null videoId filtering, thumbnail fallback)
  - 5 new tests in `YouTubeApiRepositoryImplTest` (success, empty, API error, filters invalid items, network failure)
  - `Route.PlaylistDetail(profileId, playlistId, playlistTitle, playlistThumbnailUrl)` navigation route
  - `PlaylistDetailViewModelTest` — 8 TDD tests (loading, items loaded, empty, error, retry, loading on retry, correct playlistId, sorted by position)
  - `PlaylistDetailViewModel` — one-shot API call pattern (MutableStateFlow + viewModelScope.launch), AssistedInject, retry(), sorts by position
  - `PlaylistDetailScreen` — TopAppBar, LazyColumn of PlaylistVideoCard, loading/error/empty states, retry button
  - Updated `KidHomeScreen` + `KidSearchScreen` onPlaylistClick callback signature to `(youtubeId, title, thumbnailUrl)`
  - Full `AppNavigation.kt` wiring with AssistedInject

- **Phase 2: Kiosk Mode (Screen Pinning + BackHandler)**:
  - `BackHandler` added to `KidHomeScreen` (blocks back exit in kid mode)
  - `startLockTask()` via `LaunchedEffect` in AppNavigation KidHome composable
  - `stopLockTask()` in PinEntry `onPinVerified` callback
  - Activity access via `LocalContext.current as? Activity` pattern

- **Phase 3: WebView OAuth 2.0 (F-Droid compatible)**:
  - Removed Google Auth SDK deps from libs.versions.toml and core:auth build.gradle.kts
  - `OAuthConfig` — pure Java URL builder (java.net.URLEncoder, no android.net.Uri), auth/token endpoints, redirect URI, scopes
  - `OAuthTokenExchanger` — HTTP POST via HttpURLConnection to Google token endpoint, JWT id_token parsing via java.util.Base64 + org.json.JSONObject, returns GoogleUserInfo
  - `OAuthActivity` — WebView showing Google OAuth consent screen, intercepts localhost/callback redirect, calls static onOAuthResult()
  - `GoogleSignInManagerImpl` rewritten — WebView OAuth flow with CompletableDeferred bridge between Activity and Manager, @GoogleClientId injection
  - `@GoogleClientId` qualifier annotation in core:auth
  - `ApiKeyModule` updated with `provideGoogleClientId()` from BuildConfig
  - `GOOGLE_CLIENT_ID` BuildConfig field in app/build.gradle.kts from local.properties
  - OAuthActivity registered in AndroidManifest
  - `OAuthConfigTest` — 2 tests (URL parameters, endpoint)
  - `OAuthTokenExchangerTest` — 6 tests with @RunWith(RobolectricTestRunner::class) (JWT parsing valid/null name/invalid, onOAuthResult success/cancelled/error)
  - `GOOGLE_SETUP.md` — step-by-step Google Cloud Console setup guide

- **Phase 4: Edge Case Tests + Bugfix**:
  - `YouTubeApiRepositoryImplTest` +6 tests: SocketTimeoutException, UnknownHostException, HTTP 429 rate limit, unexpected RuntimeException, blank thumbnail URLs (all blank → empty, blank high → uses medium)
  - `WhitelistRepositoryImplTest` +4 tests: empty URL, whitespace URL, non-YouTube URL, duplicate check after handle resolution
  - `KidHomeViewModelTest` +3 tests: formats 1h exactly ("1h 0m"), formats <1min ("0m"), reactive content→empty transition
  - `VideoPlayerViewModelTest` +5 tests: playPrevious noop on single video, onVideoEnded stays on last, playVideoAt out of bounds, negative index, formats 1h remaining

- **Phase 5: Optimization**:
  - `YouTubeWhitelistApp` now implements `ImageLoaderFactory` — Coil with 25% memory cache + 50MB disk cache + crossfade
  - Room composite indices: `(kidProfileId, type)` on whitelist_items, `(kidProfileId, youtubeId)` unique on whitelist_items, `(kidProfileId, watchedAt)` on watch_history
  - DB version bumped to 2 with `fallbackToDestructiveMigration()` (pre-release, no deployed data)

**Decisions Made**:
- WebView OAuth 2.0 instead of Google Sign-In SDK — F-Droid main repo compatible (no non-FOSS compiled deps)
- "Web application" OAuth client type (not Android) — required for Authorization Code flow with WebView
- CompletableDeferred as bridge between OAuthActivity and GoogleSignInManagerImpl
- org.json.JSONObject for JWT parsing — requires Robolectric in unit tests
- java.net.URLEncoder (not android.net.Uri) for OAuth URL building — JVM test compatible
- PlaylistDetailViewModel uses one-shot API call (not Flow/stateIn) since data comes from API, not local DB
- Coil 50MB disk cache — thumbnails are primary bandwidth consumers
- Unique composite index on (kidProfileId, youtubeId) — enforces DB-level uniqueness for duplicate prevention

**Test Stats**: 355 total tests (316 existing + 13 playlist + 8 OAuth + 18 edge cases), all green

**New tests breakdown**: YouTubeApiRepo +5 playlist +6 edge = 11, WhitelistRepo +4 edge, PlaylistDetailVM 8, OAuthConfig 2, OAuthTokenExchanger 6, KidHomeVM +3 edge, VideoPlayerVM +5 edge

**Notes**:
- android.net.Uri throws RuntimeException in JVM tests — always use java.net alternatives
- org.json.JSONObject requires Robolectric (Android SDK class)
- F-Droid: app will get NonFreeNet anti-feature tag (YouTube API) but can be in main repo
- Google Cloud Console OAuth + YouTube API key still needs manual setup (see GOOGLE_SETUP.md)
- All M1-M6 milestones now complete, M7 (Publication) remaining

**Next Session Focus**: M7 - Publication preparation (ProGuard rules, signing config, Play Store listing, F-Droid metadata, GitHub Release, final testing on real device).

### Session 9 - 2026-02-09: Ko-fi Donation Integration + README + Store Listing

**Objectives**: Integrate Ko-fi donation support into the app (About screen), create full English README with Ko-fi badge, prepare Play Store and F-Droid store listing texts.

**Completed**:
- **About Screen**:
  - Created `AboutScreen.kt` in `feature/parent/ui/about/` — static composable (no ViewModel), vertically scrollable
  - Content: app name + version, description, GPLv3 license, clickable GitHub link, Ko-fi donation card with `Intent(ACTION_VIEW)` to `https://ko-fi.com/peterdegi`
  - Added `Route.About` to navigation Route sealed interface
  - Added `composable<Route.About>` to `AppNavigation.kt`
  - Added `onAbout` callback + Info ActionCard to `ParentDashboardScreen` (after "Change PIN")

- **README.md** (full rewrite):
  - Ko-fi GitHub button badge at top: `[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/X8X71TWXEN)`
  - English project description, features list (13 features)
  - Build instructions (prerequisites, clone, local.properties, gradlew)
  - Link to GOOGLE_SETUP.md
  - Tech stack, GPLv3 license, Ko-fi support section with badge image

- **STORE_LISTING.md** (new file):
  - Google Play Store: title (80 char), short description (80 char), full description with features, privacy section, Ko-fi link
  - F-Droid: summary, description with feature list, privacy section, anti-features note (NonFreeNet), Ko-fi link

- **Archive**: Session 4 archived to CLAUDE_ARCHIVE_1.md

**Files Changed**:
- Created: `feature/parent/src/main/java/.../ui/about/AboutScreen.kt`
- Created: `STORE_LISTING.md`
- Modified: `app/.../navigation/Route.kt` (added About route)
- Modified: `app/.../navigation/AppNavigation.kt` (added About composable + wiring)
- Modified: `feature/parent/.../dashboard/ParentDashboardScreen.kt` (added onAbout + ActionCard)
- Overwritten: `README.md` (full English README with Ko-fi)

**Test Stats**: 355 tests, all green (no new tests — About screen is static composable)

**Notes**:
- Ko-fi link: `https://ko-fi.com/peterdegi`, widget ID: `X8X71TWXEN`
- About screen is static (no ViewModel needed) — only uses `LocalContext.current` for Intent launching
- Short session — focused solely on Ko-fi integration and store preparation

**Next Session Focus**: M7 continuation — ProGuard/R8 rules, release signing config, F-Droid metadata directory structure, GitHub Release, final device testing.

### Session 10 - 2026-02-09: M7 - Publication Preparation

**Objectives**: Complete M7 milestone: ProGuard/R8 finalization, release signing, F-Droid metadata, version bump, CHANGELOG, release build verification.

**Completed**:
- **ProGuard/R8 Rules Finalization**:
  - Comprehensive `proguard-rules.pro` rewrite covering all project-specific concerns
  - kotlinx-serialization: keep serializers, Companion objects, generated `$$serializer` classes
  - Navigation Compose: keep Route sealed interface + all subclasses (type-safe nav needs runtime serialization)
  - Export DTOs + YouTube API DTOs: keep entire packages
  - WebView JavaScript bridges: explicit `-keepclassmembers` for `@JavascriptInterface` methods + `JavascriptInterface` attribute
  - Retrofit: keep `YouTubeApiService` interface methods + annotation attributes (Signature, Exceptions, RuntimeVisibleAnnotations)
  - OkHttp: `-dontwarn` for platform-specific classes (conscrypt, bouncycastle, openjsse)
  - Room: keep entity + DAO classes
  - Tink/Security Crypto: `-dontwarn` for ErrorProne annotations (CanIgnoreReturnValue, CheckReturnValue, Immutable, RestrictedApi)
  - Kotlin: keep `kotlin.Metadata`

- **Release Signing Configuration**:
  - Generated `release-keystore.jks` (RSA 2048, 10000 days validity, CN=Peter Degi, O=degipe, L=Budapest, C=HU)
  - Added `signingConfigs { create("release") }` block to `app/build.gradle.kts` reading from `local.properties`
  - Release buildType linked to release signing config
  - Keystore path + credentials in `local.properties` (git-ignored)
  - `.gitignore` updated: uncommented `*.jks` and `*.keystore`

- **F-Droid Metadata (Triple-T format)**:
  - Created `fastlane/metadata/android/en-US/` directory structure
  - `title.txt`, `short_description.txt`, `full_description.txt` (from STORE_LISTING.md content)
  - `changelogs/1.txt` for versionCode 1
  - Created `fastlane/metadata/android/hu-HU/` Hungarian locale
  - `title.txt`, `short_description.txt`, `full_description.txt` Hungarian translations
  - `changelogs/1.txt` Hungarian changelog

- **Version Bump + CHANGELOG**:
  - `versionName` bumped from "0.1.0" to "1.0.0"
  - `versionCode` stays at 1 (first release)
  - Created `CHANGELOG.md` following Keep a Changelog format
  - All 18 features documented in [1.0.0] release entry

- **Build Verification**:
  - All 355 tests passing
  - Release APK build successful with R8 minification + resource shrinking
  - Release APK signed with release keystore
  - **APK size: 2.4 MB** (excellent for the feature set)
  - First R8 failure resolved: Tink ErrorProne annotation `-dontwarn` rules added

**Decisions Made**:
- Comprehensive ProGuard rules as belt-and-suspenders (explicit rules even when @Keep annotations exist)
- JKS keystore format (standard, compatible with all Android tools)
- Keystore credentials in `local.properties` (not in build.gradle.kts, not in git)
- Triple-T metadata format (standard for F-Droid + Play Store via fastlane)
- Bilingual metadata: en-US + hu-HU

**Files Created**:
- `CHANGELOG.md`
- `release-keystore.jks` (git-ignored)
- `fastlane/metadata/android/en-US/title.txt`
- `fastlane/metadata/android/en-US/short_description.txt`
- `fastlane/metadata/android/en-US/full_description.txt`
- `fastlane/metadata/android/en-US/changelogs/1.txt`
- `fastlane/metadata/android/hu-HU/title.txt`
- `fastlane/metadata/android/hu-HU/short_description.txt`
- `fastlane/metadata/android/hu-HU/full_description.txt`
- `fastlane/metadata/android/hu-HU/changelogs/1.txt`

**Files Modified**:
- `app/proguard-rules.pro` (comprehensive rewrite)
- `app/build.gradle.kts` (signing config + version bump)
- `.gitignore` (uncommented keystore exclusion)
- `local.properties` (signing credentials + GOOGLE_CLIENT_ID placeholder)

**Test Stats**: 355 tests, all green (no new tests — publication preparation session)

**Notes**:
- Release APK at `app/build/outputs/apk/release/app-release.apk` (2.4 MB)
- Keystore password: stored in local.properties only, change before production use
- Session 5 archived to CLAUDE_ARCHIVE_1.md (now contains sessions 1-5)
- Google Cloud Console API key + OAuth client ID still needed for runtime testing
- GitHub Release + APK upload is manual step (needs git push first)
- Play Store screenshots + feature graphic still needed before store submission

**Next Session Focus**: Real device testing with actual Google API credentials, Play Store screenshots, GitHub Release creation, F-Droid submission preparation.

### Session 11 - 2026-02-09: M7 - Google Cloud Setup + AAB Build

**Objectives**: Set up Google Cloud Console credentials for runtime use, build AAB for Play Store, prepare for device testing.

**Completed**:
- **Google Cloud Console Setup (via browser automation)**:
  - Created new GCP project: `YouTubeWhitelist` (ID: `youtubewhitelist-486917`)
  - Enabled YouTube Data API v3
  - Created API Key (unrestricted — restrict later for production)
  - Configured OAuth consent screen (External, Testing mode)
  - Created OAuth 2.0 Client ID (Web application type, redirect URI: `http://localhost/callback`)
  - Added `degi.peter@gmail.com` as test user
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

### Session 12 - 2026-02-10: Device Testing, WebView Security Fix, Cookie Persistence

**Objectives**: Real device + emulator testing, fix embed-disabled video security issue, fix YouTube Premium cookie persistence in Browse YouTube.

**Completed**:
- **WebView Navigation Security Fix (VideoPlayerScreen + SleepModeScreen)**:
  - Added `shouldOverrideUrlLoading` returning `true` to block ALL navigation in video player WebViews
  - Prevents kids from escaping the app via "Watch on YouTube" links on embed-disabled videos
  - Added `onEmbedError` callback to `VideoEndedBridge` JavaScript bridge — handles YouTube IFrame Player error codes 101/150 (embedding disabled by video owner)
  - Auto-skips to next video when embed error detected (`viewModel.playNext()`)
  - Same fixes applied to `SleepModeScreen`'s `SleepVideoEndedBridge`

- **Browse YouTube Cookie Persistence Fix**:
  - Added `CookieManager` setup to `WebViewBrowserScreen`'s `YouTubeWebView`
  - `CookieManager.setAcceptCookie(true)` + `setAcceptThirdPartyCookies(this, true)` enables YouTube login session persistence
  - `CookieManager.flush()` in `DisposableEffect.onDispose` persists cookies to disk before WebView destruction
  - YouTube Premium now recognized in Browse YouTube — no ads, no sign-in prompts

- **Attempted WebView OAuth (reverted)**:
  - Created `OAuthWebViewActivity` to share cookie store between OAuth and Browse YouTube WebViews
  - Goal: single sign-in experience (sign in once, Premium recognized everywhere)
  - **Google blocked it** — Google's policy since 2016 prohibits OAuth in embedded WebViews (error: "access denied")
  - Fully reverted to Chrome Custom Tabs + OAuthLoopbackServer implementation
  - Deleted `OAuthWebViewActivity`, restored `androidx.browser` dependency

- **Emulator Testing**:
  - Installed release APK on Android 14 emulator, verified video playback with IFrame Player
  - Navigation blocking confirmed — no "Watch on YouTube" escape links
  - Video navigation (Next/Previous) working correctly
  - Inserted test data directly via `adb shell run-as ... sqlite3` (emulator gesture navigation zone blocked FAB taps)

- **Real Device Testing**:
  - User confirmed fix works on real device ("szuper, most megy frankón")
  - YouTube Premium recognized after CookieManager fix

**Decisions Made**:
- `shouldOverrideUrlLoading` returns `true` unconditionally in player WebViews — no legitimate navigation needed
- Error codes 101/150 trigger auto-skip (not error message) — seamless UX for kids
- Chrome Custom Tabs remains the only OAuth option — Google blocks embedded WebView OAuth
- Users must sign in twice: once for app (Chrome Custom Tabs OAuth), once in Browse YouTube WebView — separate cookie stores, both sessions persist
- CookieManager.flush() on dispose ensures YouTube login survives app restarts

**Files Modified**:
- `feature/kid/src/main/java/.../player/VideoPlayerScreen.kt` (navigation blocking + embed error handling)
- `feature/sleep/src/main/java/.../ui/SleepModeScreen.kt` (same security fixes)
- `feature/parent/src/main/java/.../browser/WebViewBrowserScreen.kt` (CookieManager setup + flush)
- `core/auth/build.gradle.kts` (comment updated about Chrome Custom Tabs requirement)

**Test Stats**: 355 tests, all green (no new tests — runtime security fixes verified on device/emulator)

**Notes**:
- Google blocks OAuth in embedded WebViews since 2016 — must use Chrome Custom Tabs or system browser
- Chrome Custom Tabs cookie store is completely separate from app's WebView CookieManager
- CookieManager is global across all WebViews within the app — Browse YouTube login persists across sessions
- Emulator gesture navigation zone can swallow taps near screen bottom — use `adb shell run-as` + `sqlite3` for direct DB testing
- Session 7 archived to CLAUDE_ARCHIVE_1.md (now contains sessions 1-7)

**Next Session Focus**: Play Store screenshots, GitHub Release + tag v1.0.0, F-Droid submission, Privacy Policy page, API key restriction.
