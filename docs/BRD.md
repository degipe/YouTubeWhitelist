# Business Requirements Document (BRD)

**Project**: YouTubeWhitelist
**Version**: 1.1.0
**Last Updated**: 2026-02-10

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Objectives](#2-business-objectives)
3. [Target Users](#3-target-users)
4. [Business Requirements](#4-business-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Constraints & Assumptions](#6-constraints--assumptions)
7. [Success Metrics](#7-success-metrics)
8. [Glossary](#8-glossary)

---

## 1. Executive Summary

### Problem Statement

Parents face a fundamental challenge with YouTube: the platform's recommendation algorithm and vast content library make it impossible to ensure children only watch age-appropriate content. Existing parental controls (YouTube Kids, Restricted Mode) use automated filtering that is unreliable — inappropriate content regularly bypasses these filters, while safe educational content is often incorrectly blocked.

### Solution

**YouTubeWhitelist** is a free, open-source Android application that gives parents complete control over what YouTube content their children can access. Instead of relying on automated filters, parents manually curate a whitelist of specific channels, videos, and playlists. Children see only this curated content — nothing else.

### Value Proposition

| For | Value |
|-----|-------|
| **Parents** | Peace of mind — children can only watch content the parent has explicitly approved |
| **Children** | Safe, distraction-free YouTube experience with familiar content |
| **Privacy-conscious users** | 100% client-side, no data collection, no tracking, open-source |
| **Cost-conscious users** | Completely free, no subscriptions, no in-app purchases |

---

## 2. Business Objectives

| ID | Objective | Description |
|----|-----------|-------------|
| **BO-01** | Safe YouTube environment | Ensure children can only access parent-approved YouTube content |
| **BO-02** | Full parental control | Give parents granular control over content, time limits, and usage monitoring |
| **BO-03** | Privacy-first design | No data leaves the device — no analytics, no tracking, no backend server |
| **BO-04** | Free and open-source | GPLv3 license, no monetization through user data or subscriptions |
| **BO-05** | Platform independence | No dependency on Google Play Services SDK — works on F-Droid and de-Googled devices |

---

## 3. Target Users

### Primary: Parents with children aged 3–12

- **Demographics**: Parents, guardians, caregivers of young children
- **Technical level**: Basic smartphone users — no technical expertise required
- **Motivation**: Want children to enjoy YouTube safely without constant supervision
- **Pain points**: YouTube Kids filtering is unreliable; YouTube's algorithm recommends unsuitable content; no way to create a truly custom safe space

### Secondary: Educators and childcare providers

- **Use case**: Classroom or daycare settings where specific educational YouTube content is needed
- **Motivation**: Curate educational playlists per child/group
- **Pain points**: Cannot control what YouTube shows after the intended video ends

---

## 4. Business Requirements

### BR-01: Content Whitelisting

Parents must be able to curate a personalized safe list of YouTube content.

| ID | Requirement |
|----|------------|
| BR-01.1 | Support whitelisting individual YouTube videos |
| BR-01.2 | Support whitelisting entire YouTube channels (all current and future uploads) |
| BR-01.3 | Support whitelisting YouTube playlists |
| BR-01.4 | Provide multiple methods to add content: URL input and in-app browsing |
| BR-01.5 | Display content metadata (title, thumbnail, channel name) for easy identification |
| BR-01.6 | Prevent duplicate entries per child profile |

### BR-02: Dual Mode Operation

The app must support two distinct usage modes with clear separation.

| ID | Requirement |
|----|------------|
| BR-02.1 | **Parent Mode**: Full access to YouTube, whitelist management, and settings |
| BR-02.2 | **Kid Mode**: Restricted view showing only whitelisted content |
| BR-02.3 | Switching from Kid Mode to Parent Mode requires PIN authentication |
| BR-02.4 | Kid Mode must not expose any settings, management, or external links |

### BR-03: Multi-Child Support

The app must support families with multiple children with different content needs.

| ID | Requirement |
|----|------------|
| BR-03.1 | Support creating multiple child profiles under one parent account |
| BR-03.2 | Each profile has an independent whitelist |
| BR-03.3 | Each profile has independent watch history and statistics |
| BR-03.4 | Each profile has independently configurable time limits |

### BR-04: Usage Controls

Parents must have tools to manage how much and when children watch.

| ID | Requirement |
|----|------------|
| BR-04.1 | Configurable daily time limit per profile (in minutes) |
| BR-04.2 | Real-time tracking of time spent watching |
| BR-04.3 | Automatic enforcement when limit is reached (block further playback) |
| BR-04.4 | Configurable sleep timer with automatic shutdown |

### BR-05: Usage Monitoring

Parents must have visibility into viewing habits.

| ID | Requirement |
|----|------------|
| BR-05.1 | Track watch history per profile |
| BR-05.2 | Provide daily, weekly, and monthly watch time breakdowns |
| BR-05.3 | Show number of unique videos watched per time period |

### BR-06: Data Portability

Users must be able to back up and transfer their configuration.

| ID | Requirement |
|----|------------|
| BR-06.1 | Export all profiles and whitelist items to a standard format (JSON) |
| BR-06.2 | Import configurations from exported files |
| BR-06.3 | Support merge and overwrite import strategies |
| BR-06.4 | Export format must be human-readable and version-controlled |

### BR-07: Kiosk Mode

The app must prevent children from leaving the app without parent permission.

| ID | Requirement |
|----|------------|
| BR-07.1 | Activate Android screen pinning to lock the child in the app |
| BR-07.2 | Disable system navigation buttons (Home, Recent) |
| BR-07.3 | Block all WebView navigation to prevent escaping via external links |

### BR-08: Cross-Store Distribution

The app must be available across multiple distribution channels.

| ID | Requirement |
|----|------------|
| BR-08.1 | Publish on Google Play Store |
| BR-08.2 | Publish on F-Droid (free and open-source app store) |
| BR-08.3 | Provide direct APK download via GitHub Releases |
| BR-08.4 | No dependency on proprietary SDKs that would block F-Droid inclusion |

---

## 5. Non-Functional Requirements

| ID | Category | Requirement | Target |
|----|----------|-------------|--------|
| **NFR-01** | Performance | App cold launch time | < 2 seconds |
| **NFR-02** | Performance | UI scrolling smoothness | 60 fps (no jank) |
| **NFR-03** | Security | Token storage encryption | AES-256-GCM (Tink) |
| **NFR-04** | Security | PIN hashing | PBKDF2, 120k iterations, 256-bit |
| **NFR-05** | Security | Brute force protection | Exponential lockout after 5 attempts |
| **NFR-06** | Security | WebView sandboxing | No file/content access, no navigation |
| **NFR-07** | Privacy | Data collection | Zero — no analytics, no tracking, no telemetry |
| **NFR-08** | Privacy | Network communication | Only YouTube Data API v3 + Google OAuth |
| **NFR-09** | Privacy | Data location | 100% on-device, no cloud sync |
| **NFR-10** | Reliability | Offline capability | Cached content accessible without network |
| **NFR-11** | Reliability | API error handling | Graceful degradation, never crash |
| **NFR-12** | Compatibility | Minimum Android version | 8.0 (API 26) — covers 98%+ of active devices |
| **NFR-13** | Compatibility | Target Android version | 15 (API 35) |
| **NFR-14** | Size | APK file size | < 5 MB (actual: 2.4 MB) |
| **NFR-15** | Accessibility | Font scaling | Supports system font size preferences |
| **NFR-16** | Accessibility | Design system | Material Design 3 with dynamic colors (Android 12+) |
| **NFR-17** | Maintainability | Test coverage | 378+ unit tests, all passing |
| **NFR-18** | Maintainability | Architecture | Multi-module, Clean Architecture |

---

## 6. Constraints & Assumptions

### Technical Constraints

| Constraint | Impact | Mitigation |
|-----------|--------|------------|
| YouTube Data API v3 quota: 10,000 units/day | Limits number of API calls | Hybrid strategy: oEmbed/RSS free endpoints for most operations; kid search is local-only (0 quota); Invidious fallback; built-in API key |
| Google blocks OAuth in embedded WebViews | Cannot use WebView for sign-in | Chrome Custom Tabs (standard browser approach) |
| YouTube IFrame Player API for playback | No native player control | JavaScript bridges for player events |
| No backend server | No push notifications, no cloud sync | All data local; export/import for data portability |
| F-Droid prohibits non-free dependencies | Cannot use Google Play Services SDK | Custom OAuth implementation with Chrome Custom Tabs |

### Assumptions

| Assumption | Rationale |
|-----------|-----------|
| Parents have a Google account | Required for OAuth authentication |
| Device has a Chrome-compatible browser installed | Required for Chrome Custom Tabs OAuth flow |
| Device has internet access for initial setup and content resolution | API calls required to fetch YouTube metadata |
| Parents will manually curate content | Core design philosophy — no automated recommendations |
| Content is relatively stable | Whitelist items reference YouTube IDs which rarely change |

---

## 7. Success Metrics

### Technical Metrics (v1.0.0)

| Metric | Target | Actual |
|--------|--------|--------|
| Unit tests | Comprehensive coverage | 401+ tests, all green |
| Release APK size | < 5 MB | 2.4 MB |
| Release AAB size | < 10 MB | 5.2 MB |
| Feature completeness | All M1–M6 milestones | 100% complete |
| Supported URL formats | All major YouTube URL patterns | 10+ patterns |
| Database entities | Match PRD data model | 5 entities (4 domain + 1 cache) |
| Module count | Clean Architecture separation | 10 modules |

### User-Facing Metrics (post-launch)

| Metric | Measurement Method |
|--------|-------------------|
| Installation count | Play Store + F-Droid analytics |
| Crash-free sessions | Play Console vitals |
| User retention (7-day) | Play Console |
| GitHub stars | GitHub repository |
| Community contributions | Pull requests + issues |

---

## 8. Glossary

| Term | Definition |
|------|-----------|
| **Whitelist** | A curated list of approved YouTube content (channels, videos, playlists) that a child is allowed to access |
| **Profile** | A child-specific configuration containing an independent whitelist, watch history, time limits, and sleep timer settings |
| **Parent Mode** | The authenticated administrative mode where parents manage whitelists, profiles, and settings |
| **Kid Mode** | The restricted mode where children can only view and interact with whitelisted content |
| **PIN** | A numeric passcode (4+ digits) used to protect Parent Mode access |
| **Kiosk Mode** | Android Screen Pinning feature that prevents a child from leaving the app |
| **Sleep Timer** | A countdown timer that blocks further viewing when it expires, displaying a "Good Night" overlay |
| **Daily Time Limit** | A per-profile limit on total daily watch time, after which a "Time's Up" overlay appears |
| **IFrame Player** | YouTube's JavaScript-based video player embedded in a WebView |
| **Chrome Custom Tabs** | A lightweight browser interface used for OAuth sign-in (separate from the app's WebViews) |
| **F-Droid** | A catalog of free and open-source Android applications |
| **YouTube Data API v3** | Google's REST API for querying YouTube metadata (channels, videos, playlists, search) |
| **PBKDF2** | Password-Based Key Derivation Function 2 — a cryptographic hashing algorithm used for PIN storage |
| **AppResult\<T\>** | A sealed interface pattern (Success/Error) used for type-safe error handling in the codebase |
| **Ko-fi** | A donation platform used to accept voluntary contributions from users |
