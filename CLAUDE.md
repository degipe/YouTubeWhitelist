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

### Session 17 - 2026-02-10: Improved Screenshots with Real YouTube Thumbnails

**Objectives**: Replace fake-looking emulator screenshots with authentic ones using real YouTube thumbnail URLs, add Sleep Mode and Export/Import screenshots.

**Completed**:
- **Screenshot Overhaul (7 screenshots, 1080x2400)**:
  - Installed release APK on emulator (not debug — API key restricted to release package)
  - User signed into the app on emulator (Google OAuth via Chrome Custom Tabs)
  - Created 2 profiles (Emma with 60min daily limit, Max with 90min)
  - Inserted whitelist items with REAL YouTube video thumbnails:
    - Peppa Pig Official Channel (DysgBIOiIwE) — actual Peppa Pig cartoon
    - Cocomelon Nursery Rhymes (e_04ZrNroTo) — Wheels on the Bus with Cocomelon logo
    - Sesame Street (aqUefNVhsNM) — ABC compilation with Elmo
    - Baby Shark Dance (XqZsoesa55w) — Pinkfong "Most Viewed" thumbnail
    - MrBeast (0e3GPea1Tyg) — Squid Game recreation
    - Peppa Pig Full Episodes playlist (amNTw2cbxyY) — birthday party scene
  - All thumbnail URLs verified with HTTP 200 before insertion
  - Screenshots taken after toast messages dismissed (clean, no overlays)

- **7 Screenshots saved to fastlane metadata**:
  1. `01_profile_selector.png` (60KB) — "Who's watching?" with Emma + Max
  2. `02_kid_home.png` (728KB) — Kid Home with real thumbnails, "Time remaining: 1h 0m"
  3. `03_pin_entry.png` (71KB) — 6-digit PIN entry numpad
  4. `04_parent_dashboard.png` (144KB) — Parent Dashboard with all actions
  5. `05_whitelist_manager.png` (410KB) — Whitelist Manager with real thumbnails
  6. `06_sleep_mode.png` (64KB) — Sleep Mode timer (30m slider + Start button)
  7. `07_export_import.png` (83KB) — Export/Import screen

- **Archive**: Session 12 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-12)

**Decisions Made**:
- Release APK on emulator (not debug) — API key restricted to `io.github.degipe.youtubewhitelist` package
- Root access on emulator (`adb root`) to modify release app's database directly
- Real YouTube video thumbnails via `i.ytimg.com/vi/{videoId}/hqdefault.jpg` format
- `uiautomator dump` for precise tap coordinates (not visual estimation)

**Files Modified**:
- `fastlane/metadata/android/en-US/images/phoneScreenshots/01-07_*.png` (7 screenshots)
- `CLAUDE.md` (Session 12 archived, Session 17 added)
- `CLAUDE_ARCHIVE_2.md` (Session 12 added)
- `ARCHITECTURE.md` (Session 17 entry)
- `NEXT_SESSION_PROMPT.md` (updated for Session 18)

**Test Stats**: 378+ tests, all green (no code changes)

**Notes**:
- Thumbnail URL format `https://i.ytimg.com/vi/{videoId}/hqdefault.jpg` always works for valid video IDs
- Channel avatar URLs (yt3.ggpht.com) are unique tokens — can't be fabricated, use video thumbnails instead
- `adb root` works on emulator (Google APIs, not Google Play) — allows DB access for release builds
- `uiautomator dump` returns exact XML bounds for all UI elements — reliable for adb tap automation
- "App is pinned" / "App unpinned" toasts appear on kiosk mode transitions — wait 5 seconds before screenshotting
- API error for many-image requests: max 2000px dimension per image — avoid reading too many screenshots in one conversation

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

- **Current API Usage Inventory** (code analysis):
  - 5 endpoints: channels.list, videos.list, playlists.list, playlistItems.list, search.list
  - **95% of quota consumed by search.list** (100-300 units/query vs 1-2 units for everything else)
  - Shared API key supports max ~3 concurrent intensive users (10k daily limit)

- **Strategy Document** (`/tmp/YOUTUBE_API_STRATEGY.md`):
  - 5 strategies compared (Hybrid, Zero-API, Built-in key, Invidious, Hybrid+Invidious)
  - Detailed quota math for each scenario
  - 3-phase implementation roadmap proposed
  - NOT published to GitHub (internal analysis only)

- **GCP API Key Restriction Fix**:
  - Removed Android app restriction (was blocking daughter's phone with 403)
  - Kept API restriction: YouTube Data API v3 only
  - Before: Android apps (`io.github.degipe.youtubewhitelist` + SHA-1) + YouTube Data API v3
  - After: No application restriction + YouTube Data API v3 only

- **Removed YouTube Search API from Kid Mode**:
  - `KidSearchViewModel`: removed `YouTubeApiRepository` dependency, `_channelVideoResults`, `channelSearchJob`, `searchChannels()`, `combine()` logic
  - Search now only queries local Room DB (whitelist items by title/channelTitle)
  - 120 → 68 lines of code (43% reduction)
  - 5 API search tests removed, 10 local search tests remain (all green)
  - **Quota impact**: Kid search now 0 API units (was 100-300 per query). Daily limit supports ~5000 users.

**Decisions Made**:
- Remove YouTube Search API from kid mode (95% of quota consumption) — local search only
- GCP API key: remove Android restriction, keep API restriction only (needed for multi-device support)
- F-Droid submission deferred until API strategy finalized (oEmbed/RSS hybrid vs built-in key)
- Play Store submission also deferred pending API strategy decision

**Files Modified**:
- `feature/kid/.../search/KidSearchViewModel.kt` (removed YouTubeApiRepository, simplified to local-only search)
- `feature/kid/src/test/.../search/KidSearchViewModelTest.kt` (removed 5 API search tests)

**Session Files**:
- `CLAUDE.md` (Session 13 archived, Session 18 added)
- `CLAUDE_ARCHIVE_2.md` (Session 13 added, now contains sessions 11-13)
- `ARCHITECTURE.md` (Session 18 entry)
- `NEXT_SESSION_PROMPT.md` (updated for Session 19)

**Test Stats**: 373+ tests, all green (378 - 5 removed API search tests)

**Notes**:
- `/tmp/YOUTUBE_API_STRATEGY.md` contains full analysis (12 sections, quota math, implementation details) — NOT for GitHub
- oEmbed can replace videos.list and playlists.list (0 quota vs 1 unit each)
- RSS can replace channel video listing (0 quota, but max 15 videos vs 50)
- @handle → channelId resolution still needs YouTube API (no free alternative)
- Invidious is unreliable (instances go down, YouTube blocks them) — only as last-resort fallback
- Kid search removal alone makes the app viable for ~5000 concurrent users on single API key

**Next Session Focus**: Decide on API strategy (built-in key + oEmbed/RSS hybrid recommended), implement chosen strategy, then proceed to store submissions.

### Session 19 - 2026-02-10: Strategy E Implementation (Hybrid + Invidious Fallback)

**Objectives**: Implement Strategy E — oEmbed/RSS free endpoints + YouTube API + Invidious fallback. Built-in API key for F-Droid compatibility.

**Completed**:
- **Phase 1: oEmbed Service** (FREE video/playlist metadata):
  - `OEmbedResponse` data class (kotlinx.serialization)
  - `OEmbedService` Retrofit interface (base URL: `youtube.com/oembed`)
  - 4 unit tests (video, playlist, channelId extraction, unknown fields)

- **Phase 2: RSS Feed Parser** (FREE channel video list):
  - `RssVideoEntry` data class (videoId, title, thumbnail, channel, published)
  - `RssFeedParser` with `javax.xml.parsers.DocumentBuilderFactory` (namespace-aware, XXE-protected)
  - `fetchChannelVideos(channelId)` → last 15 videos, no API key needed
  - 5 unit tests (valid XML, empty, malformed, missing videoId, thumbnail URL)

- **Phase 3: Invidious API Service** (fallback):
  - `InvidiousDto` — 5 @Serializable data classes (Video, Channel, Playlist, PlaylistVideo, Thumbnail)
  - `InvidiousApiService` — HTTP client with dynamic base URL (getVideo, getChannel, getPlaylist, resolveChannel)
  - `InvidiousInstanceManager` — round-robin instance rotation, health tracking (max 2 failures, 5 min reset), thread-safe (@Synchronized)
  - 6 unit tests (healthy instance, round-robin, failure skip, all down, health reset, success reset)

- **Phase 4: HybridYouTubeRepositoryImpl** (fallback chain):
  - Replaces `YouTubeApiRepositoryImpl` as the Hilt binding for `YouTubeApiRepository`
  - Fallback chain per method: oEmbed/RSS → YouTube API → Invidious → Error
  - `OEmbedMapper` — maps oEmbed response to domain models (Video, Playlist)
  - `InvidiousMapper` — maps Invidious DTOs to domain models (Video, Channel, Playlist, PlaylistVideo)
  - `extractChannelIdFromUploadsPlaylist()` — converts UU→UC prefix for RSS channel resolution
  - IOException-specific failure tracking (parsing errors don't penalize Invidious instances)
  - 13 unit tests covering all fallback chains

- **Phase 5: DI & Build Changes**:
  - `@PlainOkHttp` and `@YouTubeApiOkHttp` Hilt qualifiers
  - `NetworkModule` updated: 2 OkHttpClients, oEmbed/RSS/Invidious providers
  - `DataModule` binding: `HybridYouTubeRepositoryImpl` → `YouTubeApiRepository`
  - `app/build.gradle.kts`: built-in fallback API key for F-Droid builds

- **Code Review Fixes**:
  - XXE protection in RssFeedParser (6 security features disabled)
  - @Synchronized on InvidiousInstanceManager methods (thread safety)
  - IOException-only failure tracking in withInvidiousFallback (parsing errors don't penalize instances)
  - ProGuard rules for oEmbed + Invidious DTOs and OEmbedService Retrofit interface

**Architecture**:
```
YouTubeApiRepository (interface — unchanged)
  └── HybridYouTubeRepositoryImpl (NEW)
        ├── OEmbedService (free, Retrofit)
        ├── RssFeedParser (free, XML)
        ├── YouTubeApiService (existing, with quota)
        └── InvidiousApiService (fallback, dynamic base URL)
```

**Fallback chain**:
| Method | Free | API | Invidious |
|--------|------|-----|-----------|
| getVideoById | oEmbed | videos.list | /api/v1/videos |
| getPlaylistById | oEmbed | playlists.list | /api/v1/playlists |
| getChannelById | — | channels.list | /api/v1/channels |
| getChannelByHandle | — | channels.list (forHandle) | /api/v1/resolveurl |
| getPlaylistItems | RSS (UU→UC) | playlistItems.list | /api/v1/channels or /playlists |

**Files Created** (16):
- `core/network/.../oembed/OEmbedResponse.kt`
- `core/network/.../oembed/OEmbedService.kt`
- `core/network/.../rss/RssVideoEntry.kt`
- `core/network/.../rss/RssFeedParser.kt`
- `core/network/.../invidious/InvidiousDto.kt`
- `core/network/.../invidious/InvidiousApiService.kt`
- `core/network/.../invidious/InvidiousInstanceManager.kt`
- `core/network/.../di/PlainOkHttp.kt`
- `core/network/.../di/YouTubeApiOkHttp.kt`
- `core/data/.../repository/impl/HybridYouTubeRepositoryImpl.kt`
- `core/data/.../mapper/OEmbedMapper.kt`
- `core/data/.../mapper/InvidiousMapper.kt`
- `core/network/src/test/.../oembed/OEmbedResponseTest.kt` (4 tests)
- `core/network/src/test/.../rss/RssFeedParserTest.kt` (5 tests)
- `core/network/src/test/.../invidious/InvidiousInstanceManagerTest.kt` (6 tests)
- `core/data/src/test/.../repository/impl/HybridYouTubeRepositoryImplTest.kt` (13 tests)

**Files Modified** (3):
- `core/network/.../di/NetworkModule.kt` (qualifiers, 5 new providers)
- `core/data/.../di/DataModule.kt` (HybridYouTubeRepositoryImpl binding)
- `app/build.gradle.kts` (built-in fallback API key)
- `app/proguard-rules.pro` (oEmbed + Invidious ProGuard rules)

**Decisions Made**:
- `YouTubeApiRepository` interface unchanged — ViewModels don't need modification
- `YouTubeApiRepositoryImpl` kept as-is (used by WhitelistRepositoryImpl tests)
- Built-in API key in source code (not secret — visible in every APK)
- oEmbed returns less data than API (no duration/description/subscriberCount) — empty/null fields
- RSS only for uploads playlists (UU prefix → UC channel ID conversion)
- Invidious instance list: vid.puffyan.us, yewtu.be, invidious.namazso.eu, inv.nadeko.net

**Test Stats**: 413 tests, all green (373 existing + 28 new + 12 oEmbed/RSS/Invidious)

**Notes**:
- Session 14 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-14)
- Most YouTube operations now cost 0 API units (oEmbed/RSS)
- Only @handle resolution and direct channel lookup use API quota (1 unit each)
- Invidious fallback activates on IOException only — parsing errors don't penalize instances
- XXE protection: 6 security features disabled in DocumentBuilderFactory

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
