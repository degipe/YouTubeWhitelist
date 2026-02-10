# Play Store Submission Guide

Step-by-step guide for publishing YouTubeWhitelist on Google Play Store.

## Prerequisites

- [x] Google Play Developer account ($25 one-time fee)
- [x] Signed AAB file: `app/build/outputs/bundle/release/app-release.aab`
- [x] 5 screenshots (1080x2400) in `fastlane/metadata/android/en-US/images/phoneScreenshots/`
- [x] Privacy Policy URL: https://degipe.github.io/YouTubeWhitelist/privacy-policy/
- [x] App source code: https://github.com/degipe/YouTubeWhitelist

## Step 1: Create App in Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Click **Create app**
3. Fill in:
   - **App name**: `YouTubeWhitelist - Safe YouTube for Kids`
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
4. Accept declarations and click **Create app**

## Step 2: Store Listing

### Main Store Listing

- **App name**: `YouTubeWhitelist - Safe YouTube for Kids`
- **Short description** (80 chars max):
  ```
  Whitelist-based YouTube client for kids. Parents approve, kids watch safely.
  ```
- **Full description** (4000 chars max): Copy from `fastlane/metadata/android/en-US/full_description.txt`

### Graphics

- **App icon**: 512x512 PNG (use the app launcher icon, export from `app/src/main/res/mipmap-xxxhdpi/`)
- **Feature graphic**: 1024x500 PNG (create a simple banner with app name + tagline)
- **Phone screenshots**: Upload the 5 PNGs from `fastlane/metadata/android/en-US/images/phoneScreenshots/`
  1. `01_profile_selector.png` — Profile selector
  2. `02_kid_home.png` — Kid home screen
  3. `03_pin_entry.png` — PIN entry
  4. `04_parent_dashboard.png` — Parent dashboard
  5. `05_whitelist_manager.png` — Whitelist manager

### Hungarian Listing (optional)

- Add Hungarian translation from `fastlane/metadata/android/hu-HU/`

## Step 3: App Content (Policy Compliance)

### Privacy Policy

- URL: `https://degipe.github.io/YouTubeWhitelist/privacy-policy/`

### Ads

- **Does your app contain ads?**: No

### App Access

- **All functionality is available without special access**: Yes
- Note: The app requires a YouTube Data API key which is built into the APK

### Content Rating

1. Start the **IARC questionnaire**
2. Answer honestly:
   - Violence: None
   - Sexual content: None
   - Language: None
   - Controlled substances: None
   - User-generated content: **Yes** (YouTube videos, but filtered by parent whitelist)
3. This will likely result in a **PEGI 3** / **Everyone** rating

### Target Audience and Content

- **Target age group**: Select appropriate groups
- **Is this app primarily directed at children?**: No
  - Important: The app is a **parental tool** — it is used BY parents FOR children
  - Selecting "Yes" triggers additional COPPA/GDPR-K requirements
  - The app itself does not collect data from children
- **Appeal to children**: The app has a kid-friendly interface but is a parental control tool

### Data Safety

Fill in the data safety form:
- **Does your app collect or share any user data?**: No
  - The app stores data locally only
  - No data is sent to a server you control
- **YouTube API usage**: Data is sent to Google's YouTube API servers
  - Type: App activity (search queries, video requests)
  - Purpose: App functionality
  - Is this data processed ephemerally? Yes
- **Security practices**:
  - Data is encrypted in transit: Yes (HTTPS)
  - Data deletion mechanism: Users can clear app data or uninstall

### Government Apps

- **Is this a government app?**: No

### Financial Features

- **Does this app provide financial features?**: No

## Step 4: App Releases

### Create Production Release

1. Go to **Production** → **Create new release**
2. **App signing**: Let Google manage app signing (recommended)
   - Upload your app signing key on first release
3. **Upload AAB**: Upload `app-release.aab` (5.5 MB)
4. **Release name**: `1.0.0`
5. **Release notes**: Copy from `fastlane/metadata/android/en-US/changelogs/1.txt`

### Countries and Regions

- Select **All countries** (or specific countries as preferred)

## Step 5: Review and Publish

1. Go to **Publishing overview**
2. Review all sections — each should show a green checkmark
3. Click **Send for review**
4. Google review typically takes 1-7 days for new apps

## Post-Publication Checklist

- [ ] Verify app appears in Play Store search
- [ ] Test Play Store install on a device
- [ ] Update GitHub README with Play Store badge/link
- [ ] Monitor Play Console for crashes and ANRs
- [ ] Respond to user reviews

## Notes

- The app uses YouTube Data API v3 which requires an API key built into the APK
- OAuth consent screen has been published to Production mode in GCP Console
- API key is restricted to Android apps (package: `io.github.degipe.youtubewhitelist`, SHA-1 from release keystore) and YouTube Data API v3 only
- For F-Droid: separate submission process via GitLab RFP issue (already prepared)

## App Icon Export

To create the 512x512 icon for Play Store:

```bash
# The app icon is at app/src/main/res/
# Use Android Studio's Image Asset Studio to export, or:
# Find the highest resolution icon and upscale/recreate at 512x512
```

## Feature Graphic

Create a 1024x500 PNG banner. Suggested content:
- Dark background (matching app theme)
- App name: "YouTubeWhitelist"
- Tagline: "Safe YouTube for Kids"
- Optional: simplified screenshot or icon
- Tools: Figma, Canva, or any graphics editor
