# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-02-11

### Added
- **Hybrid API Strategy**: oEmbed + RSS free endpoints reduce YouTube API quota usage by ~95%
- **Invidious Fallback**: Automatic failover to Invidious instances when YouTube API is unavailable
- **Built-in API Key**: F-Droid builds now work out of the box without `local.properties`
- **Channel Lazy Loading**: Infinite scroll for channel videos (50 per page, Room cache as single source of truth)
- **In-Channel Search**: Search within cached channel videos (Room SQL LIKE, 0 API quota)
- **Channel Video Cache**: New `CachedChannelVideoEntity` with Room DB persistence

### Changed
- **Kid Search**: Now local-only (Room DB) — removed YouTube Search API dependency (0 quota vs 100-300 units/query)
- **Network Architecture**: `HybridYouTubeRepositoryImpl` replaces direct YouTube API calls with fallback chain
- **Room Database**: Version 2 → 3 (5 entities, fallbackToDestructiveMigration)
- **OkHttp**: Dual client setup — `@PlainOkHttp` (oEmbed/RSS) and `@YouTubeApiOkHttp` (YouTube API with key)

### Security
- XXE protection in RSS feed parser (6 security features disabled in DocumentBuilderFactory)
- ProGuard rules for oEmbed + Invidious DTOs

## [1.0.0] - 2026-02-09

### Added
- **Parent Mode**: Full YouTube browsing via WebView with whitelist management
- **Kid Mode**: Clean, distraction-free interface showing only whitelisted content
- **PIN Protection**: Secure parent mode access with PIN code and brute-force protection
- **Multiple Profiles**: Create separate kid profiles with individual whitelists
- **Whitelist Management**: Add YouTube channels, videos, and playlists to per-profile whitelists
- **YouTube URL Parsing**: Automatic detection of channels, videos, and playlists from URLs
- **YouTube Data API Integration**: Channel/video/playlist metadata fetching via YouTube Data API v3
- **Daily Time Limits**: Per-profile configurable daily watch time limits (15-180 minutes)
- **Sleep Mode**: Timer-based playback (15/30/45/60 min) with gradual volume fade-out
- **Watch Statistics**: Daily, weekly, and monthly watch time tracking with visual charts
- **Kiosk Mode**: Android screen pinning to keep kids inside the app
- **Search**: In-app search within whitelisted content with debounced results
- **Playlist Support**: Full playlist browsing with video list from YouTube API
- **Export/Import**: JSON-based backup and restore of profiles and whitelists
- **About Screen**: App info, license (GPLv3), GitHub link, Ko-fi donation support
- **WebView OAuth 2.0**: F-Droid compatible Google Sign-In (no Google Play Services SDK)
- **Image Loading**: Coil-based thumbnail loading with disk and memory caching
- **Room Database**: Local SQLite storage with composite indices for performance
- **Material Design 3**: Modern Android UI with Jetpack Compose
