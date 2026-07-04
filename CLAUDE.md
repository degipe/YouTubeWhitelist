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
- **Auth**: Chrome Custom Tabs OAuth 2.0 with loopback redirect + PKCE, no client secret (F-Droid compatible, no Google Play Services SDK); plus a "continue without Google account" local-account path
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
- **Room migration rule**: Any `@Database version` bump in `core:database` REQUIRES a real `Migration` appended to `Migrations.ALL` (`core/database/.../migration/Migrations.kt`) plus a migration test in `core/database/src/androidTest/.../migration/MigrationTest.kt`. `fallbackToDestructiveMigration()` stays wired as a crash-prevention safety net only — it is NOT a substitute for a real migration (it wipes all user data).

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

### Session 25 - 2026-07-04: Audit Remediation — All 20 Deep-Analysis Findings Fixed

**Objectives**: Deep analysis of the entire codebase + SDLC specs (4 parallel review agents), then fix every finding (4 critical, 6 high, plus security/quality/docs) using subagent-driven TDD.

**Process**: Full remediation plan at `docs/superpowers/plans/2026-07-03-audit-remediation.md` (5 sessions, 20 tasks). Executed subagent-driven: fresh implementer per task (TDD, RED→GREEN), two-stage review (spec + quality) per task, fix-loops where reviews caught issues, final whole-branch review (verdict: READY TO MERGE). All work on branch `audit-remediation` → **PR #3**. Progress ledger at `.superpowers/sdd/progress.md`.

**Completed** (by theme):
- **Critical data-safety + broken player**:
  - Kid player WebView now reloads on video change via `key(youtubeId)` — fixes Next / autoplay / embed-error (101/150) auto-skip going blank.
  - Export/import MERGE now truly dedups by profile name (no duplicates on re-import); whole import wrapped in a single `database.withTransaction {}` (no data loss on mid-import failure).
  - Room schema export (`room.schemaLocation`) + `Migrations.ALL` container + committed `3.json` baseline + instrumented `MigrationTest` template; `fallbackToDestructiveMigration()` retained only as a safety net. Migration rule documented in Development Principles.
- **Kid-mode correctness + content safety**:
  - `YouTubeId` validator + gating in `YouTubeUrlParser` + import validation + `JSONObject.quote` in player HTML → closes a JS-injection hole in the kid player (defense in depth: parse + import + render).
  - API-first channel pagination (removed RSS ~15-video cap; `tryRssFeedPaginated` deleted).
  - `loadMoreFailed` flag + retry affordance; infinite-scroll gated on search (no API-quota waste during search).
  - Daily time-limit midnight rollover computed in SQL (`strftime('%s','now','localtime','start of day','utc')*1000`) — the `'utc'` modifier was a review-caught correctness fix (the plan's original SQL was off by the UTC offset in every non-UTC zone).
- **Auth / security hardening**:
  - OAuth loopback bound to `getLoopbackAddress()` + `state` CSRF validation (checked before code use).
  - PKCE (S256) via `PkceGenerator`; embedded client secret fully removed (buildConfig field + `@GoogleClientSecret` qualifier + DI provider all deleted).
  - Kid player WebView hardening flags (allowFileAccess/allowContentAccess=false, MIXED_CONTENT_NEVER_ALLOW, safeBrowsing); `allowBackup=false`.
  - "Continue without Google account" onboarding (`continueWithoutGoogle()` creates a local ParentAccount, same PinSetup destination) + blank-clientId fail-fast guard → unblocks F-Droid onboarding.
- **Network robustness + cleanup**:
  - `closeOnError()` helper — response bodies closed on all 16 Retrofit error paths + 2 raw OkHttp sites (`.use{}`) → connection-leak fix.
  - Invidious list refreshed to curl-verified-live instances (`yewtu.be`, `inv.nadeko.net`, `iv.melmac.space`; removed dead `vid.puffyan.us`, `invidious.namazso.eu`).
  - Dead code removed (~1000+ lines): unbound `YouTubeApiRepositoryImpl`, API `searchVideosInChannel`, unused DAO method. Unused deps removed (WorkManager, biometric) + `USE_BIOMETRIC` permission + dead version-catalog entries.
- **CI + hygiene + docs**:
  - GitHub Actions CI (`.github/workflows/ci.yml`): unit tests + lint + debug build on push/PR.
  - Gradle heap 4g + `-XX:+UseParallelGC` + local build cache.
  - Docs truth-up: Invidious disclosure in both privacy files, OAuth/PKCE wording (CHANGELOG/CLAUDE.md), deferred-feature flags (biometric/WorkManager in PRD), removed stale `GOOGLE_CLIENT_SECRET` refs from HLD/LLD/DEVELOPER_ONBOARDING, README API-key marked optional.

**Decisions Made**:
- Subagent-driven TDD with per-task two-stage review; Sessions 5.2/5.3 done inline by controller after an API session limit interrupted subagent dispatch.
- No-account onboarding path chosen over baked client ID (investigation confirmed the app functions with a local ParentAccount — whitelist/kid features are auth-decoupled).
- API key rotation + Console OAuth client creation left as documented USER steps (`docs/SESSION3_MANUAL_STEPS.md`), per user's "code + guide" preference.

**Manual steps pending (USER)**: see `docs/SESSION3_MANUAL_STEPS.md` — create Desktop/PKCE OAuth client + bake fallback ID; restrict+rotate YouTube API key; emulator verification (player reload, large-channel scroll, OAuth sign-in, no-account onboarding, hardened-WebView playback).

**Follow-ups (non-blocking)**: `getDailyWatchTime` (stats) uses UTC day-bucketing vs local-midnight enforcement (can disagree near midnight); dead `YouTubeApiService.search`; minor test-robustness notes (in `.superpowers/sdd/progress.md`).

**Test Stats**: full suite green (886 tests). Final whole-branch review: READY TO MERGE.

**Notes**:
- The review loop caught a real bug the plan introduced (the midnight-rollover SQL) — evidence that adversarial per-task review earns its keep.
- Session 20 archived to CLAUDE_ARCHIVE_2.md (now contains sessions 11-20).
