# Session 3 — Manual Steps (Google Cloud Console + Emulator)

The Session 3 code changes (PKCE, dropped client secret, loopback+state CSRF fix,
WebView hardening, backup disabled, no-account onboarding) are complete on branch
`audit-remediation`. Before this branch ships, YOU must perform the following
external steps that cannot be done from code. Do them in order.

---

## 1. Create a new OAuth client (PKCE / no secret) — required by Task 3.2

The app no longer sends a client secret; it uses PKCE. The old "Web application"
OAuth client type is wrong for a public installed app. Create a new one:

1. Go to https://console.cloud.google.com → select your project.
2. **APIs & Services → Credentials**.
3. **Create Credentials → OAuth client ID**.
4. **Application type: Desktop app** (installed-app PKCE flow; works with the
   `http://localhost/callback` loopback redirect and needs no secret).
5. Name it e.g. `YouTubeWhitelist Installed`.
6. Copy the new **Client ID** (looks like `xxxxx.apps.googleusercontent.com`).
   - A secret may be shown — you will NOT use it. PKCE replaces it.
7. Put the new Client ID in `local.properties`:
   ```
   GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com
   ```
8. **Bake a fallback** so F-Droid/CI builds (no `local.properties`) still work:
   in `app/build.gradle.kts`, where `GOOGLE_CLIENT_ID` is read, set the
   `.ifEmpty { "..." }` fallback to the new client ID (same pattern the YouTube
   API key already uses). A Desktop client ID is not a secret, so baking it is fine.
9. Remove the now-unused `GOOGLE_CLIENT_SECRET` line from your `local.properties`.

> Note: even without this, F-Droid users can now use **"Continue without Google
> account"** (Task 3.6). The Google path just won't function until this client
> exists. With the fallback baked in, Google sign-in works out of the box too.

---

## 2. Restrict & rotate the YouTube API key — Task 3.3

The key `AIzaSy…IzF4` is committed and public. Restrict and rotate it:

1. Console → **Credentials** → click the API key.
2. **API restrictions → Restrict key → select only "YouTube Data API v3"** → Save.
   - Do NOT add an Android app (signature) restriction — that breaks F-Droid-signed
     builds. API-restriction + quota monitoring is the compromise for a public client.
3. **Rotate** (the current key is already leaked):
   - **Create Credentials → API key** → apply the same "YouTube Data API v3" restriction.
   - Put the new key in `local.properties` as `YOUTUBE_API_KEY=...`.
   - Swap the baked fallback string in `app/build.gradle.kts` (around line 49) to the new key.
   - After the new release ships, **delete/disable the old key**.
4. Set a quota alert: APIs & Services → YouTube Data API v3 → Quotas (10,000 units/day).

---

## 3. Emulator verification — Tasks 3.2, 3.4, 3.6 (and Session 1/2 carry-overs)

Run these on an emulator/device once the client ID + keys are in place:

**OAuth (3.2):** Sign in with Google using the new Desktop client. Confirm the
Custom Tab completes and you land on PIN setup. (PKCE + state are exercised here.)

**No-account onboarding (3.6):** Fresh install → tap **"Continue without Google
account"** → confirm you reach PIN setup → create a profile → reach Kid Home, and
that adding a whitelist item from a URL works (uses the YouTube API key, no account).

**Kid WebView hardening (3.4):** In kid mode, open a whitelisted video and confirm
it still plays (the new file/content-access + safe-browsing flags don't affect
youtube.com embeds).

**Session 1/2 carry-overs to confirm while you're at it:**
- Task 1.1: in the kid player, tap an Up-Next card / let a video end / open an
  embed-disabled video — the next video must actually load (not go blank).
- Task 2.2: open a large channel (e.g. MrBeast) and scroll past ~15 videos —
  infinite scroll must keep loading older videos.

---

## 4. Release build note (pre-existing, not a Session-3 regression)

`./gradlew app:assembleRelease` without `local.properties` fails because the
`release` signingConfig reads the keystore path/passwords from `local.properties`.
This is unrelated to onboarding and existed before this work. F-Droid signs
unsigned APKs on its own build server, so this doesn't block F-Droid. If you want
local release builds, keep your keystore entries in `local.properties`.
