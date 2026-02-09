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

### Session 7 - 2026-02-09: M5 - Multi-profile, Time Limits, Watch Stats, Export/Import

**Objectives**: Complete M5 milestone in one session: multi-profile support, daily time limits, watch statistics, and JSON export/import.

**Completed**:
- **Phase 1: Watch Stats (data layer)**:
  - `DailyWatchAggregate` Room POJO for aggregated daily watch time
  - `WatchStats` + `DailyWatchStat` domain models
  - 3 new `WatchHistoryDao` queries (getVideosWatchedCount, getDailyWatchTime, getTotalWatchedSecondsFlow)
  - 3 new `WatchHistoryRepository` methods (getWatchStats, getTotalWatchedSecondsToday, getTotalWatchedSecondsTodayFlow)
  - `WatchHistoryRepositoryImpl` with `startOfToday()` helper using `java.time.LocalDate`
  - 8 new tests in `WatchHistoryRepositoryImplTest`

- **Phase 1: WatchStatsViewModel + Screen**:
  - `WatchStatsViewModel` (AssistedInject, StatsPeriod enum DAY/WEEK/MONTH, DailyStatItem, formatWatchTime)
  - `WatchStatsScreen` (period FilterChips, summary cards, daily breakdown bar chart)
  - 11 tests in `WatchStatsViewModelTest`

- **Phase 2: Time Limits**:
  - `TimeLimitChecker` interface + `TimeLimitCheckerImpl` (combines profile + watch history flows via `combine()`)
  - `TimeLimitStatus` data class (dailyLimitMinutes, watchedTodaySeconds, remainingSeconds, isLimitReached)
  - DI binding in `DataModule`
  - `KidHomeViewModel` extended: 5-flow combine, remainingTimeFormatted, isTimeLimitReached
  - `VideoPlayerViewModel` extended: observeTimeLimit(), remainingTimeFormatted, isTimeLimitReached
  - `KidHomeScreen` updated: remaining time Card + "Time's Up" full-screen overlay
  - `VideoPlayerScreen` updated: remaining time badge + "Time's Up" overlay + onParentAccess
  - 8 tests in `TimeLimitCheckerImplTest`, +3 in KidHomeVM, +4 in VideoPlayerVM

- **Phase 3: Multi-profile**:
  - `SplashViewModel` extended: `MultipleProfiles` state (profiles.size > 1)
  - `SplashScreen` updated: onMultipleProfiles callback
  - `ProfileSelectorViewModel` (flatMapLatest account→profiles) + 6 tests
  - `ProfileSelectorScreen` (2-column grid of ProfileCards, Parent Mode button)
  - `ProfileEditViewModel` (AssistedInject, save/delete profile, daily limit editing) + 14 tests
  - `ProfileEditScreen` (name, avatar URL, daily limit slider 15-180 min, Save/Delete)
  - +2 SplashViewModel tests

- **Phase 4: Export/Import**:
  - `ExportData` @Serializable DTOs (ExportData, ExportProfile, ExportWhitelistItem) in core:export
  - `ExportImportService` interface + `ExportImportServiceImpl` (DAOs directly, UUID generation)
  - `ExportModule` Hilt DI (@Binds)
  - `ExportImportViewModel` (AssistedInject, export/import/dismiss) + 10 tests
  - `ExportImportScreen` (SAF CreateDocument/OpenDocument launchers, Merge/Overwrite dialog)
  - 14 tests in `ExportImportServiceImplTest`

- **Phase 5: Navigation wiring + Dashboard**:
  - 4 new routes: ProfileSelector, ProfileEdit(profileId), WatchStats(profileId), ExportImport(parentAccountId)
  - `AppNavigation.kt` updated with all new composable destinations + AssistedInject wiring
  - `ParentDashboardViewModel` extended: parentAccountId in UiState
  - `ParentDashboardScreen` updated: 4 new action cards (Edit Profile, Watch Stats, Export/Import, Create Profile)
  - `feature:parent/build.gradle.kts`: added core:export dependency

- **Phase 6: Build verification**:
  - Fix: `AppResult` import path `core.common.result.AppResult` (not `core.common.AppResult`)
  - Fix: Relaxed MockK mock returns non-null for nullable types — explicit `coEvery { findByYoutubeId } returns null`
  - **316 tests, all green**

**Decisions Made**:
- `TimeLimitChecker` as separate class — combines profile + watch history flows (SoC)
- core:export uses DAOs directly — avoids circular deps with core:data
- Import generates new UUIDs — prevents PK conflicts
- Export version field — future-proofing for format changes
- Full Kid mode blocking when time limit reached — overlay on KidHome + VideoPlayer
- ProfileSelector at app level (not feature:parent) — shown before mode selection
- getTotalWatchedSecondsTodayFlow as Flow — reactive time limit updates without polling
- No watch history in export — only profiles + whitelist items
- ParentDashboardUiState includes parentAccountId for Export/Import route

**Test Stats**: 316 total tests (237 existing + 79 new), all green

**New tests breakdown**: WatchHistoryRepo +8, TimeLimitChecker 8, WatchStatsVM 11, KidHomeVM +3, VideoPlayerVM +4, SplashVM +2, ProfileSelectorVM 6, ProfileEditVM 14, ExportImportService 14, ExportImportVM 10

**Notes**:
- Relaxed MockK mocks return non-null objects for nullable types — always explicit mock for null returns
- `AppResult` is in `core.common.result` package (not `core.common`)
- Playlist detail screen still not implemented
- Kiosk mode deferred to later session
- Google Cloud Console YouTube API key still not created

**Next Session Focus**: M6 - Testing, bugfix, optimization. Or M3 kiosk mode, playlist detail screen.

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
