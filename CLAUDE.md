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
