# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
