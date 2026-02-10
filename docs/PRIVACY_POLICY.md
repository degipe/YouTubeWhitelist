# Privacy Policy

**YouTubeWhitelist**
**Last updated: February 10, 2026**

## Overview

YouTubeWhitelist is a free, open-source Android application that provides a whitelist-based YouTube client for kids. This privacy policy explains how the app handles your data.

## Data Collection

**YouTubeWhitelist does not collect, store, or transmit any personal data to external servers.**

The app operates entirely on your device. There is no backend server, no analytics, no tracking, and no advertising.

## Data Stored on Your Device

The following data is stored locally on your device only:

- **PIN code**: Stored as a salted PBKDF2 hash in encrypted storage (Android EncryptedSharedPreferences). The actual PIN is never stored in plain text.
- **Kid profiles**: Names and settings you create for your children.
- **Whitelisted content**: YouTube channel, video, and playlist references you add to whitelists.
- **Watch statistics**: Daily watch time data per profile.
- **Google account tokens**: If you sign in with Google, OAuth tokens are stored in encrypted storage on your device.

All locally stored data can be deleted by clearing the app's data or uninstalling the app.

## Third-Party Services

### YouTube Data API v3

The app uses the [YouTube Data API v3](https://developers.google.com/youtube/v3) to fetch metadata (titles, thumbnails, descriptions) for channels, videos, and playlists you add to whitelists. These API requests are made directly from your device to Google's servers.

YouTube API usage is subject to [Google's Privacy Policy](https://policies.google.com/privacy) and [YouTube's Terms of Service](https://www.youtube.com/t/terms).

### Google OAuth 2.0

If you choose to sign in with your Google account (optional, used for accessing the YouTube API), the app uses Google's OAuth 2.0 authentication via Chrome Custom Tabs. The app only requests read-only access to your YouTube account data. Authentication tokens are stored locally in encrypted storage.

### YouTube Video Playback

Videos are played using the YouTube IFrame Player API within a WebView. YouTube may collect data during video playback according to their own privacy policy.

## Children's Privacy

YouTubeWhitelist is designed to be used by parents/guardians to manage content for children. The app itself does not collect any data from children. All content curation is performed by the parent/guardian in the PIN-protected parent mode.

## Data Export and Import

The app provides an export/import feature for backing up profiles and whitelists. Exported data is saved as a JSON file on your device. You have full control over this file — it is not uploaded anywhere automatically.

## Open Source

YouTubeWhitelist is open-source software licensed under GPLv3. You can inspect the complete source code at:
https://github.com/degipe/YouTubeWhitelist

## Changes to This Policy

If this privacy policy is updated, the changes will be posted on this page with an updated date.

## Contact

For questions about this privacy policy or the app, please open an issue on GitHub:
https://github.com/degipe/YouTubeWhitelist/issues
