# Product Requirements Document

## YouTubeWhitelist

*Whitelist-based YouTube client for kids*

*Free software - Open source - Community project*

---

| | |
|---|---|
| **Version** | 1.1 |
| **Date** | February 9, 2026 |
| **Author** | Peter (Product Owner) |
| **Status** | Draft - Awaiting review |
| **Platform** | Android (API 26+) |
| **License** | GPLv3 (open source) |
| **Source Code** | github.com/degipe/YouTubeWhitelist |

---

## Table of Contents

1. Executive Summary
2. Problem Description
3. Goals and Success Criteria
4. User Personas
5. User Stories
6. Functional Requirements
7. Non-Functional Requirements
8. UI/UX Design
9. Technical Architecture
10. Data Model
11. API Integrations
12. Security Requirements
13. Legal and Compliance Considerations
14. Play Store Listing
15. Competitor Analysis
16. Risks and Mitigation
17. Timeline and Milestones
18. Testing Strategy
19. Future Development

---

## 1. Executive Summary

YouTubeWhitelist is a free, open-source Android application that allows parents to control which YouTube content their children can watch using a whitelist-based approach. The application operates in two modes: parent mode (full YouTube browsing and whitelist management) and kid mode (only whitelisted content is accessible).

The app is entirely client-side -- there is no backend server infrastructure, no user data collection, and no subscription model. Settings and whitelists are stored locally on the device and can be transferred to other devices via the export/import feature.

In contrast to YouTube Kids' limitations -- where many quality content (e.g., lo-fi music channels, educational videos) is unavailable while paradoxically plenty of so-called "brainrot" content is allowed through -- YouTubeWhitelist gives the parent full control. The parent is signed in with their own YouTube account; if they are a YouTube Premium subscriber, playback is ad-free.

---

## 2. Problem Description

### 2.1 Current Situation

Parents' current options for controlling their children's YouTube usage are limited, and none of them provide true content-level control:

- **YouTube Kids:** Algorithmic content curation that filters out many high-quality content (e.g., lo-fi music, specific educational channels) while allowing through many questionable, so-called "brainrot" content (repetitive, meaningless, hyper-stimulating videos). Parents have no way to add custom channels or videos.

- **Google Family Link:** App-level restrictions, but incapable of video-level whitelisting.

- **Regular YouTube app:** No content restrictions whatsoever; the recommendation algorithm can lead the child anywhere.

### 2.2 Concrete Example

A 6-year-old child likes to listen to lo-fi music to fall asleep (e.g., @chillchilljournal, @lofiailurophile, @kittylofichill channels). These are not available in YouTube Kids because they are not tagged as "kids' content." The parent either allows full YouTube access (risky) or withholds the content (poor experience).

### 2.3 The "Brainrot" Problem

YouTube Kids' algorithm creates paradoxical situations: genuinely valuable content (educational videos, calming music, quality cartoons) is filtered out because it doesn't belong to YouTube's "made for kids" category. Meanwhile, plenty of repetitive, hyper-stimulating, meaninglessly padded videos pass through the filter. YouTubeWhitelist addresses this problem at its root: no content is available to the child until the parent has explicitly approved it.

---

## 3. Goals and Success Criteria

### 3.1 Primary Goals

- The parent should have full control over the YouTube content accessible to their child.
- The child should only see and play approved content.
- A safe, simple, child-friendly user interface.
- Sleep mode with a built-in timer and fade-out function.
- Completely free and open source -- no hidden costs, no server dependency.

### 3.2 Success Criteria (KPIs)

| KPI | Target Value | Timeframe |
|-----|-------------|-----------|
| Whitelist setup time | < 30 sec / channel | MVP |
| Kid mode launch | < 2 taps | MVP |
| GitHub stars | 500+ | 6 months |
| Play Store rating | > 4.5 / 5 | 6 months |
| Active installs | 5000+ | 12 months |

---

## 4. User Personas

### 4.1 Parent ("Anna")

- 34 years old, two children (ages 4 and 7)
- YouTube Premium subscriber
- Technologically proficient but not a developer
- Wants to control what her kids watch but doesn't want to ban everything
- Frustrated: YouTube Kids lacks good content but is full of brainrot

### 4.2 Parent ("Peter")

- Highly tech-savvy, open-source advocate
- Doesn't want to send user data to any server
- Willing to sideload if necessary
- Expectation: the app should be transparent, auditable, and not depend on any backend

### 4.3 Child ("Mark")

- 6 years old, uses a tablet
- Likes listening to lo-fi music before sleep
- Favorite channels: videos about animals, cartoons, music
- Requires simple navigation with large buttons
- Doesn't see and doesn't know that any content exists beyond the whitelist

---

## 5. User Stories

### 5.1 Parent Stories

| ID | Priority | Story | Acceptance Criteria |
|----|----------|-------|---------------------|
| **US-P01** | Must | As a parent, I want to enter a PIN-protected parent mode so my child cannot access the settings. | 4-6 digit PIN, optional biometric |
| **US-P02** | Must | As a parent, I want to whitelist an entire YouTube channel with a single tap. | All current and future videos from the channel are included |
| **US-P03** | Must | As a parent, I want to whitelist an individual video. | URL paste or "+" button while browsing |
| **US-P04** | Should | As a parent, I want to whitelist a YouTube playlist. | All videos in the playlist become available |
| **US-P05** | Must | As a parent, I want to browse YouTube in parent mode and easily whitelist content. | WebView + overlay whitelist button |
| **US-P06** | Should | As a parent, I want to manage multiple kid profiles with separate whitelists. | Profile switching within the app |
| **US-P07** | Must | As a parent, I want to set a time limit for kid mode. | Daily limit in minutes |
| **US-P08** | Could | As a parent, I want to see what content my child watched and for how long. | Simple usage statistics |
| **US-P09** | Must | As a parent, I want to sign in with my Google account so my Premium subscription is recognized. | OAuth 2.0 Google sign-in |
| **US-P10** | Should | As a parent, I want to export the entire configuration (profiles, whitelists, settings) and import it on another device. | JSON export/import file from the parent dashboard |

### 5.2 Child Stories

| ID | Priority | Story | Acceptance Criteria |
|----|----------|-------|---------------------|
| **US-K01** | Must | As a child, I want to see my favorite channels in large, colorful icons on the home screen. | Channel grid view with circular thumbnails |
| **US-K02** | Must | As a child, I want to search within my approved content. | Search only within whitelisted content |
| **US-K03** | Must | As a child, I want to use sleep mode, which plays music with a dark screen and timer. | Timer + fade-out + dark UI |

---

## 6. Functional Requirements

### 6.1 Authentication and Account Management

- **FR-01:** Google OAuth 2.0 sign-in with the parent's YouTube account. After sign-in, the app utilizes the user's YouTube Premium status (if applicable).

- **FR-02:** PIN code setup (4-6 digits) at first launch. Can be changed later in parent mode.

- **FR-03:** Optional biometric authentication (fingerprint / face) alongside the PIN code.

- **FR-04:** Multiple kid profile support, each with its own whitelist, icon, and name.

### 6.2 Parent Mode

- **FR-10:** Full YouTube browsing experience in a WebView, with the parent's signed-in account.

- **FR-11:** Floating "Whitelist" button (FAB) on every page, which adds the current video or channel to the selected kid profile's whitelist.

- **FR-12:** Whitelist management interface: lists all whitelisted channels, playlists, and videos. Delete, edit, assign to profile.

- **FR-13:** Quick add via URL paste: a YouTube video, channel, or playlist URL can be pasted directly.

- **FR-14:** Time limit setting per profile: daily viewing limit in minutes. Warning 5 minutes before expiration.

- **FR-15:** Usage statistics: which profile watched what, for how long, when. Stored locally.

### 6.3 Export / Import

- **FR-16:** Export: from the parent dashboard, all configuration (profiles, whitelists, settings) can be exported to a single JSON file.

- **FR-17:** Import: JSON file import from the parent dashboard. Offers merge (add to existing) and replace (full overwrite) options.

- **FR-18:** The export file can be shared via Android share sheet (email, Drive, Bluetooth, etc.).

### 6.4 Kid Mode

- **FR-20:** Home screen: whitelisted channels in grid view, with circular channel icons and names.

- **FR-21:** Channel view: the given channel's whitelisted videos in chronological order (newest first).

- **FR-22:** Search: text search exclusively among whitelisted content titles and descriptions. No results may appear from outside the whitelist.

- **FR-23:** Video playback: via YouTube IFrame/Embed Player API. No comment section, no recommended videos, no sidebar.

- **FR-24:** Automatic next video: plays the channel's next whitelisted video, NOT YouTube's recommendation.

- **FR-25:** No external navigation: the child cannot exit kid mode, cannot open a browser, cannot access other apps.

- **FR-26:** In kid mode, there is no indication that other content exists -- the whitelisted content is the child's entire "world."

### 6.5 Sleep Mode

- **FR-30:** Activation: dedicated "Sleep" button on the kid mode home screen (moon icon).

- **FR-31:** Timer setup: 15 / 30 / 45 / 60 / 90 minutes, or "infinite" (plays until stopped).

- **FR-32:** UI transition: the screen darkens (brightness minimum), only a minimal controller remains visible.

- **FR-33:** Fade-out: 60 seconds before the timer expires, volume gradually decreases to zero, then playback stops.

- **FR-34:** Hardware button handling: volume buttons work, but the back/home button does not stop playback.

- **FR-35:** Content source: the parent can designate a "sleep playlist" per profile, or the child can choose any whitelisted content.

---

## 7. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| **NFR-01** | Performance | App launch < 3 sec, video playback should start < 2 sec (on stable internet). |
| **NFR-02** | Offline Operation | Whitelisted content lists are viewable offline. Playback requires internet. |
| **NFR-03** | Scalability | Max 500 whitelisted items / profile, max 10 profiles / device. |
| **NFR-04** | Compatibility | Android 8.0 (API 26) and above. Support: phone and tablet. |
| **NFR-05** | Accessibility | Large buttons (min 48dp), high-contrast colors, TalkBack compatibility. |
| **NFR-06** | Data Privacy | The app sends no data to any server. Everything is stored locally. |
| **NFR-07** | Localization | MVP: Hungarian and English. Expandable with community translations. |
| **NFR-08** | Server Independence | The app is 100% client-side. No backend, no account system, no telemetry. |

---

## 8. UI/UX Design

### 8.1 Navigation Structure

The application operates in two completely separated modes. Switching between modes is only possible via PIN code / biometric authentication.

**Parent Mode Screens**

- Dashboard: summary (profiles, statistics, quick actions, export/import)
- YouTube Browser: full YouTube WebView + floating whitelist FAB
- Whitelist Manager: channels, playlists, videos list per profile
- Profile Manager: creating and editing kid profiles
- Settings: PIN change, time limits, sleep mode configuration, export/import

**Kid Mode Screens**

- Home Screen: channel grid (large circular icons, 2-3 columns) + Sleep button
- Channel View: video thumbnail list
- Player: fullscreen video playback
- Search: simple text search with large input field
- Sleep Mode: minimal dark UI, timer display, lock button

### 8.2 Design Principles

- **Kid Mode:** warm colors, rounded corners, large tap targets (min 48dp), playful but not cluttered. Inspiration: YouTube Kids UI, but simpler.

- **Parent Mode:** Material Design 3, clean and professional. Minimal overlay above the YouTube WebView.

- **Sleep Mode:** dark background (#0A0A0A), minimal light emission, moon icon, breathing animation on the timer.

---

## 9. Technical Architecture

### 9.1 Core Principle

The application is 100% client-side. There is no backend server, no cloud database, no user account system on the app side. All data (profiles, whitelists, settings, viewing history) is stored on-device in a local database. Transfer to another device is done exclusively through the export/import function.

### 9.2 Technology Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Platform** | Android (Kotlin) | Native development for best system integration (kiosk mode, hardware buttons) |
| **UI Framework** | Jetpack Compose | Modern, declarative UI, animations, Material 3 support |
| **Local DB** | Room (SQLite) | Whitelist, profiles, settings, offline availability |
| **Video Playback** | YouTube IFrame Player API | Official embed player, Premium compatible |
| **API Client** | Retrofit + OkHttp | For YouTube Data API v3 calls |
| **Authentication** | Google Sign-In SDK | OAuth 2.0 token management |
| **Background Work** | WorkManager | Channel refresh, new video checking |
| **DI** | Hilt | Dependency injection |
| **Export/Import** | Kotlinx Serialization | JSON serialization / deserialization |

### 9.3 Module Structure

The application follows MVVM (Model-View-ViewModel) architecture with Clean Architecture layers:

- **:app** -- Main module, Activities, navigation
- **:feature:parent** -- Parent mode UI and logic (dashboard, WebView browser, whitelist manager, export/import)
- **:feature:kid** -- Kid mode UI and logic (grid, search, player)
- **:feature:sleep** -- Sleep mode (timer, fade-out, dark UI)
- **:core:data** -- Repositories, data sources
- **:core:database** -- Room DAOs and entities
- **:core:network** -- YouTube API client
- **:core:auth** -- Google sign-in, token management
- **:core:export** -- JSON export/import logic

### 9.4 Parent Mode: YouTube Browsing Implementation

The parent mode YouTube browser is a WebView that loads youtube.com with the signed-in Google account's cookies. A floating overlay button (FAB) appears in the bottom-right corner of the screen. The WebView monitors the current URL via JavaScript interface, extracting the video ID (v=XXXX), channel ID (/channel/XXXX or /@handle), or playlist ID (list=XXXX). When the FAB is tapped, the app calls the YouTube Data API to query metadata (title, thumbnail, channel name), then saves it to the local database.

### 9.5 Kid Mode: Content Display

Kid mode does NOT use a YouTube WebView. Instead, it calls the YouTube Data API v3 based on whitelisted channel/video/playlist IDs to query metadata (title, thumbnail, duration, publication date). Video playback is done through the YouTube IFrame Player API in a controlled WebView that is responsible solely for playback -- no navigation, no link following, no comments.

### 9.6 Export / Import Format

The export file is a JSON document with the following structure:

- **version:** schema version number for compatibility
- **exportedAt:** export timestamp (ISO 8601)
- **profiles[]:** array of kid profiles (name, avatar, settings)
- **profiles[].whitelist[]:** array of whitelisted items (type, youtubeId, title, thumbnailUrl)
- **settings:** global settings (time limits, sleep mode preferences)

The PIN code and Google account data are NOT included in the export file for security reasons. After import, a new PIN setup and Google sign-in are required on the new device.

---

## 10. Data Model

### 10.1 Main Entities

**ParentAccount**

| Field | Type | Description |
|-------|------|-------------|
| **id** | String (UUID) | Unique identifier |
| **googleAccountId** | String | Google account ID |
| **email** | String | Google email address |
| **pinHash** | String | PIN code hash (bcrypt) |
| **biometricEnabled** | Boolean | Biometric authentication enabled |
| **isPremium** | Boolean | YouTube Premium status |
| **createdAt** | Timestamp | Creation time |

**KidProfile**

| Field | Type | Description |
|-------|------|-------------|
| **id** | String (UUID) | Unique identifier |
| **parentAccountId** | String (FK) | Parent account reference |
| **name** | String | Child's name / nickname |
| **avatarUrl** | String? | Profile picture (predefined icons) |
| **dailyLimitMinutes** | Int? | Daily time limit in minutes (null = unlimited) |
| **sleepPlaylistId** | String? | Default sleep playlist |
| **createdAt** | Timestamp | Creation time |

**WhitelistItem**

| Field | Type | Description |
|-------|------|-------------|
| **id** | String (UUID) | Unique identifier |
| **kidProfileId** | String (FK) | Kid profile reference |
| **type** | Enum | CHANNEL \| VIDEO \| PLAYLIST |
| **youtubeId** | String | YouTube channel/video/playlist ID |
| **title** | String | Display title |
| **thumbnailUrl** | String | Thumbnail URL |
| **channelTitle** | String? | Channel name (for videos/playlists) |
| **addedAt** | Timestamp | Time of addition |

**WatchHistory**

| Field | Type | Description |
|-------|------|-------------|
| **id** | String (UUID) | Unique identifier |
| **kidProfileId** | String (FK) | Kid profile reference |
| **videoId** | String | YouTube video ID |
| **videoTitle** | String | Video title |
| **watchedSeconds** | Int | Seconds watched |
| **watchedAt** | Timestamp | Time of viewing |

---

## 11. API Integrations

### 11.1 YouTube Data API v3

The application uses the following YouTube Data API v3 endpoints:

- **channels.list:** Query channel metadata (name, thumbnail, description, video count). Part: snippet, contentDetails, statistics.

- **search.list:** List videos within a channel. Filtered by channelId parameter, order: date.

- **videos.list:** Individual video metadata (title, duration, thumbnail). Part: snippet, contentDetails.

- **playlistItems.list:** List playlist contents. Part: snippet.

### 11.2 API Quota Management

YouTube Data API v3 provides a default daily quota of 10,000 units. Since there is no backend, the quota applies per device (the API key is built into the app, or the user's OAuth token is used). Optimization:

- **Caching:** Channel metadata and video lists stored in Room. Refresh: every 6 hours or manual pull-to-refresh.

- **Batch requests:** Multiple IDs in a single call (videos.list max 50 IDs / call).

- **Incremental refresh:** Monitoring new uploads via the activities API.

- **Quota monitor:** Tracking daily consumption, reducing refresh frequency if needed.

### 11.3 YouTube IFrame Player API

Video playback is done through the YouTube IFrame Player API. This is the official Google embed method that complies with YouTube TOS. Player configuration: controls=1 (controls visible), rel=0 (no related videos shown), modestbranding=1 (minimal YouTube branding), iv_load_policy=3 (annotations disabled), playsinline=1 (inline playback).

---

## 12. Security Requirements

- **SEC-01:** The PIN code is stored in bcrypt-hashed form in the local database. A plaintext PIN is never stored.

- **SEC-02:** Google OAuth tokens are stored in the Android Keystore, encrypted.

- **SEC-03:** Kid mode operates in "kiosk" fashion: system navigation buttons (back, home, recent) are disabled or redirected. Uses Screen Pinning / Lock Task Mode.

- **SEC-04:** In kid mode, JavaScript navigation in the WebView is restricted: only youtube.com/embed/* URLs are allowed.

- **SEC-05:** Brute-force protection: after 5 incorrect PIN attempts, a 30-second wait is enforced, progressively increasing.

- **SEC-06:** The export file does not contain sensitive data (PIN, token). Only whitelisted items and settings.

- **SEC-07:** The app sends no data to any third-party server. No analytics, no server-side crash reporting.

---

## 13. Legal and Compliance Considerations

### 13.1 YouTube Terms of Service

The application uses YouTube's official embed player for playback and the YouTube Data API v3 for metadata. This complies with YouTube TOS. The application does not block ads (with Premium, YouTube itself doesn't show them), does not download content, and does not circumvent YouTube's access controls.

Risk: YouTube/Google may revoke API access or modify the TOS at any time. Mitigation: the app is positioned as a "parental control" tool, not as an "alternative client."

### 13.2 Data Privacy

- The app does not collect or send any personal data to a server.
- All data is stored exclusively on-device, in a local SQLite database.
- There are no third-party SDKs (analytics, advertising, crash reporting) in kid mode.
- Google OAuth is used solely for YouTube access.
- GDPR and COPPA compliance: no data is collected about children.

### 13.3 Licensing

The application is published under the GPLv3 license. The source code is publicly available on GitHub. Anyone may freely use, modify, and distribute it under the terms of GPLv3.

---

## 14. Play Store Listing

### 14.1 Short Description (80 characters)

*Whitelist-based parental control tool for YouTube content.*

### 14.2 Full Description

**What is YouTubeWhitelist for?**

YouTubeWhitelist is a parental control application that lets you decide exactly which YouTube content your child can watch -- no more, no less.

YouTube Kids' algorithm is unfortunately not perfect: it filters out many valuable content (educational videos, calming music, quality cartoons) while allowing questionable content through. YouTubeWhitelist takes a different approach: by default, NOTHING is available to your child. You browse in parent mode and explicitly whitelist the channels, playlists, or individual videos that your child may watch. In kid mode, only these appear.

**Key features:**

- Parent mode: browse YouTube and whitelist with a single tap
- Kid mode: child-friendly, simple interface with only whitelisted content
- Sleep mode: timer and gradual volume fade-out for falling asleep
- Multiple kid profile support with separate whitelists
- Time limit setting per profile
- PIN code and biometric protection for parent settings
- Export and import settings to another device
- Completely free and open source (GPLv3)

**Important notes -- what it does NOT do:**

- This is NOT a YouTube alternative -- it's a parental control tool that uses YouTube's official API and embed player.
- It does NOT automatically filter ads. If you're a YouTube Premium subscriber, playback is ad-free. If not, YouTube's standard ads may appear.
- It does NOT contain built-in content moderation or AI filtering. The selection of whitelisted content is entirely the parent's responsibility and discretion.
- It does NOT collect data and sends NOTHING to any server. Everything stays on your device.

**Open source and privacy:**

YouTubeWhitelist's complete source code is publicly available. There is no hidden data collection, no server, no subscription. If you find it useful, you can support development with a donation.

### 14.3 Play Store Category and Positioning

- **Category:** Tools > Parental Control
- **Target audience:** Parents (the parent downloads and configures). NOT an app in the "Families" program.
- **Content rating:** Everyone
- **Distribution:** Google Play Store + F-Droid + GitHub Releases (APK)

---

## 15. Competitor Analysis

| | YouTube Kids | Google Family Link | YouTubeWhitelist |
|---|---|---|---|
| **Whitelist** | No (algorithmic) | App-level | Video/channel/playlist |
| **Brainrot filtering** | Weak (many allowed through) | None | Complete (only whitelist visible) |
| **Parental control** | Limited | App block | Full content control |
| **Sleep mode** | None | None | Timer + fade-out |
| **Ad-free** | With Premium | N/A | With Premium |
| **Price** | Free | Free | Free (FOSS) |
| **Data collection** | Google analytics | Google analytics | None |
| **Open source** | No | No | Yes (GPLv3) |

---

## 16. Risks and Mitigation

| ID | Risk | Severity | Mitigation |
|----|------|----------|------------|
| **R-01** | Google revokes API access or removes the app from Play Store | High | Parental control positioning, official API/embed usage, F-Droid and GitHub APK as alternative distribution. |
| **R-02** | YouTube API quota insufficient with many whitelisted channels | Medium | Aggressive local caching, incremental refresh, quota increase request to Google. |
| **R-03** | Child finds a way to bypass kiosk mode | Medium | Lock Task Mode, Device Admin, regular testing on different Android versions and manufacturers. |
| **R-04** | YouTube embed player behavior changes | Low | Version tracking, automated testing, fast hotfix release process. |
| **R-05** | Limited market size (niche product) | Medium | FOSS model -- no operational cost, organic growth on parenting forums, Reddit, GitHub. |
| **R-06** | Manufacturer-specific Android behaviors (Samsung, Xiaomi, Huawei) | Medium | Community bug reports, documenting device-specific workarounds. |

---

## 17. Timeline and Milestones

| Milestone | Timeframe | Content |
|-----------|-----------|---------|
| **M1** | Week 1-4 | Core infrastructure: project setup, Google OAuth, PIN management, Room database, basic navigation (parent/kid mode switching). |
| **M2** | Week 5-8 | Parent mode: YouTube WebView integration, URL parsing, whitelist CRUD, YouTube Data API integration. |
| **M3** | Week 9-12 | Kid mode: home screen grid, channel view, video playback (IFrame Player), search, kiosk mode. |
| **M4** | Week 13-14 | Sleep mode: timer, fade-out, dark UI, hardware button handling. |
| **M5** | Week 15-16 | Multiple profiles, time limits, usage statistics, export/import. |
| **M6** | Week 17-18 | Testing, bugfix, performance optimization, community beta. |
| **M7** | Week 19-20 | Play Store + F-Droid + GitHub publication, README, documentation. |

---

## 18. Testing Strategy

### 18.1 Testing Levels

- **Unit tests:** ViewModels, Repositories, URL parser logic, whitelist filtering logic, export/import serialization. Target: > 80% code coverage on core modules.

- **Integration tests:** Room database operations, YouTube API client (with mock server), OAuth flow.

- **UI tests (Espresso/Compose Testing):** Parent/kid mode switching, whitelist add/remove, search functionality, sleep mode timer.

- **E2E tests:** Complete user journey: sign-in -> whitelist -> kid mode -> video playback -> sleep mode.

### 18.2 Special Test Areas

- **Security tests:** Kiosk mode bypass attempts (back button, recent apps, quick settings, notification panel). PIN brute-force protection.

- **Compatibility:** Android 8.0, 10, 12, 13, 14, and 15. Samsung, Xiaomi, Huawei, Pixel devices (manufacturer-specific behaviors).

- **Performance:** Memory usage monitoring (especially WebView), battery consumption in sleep mode, API quota consumption.

- **Usability:** Involving child testers (ages 4-8) in kid mode UI testing. Goal: the child navigates without help.

- **Export/Import:** Round-trip test: export from device A -> import to device B -> verify all profiles and whitelists are intact. Schema version compatibility testing.

---

## 19. Future Development

### V2 -- Next Phase

- iOS port (Swift/SwiftUI) or cross-platform migration (Kotlin Multiplatform)
- Community whitelist sharing: parents can share curated channel lists with each other as JSON
- Widgets: launching kid mode with a single button from the home screen
- Wear OS support: controlling sleep mode from a smartwatch
- Android Auto: audio-only playback for whitelisted music

### V3 -- Long-Term Vision

- Multi-platform integration: not just YouTube, but e.g., Spotify, other streaming service whitelists
- Community-curated lists: age-appropriate recommended channel lists (community-driven)
- Age-specific UI themes: 3-5 years, 6-8 years, 9-12 years
- Localization expansion: community translations with Weblate / Crowdin integration

---

## Appendix: Support

YouTubeWhitelist is completely free and open source. There is no subscription, no in-app advertising, no data collection. Development can be supported with voluntary donations:

- GitHub Sponsors
- Buy Me a Coffee / Ko-fi
- PayPal one-time transfer

Donations do not provide extra features -- every user receives the same complete application. Support covers development time, testing, and Play Store registration fees.
