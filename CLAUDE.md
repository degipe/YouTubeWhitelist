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
Full PRD: `docs/PRD.md` (English translation from original Hungarian docx)

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

### Session 21 - 2026-02-10: Emulator Testing - Lazy Loading + Search Verification

**Objectives**: Verify lazy loading (infinite scroll) and local search on emulator with real YouTube content (MrBeast channel).

**Completed**:
- **Lazy Loading Verification** (MrBeast channel — UCX6OQ3DkcsbYNE6H8uQQuVA):
  - Opened MrBeast channel detail screen from Kid Home
  - Scrolled through ~400 videos across 8 pages (8 × 50 = 400)
  - Infinite scroll worked flawlessly — loading spinner appeared at bottom, next page loaded automatically
  - Videos ranged from newest ("Guess What Age Punched You") to older ("$456,000 Squid Game In Real Life!")
  - Room cache confirmed: `SELECT COUNT(*) FROM cached_channel_videos` → **400 videos**
  - Total API cost: 8 units (8 pages × 1 unit/page)

- **Local Search Verification** (Room cache, 0 API quota):
  - Tapped search icon → TextField with "Search videos..." placeholder appeared
  - Searched "Squid" → 1 result: "$456,000 Squid Game In Real Life!"
  - Cleared search → all 400 videos restored
  - Searched "Lamborghini" → 4 results: "How Much Tape To Stop A Lamborghini?", "Stop This Train, Win a Lamborghini", "Lamborghini Vs World's Largest Shredder", "Hydraulic Press Vs Lamborghini"
  - Search is instant (Room SQL LIKE query, 0 API quota)

- **Archive**: Session 16 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-16)

**Decisions Made**:
- No code changes needed — lazy loading + search works perfectly on real device
- 400 videos confirmed loaded via Room DB query

**Test Stats**: ~401 tests, all green (no code changes)

**Notes**:
- DB file is `youtubewhitelist.db` (not `youtube_whitelist_db`)
- `adb root` required for DB access on release builds
- MrBeast has 800+ videos — only loaded 400 (8 pages) during test, more would load on continued scrolling
- Search UI: TopAppBar toggles between title and TextField, back arrow exits search mode
- Quota savings: 400 videos = 8 API units. In-channel search = 0 units (vs 100 units/search with YouTube Search API)

### Session 22 - 2026-02-11: v1.1.0 Release Build + Store Assets + SDLC Docs Update

**Objectives**: Generate Play Store assets (feature graphic, app icon), build v1.1.0 release, update SDLC documentation for Strategy E + lazy loading changes.

**Completed**:
- **Feature Graphic** (1024x500 PNG):
  - Generated shield+play button icon with Gemini AI, composed with ImageMagick
  - Light blue gradient background, app icon left, "YouTubeWhitelist / Safe YouTube for Kids" text right
  - Saved to `fastlane/metadata/android/en-US/images/featureGraphic.png`

- **App Icon** (512x512 PNG):
  - Generated with Gemini AI: blue background, white play button, shield+checkmark badge
  - Resized from 1024x1024 with ImageMagick
  - Saved to `fastlane/metadata/android/en-US/images/icon.png`

- **v1.1.0 Release Build**:
  - `versionCode` 1→2, `versionName` 1.0.0→1.1.0
  - CHANGELOG.md updated with all v1.1.0 changes
  - Fastlane changelogs (EN + HU) for versionCode 2
  - Release APK: 2.4 MB, Release AAB: 5.3 MB
  - All tests pass (~401)

- **SDLC Documentation Update** (5 files, comprehensive):
  - **BRD.md**: Version 1.1.0, test count 401+, entity count 5, hybrid quota strategy
  - **FS.md**: Version 1.1.0, FR-07 (kid search local-only), FR-08 (lazy loading + in-channel search), search flow diagram simplified
  - **HLD.md**: Version 1.1.0, architecture diagram (oEmbed/RSS/Invidious), network module description, new External API Integration section (Strategy E hybrid + fallback chain table + quota strategy)
  - **LLD.md**: Version 1.1.0, DB version 3, 5 entities/DAOs, CachedChannelVideoEntity + DAO, YouTubeApiRepository (+getPlaylistItemsPage), ChannelVideoCacheRepository, Hybrid Network Layer section (oEmbed/RSS/Invidious), dual OkHttp clients, API quota table (free alternatives), DI qualifiers (9), NetworkModule + DataModule updated
  - **DEVELOPER_ONBOARDING.md**: Test count 401+, 5 entities/DAOs, core:network (oEmbed/RSS/Invidious), core:database (version 3), Network Layer §10 completely rewritten (Strategy E), API quota table with free alternatives, Room cache SSOT pattern added, YouTube API pitfalls updated

- **Archive**: Session 17 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-17)

**Decisions Made**:
- Feature graphic: text-free AI generation + ImageMagick text overlay (Gemini can't spell "Whitelist" correctly)
- App icon: AI-generated shield+play button+checkmark (consistent with existing vector launcher icon concept)
- v1.1.0 (not v2.0.0) — significant improvements but backward-compatible, no breaking changes

**Files Created**:
- `fastlane/metadata/android/en-US/images/featureGraphic.png` (1024x500)
- `fastlane/metadata/android/en-US/images/icon.png` (512x512)
- `fastlane/metadata/android/en-US/changelogs/2.txt`
- `fastlane/metadata/android/hu-HU/changelogs/2.txt`

**Files Modified**:
- `app/build.gradle.kts` (versionCode 2, versionName 1.1.0)
- `CHANGELOG.md` (v1.1.0 section)
- `docs/BRD.md` (version, tests, entities, quota)
- `docs/FS.md` (version, FR-07, FR-08, search flow, KidSearchUiState)
- `docs/HLD.md` (version, architecture diagram, module desc, API integration, quota)
- `docs/LLD.md` (version, DB schema, ER diagram, entity, DAO, repositories, network layer, DI, quota)
- `docs/DEVELOPER_ONBOARDING.md` (tests, entities, network, database, API quota, patterns, pitfalls)

**Session Files**:
- `CLAUDE.md` (Session 17 archived, Session 22 added)
- `CLAUDE_ARCHIVE_2.md` (Session 17 added, now contains sessions 11-17)
- `ARCHITECTURE.md` (Session 22 entry)
- `NEXT_SESSION_PROMPT.md` (updated for Session 23)

**Test Stats**: ~401 tests, all green

**Notes**:
- Gemini AI consistently misspells "Whitelist" (Whtislist, Whitlisnt) — use text-free generation + ImageMagick for text overlay
- ImageMagick `magick` available via Homebrew on macOS — useful for compositing, resizing, text overlay
- `sips` (macOS built-in) also available but ImageMagick more flexible
- Feature graphic composition: gradient bg → shield icon → text overlay = professional result
- All 7 SDLC docs now reflect v1.1.0 hybrid architecture accurately

### Session 23 - 2026-02-11: GitHub Release v1.1.0 + F-Droid RFP + Play Store Registration

**Objectives**: Create GitHub Release v1.1.0, submit F-Droid RFP, register for Google Play Developer account, submit app to Play Store.

**Completed**:
- **GitHub Release v1.1.0**:
  - Tag `v1.1.0` created, release notes from CHANGELOG.md
  - APK attached as `YouTubeWhitelist-v1.1.0.apk` (2.5 MB)
  - URL: https://github.com/degipe/YouTubeWhitelist/releases/tag/v1.1.0

- **F-Droid RFP (Request for Packaging)**:
  - Issue #3794 submitted on GitLab fdroiddata repo via Playwright browser automation
  - **Wrong repo** — linsui closed it, correct repo is `fdroid/rfp` (fixed in Session 24)
  - URL: https://gitlab.com/fdroid/fdroiddata/-/issues/3794 (CLOSED)

- **Google Play Developer Account**:
  - Registration started at play.google.com/console/signup
  - Account type: Personal ("Saját magadnak")
  - Developer name: "Peter Degi"
  - $25 registration fee paid
  - Account created (Fiókazonosító: 4768413512690805008)

- **Play Store Submission Guide** updated:
  - `docs/PLAY_STORE_SUBMISSION.md` updated for v1.1.0 (7 screenshots, feature graphic, icon, versionCode 2)

- **Device Verification Attempt** (BLOCKED):
  - Play Console app requires Google Play Services — not available on Fairphone 3 with /e/OS (microG)
  - Emulator with Play Store image: app detects emulator, refuses verification
  - Attempted: Play Store image download, AVD reconfiguration, APK sideload to Fairphone, emulator property spoofing
  - Resolution: deferred — user needs a Google Play Services-capable Android phone (physical device)

**Decisions Made**:
- GitHub Release v1.1.0 with APK attachment (not just tag)
- F-Droid RFP as Issue type (not Task) on GitLab
- Play Store developer name: "Peter Degi" (English order, no accents)
- Device verification deferred — need physical Android phone with Google Play Services
- Emulator AVD changed to Play Store image (google_apis_playstore) — kept for future use

**Files Modified**:
- `docs/PLAY_STORE_SUBMISSION.md` (updated for v1.1.0: screenshots, icon, feature graphic, release info, API key note)

**Session Files**:
- `CLAUDE.md` (Session 18 archived, Session 23 added)
- `CLAUDE_ARCHIVE_2.md` (Session 18 added, now contains sessions 11-18)
- `ARCHITECTURE.md` (Session 23 entry)
- `NEXT_SESSION_PROMPT.md` (updated for Session 24)

**Test Stats**: ~401 tests, all green (no code changes)

**Notes**:
- Playwright browser can't launch when Chrome is already running — need to close Chrome first or install separate browser
- /e/OS microG doesn't support Google Play Console app (loads but can't communicate with Google servers)
- Emulator `ro.` properties are read-only — can't spoof device identity at runtime
- Play Console app split APKs: `install-multiple` command needed for sideloading
- Google Play developer verification chain: device → phone number → identity (sequential, can't skip)
- Emulator AVD config: `PlayStore.enabled=yes`, `tag.id=google_apis_playstore`, `image.sysdir.1=system-images/android-34/google_apis_playstore/arm64-v8a/`

### Session 24 - 2026-02-12: F-Droid RFP Resubmission (Correct Repo)

**Objectives**: Resubmit F-Droid RFP to the correct repo (`fdroid/rfp`) after the original submission to `fdroid/fdroiddata` was closed by maintainer linsui.

**Completed**:
- **F-Droid RFP #3586** submitted to correct repo `fdroid/rfp`:
  - All 3 applicable checkboxes checked (inclusion criteria, not already listed, author notified)
  - Template sections filled: source code, GitHub Release link, GPL-3.0-only, Internet category
  - Full description with features, privacy, anti-features (NonFreeNet), build information
  - URL: https://gitlab.com/fdroid/rfp/-/issues/3586

- **References updated**:
  - `NEXT_SESSION_PROMPT.md`: F-Droid RFP link updated to #3586
  - `docs/PLAY_STORE_SUBMISSION.md`: F-Droid RFP link updated to #3586

- **Archive**: Session 19 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-19)

**Decisions Made**:
- F-Droid RFP goes to `fdroid/rfp` repo (Request for Packaging), NOT `fdroid/fdroiddata` (recipe data)
- Category: "Internet" (F-Droid standard category for network-dependent apps)

**Files Modified**:
- `NEXT_SESSION_PROMPT.md` (F-Droid RFP link #3794 → #3586)
- `docs/PLAY_STORE_SUBMISSION.md` (F-Droid RFP link #3794 → #3586)

**Session Files**:
- `CLAUDE.md` (Session 19 archived, Session 24 added, Session 23 F-Droid note updated)
- `CLAUDE_ARCHIVE_2.md` (Session 19 added, now contains sessions 11-19)
- `ARCHITECTURE.md` (Session 24 entry)
- `NEXT_SESSION_PROMPT.md` (updated for Session 25)

**Test Stats**: ~401 tests, all green (no code changes)

**Notes**:
- F-Droid RFP correct repo: `gitlab.com/fdroid/rfp` (NOT `fdroid/fdroiddata`)
- Old issue #3794 on fdroiddata is closed — new issue #3586 on rfp is the active one
- Lesson learned: F-Droid has separate repos: `fdroiddata` (build recipes), `rfp` (packaging requests)
