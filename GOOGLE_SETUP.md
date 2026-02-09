# Google Cloud Setup Guide

This guide explains how to configure the Google Cloud project for YouTubeWhitelist.

## Prerequisites

- A Google account
- Access to [Google Cloud Console](https://console.cloud.google.com/)

## Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click **Select a project** → **New Project**
3. Enter project name: `YouTubeWhitelist` (or your preferred name)
4. Click **Create**

## Step 2: Enable YouTube Data API v3

1. Go to **APIs & Services** → **Library**
2. Search for **YouTube Data API v3**
3. Click **Enable**

## Step 3: Create an API Key (for YouTube Data API)

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **API Key**
3. Copy the generated key
4. (Recommended) Click **Edit API Key** → **Restrict key**:
   - Under **API restrictions**, select **Restrict key** → **YouTube Data API v3**
   - Under **Application restrictions**, select **Android apps** and add your package name + SHA-1 fingerprint
5. Click **Save**

## Step 4: Create an OAuth 2.0 Client ID (for Google Sign-In)

1. Go to **APIs & Services** → **OAuth consent screen**
2. Select **External** → **Create**
3. Fill in required fields:
   - App name: `YouTubeWhitelist`
   - User support email: your email
   - Developer contact: your email
4. Add scopes: `openid`, `email`, `profile`
5. Save and continue through all steps

6. Go to **Credentials** → **Create Credentials** → **OAuth client ID**
7. Select **Web application** (required for Authorization Code flow)
8. Add authorized redirect URI: `http://localhost/callback`
9. Click **Create**
10. Copy the **Client ID**

> **Note**: We use a "Web application" type client ID because the app uses a WebView-based OAuth Authorization Code flow (for F-Droid compatibility). Android-type client IDs do not provide a client_id suitable for this flow.

## Step 5: Get SHA-1 Fingerprint (for API Key restriction)

### Debug keystore:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Release keystore:
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias
```

## Step 6: Configure local.properties

Add the following to your project's `local.properties` file (never commit this file):

```properties
YOUTUBE_API_KEY=your_youtube_api_key_here
GOOGLE_CLIENT_ID=your_oauth_client_id_here
```

## API Quota

- YouTube Data API v3 has a default quota of **10,000 units/day**
- Common operations and their costs:
  - `search.list`: 100 units
  - `channels.list`: 1 unit
  - `videos.list`: 1 unit
  - `playlists.list`: 1 unit
  - `playlistItems.list`: 1 unit
- Monitor usage at **APIs & Services** → **Dashboard**

## Troubleshooting

### "API key not valid" error
- Verify the key is correctly copied to `local.properties`
- Check API key restrictions match your app's package name and SHA-1

### OAuth sign-in fails
- Verify the OAuth client ID is for **Web application** type
- Verify the redirect URI is exactly `http://localhost/callback`
- Check that the OAuth consent screen is configured with required scopes

### "This app isn't verified" warning
- Normal during development with External consent screen
- Click **Continue** to proceed (only your test users will see this)
- For production: submit app for Google verification
