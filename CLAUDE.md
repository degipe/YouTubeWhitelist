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
- **Auth**: Google Sign-In SDK (OAuth 2.0)
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

### Session 2 - 2026-02-09: PIN Management, Auth Infrastructure, Navigation

**Objectives**: Implement M1 milestone infrastructure: PIN-based parent/kid mode switching, Google OAuth preparation, and complete navigation graph.

**Completed**:
- **core:common**: Added Hilt DI + coroutines deps, created DispatcherQualifiers (@IoDispatcher, @DefaultDispatcher, @MainDispatcher) and DispatcherModule
- **core:data**: Created domain models (ParentAccount, AuthState, PinVerificationResult) and repository interfaces (PinRepository, AuthRepository, ParentAccountRepository)
- **core:auth PIN system**: PinHasher interface + Pbkdf2PinHasher (PBKDF2WithHmacSHA256, 120k iterations, 16-byte salt, java.util.Base64), BruteForceProtection (SharedPreferences-backed exponential backoff: 5 fails=30s, 10=60s, 15=120s), PinRepositoryImpl
- **core:auth Auth system**: TokenManager + EncryptedTokenManager (Android Keystore-backed EncryptedSharedPreferences), GoogleSignInManager interface + mock implementation, ParentAccountRepositoryImpl, AuthRepositoryImpl (reuses existing account on re-sign-in to prevent cascade deletion)
- **core:auth DI**: AuthModule with @Binds for all repository/manager interfaces
- **Navigation**: Type-safe @Serializable Route sealed interface, full NavHost with 8 destinations (Splash, SignIn, PinSetup, PinEntry, PinChange, ProfileCreation, KidHome, ParentDashboard)
- **ViewModels**: SplashViewModel, SignInViewModel, PinSetupViewModel, PinEntryViewModel, PinChangeViewModel, ProfileCreationViewModel
- **UI Screens**: All 8 screen composables + reusable PinDots and PinKeypad components
- **Tests (TDD)**: Pbkdf2PinHasherTest, BruteForceProtectionTest, PinRepositoryImplTest, ParentAccountRepositoryImplTest, AuthRepositoryImplTest, SplashViewModelTest, SignInViewModelTest, PinSetupViewModelTest, PinEntryViewModelTest, PinChangeViewModelTest
- **Code review + 8 bug fixes**: android.util.Base64→java.util.Base64, added kotlinx-serialization-json to app, added coroutines-core/android to version catalog + core:common/core:data, fixed BruteForceProtection.reset() to use remove() instead of clear(), eliminated THRESHOLD duplication, fixed navigation back-stack, prevented duplicate account creation

**Decisions Made**:
- PIN hash format: `base64(salt):base64(hash)` stored in existing `pinHash` field (no schema migration)
- PBKDF2WithHmacSHA256 instead of bcrypt (native Android, no extra dependency)
- java.util.Base64 (not android.util.Base64) for JVM test compatibility
- BruteForceProtection in regular SharedPreferences (not encrypted, not sensitive)
- Mock Google Sign-In (real integration needs Google Cloud Console setup)
- 3 separate PIN ViewModels (Setup, Entry, Change) instead of one shared
- Domain interfaces in core:data, implementations in core:auth
- Coroutines as `api` dependency in core:common for transitive availability

**Notes**:
- TDD process should be improved: need to use the TDD skill for more disciplined test-first workflow
- Google Sign-In is fully mocked - real integration requires Cloud Console OAuth client setup
- All tests are unit tests (JVM) - no instrumented/Robolectric tests yet
- Build verification still pending (no JDK 17 / ANDROID_HOME on dev machine)

**Next Session Focus**: M1 completion - Build verification, potential test fixes, then start M2 (WebView browser, URL parsing).

### Session 3 - 2026-02-09: M2 - YouTube API, URL Parsing, Whitelist CRUD

**Objectives**: Begin M2 milestone: YouTube Data API v3 integration, URL parsing, whitelist repository layer. TDD discipline enforced.

**Completed**:
- **core:common**: Created `AppResult<T>` sealed interface (Success/Error) with map/onSuccess/onError/getOrNull extensions for clean error handling
- **core:common**: Moved `WhitelistItemType` enum from core:database to core:common/model/ (shared across modules, avoids duplication)
- **core:common**: Created `YouTubeUrlParser` object (pure Kotlin, java.net.URI-based) with `ParsedYouTubeUrl` and `YouTubeContentType` enum — handles video (/watch, youtu.be, /shorts, /embed, /live, mobile), channel (/channel/, /@handle, /c/), playlist URLs, URL-decoded query params
- **core:common**: YouTubeUrlParser test suite (27 test cases covering all URL types + edge cases)
- **core:network**: Created YouTube Data API v3 DTOs: `YouTubeListResponse<T>`, `ChannelDto`, `VideoDto`, `PlaylistDto`, `PlaylistItemDto`, `SearchResultDto`, `ThumbnailSet` — all `@Serializable` with proper defaults
- **core:network**: DTO deserialization test suite (9 tests: channel, video, playlist, playlistItem, search, missing fields, empty items, unknown keys, thumbnails)
- **core:network**: Created `YouTubeApiService` Retrofit interface (channels, videos, playlists, playlistItems, search endpoints with Response<T>)
- **core:network**: Created `ApiKeyInterceptor` OkHttp interceptor + tests (2 tests)
- **core:network**: Created `@YouTubeApiKey` qualifier annotation
- **core:network**: Created `NetworkModule` Hilt DI (Json, OkHttpClient, Retrofit, YouTubeApiService) with debug-only HTTP logging
- **core:data**: Created domain models: `WhitelistItem`, `YouTubeMetadata` (sealed: Channel/Video/Playlist), `KidProfile` (with sleepPlaylistId)
- **core:data**: Created repository interfaces: `YouTubeApiRepository`, `WhitelistRepository`, `KidProfileRepository`
- **core:data**: `YouTubeApiRepositoryImpl` + tests (10 tests: success mapping, not found, network/HTTP errors, thumbnail fallback chain high>medium>default)
- **core:data**: `WhitelistRepositoryImpl` + tests (12 tests: entity mapping, video/channel/playlist/handle/custom URL flows, early duplicate check optimization, API error propagation)
- **core:data**: `KidProfileRepositoryImpl` + tests (7 tests: CRUD mapping, UUID generation, null handling)
- **core:data**: Created `DataModule` Hilt DI (@Binds for all 3 repositories)
- **app**: Created `ApiKeyModule` (provides @YouTubeApiKey from BuildConfig)
- **app**: Updated `build.gradle.kts` with `buildConfigField` for YouTube API key from `local.properties`
- **core:network**: Added `buildFeatures { buildConfig = true }` for debug flag access
- **Code review fixes**: HTTP logging debug-only (security), early duplicate check before API call (quota optimization), CHANNEL_CUSTOM documented limitation, KidProfile sleepPlaylistId preservation, URL-decode query params, isLenient removed from JSON config, getOrNull() added to AppResult

**Decisions Made**:
- `AppResult<T>` sealed interface for network errors (not bare exceptions)
- Three-layer mapping: API DTOs (core:network) → Domain models (core:data) → Room entities (core:database)
- `WhitelistItemType` lives in core:common (shared by database + data modules)
- `YouTubeContentType` separate from `WhitelistItemType` (URL parsing vs storage concerns)
- YouTube API key via BuildConfig from local.properties, injected through Hilt qualifier
- Early duplicate check for known IDs (video/channel/playlist) saves API quota; handles need resolution first
- CHANNEL_CUSTOM (/c/name) falls through to forHandle API — modern YouTube maps these to @handles
- HTTP logging only in debug builds (API key in query params)

**Test Stats**: 67 test cases total this session (27 URL parser + 9 DTO + 2 interceptor + 10 API repo + 12 whitelist repo + 7 profile repo)

**Notes**:
- Build environment still not set up (no JDK 17, no Android SDK) — all code is uncompiled, tests are designed for JVM
- Google Cloud Console YouTube API key not yet created — needs setup before runtime testing
- WebView browser UI (feature:parent) deferred to next session
- Parent mode ViewModels deferred to next session

**Next Session Focus**: M2 continuation - Parent mode UI (WebView browser, whitelist manager screens, ViewModels), navigation route additions, build verification if environment is ready.

### Session 4 - 2026-02-09: M2 - Parent Mode UI, ViewModels, Navigation

**Objectives**: M2 continuation: Parent mode ViewModels (TDD), UI screens (WhitelistManager, WebViewBrowser, ParentDashboard), navigation route additions.

**Completed**:
- **feature:parent ViewModels (TDD)**:
  - `WhitelistManagerViewModel` — list items by profile, filter by type (CHANNEL/VIDEO/PLAYLIST), add from URL, remove item, loading/error/success states, add URL dialog state. Uses Hilt Assisted Injection for profileId parameter.
  - `WebViewBrowserViewModel` — URL change detection via YouTubeUrlParser, detected content type for FAB, add to whitelist via WhitelistRepository, AddToWhitelistResult sealed interface (Success/Error).
  - `ParentDashboardViewModel` — loads parent account + kid profiles via flatMapLatest, auto-selects first profile, preserves selection on reactive updates.
- **feature:parent Tests (TDD, test-first)**:
  - `WhitelistManagerViewModelTest` — 15 test cases (initial state, loading, filter by type, clear filter, add URL dialog, add success/error/loading/blank, remove, dismiss messages)
  - `WebViewBrowserViewModelTest` — 13 test cases (URL detection for video/channel/handle/playlist, clear on non-YouTube, add success/loading/error, no action without detection, dismiss result)
  - `ParentDashboardViewModelTest` — 9 test cases (loading, profiles load, auto-select, no account, select profile, reactive updates, preserves selection)
- **feature:parent UI Screens**:
  - `WhitelistManagerScreen` — Scaffold + TopAppBar, FilterChipRow (All/Channels/Videos/Playlists), LazyColumn with WhitelistItemCard (title, channel, type badge, delete), FAB for add, AddUrlDialog with loading state, Snackbar for success/error, empty state, loading state
  - `WebViewBrowserScreen` — AndroidView WebView with security hardening (no file access, no mixed content, safe browsing), LinearProgressIndicator for page load, animated ExtendedFAB for detected YouTube content, Snackbar for add results, proper WebView cleanup (about:blank + stopLoading + destroy)
  - `ParentDashboardScreen` — profile selector (LazyRow with ProfileChip cards), action cards (Manage Whitelist, Browse YouTube, Change PIN), loading state, enabled/disabled based on profile selection
- **Navigation Updates**:
  - Added `Route.WhitelistManager(profileId: String)` and `Route.WebViewBrowser(profileId: String)` to type-safe Route sealed interface
  - Updated `AppNavigation.kt` with new composable destinations, Hilt ViewModel injection, Assisted Injection for WhitelistManager
  - Removed old placeholder ParentDashboardScreen from app module (replaced by feature:parent version)
- **Code Review Fixes (9 issues found, 6 fixed)**:
  - CRITICAL: WebView memory leak fix — proper cleanup (about:blank, stopLoading, clearHistory, destroy)
  - CRITICAL: WebView security hardening — allowFileAccess=false, allowContentAccess=false, MIXED_CONTENT_NEVER_ALLOW, safeBrowsingEnabled
  - CRITICAL: ParentDashboardViewModel race condition — nested collectLatest/collect replaced with flatMapLatest
  - MEDIUM: Inconsistent error state clearing — successMessage nulled on new add action
  - MEDIUM: Missing loading state UI in WhitelistManagerScreen — added isLoading=true initial state + CircularProgressIndicator
  - MEDIUM: Missing FAB content description — added accessibility label

**Decisions Made**:
- ViewModels in feature:parent with subpackage structure: ui/dashboard/, ui/whitelist/, ui/browser/
- WhitelistManagerViewModel uses Hilt Assisted Injection (@AssistedFactory) for profileId parameter
- WebViewBrowser route takes profileId parameter (not savedStateHandle) for clean data passing
- WebView: JavaScript enabled (required for YouTube), but file access and mixed content disabled
- ParentDashboard uses flatMapLatest for account→profiles flow chain (avoids coroutine leaks)
- Kept manual Job tracking in WhitelistManagerViewModel for filter change cancel+restart pattern

**Test Stats**: 37 test cases this session (15 whitelist manager + 13 browser + 9 dashboard)

**Notes**:
- Build environment still not set up (JDK 8, no Android SDK) — all code uncompiled
- WebView state restoration on config changes (rotation) deferred — nice-to-have for later
- Old app module ParentDashboardScreen removed (was placeholder, replaced by feature:parent version)
- Emojis used in WhitelistItemCard type icons — may need replacement with proper vector icons later

**Next Session Focus**: M2 completion — Build verification (JDK 17 + Android SDK setup), compile and run all tests, fix any compilation issues. Then start M3 (Kid mode: content grid, channel view, video player).

### Session 5 - 2026-02-09: Build Verification + M3 Kid Mode

**Objectives**: Build environment setup, full compilation and test verification, M3 Kid Mode implementation (data layer, navigation, ViewModels with TDD, UI screens).

**Completed**:
- **Build Environment Setup**:
  - Installed JDK 17 via Homebrew (`brew install openjdk@17`)
  - Installed Android SDK CLI tools + platform-tools, build-tools 35, platforms android-35
  - Created `local.properties` with SDK path and dummy YouTube API key
  - Fixed `settings.gradle.kts` (`dependencyResolution` → `dependencyResolutionManagement`)
  - Fixed `app/build.gradle.kts` properties loading (import + use{} for stream cleanup)
  - Created adaptive launcher icons (foreground/background XML + mipmap wrappers)
  - Added missing `retrofit` dependency to core:data
  - **All 174 existing tests passing, assembleDebug successful**

- **Test Fixes (3 failures)**:
  - `Pbkdf2PinHasherTest`: replaceFirst flaky → Base64 decode + XOR byte tampering
  - `AuthRepositoryImplTest`: MockK slot not captured → explicit coEvery for getParentAccountOnce()
  - `SignInViewModelTest`: StandardTestDispatcher timing → Turbine + CompletableDeferred gate

- **M3 Phase 1: Data Layer**:
  - Added 6 kid mode queries to `WhitelistItemDao` (channels, videos, playlists by profile, videos by channel, search, item by ID)
  - Created `WatchHistory` domain model + `WatchHistoryRepository` interface + `WatchHistoryRepositoryImpl` (TDD: 5 tests)
  - Extended `WhitelistRepository` interface + impl with 6 new kid mode methods
  - Updated `DataModule` with WatchHistoryRepository binding

- **M3 Phase 2: Navigation**:
  - `Route.KidHome` changed from `data object` to `data class(profileId)`
  - Added `Route.ChannelDetail(profileId, channelTitle, channelThumbnailUrl)`, `Route.VideoPlayer(profileId, videoId, channelTitle?)`, `Route.KidSearch(profileId)`
  - `SplashUiState.ReturningUser` → `data class(profileId)` — SplashVM now loads first profile via KidProfileRepository
  - `ProfileCreationUiState` → `createdProfileId: String?` — returns created profile ID
  - `ParentDashboardScreen.onBackToKidMode` → passes `selectedProfileId`
  - Updated `AppNavigation.kt` with all profileId-based navigations
  - Updated SplashViewModelTest (6 tests, from 3 — added profile loading scenarios)

- **M3 Phase 3: ViewModels (TDD)**:
  - `KidHomeViewModel` — combine() 4 flows (profile + channels + videos + playlists), stateIn(Eagerly), AssistedInject. **11 tests**
  - `ChannelDetailViewModel` — videos by channelTitle, stateIn(Eagerly), AssistedInject with @Assisted("id"). **6 tests**
  - `VideoPlayerViewModel` — video loading, sibling navigation, auto-next, watch history recording, playNext/playPrevious/playVideoAt. **15 tests**
  - Fixed `@Assisted` duplicate String types: `@Assisted("profileId")`, `@Assisted("channelTitle")`, `@Assisted("videoId")`

- **M3 Phase 4: UI Screens**:
  - `KidHomeScreen` (feature:kid) — greeting, channel grid (2-col LazyVerticalGrid), video LazyRow, playlist LazyRow, parent access FAB, empty state
  - `ChannelDetailScreen` — TopAppBar + LazyColumn of video cards (thumbnail + title + channel)
  - `VideoPlayerScreen` — YouTube IFrame Player via WebView, next/prev controls, "Up Next" list with clickable cards
  - `YouTubePlayerHtml` — IFrame API HTML generator (autoplay, controls, rel=0, modestbranding, no annotations)
  - Deleted old placeholder `KidHomeScreen` from app module
  - Full `AppNavigation.kt` wiring with AssistedInject for KidHome, ChannelDetail, VideoPlayer

- **Code Review Fixes (7 issues found, all fixed)**:
  - CRITICAL: JavaScript bridge `@Keep` annotation — extracted `VideoEndedBridge` class to survive R8
  - CRITICAL: WebView memory leak — `DisposableEffect(youtubeId)` instead of `Unit`, + `webViewRef.clear()`
  - CRITICAL: Coroutine leak in KidHomeVM/ChannelDetailVM — `stateIn(Eagerly)` instead of `launch{collect}`
  - HIGH: UpNextCard clickability — added `playVideoAt(index)` public method + onClick callback
  - HIGH: ProfileCreationViewModel architecture violation — replaced direct DAO usage with KidProfileRepository

**Decisions Made**:
- `stateIn(Eagerly)` for KidHome/ChannelDetail VMs (always active when screen shown, avoids test complexity)
- `@Assisted("identifier")` for disambiguating multiple String params in Hilt AssistedInject
- `VideoEndedBridge` with `@Keep` for R8-safe JavaScript bridge
- `WebView` DisposableEffect keyed on `youtubeId` for proper cleanup on video navigation
- Channel thumbnails passed as route params (not re-fetched from DB)

**Test Stats**: 212 total tests (174 existing + 38 new: 5 WatchHistory + 3 SplashVM new + 11 KidHome + 6 ChannelDetail + 15 VideoPlayer — some existing tests adjusted)

**Notes**:
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` required for all Gradle commands
- Android SDK at `/opt/homebrew/share/android-commandlinetools`
- KidSearch screen deferred (nice-to-have, not in this session)
- Playlist detail screen not yet implemented (placeholder click handler)
- No image loading library yet (Coil/Glide) — thumbnails shown as placeholder icons
- Google Cloud Console YouTube API key still not created — needs setup before runtime testing

**Next Session Focus**: M3 completion — Add Coil image loading for thumbnails, KidSearch screen, then M4 Sleep mode or M3 kiosk mode.

### Session 6 - 2026-02-09: M3 Completion (Coil, KidSearch) + M4 Sleep Mode

**Objectives**: Complete M3 milestone (Coil image loading, KidSearch screen), begin and complete M4 milestone (Sleep Mode with timer, fade-out, dark UI).

**Completed**:
- **Archive**: Session 1 archived to CLAUDE_ARCHIVE_1.md, removed from CLAUDE.md, ARCHITECTURE.md updated

- **Coil Image Loading (M3 completion)**:
  - Added Coil 2.7.0 to `libs.versions.toml` (version + `coil-compose` library entry)
  - Added `implementation(libs.coil.compose)` to feature:kid and feature:parent build.gradle.kts
  - Replaced all placeholder Icons with `AsyncImage` composables:
    - `KidHomeScreen`: ChannelCard (circle, 80dp) + VideoCard (16:9 aspect ratio)
    - `ChannelDetailScreen`: ChannelVideoCard (160dp, 16:9)
    - `VideoPlayerScreen`: UpNextCard (120dp, 16:9)
    - `WhitelistManagerScreen`: WhitelistItemCard (64dp, rounded corners)

- **KidSearch Screen (TDD, M3 completion)**:
  - `KidSearchViewModelTest` — 10 test cases (initial state, debounce triggering, no trigger before debounce, rapid typing, empty/blank query, clear query, mixed content types, no matches, correct profileId)
  - `KidSearchViewModel` — `debounce(300)` + `flatMapLatest` + `stateIn(Eagerly)`, AssistedInject
  - `KidSearchScreen` — search bar with auto-focus, clear button, results list with SearchResultCard (circle thumbnails for channels, 16:9 for videos/playlists), empty state hints
  - Added search icon button to `KidHomeScreen`
  - Wired `Route.KidSearch` into `AppNavigation.kt`

- **Sleep Mode (M4 complete)**:
  - `SleepModeViewModelTest` — 15 test cases (initial state, load videos, selectDuration, startTimer transition, countdown, timer expiry, fadeVolume at 3 levels, stopTimer reset, onVideoEnded advance/wrap/expired, currentVideo, empty videos)
  - `SleepModeViewModel` — `TimerState` enum (SELECTING/RUNNING/EXPIRED), timer with `delay(1000)` loop, volume fade-out (linear decrease over last 120 seconds), video cycling with modulo wrap-around, `onCleared()` cleanup
  - `SleepModeScreen` — custom `darkColorScheme`, 4 states: loading → empty → timer selection (15/30/45/60 min FilterChips) → playback (countdown display + WebView player) → expired ("Good night!")
  - `SleepYouTubePlayer` — WebView with same security hardening as VideoPlayerScreen, `@Keep` SleepVideoEndedBridge, `SleepPlayerHtml` HTML generator (dark background, no controls, autoplay)
  - Added `Route.SleepMode(profileId)` to navigation
  - Added Sleep Mode action card to `ParentDashboardScreen`
  - Wired into `AppNavigation.kt`

- **Build Verification & Test Fixes**:
  - Initial run: 7 failures
  - KidSearchViewModelTest: `advanceUntilIdle()` after `advanceTimeBy(100)` was advancing past debounce — removed unnecessary `advanceUntilIdle()`
  - SleepModeViewModelTest: 6 failures — `advanceUntilIdle()` after `startTimer()` ran entire timer to EXPIRED — removed, use only `advanceTimeBy()` for specific time advancement
  - Off-by-one: `advanceTimeBy(5_000)` boundary-exclusive — changed to `advanceTimeBy(5_001)`
  - **All 237 tests green after fixes**

- **Code Review Fixes**:
  - CRITICAL: WebView memory leak in VideoPlayerScreen — `mutableListOf<WebView?>()` → `mutableStateOf<WebView?>(null)`
  - CRITICAL: WebView memory leak in SleepModeScreen — same fix applied
  - Timer cleanup verified: `onCleared()` cancels timerJob, `startTimer()` cancels existing job before new one

**Decisions Made**:
- Coil 2.7.0 for image loading (stable, Compose-first API)
- KidSearchViewModel: `debounce(300) + flatMapLatest + stateIn(Eagerly)` — clean reactive pattern
- SleepModeViewModel: `delay(1000)` loop timer (simple, testable with `advanceTimeBy`)
- Volume fade: linear decrease over last 120 seconds (`FADE_DURATION_SECONDS`)
- Sleep mode dark theme: custom `darkColorScheme()` independent of app theme
- `mutableStateOf<WebView?>` (not `mutableListOf`) for WebView reference tracking

**Test Stats**: 237 total tests (212 existing + 10 KidSearch + 15 SleepMode), all green

**Notes**:
- `advanceUntilIdle()` processes ALL pending delays including future debounces/timers — use sparingly, prefer `advanceTimeBy()` for time-sensitive tests
- `advanceTimeBy(N)` is boundary-exclusive — add +1ms for inclusive boundary
- Playlist detail screen still not implemented (placeholder click handler)
- Kiosk mode deferred to later session
- Google Cloud Console YouTube API key still not created

**Next Session Focus**: M5 - Multi-profile support, time limits, watch stats, export/import. Or M3 kiosk mode if prioritized.
