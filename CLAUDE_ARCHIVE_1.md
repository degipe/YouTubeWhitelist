# YouTubeWhitelist - Session Archive 1 (Sessions 1-10)

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
- **Coil Image Loading (M3 completion)**: Added Coil 2.7.0, replaced all placeholder Icons with `AsyncImage` composables in KidHome, ChannelDetail, VideoPlayer, WhitelistManager screens
- **KidSearch Screen (TDD, M3 completion)**: KidSearchViewModel (debounce(300) + flatMapLatest + stateIn(Eagerly), AssistedInject), KidSearchScreen, 10 tests
- **Sleep Mode (M4 complete)**: SleepModeViewModel (TimerState enum, delay(1000) loop timer, volume fade-out over last 120s), SleepModeScreen (dark theme, 4 states), SleepYouTubePlayer (WebView), 15 tests
- **Build Verification & Test Fixes**: advanceUntilIdle() issues, boundary-exclusive advanceTimeBy fix
- **Code Review Fixes**: WebView memory leak fix (mutableStateOf instead of mutableListOf)

**Test Stats**: 237 total tests, all green

**Next Session Focus**: M5 - Multi-profile support, time limits, watch stats, export/import.

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

**Notes**:
- android.net.Uri throws RuntimeException in JVM tests — always use java.net alternatives
- org.json.JSONObject requires Robolectric (Android SDK class)
- F-Droid: app will get NonFreeNet anti-feature tag (YouTube API) but can be in main repo
- Google Cloud Console OAuth + YouTube API key still needs manual setup (see GOOGLE_SETUP.md)
- All M1-M6 milestones now complete, M7 (Publication) remaining

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
