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
  - `searchVideosInChannel(channelId, query)` added to `YouTubeApiRepository` — uses YouTube Search API (`search.list`, `channelId` + `q` filter, max 10 results)
  - `getChannelYoutubeIds(profileId)` added to `WhitelistRepository` — delegates to existing DAO `getYoutubeIdsByType`
  - `KidSearchViewModel` refactored: `combine()` merges local DB results + API channel video results via `_channelVideoResults` StateFlow
  - `searchChannels()` fires as side-effect in `flatMapLatest`, runs in separate coroutine Job (cancellable)
  - Max 3 channels searched (quota protection: 100 units/search, 10k daily limit)
  - `distinctBy { it.youtubeId }` deduplicates local + API results
  - API errors silently skipped — local results always shown
  - +5 YouTubeApiRepo tests + 5 KidSearchViewModel tests

**Decisions Made**:
- Reuse `PlaylistVideo` model for search results (position=0) — avoids new model class
- Max 3 channel API searches per query — quota protection (300 units max per search)
- `_channelVideoResults` MutableStateFlow + `combine()` inside `flatMapLatest` — clean separation of local (reactive) + API (one-shot) search
- `searchChannels()` as fire-and-forget coroutine with explicit Job cancellation on new query
- API search results mapped to `WhitelistItem` with `id = "search-${videoId}"` prefix — prevents LazyColumn key conflicts

**Files Modified**:
- `core/data/.../repository/YouTubeApiRepository.kt` (+1 method)
- `core/data/.../repository/impl/YouTubeApiRepositoryImpl.kt` (+searchVideosInChannel impl)
- `core/data/.../repository/WhitelistRepository.kt` (+1 method)
- `core/data/.../repository/impl/WhitelistRepositoryImpl.kt` (+getChannelYoutubeIds impl)
- `core/database/.../dao/WhitelistItemDao.kt` (searchItems SQL updated for channelTitle)
- `feature/kid/.../search/KidSearchViewModel.kt` (major: +YouTubeApiRepository dep, channel search logic)
- `feature/kid/.../search/KidSearchScreen.kt` (TextField uses immediate query state)
- `feature/kid/.../home/KidHomeViewModel.kt` (+SleepTimerManager, nested combine)
- `feature/kid/.../home/KidHomeScreen.kt` (channel grid fix + "Good Night" overlay)
- `feature/kid/.../player/VideoPlayerViewModel.kt` (+SleepTimerManager, observeSleepTimer)
- `feature/kid/.../player/VideoPlayerScreen.kt` (fullscreen exit + pause + overlay)
- `feature/parent/.../whitelist/WhitelistManagerScreen.kt` (FAB → inline button)
- `feature/parent/.../dashboard/ParentDashboardScreen.kt` (subtitle text)
- `app/.../navigation/AppNavigation.kt` (SleepTimerManager wiring)
- `app/.../MainActivity.kt` (+SleepTimerManager injection)

**Test Files Modified**:
- `core/data/src/test/.../YouTubeApiRepositoryImplTest.kt` (+5 searchVideosInChannel tests)
- `feature/kid/src/test/.../search/KidSearchViewModelTest.kt` (+7 tests: 2 query sync + 5 channel search)
- `feature/kid/src/test/.../home/KidHomeViewModelTest.kt` (+4 sleep timer tests)
- `feature/kid/src/test/.../player/VideoPlayerViewModelTest.kt` (+3 sleep timer tests)

**Test Stats**: 378+ tests, all green (355 existing + 7 sleep + 2 query + 5 search repo + 5 search VM + ~4 others)

**Notes**:
- YouTube Search API costs 100 units per call — with 3 channels max, each search uses up to 300 units (10k daily limit)
- Channel video search results appear as VIDEO type WhitelistItems with `id = "search-..."` prefix
- Sleep timer now works as background countdown — overlay appears on KidHome and VideoPlayer when expired
- `combine()` inside `flatMapLatest` is cancelled when new query arrives — clean cancellation
- Session 8 archived to CLAUDE_ARCHIVE_1.md (now contains sessions 1-8)

**Next Session Focus**: Store submission + final polish (GitHub Release, Privacy Policy, Play Store screenshots, API key restriction, F-Droid submission).

### Session 14 - 2026-02-10: Reverse SDLC Documentation

**Objectives**: Generate complete SDLC documentation from codebase (reverse engineering) for professional GitHub presentation.

**Completed**:
- **LLD.md (Low-Level Design)** — 948 lines, 5 Mermaid diagrams:
  - Module dependency graph (all 10 modules with arrows)
  - ER diagram (4 entities with relationships, PKs, FKs, indices)
  - All 4 DAO method inventories (38 methods total)
  - 7 repository interfaces with full Kotlin method signatures
  - URL parsing flow diagram (two-phase duplicate detection)
  - YouTube API Service (5 endpoints with parameters + quota table)
  - Complete DTO hierarchy (ChannelDto, VideoDto, PlaylistDto, SearchResultDto, ThumbnailSet)
  - OkHttp config (ApiKeyInterceptor, debug-only logging)
  - OAuth 2.0 sequence diagram (Chrome Custom Tabs + Loopback Server)
  - PIN security (PBKDF2 config table + brute force lockout schedule)
  - Token storage (EncryptedSharedPreferences, AES256-GCM)
  - All 7 Hilt modules + 7 qualifier annotations + AssistedInject pattern
  - SleepTimerManager state machine diagram (IDLE → RUNNING → EXPIRED)
  - TimeLimitChecker combine pattern
  - Export/Import JSON schema + strategies
  - WebView architecture (player + browser, security settings)
  - ProGuard/R8 rules summary table
  - Key design patterns (AppResult, Flow combining, debounce+flatMapLatest, manual Job tracking)

- **HLD.md (High-Level Design)** — 470 lines, 8 Mermaid diagrams:
  - System architecture overview (layered: Presentation → Domain → Data → External)
  - Module architecture graph with responsibility matrix
  - Technology stack table (20+ libraries with versions)
  - MVVM data flow diagram (Compose → ViewModel → Repository → Room/API)
  - Navigation screen flow (20 screens, type-safe routes)
  - Security architecture (auth layers, data protection)
  - Data storage strategy (Room, ESP, SharedPreferences, CookieManager, BuildConfig)
  - YouTube API integration with quota management
  - Build & release pipeline (debug/release, dual APK+AAB)
  - Error handling strategy (AppResult pattern, layer-by-layer)

- **FS.md (Functional Specification)** — 463 lines, 5 Mermaid diagrams:
  - Application overview + 2 user roles (Parent/Kid)
  - 18-screen inventory table (route, ViewModel, module)
  - 15 functional requirements (FR-01 through FR-15)
  - 5 user flow diagrams (first-time setup, returning user, video playback, whitelist management, search)
  - 6 UiState specifications (field tables per ViewModel)
  - Validation rules + error handling & user feedback tables

- **BRD.md (Business Requirements Document)** — 257 lines:
  - Executive summary (problem, solution, value proposition)
  - 5 business objectives + target users
  - 8 business requirements with sub-requirements
  - 18 non-functional requirements (performance, security, privacy, reliability)
  - Constraints & assumptions tables
  - Success metrics (technical + post-launch)
  - Glossary (15 terms)

- **Mermaid Diagram Validation**:
  - All 18 diagrams extracted and validated with `mermaid-cli` (`mmdc`)
  - 18/18 rendered successfully — zero syntax errors
  - Verified no sensitive data (API keys, passwords) in any document

**Files Created**:
- `docs/LLD.md` (948 lines)
- `docs/HLD.md` (470 lines)
- `docs/FS.md` (463 lines)
- `docs/BRD.md` (257 lines)

**Test Stats**: 378+ tests, all green (no changes to code — documentation only session)

**Notes**:
- Total: 2138 lines of documentation, 18 Mermaid diagrams across 3 files
- Documents cross-reference each other (e.g., HLD → LLD §10)
- GitHub renders Mermaid diagrams natively — no additional tooling needed
- Codebase explored with 6 parallel agents (DB, network, auth, repositories, DI, navigation/VMs)
- Session 9 archived to CLAUDE_ARCHIVE_1.md (now contains sessions 1-9)

**Next Session Focus**: Store submission + final polish (GitHub Release, Privacy Policy, Play Store screenshots, API key restriction, F-Droid submission).

### Session 15 - 2026-02-10: SDLC Documentation Completion (Onboarding, User Manual, PRD Translation)

**Objectives**: Create comprehensive developer onboarding guide, detailed user manual, and translate Hungarian PRD to English markdown.

**Completed**:
- **Developer Onboarding Guide** (`docs/DEVELOPER_ONBOARDING.md` — ~750 lines):
  - 18 chapters covering full developer ramp-up
  - Prerequisites, environment setup (macOS + Android SDK + JDK 17)
  - Project structure walkthrough (all 10 modules with responsibilities)
  - Architecture deep-dive (MVVM + Clean Architecture layers)
  - Module-by-module guide with key classes and patterns
  - Navigation system (type-safe routes, 18 screens)
  - Database schema (4 entities, relationships, DAOs)
  - Hilt DI setup (7 modules, qualifier annotations, AssistedInject)
  - YouTube API integration (5 endpoints, quota management, ApiKeyInterceptor)
  - Auth & security (OAuth 2.0 flow, PIN/PBKDF2, EncryptedSharedPreferences, WebView security)
  - 5 state management patterns with code examples
  - Testing guide (378+ tests, JUnit + MockK + Truth + Turbine patterns)
  - Build & release (debug/release, APK/AAB, ProGuard, signing)
  - Code conventions, common pitfalls, contribution workflow, quick reference card

- **User Manual** (`docs/USER_MANUAL.md` — ~500 lines):
  - 15 chapters covering all app functionality for end users
  - Installation (Play Store, F-Droid, GitHub, sideloading)
  - First-time setup (PIN creation, profile, Google sign-in)
  - Kid Mode (home screen, video player, channels, playlists, search)
  - Parent Mode (dashboard, whitelist management, Browse YouTube)
  - Profiles, daily time limits, sleep mode
  - Watch statistics, export/import
  - PIN management, kiosk mode
  - FAQ (15+ questions), troubleshooting, privacy & security

- **PRD Translation** (`docs/PRD.md` — ~732 lines):
  - Full English translation of Hungarian PRD v1.1
  - 19 sections: executive summary, problem description, goals/KPIs, personas, user stories, 35 functional requirements, NFRs, UI/UX, technical architecture, data model, API integrations, security, legal/compliance, store listing, competitor analysis, risks, timeline, testing, future development
  - Original `YouTubeWhitelist_PRD_v1.1.docx` deleted (no longer needed)
  - pandoc installed via Homebrew for docx→md conversion

**Decisions Made**:
- Developer onboarding structured as progressive learning path (setup → architecture → modules → patterns → testing)
- User manual organized by user role (kid mode → parent mode → advanced features)
- PRD kept as faithful translation — markdown version is the canonical reference
- Original docx deleted after translation

**Files Created**:
- `docs/DEVELOPER_ONBOARDING.md` (~750 lines)
- `docs/USER_MANUAL.md` (~500 lines)
- `docs/PRD.md` (~732 lines)

**Files Deleted**:
- `YouTubeWhitelist_PRD_v1.1.docx` (replaced by docs/PRD.md)
- `~$uTubeWhitelist_PRD_v1.1.docx` (Word temp file)

**Files Modified**:
- `CLAUDE.md` (PRD reference updated, Session 10 archived, Session 15 added)
- `CLAUDE_ARCHIVE_1.md` (Session 10 archived, now contains sessions 1-10)
- `ARCHITECTURE.md` (Session 15 entry + archive update)
- `NEXT_SESSION_PROMPT.md` (updated for Session 16)

**Test Stats**: 378+ tests, all green (no changes to code — documentation only session)

**Notes**:
- Total documentation suite: BRD, FS, HLD, LLD, PRD, Developer Onboarding, User Manual (7 docs in docs/)
- pandoc v3.9 installed via Homebrew for docx conversion
- Explored codebase with 2 parallel agents for comprehensive documentation
- Session 10 archived to CLAUDE_ARCHIVE_1.md (now contains sessions 1-10)

**Next Session Focus**: Store submission + final polish (GitHub Release + v1.0.0 tag, Privacy Policy, Play Store screenshots, API key restriction, F-Droid submission).

### Session 16 - 2026-02-10: Store Submission + Final Polish

**Objectives**: GitHub Release v1.0.0, Privacy Policy, Play Store screenshots, API key restriction, F-Droid submission, Play Store guide.

**Completed**:
- **Privacy Policy** (GitHub Pages + repo):
  - `docs/privacy-policy.md` — Jekyll version with front matter, served at `https://degipe.github.io/YouTubeWhitelist/privacy-policy/`
  - `docs/PRIVACY_POLICY.md` — repo-visible markdown version
  - `docs/_config.yml` — Jekyll config (minima theme, baseurl)
  - GitHub Pages enabled from `/docs` folder on main branch

- **AboutScreen v1.0.0 Update**:
  - Version bumped from v0.1.0 to v1.0.0
  - Added Privacy Policy section with clickable link to GitHub Pages URL

- **GitHub Release v1.0.0**:
  - Created annotated git tag `v1.0.0`
  - GitHub Release with CHANGELOG content + release APK attached
  - URL: https://github.com/degipe/YouTubeWhitelist/releases/tag/v1.0.0

- **API Key Restriction (GCP Console via Playwright)**:
  - Application restriction: Android apps (package `io.github.degipe.youtubewhitelist` + SHA-1 `B7:4F:49:A4:26:83:7B:EE:A6:D0:11:63:AB:1E:18:F1:8C:7E:05:81`)
  - API restriction: YouTube Data API v3 only
  - OAuth consent screen: Published from Testing → Production mode

- **F-Droid Submission**:
  - RFP issue content prepared in `/tmp/fdroid-rfp-issue.md`
  - GitLab blocked Playwright (Cloudflare) — content provided for manual submission

- **Play Store Screenshots** (5 screenshots, 1080x2400):
  - `01_profile_selector.png` — "Who's watching?" with Emma/Max profiles
  - `02_kid_home.png` — Kid Home with channels (thumbnails), videos, playlists, time limit
  - `03_pin_entry.png` — 6-digit PIN entry screen
  - `04_parent_dashboard.png` — Parent Dashboard with all actions
  - `05_whitelist_manager.png` — Whitelist Manager with content list + thumbnails
  - Saved to `fastlane/metadata/android/en-US/images/phoneScreenshots/`

- **Play Store Submission Guide** (`docs/PLAY_STORE_SUBMISSION.md`):
  - Step-by-step guide: app creation, store listing, content rating, data safety, release upload
  - References existing fastlane metadata for descriptions

- **Archive**: Session 11 archived to CLAUDE_ARCHIVE_2.md (sessions 11+)

**Decisions Made**:
- Privacy Policy hosted on GitHub Pages (Jekyll minima theme) + repo copy
- API key fully restricted to Android + YouTube Data API v3 only
- OAuth consent screen published to Production (no longer Testing-only)
- Screenshots taken on emulator with debug build (visually identical to release)
- Competitor apps (WhitelistVideo) pose no legal risk — different name, approach, and license

**Files Created**:
- `docs/privacy-policy.md` (GitHub Pages)
- `docs/PRIVACY_POLICY.md` (repo)
- `docs/_config.yml` (Jekyll)
- `docs/PLAY_STORE_SUBMISSION.md`
- `CLAUDE_ARCHIVE_2.md` (Session 11)
- `fastlane/metadata/android/en-US/images/phoneScreenshots/01-05_*.png`

**Files Modified**:
- `feature/parent/.../about/AboutScreen.kt` (v1.0.0 + Privacy Policy link)
- `CLAUDE.md` (Session 11 archived, Session 16 added)
- `ARCHITECTURE.md` (Session 16 entry + archive update)
- `NEXT_SESSION_PROMPT.md` (updated for Session 17)

**Test Stats**: 378+ tests, all green (no code changes affecting tests)

**Notes**:
- GitHub Release: https://github.com/degipe/YouTubeWhitelist/releases/tag/v1.0.0
- Privacy Policy: https://degipe.github.io/YouTubeWhitelist/privacy-policy/
- API key restricted: Android only + YouTube Data API v3 — curl from macOS returns 403
- Emulator screenshots required: normal GPU mode (not swiftshader), `exec-out screencap -p` instead of `screencap -p /sdcard/`, real thumbnail URLs in DB (fake yt3.ggpht.com URLs return 400)
- F-Droid GitLab submission requires manual paste (Cloudflare blocks automation)

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
