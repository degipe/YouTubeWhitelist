# APK Sideloading Guide (macOS → Android)

## Method 1: Copy APK to device (simplest)

### Step 1: Allow installation from unknown sources

1. **Settings → Security** (or Settings → Apps → Special app access → Install unknown apps)
2. Enable for your file manager (e.g., "Files", "My Files", or whatever you'll use to open the APK)

### Step 2: Connect device via USB

1. Connect your Android device to your Mac with a USB cable
2. On the Android device, pull down the notification shade
3. Tap the **USB notification** ("Charging this device via USB")
4. Select **File Transfer / Android Auto** (not just charging)

### Step 3: Copy the APK

- On macOS you need **Android File Transfer** or **OpenMFT** to browse Android devices:
  - Download: https://www.android.com/filetransfer/ (official Google tool)
  - Or use `adb push` (see below)
- Copy `app/build/outputs/apk/release/app-release.apk` to any folder on the device (e.g., `Download/`)

Alternative with adb (no extra app needed):
```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb push \
  app/build/outputs/apk/release/app-release.apk \
  /sdcard/Download/YouTubeWhitelist.apk
```

### Step 4: Install on the device

1. Open a **File Manager** app on the device (built-in "Files" or "My Files")
2. Navigate to the `Download/` folder
3. Tap **YouTubeWhitelist.apk** (or `app-release.apk`)
4. The built-in package installer opens → tap **Install**
5. Done! The app appears in your app drawer

---

## Method 2: Install directly via adb

### Step 1: Enable USB Debugging

1. **Settings → About phone** → tap **Build number** 7 times → "You are now a developer!"
2. **Settings → System → Developer options** → enable **USB debugging**

### Step 2: Connect and verify

```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb devices
```

Expected output:
```
List of devices attached
XXXXXXXX    device
```

If it shows `unauthorized` → approve the popup on the phone.

### Step 3: Install

```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb install \
  app/build/outputs/apk/release/app-release.apk
```

Expected output: `Success`

### Reinstall (if already installed)

```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb install -r \
  app/build/outputs/apk/release/app-release.apk
```

### Uninstall

```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb uninstall \
  io.github.degipe.youtubewhitelist
```

---

## Method 3: WiFi ADB (no cable, Android 11+)

1. **Developer options → Wireless debugging** → enable
2. Tap it → **Pair device with pairing code**
3. On Mac:
   ```bash
   /opt/homebrew/share/android-commandlinetools/platform-tools/adb pair <IP>:<PORT>
   ```
4. Enter the pairing code
5. Then connect:
   ```bash
   /opt/homebrew/share/android-commandlinetools/platform-tools/adb connect <IP>:<PORT>
   ```
6. Now `adb install` works wirelessly

---

## Tip: Add adb to PATH

Add this to `~/.zshrc` so you can just type `adb` instead of the full path:

```bash
export PATH="$PATH:/opt/homebrew/share/android-commandlinetools/platform-tools"
```

Then restart terminal or run `source ~/.zshrc`.
