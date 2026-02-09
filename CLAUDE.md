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
