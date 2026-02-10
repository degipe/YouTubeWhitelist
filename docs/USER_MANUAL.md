# YouTubeWhitelist - User Manual

A safe YouTube experience for kids. Parents whitelist channels, videos, and playlists — kids only see what's approved.

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Getting Started](#2-getting-started)
   - [Installation](#21-installation)
   - [First-Time Setup](#22-first-time-setup)
3. [Kid Mode](#3-kid-mode)
   - [Home Screen](#31-home-screen)
   - [Watching Videos](#32-watching-videos)
   - [Browsing Channels](#33-browsing-channels)
   - [Browsing Playlists](#34-browsing-playlists)
   - [Searching Content](#35-searching-content)
4. [Parent Mode](#4-parent-mode)
   - [Accessing Parent Mode](#41-accessing-parent-mode)
   - [Parent Dashboard](#42-parent-dashboard)
   - [Adding Content to the Whitelist](#43-adding-content-to-the-whitelist)
   - [Managing the Whitelist](#44-managing-the-whitelist)
   - [Browsing YouTube](#45-browsing-youtube)
5. [Profiles](#5-profiles)
   - [Creating a Profile](#51-creating-a-profile)
   - [Editing a Profile](#52-editing-a-profile)
   - [Switching Profiles](#53-switching-profiles)
   - [Deleting a Profile](#54-deleting-a-profile)
6. [Daily Time Limits](#6-daily-time-limits)
7. [Sleep Mode](#7-sleep-mode)
8. [Watch Statistics](#8-watch-statistics)
9. [Export & Import](#9-export--import)
10. [PIN Management](#10-pin-management)
11. [Kiosk Mode (Screen Pinning)](#11-kiosk-mode-screen-pinning)
12. [Settings & About](#12-settings--about)
13. [Frequently Asked Questions](#13-frequently-asked-questions)
14. [Troubleshooting](#14-troubleshooting)
15. [Privacy & Security](#15-privacy--security)

---

## 1. Introduction

**YouTubeWhitelist** is a free, open-source Android app designed for parents who want their children to enjoy YouTube safely. Instead of relying on algorithmic filters or age-restriction systems, this app takes a simple and effective approach: **parents choose exactly which content their kids can watch**.

### How It Works

1. **Parents** sign in, create kid profiles, and curate a whitelist of approved YouTube channels, videos, and playlists
2. **Kids** only see whitelisted content in a clean, distraction-free interface — no recommendations, no ads, no rabbit holes
3. The app runs entirely on-device — **no data is sent to external servers** (other than YouTube API calls to fetch video metadata)

### Key Features

- **Whitelist-based filtering** — Only approved content is visible to kids
- **Multiple kid profiles** — Each child gets their own personalized whitelist
- **PIN-protected parent mode** — Kids cannot access parent controls
- **Daily time limits** — Set how long each child can watch per day
- **Sleep timer** — Automatic bedtime shutdown with a countdown overlay
- **Watch statistics** — Track how much time each child spends watching
- **Kiosk mode** — Locks the device to the app (kids can't switch to other apps)
- **Backup & restore** — Export your profiles and whitelists to a file
- **No ads, no tracking** — Completely free and open source

### Requirements

- Android 8.0 (Oreo) or higher
- Internet connection (for YouTube video playback and metadata)
- A Google account (for initial sign-in)

---

## 2. Getting Started

### 2.1 Installation

#### From Google Play Store
Search for "YouTubeWhitelist" on the Play Store and install.

#### From F-Droid
Add the repository or search for "YouTubeWhitelist" in the F-Droid client.

#### From GitHub (Sideloading)
1. Download the latest APK from [GitHub Releases](https://github.com/degipe/YouTubeWhitelist/releases)
2. On your Android device, go to **Settings > Security > Install unknown apps**
3. Allow your browser or file manager to install apps
4. Open the downloaded APK and tap **Install**

### 2.2 First-Time Setup

When you first open the app, you'll go through a short setup process:

#### Step 1: Sign In with Google

- Tap **"Sign In"**
- A browser tab (Chrome Custom Tab) will open with the Google sign-in page
- Enter your Google account credentials
- Grant the requested permissions
- The browser will close automatically and return you to the app

> **Note:** This sign-in is only used to create your parent account. The app does not access your Google data beyond basic account identification.

#### Step 2: Create Your PIN

- Enter a **4-digit PIN** that you'll use to access Parent Mode
- Confirm the PIN by entering it again
- **Remember this PIN!** You'll need it every time you want to change settings or add content

> **Tip:** Choose a PIN that your child won't easily guess. Avoid obvious combinations like 1234 or your child's birth year.

#### Step 3: Create Your First Kid Profile

- Enter your child's name
- Tap **"Create"**
- You'll be taken to the Kid Home screen (which will be empty at first)

**That's it! Setup is complete.** Now you'll want to add some content to the whitelist (see [Adding Content to the Whitelist](#43-adding-content-to-the-whitelist)).

---

## 3. Kid Mode

Kid Mode is the primary interface your children will use. It shows only whitelisted content in a clean, safe environment.

### 3.1 Home Screen

The Kid Home screen is the first thing your child sees when they open the app. It displays:

- **Greeting** — "Hi [Name]!" with the child's profile name
- **Channels** — A grid of whitelisted channels (shown as circular thumbnails with titles)
- **Videos** — A horizontal scrolling row of whitelisted videos
- **Playlists** — A horizontal scrolling row of whitelisted playlists

If no content has been whitelisted yet, the screen shows a friendly message: *"No whitelisted content yet. Ask a parent to add videos!"*

**Navigation from Home:**
- Tap a **channel** to see all videos in that channel
- Tap a **video** to start watching it
- Tap a **playlist** to see all videos in that playlist
- Tap the **search icon** (magnifying glass) to search within whitelisted content
- Tap the **lock icon** (top right) to enter Parent Mode (requires PIN)

> **Note:** The Back button is disabled in Kid Mode to prevent children from accidentally exiting the app.

### 3.2 Watching Videos

When your child taps on a video, the **Video Player** opens:

- The video starts playing automatically using YouTube's embedded player
- Standard playback controls are available (play/pause, seek, volume, fullscreen)
- **Navigation controls** appear below the video:
  - **Previous** / **Next** buttons to skip between videos
  - Current position indicator (e.g., "3 / 10")
- **Up Next** list shows remaining videos from the same context (channel, playlist, or search results)

**Safety features in the Video Player:**
- All external links are blocked — tapping "Watch on YouTube" or any other link does nothing
- If a video cannot be embedded (blocked by the video owner), it automatically skips to the next video
- If a daily time limit or sleep timer expires, a full-screen overlay appears and pauses the video

**Fullscreen mode:**
- Tap the fullscreen button in the player
- The device rotates to landscape
- Tap the back button or the exit-fullscreen button to return to portrait

### 3.3 Browsing Channels

Tapping a channel card on the Home screen opens the **Channel Detail** screen:

- Shows the channel name at the top
- Lists all available videos from that channel (fetched from YouTube)
- Tap any video to start watching it

### 3.4 Browsing Playlists

Tapping a playlist card on the Home screen opens the **Playlist Detail** screen:

- Shows the playlist name at the top
- Lists all videos in the playlist in order
- Tap any video to start watching it

### 3.5 Searching Content

Tap the **search icon** on the Home screen to open the **Search** screen:

- Type a search query in the text field at the top
- Results appear in real-time as you type
- Search finds matches in:
  - Video titles
  - Channel titles
  - Playlist titles
  - Videos from whitelisted channels that match your query (via YouTube API)

Tap any search result to navigate to that video, channel, or playlist.

---

## 4. Parent Mode

Parent Mode gives you full control over your children's YouTube experience. It's protected by your PIN code.

### 4.1 Accessing Parent Mode

To enter Parent Mode from anywhere in the app:

1. Tap the **lock icon** (appears on the Kid Home screen and Video Player)
2. Enter your **4-digit PIN**
3. Tap **"Submit"**

If you enter the wrong PIN too many times (5 attempts), a **temporary lockout** activates for 15 minutes. This prevents children from guessing the PIN.

### 4.2 Parent Dashboard

The Parent Dashboard is your control center. At the top, you'll see **profile chips** — tap one to select which child's settings you want to manage.

**Available actions:**

| Action | Description |
|--------|-------------|
| **Manage Whitelist** | View, add, and remove whitelisted content |
| **Browse YouTube** | Browse YouTube freely and add content with one tap |
| **Sleep Mode** | Set a countdown timer for bedtime |
| **Edit Profile** | Change the child's name, avatar, or daily time limit |
| **Watch Stats** | View how much time the child has been watching |
| **Export / Import** | Backup or restore profiles and whitelists |
| **Create Profile** | Add a new kid profile |
| **Change PIN** | Update your parent access PIN |
| **About** | App info, license, and support links |

Tap **"Back to Kid Mode"** at the bottom to return to the selected child's Home screen.

### 4.3 Adding Content to the Whitelist

There are two ways to add YouTube content to a child's whitelist:

#### Method A: Paste a URL (Whitelist Manager)

1. Go to **Parent Dashboard > Manage Whitelist**
2. Tap the **+ button** in the filter chip row
3. Paste a YouTube URL into the text field
4. Tap **"Add"**

The app automatically detects whether the URL is a video, channel, or playlist and fetches the metadata (title, thumbnail) from YouTube.

**Supported URL formats:**
- Videos: `https://youtube.com/watch?v=VIDEO_ID` or `https://youtu.be/VIDEO_ID`
- Channels: `https://youtube.com/@handle`, `https://youtube.com/channel/CHANNEL_ID`, or `https://youtube.com/c/CustomName`
- Playlists: `https://youtube.com/playlist?list=PLAYLIST_ID`

#### Method B: Browse and Add (Browse YouTube)

1. Go to **Parent Dashboard > Browse YouTube**
2. Browse YouTube as you normally would — search, watch, explore
3. When you find content you want to add, look for the **floating button** at the bottom of the screen
4. The button shows "Add Video", "Add Channel", or "Add Playlist" depending on what page you're on
5. Tap the button to add it to the selected child's whitelist

> **Tip:** Browse YouTube is the fastest way to curate content. You can browse YouTube naturally and add content with a single tap.

> **Note:** Your YouTube session (including YouTube Premium benefits) is preserved between browsing sessions. If you sign into YouTube within Browse YouTube, you'll stay signed in.

### 4.4 Managing the Whitelist

Go to **Parent Dashboard > Manage Whitelist** to see all whitelisted content for the selected profile.

**Features:**
- **Filter by type** — Tap "Channels", "Videos", or "Playlists" chips to filter the view
- **Remove items** — Tap the **delete icon** on any item to remove it from the whitelist
- **Add new items** — Tap the **+ button** to paste a URL

Each item in the list shows:
- Thumbnail image
- Title
- Channel name (for videos and playlists)
- Content type label

---

## 5. Profiles

YouTubeWhitelist supports multiple kid profiles, each with its own whitelist, time limits, and watch history.

### 5.1 Creating a Profile

1. Go to **Parent Dashboard > Create Profile**
2. Enter the child's name
3. Tap **"Create"**

The new profile will appear in the profile chips on the Parent Dashboard.

### 5.2 Editing a Profile

1. Select the profile on the Parent Dashboard
2. Tap **"Edit Profile"**
3. You can change:
   - **Name** — The display name
   - **Avatar URL** — Optional profile picture URL
   - **Daily Time Limit** — Enable/disable and set duration (15-180 minutes)
4. Tap **"Save"**

### 5.3 Switching Profiles

**From the Parent Dashboard:**
Tap the profile chips at the top to switch between profiles. All actions (whitelist, browse, stats) apply to the selected profile.

**From the Profile Selector:**
If you have multiple profiles, the app shows a profile selector at startup. Your child taps their name to enter their personalized Kid Mode.

### 5.4 Deleting a Profile

1. Go to **Edit Profile** for the profile you want to delete
2. Scroll down and tap **"Delete Profile"** (red button)
3. Confirm the deletion in the dialog

> **Warning:** Deleting a profile permanently removes all its whitelisted content, watch history, and settings. This cannot be undone (unless you have a backup — see [Export & Import](#9-export--import)).

---

## 6. Daily Time Limits

You can set a daily watch time limit for each kid profile.

### Setting a Time Limit

1. Go to **Parent Dashboard > Edit Profile**
2. Toggle **"Daily Time Limit"** on
3. Adjust the slider to set the desired duration (15 to 180 minutes, in 5-minute increments)
4. Tap **"Save"**

### How It Works

- The countdown starts when the child begins watching content
- A **remaining time badge** appears on the Kid Home screen and Video Player (e.g., "45m", "1h 20m")
- When time runs out:
  - A full-screen **"Time's Up!"** overlay appears
  - Video playback pauses
  - The overlay says: *"Your daily screen time is over. Ask a parent to continue."*
  - The child can only access Parent Mode (via PIN) to dismiss the overlay

### Resetting the Timer

The daily timer resets automatically at midnight. There is no manual override — this is by design to maintain consistency.

---

## 7. Sleep Mode

Sleep Mode lets you set a countdown timer that triggers a "Good Night" overlay when time runs out — perfect for bedtime routines.

### Setting Up Sleep Mode

1. Go to **Parent Dashboard > Sleep Mode**
2. Adjust the slider to set the desired duration (5 to 600 minutes / 10 hours)
3. Tap **"Start Sleep Timer"**
4. You'll be taken back to the Kid Home screen

### How It Works

- The timer runs **in the background** while the child watches content normally
- A countdown is visible on the Sleep Mode screen if you return to it
- When the timer reaches zero:
  - A dark **"Good Night!"** overlay appears on all kid screens
  - The overlay shows: *"Time to sleep. Sweet dreams!"* with a moon icon
  - Video playback pauses automatically
  - The child cannot dismiss the overlay — only a parent can (via PIN)

### Canceling the Timer

- Go back to **Parent Dashboard > Sleep Mode** while the timer is running
- Tap **"Cancel Timer"**

### Dismissing the Overlay

When the "Good Night" overlay is active:
1. Tap the **lock icon** on the overlay
2. Enter your **PIN**
3. The timer stops and you'll be in Parent Mode

---

## 8. Watch Statistics

Track your children's viewing habits with the Watch Statistics screen.

### Viewing Statistics

1. Go to **Parent Dashboard > Watch Stats**
2. Select a time period: **Today**, **This Week**, or **This Month**

### Available Data

- **Total Watch Time** — How long the child has been watching (e.g., "2h 30m")
- **Videos Watched** — Number of videos viewed
- **Daily Breakdown** — A list showing each day's watch time with a visual progress bar

> **Note:** Watch history is tracked automatically whenever a video is played in Kid Mode.

---

## 9. Export & Import

Backup your profiles and whitelists to a JSON file, or restore them from a previous backup.

### Exporting (Backup)

1. Go to **Parent Dashboard > Export / Import**
2. Tap **"Export to File"**
3. Choose where to save the file (your device will show a file picker)
4. The JSON file contains all profiles, their whitelists, and settings

### Importing (Restore)

1. Go to **Parent Dashboard > Export / Import**
2. Tap **"Import from File"**
3. Select a previously exported JSON file
4. Choose an import strategy:
   - **Merge** — Adds imported profiles alongside existing ones (no data is overwritten)
   - **Overwrite** — Replaces all existing profiles with the imported ones

After import, you'll see a summary: *"Imported X profiles, Y items (Z skipped)"*

> **Tip:** Create regular backups, especially before deleting profiles or updating the app. Export files can be shared between devices to set up the same whitelist on a new phone or tablet.

---

## 10. PIN Management

### Changing Your PIN

1. Go to **Parent Dashboard > Change PIN**
2. Enter your **current PIN** (verified with brute force protection)
3. Enter your **new PIN** (4 or more digits)
4. Confirm the new PIN
5. The PIN is updated immediately

### Brute Force Protection

The PIN system includes security against guessing:

| Failed Attempts | Lockout Duration |
|-----------------|------------------|
| 5 | 15 minutes |
| 10 | 30 minutes |
| 15+ | Increases progressively |

During lockout, the Submit button is disabled and a countdown message appears.

### If You Forget Your PIN

The app stores your PIN securely (hashed, not in plain text). There is currently no built-in PIN recovery mechanism. If you forget your PIN:

1. Clear the app's data: **Settings > Apps > YouTubeWhitelist > Clear Data**
2. This will reset the app entirely (all profiles, whitelists, and settings will be lost)
3. If you have an export file, you can restore your data after setting up again

> **Important:** Create regular backups (see [Export & Import](#9-export--import)) to protect against data loss.

---

## 11. Kiosk Mode (Screen Pinning)

Kiosk Mode uses Android's built-in **screen pinning** feature to lock the device to the YouTubeWhitelist app. When active, kids cannot:
- Press the Home button to leave the app
- Switch to other apps via the Recent button
- Pull down the notification shade

### How It Works

- Screen pinning activates **automatically** when entering Kid Mode
- To exit, a parent must enter their PIN (which also stops screen pinning)
- Screen pinning resumes when returning to Kid Mode

### Setup Requirements

For Kiosk Mode to work, you may need to enable screen pinning in your device settings:

1. Go to **Android Settings > Security > Screen Pinning** (or **Advanced > Screen Pinning**)
2. Toggle it **On**

> **Note:** The exact location of this setting varies by Android version and device manufacturer. On some devices, it may be under **Settings > Security > Other Security Settings**.

---

## 12. Settings & About

### About Screen

Go to **Parent Dashboard > About** to see:

- **App version** (currently 1.0.0)
- **Description** of the app's purpose
- **License** — GNU General Public License v3.0 (GPLv3)
- **Source code** link — [github.com/degipe/YouTubeWhitelist](https://github.com/degipe/YouTubeWhitelist)
- **Support development** — Ko-fi donation link

---

## 13. Frequently Asked Questions

### General

**Q: Is this app free?**
A: Yes, completely free. No ads, no in-app purchases, no premium tiers. It's open source under the GPLv3 license.

**Q: Does the app collect any data?**
A: No. All data stays on your device. The only external communication is with YouTube's API to fetch video metadata and play videos.

**Q: Can my child access regular YouTube through this app?**
A: No. Kids can only see and play content that parents have explicitly added to their whitelist. There are no recommendations, suggested videos, or ways to browse beyond the whitelist.

**Q: Does this work without an internet connection?**
A: The whitelist and profiles are stored offline, but an internet connection is required to play videos and fetch metadata when adding new content.

### Content

**Q: What types of content can I whitelist?**
A: Three types:
- **Channels** — All public videos in a channel become available
- **Videos** — Individual videos
- **Playlists** — All videos in a playlist

**Q: If I whitelist a channel, does my child see ALL videos from that channel?**
A: Yes, all public videos from that channel will be available. If you want to restrict to specific videos, add them individually instead.

**Q: Can I whitelist a specific video from a non-whitelisted channel?**
A: Yes! You can add individual videos regardless of whether their channel is whitelisted.

**Q: What happens if a whitelisted video is deleted from YouTube or made private?**
A: The video will remain in the whitelist but won't be playable. You can remove it manually from the Whitelist Manager.

**Q: Does the app support YouTube Shorts?**
A: YouTube Shorts URLs (youtube.com/shorts/VIDEO_ID) can be added as regular videos, but they play in the standard embedded player, not in the Shorts format.

### Safety

**Q: Can my child click on ads or external links while watching?**
A: No. The embedded player blocks all external navigation. Clicking any link (including "Watch on YouTube") does nothing.

**Q: What if an embedded video is blocked by the content creator?**
A: Some content creators disable embedding. If a video can't be embedded, it's automatically skipped and the next video plays.

**Q: Can my child search for anything on YouTube?**
A: No. The search feature only finds results within the whitelisted content and whitelisted channels. It never searches all of YouTube.

### Technical

**Q: Why do I need to sign in with Google?**
A: The Google sign-in creates your parent account. The app needs a YouTube Data API key (provided by the developer) to fetch video metadata, and an account to identify you as the parent.

**Q: Why does the app ask me to sign in twice (once in the app, once in Browse YouTube)?**
A: The initial sign-in uses Chrome Custom Tabs (for security), while Browse YouTube uses an in-app WebView. These have separate cookie stores, so your YouTube session in Browse YouTube is independent. Both sessions persist between app restarts.

**Q: Does the app work with YouTube Premium?**
A: Yes! If you sign into your YouTube Premium account within Browse YouTube, premium benefits (no ads) apply to that browsing session. However, the embedded player in Kid Mode uses YouTube's standard embed, so Premium ad-free playback may not apply there.

---

## 14. Troubleshooting

### "Sign-in failed" error

- Ensure you have a stable internet connection
- Make sure you're using a Google account (not a non-Google email)
- Try clearing the app's cache and trying again
- If the problem persists, the OAuth configuration may need updating — check the [GitHub issues](https://github.com/degipe/YouTubeWhitelist/issues)

### Videos not playing

- Check your internet connection
- The video may have been removed or made private on YouTube
- The video may have embedding disabled (it should auto-skip, but if not, try the next video)
- Try clearing the app's cache

### "Time's Up" overlay won't go away

This is by design. The overlay appears when the daily time limit is reached. To dismiss it:
1. Tap the lock icon on the overlay
2. Enter your parent PIN
3. You'll be in Parent Mode — you can edit the profile's time limit or go back to Kid Mode (the timer resets at midnight)

### App crashes or freezes

1. Force-close the app: **Settings > Apps > YouTubeWhitelist > Force Stop**
2. Clear the cache: **Settings > Apps > YouTubeWhitelist > Clear Cache**
3. Restart the app
4. If the problem persists, try **Clear Data** (this resets the app — make sure you have a backup first!)
5. Report the issue at [GitHub Issues](https://github.com/degipe/YouTubeWhitelist/issues)

### Kiosk mode not working

1. Ensure screen pinning is enabled: **Settings > Security > Screen Pinning**
2. Some devices require developer options to be enabled
3. On some manufacturer skins (Samsung, Xiaomi), the setting may be in a different location

### Content not appearing after adding

- Wait a moment — the app fetches metadata from YouTube, which may take a few seconds
- Check your internet connection
- The URL may be invalid or the content may be private/deleted
- Check the Whitelist Manager to see if the item was added successfully

---

## 15. Privacy & Security

### Data Storage

All your data is stored **locally on your device**:
- Profiles and whitelists → SQLite database (Room)
- Authentication tokens → Encrypted storage (AES-256-GCM)
- PIN → Stored as a one-way hash (PBKDF2), not in plain text
- Watch history → SQLite database

### Network Communication

The app only communicates with:
- **YouTube Data API v3** (googleapis.com) — To fetch video/channel/playlist metadata
- **YouTube** (youtube.com) — To play videos and browse YouTube in Parent Mode
- **Google OAuth** (accounts.google.com) — For initial sign-in only

No data is sent to any other server. The app has no analytics, no crash reporting, and no advertising.

### Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | YouTube API calls and video playback |
| `ACCESS_NETWORK_STATE` | Check if internet is available before making API calls |
| `USE_BIOMETRIC` | Optional biometric authentication (future feature) |

### Open Source

The complete source code is available at [github.com/degipe/YouTubeWhitelist](https://github.com/degipe/YouTubeWhitelist) under the GPLv3 license. You can audit the code, build it yourself, or contribute improvements.

---

*YouTubeWhitelist v1.0.0 — Made with love for families.*
