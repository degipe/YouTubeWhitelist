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

### Session 1 - 2026-02-09: Project Initialization

**Objectives**: Initialize project from PRD, set up multi-module Android/Kotlin/Compose project, push to GitHub.

**Completed**:
- Read and analyzed full PRD (YouTubeWhitelist_PRD_v1.1.docx)
- Initialized git repository with `main` branch
- Created comprehensive `.gitignore` for Android/Kotlin
- Set up multi-module Gradle project with version catalog (`libs.versions.toml`)
- Created all 10 modules: `:app`, `:feature:parent`, `:feature:kid`, `:feature:sleep`, `:core:common`, `:core:data`, `:core:database`, `:core:network`, `:core:auth`, `:core:export`
- Configured dependencies: Compose BOM, Hilt, Room, Retrofit, OkHttp, KotlinX Serialization, WorkManager, Google Auth, etc.
- Created Room database entities matching PRD data model: `ParentAccountEntity`, `KidProfileEntity`, `WhitelistItemEntity`, `WatchHistoryEntity`
- Created Room DAOs with Flow-based reactive queries
- Created `YouTubeWhitelistDatabase` with all DAOs
- Created Hilt `DatabaseModule` for dependency injection
- Created `YouTubeWhitelistApp` (Application class with @HiltAndroidApp)
- Created `MainActivity` with Compose setup
- Created `YouTubeWhitelistTheme` with Material 3 + dynamic colors
- Created `HiltTestRunner` for instrumented tests
- Created basic `AppNavigation` skeleton
- Added GPLv3 LICENSE file
- Created CLAUDE.md, ARCHITECTURE.md, NEXT_SESSION_PROMPT.md
- Initial commit and push to GitHub

**Decisions Made**:
- Package name: `io.github.degipe.youtubewhitelist`
- Using Gradle version catalog for dependency management
- compileSdk/targetSdk = 35, minSdk = 26 (per PRD)
- Using KSP (not kapt) for annotation processing
- Kotlin 2.1.0 with integrated Compose compiler

**Notes**:
- JDK 8 is on the machine, but JDK 17+ needed for Android build. User will need to set up Android Studio / JDK 17.
- No `ANDROID_HOME` or `JAVA_HOME` environment variables set - Android Studio will configure these.
- Gradle wrapper jar downloaded, but full build verification will happen in Android Studio.

**Next Session Focus**: M1 infrastructure - PIN management, Google OAuth integration, basic parent/kid mode navigation with PIN switching.

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
