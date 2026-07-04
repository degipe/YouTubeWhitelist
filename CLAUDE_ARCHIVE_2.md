# YouTubeWhitelist - Session Archive 2 (Sessions 11-19)

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

### Session 12 - 2026-02-10: Device Testing, WebView Security Fix, Cookie Persistence

**Objectives**: Real device + emulator testing, fix embed-disabled video security issue, fix YouTube Premium cookie persistence in Browse YouTube.

**Completed**:
- **WebView Navigation Security Fix (VideoPlayerScreen + SleepModeScreen)**:
  - Added `shouldOverrideUrlLoading` returning `true` to block ALL navigation in video player WebViews
  - Prevents kids from escaping the app via "Watch on YouTube" links on embed-disabled videos
  - Added `onEmbedError` callback to `VideoEndedBridge` JavaScript bridge — handles YouTube IFrame Player error codes 101/150
  - Auto-skips to next video when embed error detected
  - Same fixes applied to `SleepModeScreen`'s `SleepVideoEndedBridge`

- **Browse YouTube Cookie Persistence Fix**:
  - Added `CookieManager` setup to `WebViewBrowserScreen`'s `YouTubeWebView`
  - `CookieManager.setAcceptCookie(true)` + `setAcceptThirdPartyCookies(this, true)`
  - `CookieManager.flush()` in `DisposableEffect.onDispose` persists cookies to disk
  - YouTube Premium now recognized in Browse YouTube

- **Attempted WebView OAuth (reverted)**: Google blocks OAuth in embedded WebViews since 2016
- **Emulator + Real Device Testing**: Release APK verified on both

**Decisions Made**:
- `shouldOverrideUrlLoading` returns `true` unconditionally in player WebViews
- Error codes 101/150 trigger auto-skip — seamless UX for kids
- Chrome Custom Tabs remains the only OAuth option
- Users sign in twice (Chrome Custom Tabs + Browse YouTube WebView — separate cookie stores)

**Test Stats**: 355 tests, all green

### Session 13 - 2026-02-10: Sleep Mode Refactoring + Device Testing Bug Fixes + Channel Video Search

**Objectives**: Refactor Sleep Mode from standalone video player to background timer, device testing with bug fixes, implement channel video search in kid mode.

**Completed**:
- **Sleep Mode Refactoring (Phases 4-5)**:
  - Phases 1-3 completed in previous session (SleepTimerManager, SleepModeViewModel rewrite, SleepModeScreen rewrite)
  - Phase 4: Added `SleepTimerManager` to `KidHomeViewModel` and `VideoPlayerViewModel`
  - Nested `combine()` for 6th flow in KidHomeViewModel (Kotlin combine() supports max 5 natively)
  - `isSleepTimerExpired` added to both UiStates (checks profileId match)
  - "Good Night" overlay on both KidHomeScreen and VideoPlayerScreen (dark theme, Bedtime icon, Lock FAB)
  - Phase 5: Navigation wiring — SleepTimerManager injected in MainActivity, passed to AppNavigation
  - PinEntry: `sleepTimerManager.stopTimer()` when timer EXPIRED and PIN verified
  - SleepModeScreen `onStartTimer` → navigates to KidHome
  - +7 new tests (4 KidHome + 3 VideoPlayer)

- **Device Testing Bug Fixes**:
  - **Fullscreen + sleep timer**: `LaunchedEffect(shouldBlock)` exits fullscreen + JavaScript `pauseVideo()` when overlay appears
  - **Channel cards cut off**: Replaced `LazyVerticalGrid` (fixed height) with `Column` + `Row` + `chunked(2)` for natural sizing
  - **FAB covers delete button**: Moved Add button from Scaffold FAB to inline `IconButton` in filter chip row
  - **Search only accepts 1 character**: Exposed `queryFlow.asStateFlow()` as `query` — TextField uses immediate state, results use debounced state
  - **Search also checks channelTitle**: SQL updated to `(title LIKE '%' || :query || '%' OR channelTitle LIKE '%' || :query || '%')`

- **Channel Video Search via YouTube API (TDD)**:
  - `searchVideosInChannel(channelId, query)` added to `YouTubeApiRepository`
  - `getChannelYoutubeIds(profileId)` added to `WhitelistRepository`
  - `KidSearchViewModel` refactored: `combine()` merges local DB results + API channel video results
  - Max 3 channels searched (quota protection: 100 units/search, 10k daily limit)
  - +5 YouTubeApiRepo tests + 5 KidSearchViewModel tests

**Test Stats**: 378+ tests, all green

### Session 14 - 2026-02-10: Reverse SDLC Documentation

**Objectives**: Generate complete SDLC documentation from codebase (reverse engineering) for professional GitHub presentation.

**Completed**:
- **LLD.md** (948 lines, 5 Mermaid diagrams): Module deps, ER diagram, DAOs, repositories, URL parsing, API service, DTOs, OAuth, PIN, tokens, Hilt modules, SleepTimer, Export/Import, WebView, ProGuard, patterns
- **HLD.md** (470 lines, 8 Mermaid diagrams): System architecture, module graph, tech stack, MVVM flow, navigation, security, storage, API quota, build pipeline, error handling
- **FS.md** (463 lines, 5 Mermaid diagrams): Roles, 18 screens, 15 FRs, 5 user flows, 6 UiStates, validation
- **BRD.md** (257 lines): Executive summary, objectives, 8 BRs, 18 NFRs, constraints, metrics, glossary
- All 18 Mermaid diagrams validated with mermaid-cli

**Files Created**: `docs/LLD.md`, `docs/HLD.md`, `docs/FS.md`, `docs/BRD.md`

**Test Stats**: 378+ tests, all green (documentation only session)

### Session 15 - 2026-02-10: SDLC Documentation Completion (Onboarding, User Manual, PRD Translation)

**Objectives**: Create comprehensive developer onboarding guide, detailed user manual, and translate Hungarian PRD to English markdown.

**Completed**:
- **Developer Onboarding Guide** (`docs/DEVELOPER_ONBOARDING.md` — ~750 lines): 18 chapters (setup → architecture → modules → patterns → testing)
- **User Manual** (`docs/USER_MANUAL.md` — ~500 lines): 15 chapters (kid mode → parent mode → advanced features), FAQ, troubleshooting
- **PRD Translation** (`docs/PRD.md` — ~732 lines): Full English translation of Hungarian PRD v1.1 (19 sections, 35 FRs). Original docx deleted.

**Files Created**: `docs/DEVELOPER_ONBOARDING.md`, `docs/USER_MANUAL.md`, `docs/PRD.md`

**Test Stats**: 378+ tests, all green (documentation only session)

### Session 16 - 2026-02-10: Store Submission + Final Polish

**Objectives**: GitHub Release v1.0.0, Privacy Policy, Play Store screenshots, API key restriction, F-Droid submission, Play Store guide.

**Completed**:
- **Privacy Policy** (GitHub Pages + repo): `docs/privacy-policy.md` (Jekyll), `docs/PRIVACY_POLICY.md` (repo), `docs/_config.yml`
- **AboutScreen v1.0.0 Update**: Version bumped, Privacy Policy link added
- **GitHub Release v1.0.0**: Tag + Release + APK attached
- **API Key Restriction**: Android apps + YouTube Data API v3 only, OAuth consent → Production
- **F-Droid Submission**: RFP content prepared, GitLab blocked by Cloudflare
- **Play Store Screenshots**: 5 screenshots (1080x2400) in fastlane metadata
- **Play Store Submission Guide**: `docs/PLAY_STORE_SUBMISSION.md`

**Test Stats**: 378+ tests, all green

### Session 17 - 2026-02-10: Improved Screenshots with Real YouTube Thumbnails

**Objectives**: Replace fake-looking emulator screenshots with authentic ones using real YouTube thumbnail URLs, add Sleep Mode and Export/Import screenshots.

**Completed**:
- 7 screenshots (1080x2400) with real YouTube thumbnails (Peppa Pig, Cocomelon, Sesame Street, Baby Shark, MrBeast)
- Release APK on emulator, adb root for DB access, uiautomator dump for precise taps

**Test Stats**: 378+ tests, all green (no code changes)

### Session 18 - 2026-02-10: F-Droid/API Strategy Analysis + Remove API Search from Kid Mode

**Objectives**: Analyze F-Droid inclusion policy compliance, research API-free YouTube endpoints, remove expensive YouTube Search API from kid mode.

**Completed**:
- **F-Droid Inclusion Policy Analysis**:
  - Reviewed full inclusion criteria against project
  - All FLOSS requirements met (GPLv3, no proprietary deps, no Play Services, no tracking)
  - Identified critical issue: "F-Droid does not sign up for any API keys" — app needs YouTube API key
  - F-Droid builds from source → `local.properties` not available → empty API keys → broken app

- **YouTube API-Free Endpoints Research** (parallel agents):
  - **oEmbed API** (`youtube.com/oembed`): returns title, author_name, thumbnail for videos/playlists (NOT channels). No API key, no quota, no rate limit.
  - **RSS/Atom feeds** (`youtube.com/feeds/videos.xml`): returns last 15 videos per channel/playlist with full metadata. No API key. NOT compatible with @handles.
  - **Direct thumbnails** (`i.ytimg.com/vi/{id}/mqdefault.jpg`): always available, no API needed
  - **Invidious/Piped**: open-source YouTube proxy with full API, no key needed, but unreliable instances

- **Removed YouTube Search API from Kid Mode**: local-only search, 0 API quota
- **GCP API Key Restriction Fix**: removed Android app restriction, kept YouTube Data API v3 only

**Test Stats**: 373+ tests, all green

### Session 19 - 2026-02-10: Strategy E Implementation (Hybrid + Invidious Fallback)

**Objectives**: Implement Strategy E — oEmbed/RSS free endpoints + YouTube API + Invidious fallback. Built-in API key for F-Droid compatibility.

**Completed**:
- **oEmbed Service**: FREE video/playlist metadata via `youtube.com/oembed` (Retrofit, 4 tests)
- **RSS Feed Parser**: FREE channel video list via XML feeds, XXE-protected (5 tests)
- **Invidious API Service**: Fallback with round-robin instance rotation, health tracking (6 tests)
- **HybridYouTubeRepositoryImpl**: Replaces YouTubeApiRepositoryImpl, fallback chain: oEmbed/RSS → YouTube API → Invidious (13 tests)
- **DI & Build**: `@PlainOkHttp`/`@YouTubeApiOkHttp` qualifiers, built-in fallback API key for F-Droid
- **Code Review**: XXE protection, @Synchronized, IOException-only failure tracking, ProGuard rules

**Architecture**: `YouTubeApiRepository` → `HybridYouTubeRepositoryImpl` (OEmbedService + RssFeedParser + YouTubeApiService + InvidiousApiService)

**Files Created**: 16 (6 source + 4 mapper + 6 test files)
**Files Modified**: NetworkModule, DataModule, build.gradle.kts, proguard-rules.pro

**Test Stats**: 413 tests, all green

### Session 20 - 2026-02-10: Channel Video Lazy Loading + Room Cache + Search

**Objectives**: Implement infinite scroll (lazy loading) for channel detail, Room cache as single source of truth, local search in cached videos (0 API quota).

**Completed**:
- **Phase 1: Database Layer** (core:database):
  - `CachedChannelVideoEntity` — Room entity with composite PK `(channelId, videoId)`, index on channelId
  - `CachedChannelVideoDao` — getVideosByChannel (Flow), searchVideosInChannel (LIKE query, Flow), upsertAll, deleteByChannel, getMaxPosition
  - Room DB version 2→3 (`fallbackToDestructiveMigration()`)
  - DatabaseModule updated with DAO provider
  - 5 Robolectric DAO tests (insert+get, search match, search no match, delete, upsert duplicate)

- **Phase 2: Repository Layer** (core:data):
  - `PaginatedPlaylistResult` data class (videos + nextPageToken)
  - `YouTubeApiRepository.getPlaylistItemsPage()` — new interface method
  - `HybridYouTubeRepositoryImpl.getPlaylistItemsPage()` — RSS (first page only) → YouTube API (pageToken) → Invidious fallback
  - `ChannelVideoCacheRepository` interface + `ChannelVideoCacheRepositoryImpl` — maps Entity↔PlaylistVideo
  - `YouTubeApiRepositoryImpl.getPlaylistItemsPage()` implementation
  - DataModule binding for ChannelVideoCacheRepository
  - 5 HybridYouTubeRepositoryImpl pagination tests

- **Phase 3: ViewModel Rewrite** (feature:kid):
  - `ChannelDetailViewModel` fully rewritten:
    - Room cache as Single Source of Truth (UI reads from Room Flow, API writes to Room)
    - `_searchQuery` MutableStateFlow + `debounce(300)` + `flatMapLatest` → Room query switching
    - `_controlState` for loading/error/hasMorePages (separate from videos)
    - `combine(videosFlow, _controlState)` → `stateIn(Eagerly)` for uiState
    - `loadMore()` — fetches next page → caches → Room Flow auto-updates
    - `onSearchQueryChanged()`, `onClearSearch()` for search bar
  - 13 ViewModel tests (9 rewritten + 4 new: hasMorePages, loadMore, search, clear search, loadMore error)

- **Phase 4: UI Update** (feature:kid):
  - `ChannelDetailScreen` — search bar toggle in TopAppBar (TextField + FocusRequester + keyboard control)
  - Back button: exits search mode when active, navigates back otherwise
  - Clear button in search mode when query non-empty
  - Infinite scroll: `LaunchedEffect(Unit)` in trailing `item{}` when `hasMorePages = true`
  - Loading spinner at list bottom during page fetch
  - Empty state: "No videos found" (search) vs "No videos in this channel yet" (no videos)
  - Error state: only shows full-screen error when no videos loaded (loadMore error preserves existing videos)

**Architecture**:
```
ChannelDetailViewModel
  ├── YouTubeApiRepository.getPlaylistItemsPage(playlistId, pageToken)
  │     └── HybridYouTubeRepositoryImpl (RSS → YouTube API → Invidious)
  └── ChannelVideoCacheRepository (Room cache)
        └── CachedChannelVideoDao → cached_channel_videos table

UI observes: Room Flow → auto-updates on cache changes
API writes: fetch page → cacheVideos() → Room Flow emits → UI updates
Search: Room SQL LIKE query (0 API quota)
```

**Decisions Made**:
- Composite PK `(channelId, videoId)` instead of auto-generated ID — required for `@Upsert` to work correctly
- Room cache cleared on each channel open (fresh data per visit)
- RSS only for first page (no pagination support), YouTube API for continuation pages
- Error state shows inline only if existing videos are loaded (loadMore error doesn't clear list)
- `Dispatchers.resetMain()` must be LAST in `tearDown()` — StateFlow.setValue after resetMain throws IllegalStateException

**Files Created** (5 source + 1 test):
- `core/database/.../entity/CachedChannelVideoEntity.kt`
- `core/database/.../dao/CachedChannelVideoDao.kt`
- `core/data/.../model/PaginatedPlaylistResult.kt`
- `core/data/.../repository/ChannelVideoCacheRepository.kt`
- `core/data/.../repository/impl/ChannelVideoCacheRepositoryImpl.kt`
- `core/database/src/test/.../dao/CachedChannelVideoDaoTest.kt` (5 tests)

**Files Modified** (8 source + 2 test):
- `core/database/.../YouTubeWhitelistDatabase.kt` (entity + version 3 + DAO getter)
- `core/database/.../di/DatabaseModule.kt` (DAO provider)
- `core/database/build.gradle.kts` (androidx-test-core dep)
- `gradle/libs.versions.toml` (androidx-test-core entry)
- `core/data/.../repository/YouTubeApiRepository.kt` (+getPlaylistItemsPage)
- `core/data/.../repository/impl/HybridYouTubeRepositoryImpl.kt` (+paginated methods)
- `core/data/.../repository/impl/YouTubeApiRepositoryImpl.kt` (+getPlaylistItemsPage)
- `core/data/.../di/DataModule.kt` (+ChannelVideoCacheRepository binding)
- `feature/kid/.../channel/ChannelDetailViewModel.kt` (full rewrite)
- `feature/kid/.../channel/ChannelDetailScreen.kt` (search bar + infinite scroll)
- `core/data/src/test/.../HybridYouTubeRepositoryImplTest.kt` (+5 pagination tests)
- `feature/kid/src/test/.../channel/ChannelDetailViewModelTest.kt` (9→13 tests, full rewrite)

**Test Stats**: ~401 tests, all green (378 existing + 5 DAO + 5 repo + 13 VM = ~401)

**Notes**:
- Quota impact: `playlistItems.list` = 1 unit per 50 videos. 200 videos = 4 units. In-channel search = 0 units.
- `@Upsert` matches on PRIMARY KEY, not unique indices — composite PK required for proper upsert
- `Dispatchers.resetMain()` in tearDown must be LAST — setting StateFlow.value dispatches to Main
- `advanceTimeBy(301)` for debounce(300) boundary-exclusive testing
- Session 15 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-15)
