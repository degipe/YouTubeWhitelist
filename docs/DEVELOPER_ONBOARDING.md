# YouTubeWhitelist - Developer Onboarding Guide

Welcome to the YouTubeWhitelist project! This guide will help you get up and running quickly as a contributor. It covers everything from environment setup to architecture patterns, testing conventions, and common pitfalls.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Prerequisites](#2-prerequisites)
3. [Environment Setup](#3-environment-setup)
4. [Project Structure](#4-project-structure)
5. [Architecture](#5-architecture)
6. [Module Deep Dive](#6-module-deep-dive)
7. [Navigation System](#7-navigation-system)
8. [Database Schema](#8-database-schema)
9. [Dependency Injection](#9-dependency-injection)
10. [Network Layer (YouTube API)](#10-network-layer-youtube-api)
11. [Authentication & Security](#11-authentication--security)
12. [State Management Patterns](#12-state-management-patterns)
13. [Testing Guide](#13-testing-guide)
14. [Build & Release](#14-build--release)
15. [Code Style & Conventions](#15-code-style--conventions)
16. [Common Pitfalls & Lessons Learned](#16-common-pitfalls--lessons-learned)
17. [Existing Documentation](#17-existing-documentation)
18. [Contribution Workflow](#18-contribution-workflow)

---

## 1. Project Overview

**YouTubeWhitelist** is a free, open-source (GPLv3) Android app that lets parents create safe YouTube environments for their kids. Parents whitelist specific channels, videos, and playlists — kids only see approved content.

**Key characteristics:**
- 100% client-side (no backend server)
- No ads, no tracking
- F-Droid compatible (no Google Play Services SDK dependency)
- Two user modes: **Parent Mode** (full YouTube browsing + management) and **Kid Mode** (whitelisted content only)

| Metric | Value |
|--------|-------|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Modules | 10 (1 app + 3 feature + 6 core) |
| Screens | 20 |
| Tests | 378+ |
| Release APK | 2.4 MB |
| License | GPLv3 |

---

## 2. Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| JDK | 17 | Required. Higher versions may cause Gradle issues |
| Android Studio | 2024.x+ | Or any IDE with Kotlin/Gradle support |
| Android SDK | API 35 | Install via SDK Manager |
| Git | Any recent | For version control |
| Google Cloud Console account | — | For YouTube API key and OAuth credentials |

### macOS-Specific Setup

```bash
# Install JDK 17 via Homebrew
brew install openjdk@17

# Set JAVA_HOME (add to ~/.zshrc or ~/.bashrc)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Install Android command-line tools (alternative to Android Studio)
brew install --cask android-commandlinetools
```

---

## 3. Environment Setup

### 3.1 Clone the Repository

```bash
git clone https://github.com/degipe/YouTubeWhitelist.git
cd YouTubeWhitelist
```

### 3.2 Create `local.properties`

This file is **git-ignored** and contains secrets. Create it in the project root:

```properties
# Android SDK location (auto-created by Android Studio)
sdk.dir=/path/to/your/android/sdk

# YouTube Data API v3 key (required for app functionality)
YOUTUBE_API_KEY=your_youtube_api_key_here

# Google OAuth 2.0 credentials (required for sign-in)
# Must be "Web application" type, NOT "Android" type
# Redirect URI: http://localhost/callback
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_client_secret

# Release signing (optional, only needed for release builds)
RELEASE_KEYSTORE_PATH=release-keystore.jks
RELEASE_KEYSTORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=release_key
RELEASE_KEY_PASSWORD=your_key_password
```

### 3.3 Google Cloud Console Setup

1. Create a new project at [Google Cloud Console](https://console.cloud.google.com/)
2. Enable **YouTube Data API v3**
3. Create an **API Key** (restrict to YouTube Data API v3 later)
4. Configure **OAuth consent screen** (External, Testing mode)
5. Create **OAuth 2.0 Client ID**:
   - Type: **Web application** (NOT Android!)
   - Authorized redirect URI: `http://localhost/callback`
6. Add test users to OAuth consent screen

For detailed instructions, see [GOOGLE_SETUP.md](../GOOGLE_SETUP.md).

### 3.4 Build and Run

```bash
# Build debug APK
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run instrumentation tests (requires emulator/device)
./gradlew connectedAndroidTest

# Build release APK (requires signing config in local.properties)
./gradlew assembleRelease

# Build AAB for Play Store
./gradlew bundleRelease
```

### 3.5 Install on Device/Emulator

```bash
# Via adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or run directly from Android Studio
# Select device → Run → app
```

---

## 4. Project Structure

```
YouTubeWhitelist/
├── app/                          # Main application module
│   ├── src/main/java/.../
│   │   ├── MainActivity.kt       # Single Activity, hosts Compose NavHost
│   │   ├── YouTubeWhitelistApp.kt # Hilt Application class
│   │   ├── navigation/
│   │   │   ├── Route.kt          # Type-safe navigation routes (sealed interface)
│   │   │   └── AppNavigation.kt  # NavHost with all composable routes
│   │   ├── di/
│   │   │   └── ApiKeyModule.kt   # Provides API keys from BuildConfig
│   │   └── ui/screen/            # App-level screens (splash, auth, PIN, profile)
│   └── src/test/                 # Unit tests for app-level ViewModels
│
├── feature/                      # Feature modules (UI layer)
│   ├── parent/                   # Parent mode screens
│   │   └── src/main/java/.../ui/
│   │       ├── dashboard/        # ParentDashboardScreen + ViewModel
│   │       ├── whitelist/        # WhitelistManagerScreen + ViewModel
│   │       ├── browser/          # WebViewBrowserScreen + ViewModel
│   │       ├── profile/          # ProfileEditScreen + ViewModel
│   │       ├── stats/            # WatchStatsScreen + ViewModel
│   │       ├── exportimport/     # ExportImportScreen + ViewModel
│   │       └── about/            # AboutScreen (static, no ViewModel)
│   │
│   ├── kid/                      # Kid mode screens
│   │   └── src/main/java/.../ui/
│   │       ├── home/             # KidHomeScreen + ViewModel
│   │       ├── channel/          # ChannelDetailScreen + ViewModel
│   │       ├── playlist/         # PlaylistDetailScreen + ViewModel
│   │       ├── player/           # VideoPlayerScreen + ViewModel
│   │       └── search/           # KidSearchScreen + ViewModel
│   │
│   └── sleep/                    # Sleep mode
│       └── src/main/java/.../ui/ # SleepModeScreen + ViewModel
│
├── core/                         # Core library modules (business logic + data)
│   ├── common/                   # Shared utilities, theme, types
│   │   └── src/main/java/.../
│   │       ├── result/           # AppResult<T> sealed interface
│   │       ├── model/            # WhitelistItemType enum
│   │       └── ui/theme/         # Material 3 theme (colors, typography)
│   │
│   ├── data/                     # Repository implementations, domain models
│   │   └── src/main/java/.../
│   │       ├── repository/       # 7 repository interfaces + implementations
│   │       ├── model/            # Domain models (WhitelistItem, KidProfile, etc.)
│   │       ├── sleep/            # SleepTimerManager
│   │       ├── timelimit/        # TimeLimitChecker
│   │       └── parser/           # YouTube URL parser
│   │
│   ├── database/                 # Room database
│   │   └── src/main/java/.../
│   │       ├── entity/           # 4 Room entities
│   │       ├── dao/              # 4 DAO interfaces (38 methods total)
│   │       ├── converter/        # Room type converters
│   │       └── di/               # DatabaseModule (Hilt)
│   │
│   ├── network/                  # YouTube API client
│   │   └── src/main/java/.../
│   │       ├── api/              # YouTubeApiService (Retrofit interface)
│   │       ├── dto/              # API response DTOs
│   │       ├── interceptor/      # ApiKeyInterceptor
│   │       └── di/               # NetworkModule (Hilt)
│   │
│   ├── auth/                     # Authentication & PIN management
│   │   └── src/main/java/.../
│   │       ├── repository/       # AuthRepository, PinRepository
│   │       ├── pin/              # PinHasher, BruteForceProtection
│   │       ├── token/            # EncryptedTokenManager
│   │       ├── signin/           # GoogleSignInManager, OAuthLoopbackServer
│   │       └── di/               # AuthModule (Hilt)
│   │
│   └── export/                   # JSON export/import
│       └── src/main/java/.../
│           ├── model/            # Export DTOs
│           └── service/          # ExportService, ImportService
│
├── gradle/
│   ├── wrapper/                  # Gradle wrapper (v8.11.1)
│   └── libs.versions.toml        # Version catalog (all dependencies centralized)
│
├── docs/                         # SDLC documentation
│   ├── BRD.md                    # Business Requirements Document
│   ├── FS.md                     # Functional Specification
│   ├── HLD.md                    # High-Level Design
│   └── LLD.md                    # Low-Level Design (948 lines, 18 Mermaid diagrams)
│
├── fastlane/metadata/android/    # F-Droid / Play Store metadata
│   ├── en-US/                    # English descriptions + changelogs
│   └── hu-HU/                    # Hungarian descriptions + changelogs
│
├── build.gradle.kts              # Root build file (plugin declarations)
├── settings.gradle.kts           # Module includes
├── CHANGELOG.md                  # Release notes (Keep a Changelog format)
├── README.md                     # Project overview
└── LICENSE                       # GPLv3
```

### Package Convention

All packages follow this pattern:
```
io.github.degipe.youtubewhitelist.<module>.<layer>
```

Examples:
- `io.github.degipe.youtubewhitelist.core.data.repository`
- `io.github.degipe.youtubewhitelist.feature.kid.ui.home`
- `io.github.degipe.youtubewhitelist.core.database.entity`

---

## 5. Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│  Presentation Layer (feature modules)               │
│  Compose Screens ← ViewModels ← UiState            │
├─────────────────────────────────────────────────────┤
│  Domain Layer (core:data)                           │
│  Repository interfaces, Domain models               │
├─────────────────────────────────────────────────────┤
│  Data Layer (core:database, core:network, core:auth)│
│  Room DAOs, Retrofit API, Token storage             │
└─────────────────────────────────────────────────────┘
```

### Module Dependency Graph

```
app ──────┬──→ feature:parent ──→ core:common
          ├──→ feature:kid    ──→ core:data ──→ core:database
          ├──→ feature:sleep  ──→ core:data ──→ core:network
          ├──→ core:common                  ──→ core:common
          ├──→ core:data
          ├──→ core:database
          ├──→ core:network
          ├──→ core:auth      ──→ core:database
          └──→ core:export    ──→ core:database (DAOs directly)
                              ──→ core:common
```

### Data Flow (MVVM)

```
User Action → Composable → ViewModel → Repository → DAO/API
                ↑                         ↓
            UiState ← StateFlow ← Flow<Entity> from Room
```

### Three-Layer Data Mapping

```
Network DTOs (core:network)  →  Domain Models (core:data)  →  Room Entities (core:database)
   ChannelDto                     YouTubeMetadata.Channel       WhitelistItemEntity
   VideoDto                       WhitelistItem                 KidProfileEntity
   PlaylistDto                    KidProfile                    WatchHistoryEntity
```

### Error Handling: AppResult<T>

All network/repository operations that can fail return `AppResult<T>`:

```kotlin
// Defined in core:common
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val exception: Throwable? = null) : AppResult<Nothing>
}

// Usage in repository
suspend fun addItemFromUrl(profileId: String, url: String): AppResult<WhitelistItem>

// Usage in ViewModel
when (val result = repository.addItemFromUrl(profileId, url)) {
    is AppResult.Success -> handleSuccess(result.data)
    is AppResult.Error -> showError(result.message)
}
```

---

## 6. Module Deep Dive

### app Module

The application entry point. Contains:
- `MainActivity` — Single Activity with `setContent { AppNavigation() }`
- `YouTubeWhitelistApp` — `@HiltAndroidApp` application class
- Navigation graph (`Route.kt`, `AppNavigation.kt`)
- App-level screens: Splash, SignIn, PIN Setup/Entry/Change, Profile Creation/Selector
- `ApiKeyModule` — Provides API keys from `BuildConfig` to Hilt

### feature:parent

Parent mode UI. All screens follow the same pattern:
- `*Screen.kt` — Composable function accepting ViewModel + navigation callbacks
- `*ViewModel.kt` — `@HiltViewModel` or `@AssistedInject` for runtime params

**Screens:** ParentDashboard, WhitelistManager, WebViewBrowser, ProfileEdit, WatchStats, ExportImport, About

### feature:kid

Kid mode UI. The safest layer — all navigation is blocked, content is filtered.

**Screens:** KidHome, KidSearch, ChannelDetail, PlaylistDetail, VideoPlayer

**Key security features in VideoPlayer:**
- `shouldOverrideUrlLoading` returns `true` (blocks ALL WebView navigation)
- YouTube IFrame Player error codes 101/150 auto-skip (embed-disabled videos)
- Sleep timer/time limit overlays pause video and block interaction

### feature:sleep

Sleep timer screen with dark theme. Three states: timer selection, running, expired.

### core:common

Shared across all modules:
- `AppResult<T>` sealed interface
- `WhitelistItemType` enum (CHANNEL, VIDEO, PLAYLIST)
- Material 3 theme (colors, typography, shapes)
- Shared composables

### core:data

The domain layer. Contains:
- 7 repository interfaces + implementations
- Domain models (`WhitelistItem`, `KidProfile`, `ParentAccount`, etc.)
- `YouTubeUrlParser` — Parses various YouTube URL formats
- `SleepTimerManager` — Background countdown timer (Singleton)
- `TimeLimitChecker` — Combines watch history with profile settings

### core:database

Room database layer:
- `YouTubeWhitelistDatabase` (version 2, 4 entities)
- 4 DAOs with 38 methods total
- Type converters for enums
- Composite indices for performance
- `fallbackToDestructiveMigration` (appropriate for pre-production)

### core:network

YouTube API client:
- `YouTubeApiService` — 5 Retrofit endpoints
- DTOs with `@Serializable` (kotlinx-serialization)
- `ApiKeyInterceptor` — Appends API key to all requests
- HTTP logging interceptor (debug builds only!)

### core:auth

Authentication subsystem:
- `GoogleSignInManager` — Chrome Custom Tabs OAuth flow
- `OAuthLoopbackServer` — Local HTTP server for OAuth redirect
- `EncryptedTokenManager` — AES-256-GCM token storage (Tink)
- `PinHasher` — PBKDF2 PIN hashing
- `BruteForceProtection` — Exponential lockout after failed attempts

### core:export

JSON backup/restore:
- Uses DAOs directly (avoids circular dependency with core:data)
- Import generates new UUIDs (prevents PK conflicts)
- Two strategies: Merge or Overwrite

---

## 7. Navigation System

### Type-Safe Routes

Navigation uses Kotlin Serialization for type-safe route parameters:

```kotlin
// Route.kt
@Serializable
sealed interface Route {
    @Serializable data object Splash : Route
    @Serializable data object SignIn : Route
    @Serializable data class KidHome(val profileId: String) : Route
    @Serializable data class VideoPlayer(
        val profileId: String,
        val videoId: String,
        val videoTitle: String = "",
        val channelTitle: String? = null
    ) : Route
    // ... 18 routes total
}
```

### Navigation Graph

```
Splash (entry)
├─ First Run → SignIn → PinSetup → ProfileCreation → KidHome
├─ Returning User (single profile) → KidHome
└─ Returning User (multiple profiles) → ProfileSelector → KidHome

KidHome
├─ Search icon → KidSearch
├─ Channel card → ChannelDetail → VideoPlayer
├─ Video card → VideoPlayer
├─ Playlist card → PlaylistDetail → VideoPlayer
└─ Lock FAB → PinEntry → ParentDashboard

ParentDashboard (requires PIN)
├─ Manage Whitelist → WhitelistManager
├─ Browse YouTube → WebViewBrowser
├─ Sleep Mode → SleepModeScreen → KidHome (after timer starts)
├─ Edit Profile → ProfileEdit
├─ Watch Stats → WatchStats
├─ Export/Import → ExportImport
├─ Create Profile → ProfileCreation
├─ Change PIN → PinChange
├─ About → About
└─ Back to Kid Mode → KidHome
```

### ViewModels with Runtime Parameters

ViewModels that need runtime parameters (like `profileId`) use Hilt's `AssistedInject`:

```kotlin
@HiltViewModel(assistedFactory = KidHomeViewModel.Factory::class)
class KidHomeViewModel @AssistedInject constructor(
    whitelistRepository: WhitelistRepository,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): KidHomeViewModel
    }
}

// In AppNavigation.kt
val viewModel: KidHomeViewModel =
    hiltViewModel<KidHomeViewModel, KidHomeViewModel.Factory> { factory ->
        factory.create(route.profileId)
    }
```

---

## 8. Database Schema

### Entity Relationship

```
ParentAccount (1) ──→ (N) KidProfile (1) ──→ (N) WhitelistItem
                                    (1) ──→ (N) WatchHistory
```

### Entities

| Entity | Table | Primary Key | Foreign Keys | Key Indices |
|--------|-------|-------------|--------------|-------------|
| `ParentAccountEntity` | `parent_accounts` | `id` (UUID) | — | — |
| `KidProfileEntity` | `kid_profiles` | `id` (UUID) | `parentAccountId` → `parent_accounts.id` (CASCADE) | `parentAccountId` |
| `WhitelistItemEntity` | `whitelist_items` | `id` (UUID) | `kidProfileId` → `kid_profiles.id` (CASCADE) | `kidProfileId`, `kidProfileId+type`, `kidProfileId+youtubeId` (UNIQUE) |
| `WatchHistoryEntity` | `watch_history` | `id` (UUID) | `kidProfileId` → `kid_profiles.id` (CASCADE) | `kidProfileId`, `kidProfileId+watchedAt` |

### Key Database Design Decisions

- **Composite unique index** on `(kidProfileId, youtubeId)` in whitelist items prevents duplicates at DB level
- **CASCADE delete** on foreign keys: deleting a profile removes all its whitelist items and watch history
- **UUID primary keys**: Generated via `java.util.UUID.randomUUID().toString()`
- **Version 2** with `fallbackToDestructiveMigration()` — acceptable for pre-production

---

## 9. Dependency Injection

### Hilt Module Organization

| Module | Location | Scope | Responsibility |
|--------|----------|-------|----------------|
| `ApiKeyModule` | `app/di/` | `SingletonComponent` | API keys from BuildConfig |
| `DatabaseModule` | `core:database/di/` | `SingletonComponent` | Room database + DAOs |
| `NetworkModule` | `core:network/di/` | `SingletonComponent` | OkHttp, Retrofit, API service |
| `AuthModule` | `core:auth/di/` | `SingletonComponent` | Auth repos, PIN hasher, token manager |
| `DataModule` | `core:data/di/` | `SingletonComponent` | Domain repositories, SleepTimerManager |
| `DispatcherModule` | `core:common/di/` | `SingletonComponent` | Coroutine dispatchers |
| `ExportModule` | `core:export/di/` | `SingletonComponent` | Export/import services |

### Custom Qualifiers

```kotlin
@Qualifier annotation class YouTubeApiKey      // YouTube Data API v3 key
@Qualifier annotation class GoogleClientId     // OAuth client ID
@Qualifier annotation class GoogleClientSecret // OAuth client secret
```

### Binding Pattern

Repositories use `@Binds` for interface-to-implementation mapping:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton
    abstract fun bindWhitelistRepository(
        impl: WhitelistRepositoryImpl
    ): WhitelistRepository
}
```

---

## 10. Network Layer (YouTube API)

### API Service (Retrofit)

```kotlin
interface YouTubeApiService {
    @GET("channels")   suspend fun getChannels(...): Response<YouTubeListResponse<ChannelDto>>
    @GET("videos")     suspend fun getVideos(...): Response<YouTubeListResponse<VideoDto>>
    @GET("playlists")  suspend fun getPlaylists(...): Response<YouTubeListResponse<PlaylistDto>>
    @GET("playlistItems") suspend fun getPlaylistItems(...): Response<YouTubeListResponse<PlaylistItemDto>>
    @GET("search")     suspend fun search(...): Response<YouTubeListResponse<SearchResultDto>>
}
```

### API Quota Management

YouTube Data API v3 has a **10,000 units/day** quota:

| Endpoint | Cost | Notes |
|----------|------|-------|
| `channels.list` | 1 unit | Per channel lookup |
| `videos.list` | 1 unit | Per video lookup |
| `playlists.list` | 1 unit | Per playlist lookup |
| `playlistItems.list` | 1 unit | Per page (50 items) |
| `search.list` | **100 units** | Expensive! |

**Quota protection measures:**
- Check for duplicates BEFORE making API calls
- Channel video search limited to max 3 channels per query (300 units max)
- No unnecessary re-fetches

### OkHttp Configuration

- `ApiKeyInterceptor` appends `key=API_KEY` query parameter to all requests
- HTTP logging interceptor added **only in debug builds** (API key appears in URL!)
- Base URL: `https://www.googleapis.com/youtube/v3/`

### URL Parser

`YouTubeUrlParser` handles all YouTube URL formats:
- `youtube.com/watch?v=VIDEO_ID`
- `youtu.be/VIDEO_ID`
- `youtube.com/@handle`
- `youtube.com/channel/CHANNEL_ID`
- `youtube.com/c/CustomName` (resolved via `forHandle` API)
- `youtube.com/playlist?list=PLAYLIST_ID`

---

## 11. Authentication & Security

### OAuth Flow

```
1. User taps "Sign In"
2. App starts OAuthLoopbackServer (local HTTP server on random port)
3. Chrome Custom Tab opens Google OAuth consent page
4. User authenticates with Google
5. Google redirects to http://localhost:{port}/callback?code=AUTH_CODE
6. OAuthLoopbackServer captures the authorization code
7. App exchanges code for access_token + refresh_token
8. Tokens stored in EncryptedSharedPreferences (AES-256-GCM)
```

**Why Chrome Custom Tabs, not WebView?**
Google has blocked OAuth in embedded WebViews since 2016. Chrome Custom Tabs is the only F-Droid-compatible option.

### PIN Security

- **Hashing:** PBKDF2WithHmacSHA256 (not plaintext)
- **Brute force protection:** Exponential lockout after 5 failed attempts
  - 5 failures → 15 min lockout
  - 10 failures → 30 min lockout
  - Increases with repeated failures

### WebView Security

All WebViews in the app have security hardening:

```kotlin
webView.settings.apply {
    allowFileAccess = false
    allowContentAccess = false
    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
    safeBrowsingEnabled = true
}
```

**Player WebViews** additionally block ALL navigation:
```kotlin
webViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(...) = true  // Block everything
}
```

### Screen Pinning (Kiosk Mode)

- `Activity.startLockTask()` when entering Kid Mode
- `Activity.stopLockTask()` when parent verifies PIN
- Prevents Home/Recent button usage

---

## 12. State Management Patterns

### Pattern 1: Reactive Multi-Source State

Used when UI depends on multiple Room Flow sources:

```kotlin
val uiState: StateFlow<UiState> = combine(
    repository.getChannels(profileId),
    repository.getVideos(profileId),
    repository.getPlaylists(profileId)
) { channels, videos, playlists ->
    UiState(channels = channels, videos = videos, playlists = playlists)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,  // Always active (required for tests)
    initialValue = UiState()
)
```

### Pattern 2: Nested combine() for 6+ Flows

Kotlin's `combine()` supports max 5 flows. For more, nest them:

```kotlin
combine(
    flow1, flow2, flow3, flow4,
    combine(flow5, flow6) { a, b -> a to b }
) { f1, f2, f3, f4, (f5, f6) ->
    // Build state from all 6 sources
}
```

### Pattern 3: Debounced Search

```kotlin
private val _query = MutableStateFlow("")
val query: StateFlow<String> = _query.asStateFlow()  // Non-debounced for TextField

val results: StateFlow<List<Item>> = _query
    .debounce(300)
    .flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList())
        else repository.search(profileId, query)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

**Important:** TextField must use the non-debounced `query` state. Using debounced state causes the input to reset during the delay window.

### Pattern 4: One-Shot API Calls

For operations that aren't reactive (e.g., sign-in):

```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

fun signIn() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        when (val result = authRepository.signIn()) {
            is AppResult.Success -> _uiState.update { it.copy(isSuccess = true) }
            is AppResult.Error -> _uiState.update { it.copy(error = result.message) }
        }
    }
}
```

### Pattern 5: Fire-and-Forget with Job Tracking

When mixing reactive Flow collection with one-shot API calls:

```kotlin
private var searchJob: Job? = null

val results: StateFlow<List<Item>> = _query
    .debounce(300)
    .flatMapLatest { query ->
        searchJob?.cancel()  // Cancel previous API call
        searchJob = viewModelScope.launch {
            // Fire-and-forget API call that updates _apiResults
        }
        combine(localResults, _apiResults) { local, api -> local + api }
    }
    .stateIn(...)
```

---

## 13. Testing Guide

### Test Stack

| Library | Purpose |
|---------|---------|
| JUnit 4 | Test framework |
| MockK | Mocking (Kotlin-friendly) |
| Coroutines Test | `runTest`, `advanceUntilIdle()`, `advanceTimeBy()` |
| Turbine | Flow testing DSL |
| Truth | Fluent assertions |
| Robolectric | Android framework in JVM tests |

### Running Tests

```bash
# All unit tests
./gradlew test

# Specific module
./gradlew :core:data:test
./gradlew :feature:kid:test

# With test output
./gradlew test --info

# Instrumentation tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Test Structure Convention

Tests mirror the source structure:

```
src/main/java/.../repository/WhitelistRepositoryImpl.kt
src/test/java/.../repository/WhitelistRepositoryImplTest.kt

src/main/java/.../ui/home/KidHomeViewModel.kt
src/test/java/.../ui/home/KidHomeViewModelTest.kt
```

### Writing ViewModel Tests

```kotlin
class KidHomeViewModelTest {
    // MockK mocks
    private val whitelistRepository = mockk<WhitelistRepository>()
    private val kidProfileRepository = mockk<KidProfileRepository>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()  // Replaces Dispatchers.Main

    @Before
    fun setup() {
        // Setup mock return values
        every { whitelistRepository.getChannelsByProfile(any()) } returns flowOf(emptyList())
        // ...
    }

    @Test
    fun `when profile has channels, uiState shows them`() = runTest {
        val channels = listOf(testChannel())
        every { whitelistRepository.getChannelsByProfile("profile1") } returns flowOf(channels)

        val viewModel = createViewModel(profileId = "profile1")

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.channels).isEqualTo(channels)
    }
}
```

### Writing Repository Tests

```kotlin
class WhitelistRepositoryImplTest {
    private val whitelistItemDao = mockk<WhitelistItemDao>()
    private val youTubeApiService = mockk<YouTubeApiService>()

    private lateinit var repository: WhitelistRepositoryImpl

    @Before
    fun setup() {
        repository = WhitelistRepositoryImpl(whitelistItemDao, youTubeApiService, ...)
    }

    @Test
    fun `addItemFromUrl with valid video URL adds item`() = runTest {
        coEvery { youTubeApiService.getVideos(any(), any()) } returns Response.success(videoResponse)
        coEvery { whitelistItemDao.getByProfileAndYoutubeId(any(), any()) } returns null
        coEvery { whitelistItemDao.insert(any()) } returns Unit

        val result = repository.addItemFromUrl("profile1", "https://youtube.com/watch?v=abc123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
    }
}
```

### Testing Flow with Turbine

```kotlin
@Test
fun `items flow emits updated list when item added`() = runTest {
    val items = MutableSharedFlow<List<WhitelistItem>>()
    every { dao.getByProfile(any()) } returns items

    repository.getItemsByProfile("profile1").test {
        items.emit(emptyList())
        assertThat(awaitItem()).isEmpty()

        items.emit(listOf(testItem()))
        assertThat(awaitItem()).hasSize(1)
    }
}
```

### Important Testing Notes

- **Use `SharingStarted.Eagerly`** in ViewModels — `WhileSubscribed` breaks tests without active subscribers
- **Use `advanceTimeBy(n)` for time-sensitive tests** — `advanceUntilIdle()` processes ALL pending delays (including timers/debounces)
- **`advanceTimeBy(N)` is boundary-exclusive** — add `+1` ms for inclusive boundary tick
- **MockK relaxed mocks return non-null objects for nullable return types** — always explicitly mock `returns null` when needed
- **Use `java.util.Base64`** (not `android.util.Base64`) in plain JUnit tests
- **`android.net.Uri`** throws `RuntimeException` in JVM tests — use `java.net.URLEncoder` instead
- **`org.json.JSONObject`** requires Robolectric (Android SDK class)

---

## 14. Build & Release

### Build Variants

| Variant | `isMinifyEnabled` | `isShrinkResources` | Signing | App ID Suffix |
|---------|-------------------|---------------------|---------|---------------|
| `debug` | false | false | Debug keystore | `.debug` |
| `release` | true (R8) | true | Release keystore | — |

### Version Management

In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 1        // Increment for every Play Store upload
    versionName = "1.0.0"  // Semantic versioning
}
```

### Dependency Management

All dependencies are centralized in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.1.0"
composeBom = "2025.01.01"
hilt = "2.53.1"
room = "2.7.0"
# ...

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
# ...

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
# ...
```

Usage in `build.gradle.kts`:
```kotlin
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)
```

### ProGuard/R8 Rules

Key rules in `app/proguard-rules.pro`:
- Keep all `@Serializable` classes and their `$$serializer` companions
- Keep Navigation Compose `Route` sealed interface + subclasses
- Keep export DTOs and YouTube API DTOs
- Keep `@JavascriptInterface` methods (WebView bridges)
- Keep Retrofit service interface methods and annotations
- Keep Room entities and DAOs

### Release Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (for F-Droid / GitHub Releases)
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk (2.4 MB)

# Release AAB (for Google Play Store)
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab (5.2 MB)
```

---

## 15. Code Style & Conventions

### Naming

- **Screens:** `*Screen.kt` — Top-level `@Composable` function
- **ViewModels:** `*ViewModel.kt` — `@HiltViewModel` class
- **UiState:** `*UiState` — data class inside ViewModel file
- **Repositories:** `*Repository.kt` (interface) + `*RepositoryImpl.kt` (implementation)
- **Entities:** `*Entity.kt` — Room entity
- **DAOs:** `*Dao.kt` — Room DAO interface
- **DTOs:** `*Dto.kt` — Network response classes
- **Tests:** `*Test.kt` — Test class

### Architecture Rules

1. **Feature modules depend on core modules, never on each other**
2. **core:data depends on core:database and core:network** (repository pattern)
3. **core:export depends on core:database directly** (avoids circular deps with core:data)
4. **Domain models live in core:data** (not in core:database or core:network)
5. **app module wires everything together** (navigation, DI root)

### Kotlin Style

- **KSP** for annotation processing (not kapt)
- **kotlinx-serialization** for JSON (not Gson/Moshi)
- `sealed interface` for type-safe discriminated unions (`AppResult`, `Route`)
- `data class` for immutable state objects
- `Flow<T>` for reactive data streams from Room
- `StateFlow<T>` for ViewModel state exposed to UI

---

## 16. Common Pitfalls & Lessons Learned

### Android/JVM Test Compatibility

| Class | JVM Test | Solution |
|-------|----------|----------|
| `android.util.Base64` | Crashes | Use `java.util.Base64` |
| `android.net.Uri` | Crashes | Use `java.net.URLEncoder` |
| `org.json.JSONObject` | Crashes | Use Robolectric or kotlinx-serialization |

### Flow & Coroutines

- **`flatMapLatest`** cancels the previous inner Flow but NOT standalone `launch` Jobs — track Jobs manually
- **`combine()` supports max 5 flows** — nest combines for 6+
- **`stateIn(SharingStarted.Eagerly)`** is required for ViewModels used in tests
- **`advanceUntilIdle()`** processes ALL delays including timers — use `advanceTimeBy()` for precision

### WebView

- **`loadUrl("about:blank") + stopLoading() + clearHistory() + destroy()`** for proper cleanup
- **`CookieManager.flush()`** before WebView destroy to persist cookies
- **`setAcceptThirdPartyCookies(webView, true)`** needed for YouTube login persistence
- **DisposableEffect key should match content ID** (e.g., `youtubeId`) for cleanup on navigation
- **WebView ref: use `mutableStateOf<WebView?>(null)`**, NOT `mutableListOf` (accumulates on recomposition)

### YouTube API

- **Check duplicates BEFORE API calls** — saves quota
- **YouTube Search API costs 100 units/call** — limit channel searches to max 3 per query
- **`/c/CustomName` URLs** → use `forHandle` API (YouTube maps these to `@handles`)
- **URL-decode query parameters** in URL parser using `java.net.URLDecoder`

### Hilt/DI

- **Multiple `@Assisted String` params need `@Assisted("identifier")`** to disambiguate
- **`@Keep` annotation on JavaScript bridge classes** to survive R8 in release builds
- **Android library modules need `buildFeatures { buildConfig = true }`** to access `BuildConfig.DEBUG`

### Room

- **Always check for existing account before insert** to prevent cascade FK deletion
- **Domain models must include ALL entity fields** (e.g., `sleepPlaylistId`) to prevent data loss on update
- **Composite unique index** enforces DB-level duplicate prevention

---

## 17. Existing Documentation

| Document | Location | Content |
|----------|----------|---------|
| README.md | Project root | Quick start, features, building |
| CHANGELOG.md | Project root | Release notes (Keep a Changelog) |
| GOOGLE_SETUP.md | Project root | GCP Console setup instructions |
| SIDELOADING.md | Project root | APK installation via adb |
| STORE_LISTING.md | Project root | Play Store / F-Droid descriptions |
| BRD.md | `docs/` | Business Requirements Document |
| FS.md | `docs/` | Functional Specification (18 screens, 15 FRs) |
| HLD.md | `docs/` | High-Level Design (architecture, tech stack, 8 Mermaid diagrams) |
| LLD.md | `docs/` | Low-Level Design (948 lines, components, 18 Mermaid diagrams) |
| ARCHITECTURE.md | Project root | Session archive index |

**Recommended reading order for new developers:**
1. This onboarding guide (you're here)
2. `docs/HLD.md` — Understand the big picture
3. `docs/LLD.md` — Detailed component specifications
4. `docs/FS.md` — Functional requirements and screen inventory

---

## 18. Contribution Workflow

### Development Process

1. **Understand the task** — Read relevant docs and code
2. **Write tests first** (TDD) — This project follows test-driven development
3. **Implement** — Write the minimum code to pass tests
4. **Review** — Self-review for security, performance, and correctness
5. **Test on device** — Run on emulator or physical device
6. **Commit** — Clear commit message describing the change

### Adding a New Screen

1. Define the route in `Route.kt`:
   ```kotlin
   @Serializable data class NewScreen(val param: String) : Route
   ```

2. Create ViewModel in the appropriate feature module:
   ```kotlin
   @HiltViewModel(assistedFactory = NewScreenViewModel.Factory::class)
   class NewScreenViewModel @AssistedInject constructor(
       repository: SomeRepository,
       @Assisted private val param: String
   ) : ViewModel() { ... }
   ```

3. Create Screen composable:
   ```kotlin
   @Composable
   fun NewScreen(viewModel: NewScreenViewModel, onNavigateBack: () -> Unit) { ... }
   ```

4. Add route to `AppNavigation.kt`:
   ```kotlin
   composable<Route.NewScreen> { backStackEntry ->
       val route = backStackEntry.toRoute<Route.NewScreen>()
       val viewModel = hiltViewModel<NewScreenViewModel, NewScreenViewModel.Factory> {
           it.create(route.param)
       }
       NewScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
   }
   ```

5. Write tests for the ViewModel
6. Update ProGuard rules if new serializable classes are added

### Adding a New Repository

1. Define interface in `core:data/repository/`
2. Create implementation in `core:data/repository/impl/`
3. Add `@Binds` to `DataModule`
4. Write tests
5. Inject via Hilt in ViewModels

### Key Files to Always Check

When making changes, ensure these stay consistent:
- `Route.kt` — Navigation routes
- `AppNavigation.kt` — Route wiring
- `proguard-rules.pro` — R8 keep rules for new serializable/reflective classes
- `libs.versions.toml` — Dependency versions
- `CHANGELOG.md` — Document notable changes

---

## Quick Reference Card

```
Build debug:          ./gradlew assembleDebug
Build release APK:    ./gradlew assembleRelease
Build release AAB:    ./gradlew bundleRelease
Run all tests:        ./gradlew test
Run module tests:     ./gradlew :feature:kid:test
Clean build:          ./gradlew clean

Package:              io.github.degipe.youtubewhitelist
Min SDK:              26
Target SDK:           35
JDK:                  17
Gradle:               8.11.1 (wrapper)
Kotlin:               2.1.0

Main entry:           app/src/main/.../MainActivity.kt
Navigation:           app/src/main/.../navigation/Route.kt
Database:             core/database/src/main/.../YouTubeWhitelistDatabase.kt
API Service:          core/network/src/main/.../api/YouTubeApiService.kt
Dependencies:         gradle/libs.versions.toml
Secrets:              local.properties (git-ignored)
```
