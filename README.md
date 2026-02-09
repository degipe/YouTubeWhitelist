# YouTubeWhitelist

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/X8X71TWXEN)

A free, open-source Android app that lets parents whitelist specific YouTube channels, videos, and playlists. Kids only see what's approved — nothing else.

## Features

- **Parent Mode** — Browse YouTube freely, add channels/videos/playlists to your kid's whitelist
- **Kid Mode** — Kids only see whitelisted content in a clean, distraction-free interface
- **PIN Protection** — Parent mode is locked behind a secure PIN
- **Multiple Profiles** — Create separate whitelists for each child
- **Daily Time Limits** — Set per-profile daily watch time limits
- **Sleep Mode** — Timer-based playback with gradual volume fade-out for bedtime
- **Watch Statistics** — Track daily, weekly, and monthly watch time per profile
- **Kiosk Mode** — Screen pinning keeps kids inside the app
- **Export / Import** — Backup and restore profiles and whitelists as JSON
- **Search** — Kids can search within their whitelisted content
- **Playlist Support** — Whitelist entire playlists with automatic video listing
- **100% Client-Side** — No backend server, your data stays on your device
- **No Ads, No Tracking** — Completely free and open source

## Building

### Prerequisites

- Android Studio (or JDK 17 + Android SDK)
- A YouTube Data API v3 key from [Google Cloud Console](https://console.cloud.google.com/)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/degipe/YouTubeWhitelist.git
   cd YouTubeWhitelist
   ```

2. Create `local.properties` in the project root (if not already present) and add your YouTube API key:
   ```properties
   YOUTUBE_API_KEY=your_api_key_here
   ```

3. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

For detailed Google Cloud Console setup instructions (OAuth client, API key restrictions), see [GOOGLE_SETUP.md](GOOGLE_SETUP.md).

## Tech Stack

- Kotlin + Jetpack Compose
- Material Design 3
- MVVM + Clean Architecture (multi-module)
- Hilt (DI), Room (database), Retrofit (network)
- YouTube Data API v3 + IFrame Player API

## License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

## Support Development

If you find this app useful, consider supporting its development:

<a href='https://ko-fi.com/X8X71TWXEN' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
