# Session 7 Starting Prompt

Ez a 7. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M5 - Multi-profile, Time Limits, Stats, Export/Import

### 1. Multi-profile támogatás
- Profil váltás a Kid mode-ban (jelenleg csak az első profilt használjuk)
- Profil szerkesztés (név, avatar módosítás)
- Profil törlés (cascade delete a whitelist items-re)

### 2. Time Limits (napi idő korlát)
- `DailyTimeLimitEntity` hozzáadása a Room DB-hez
- `TimeLimitRepository` TDD-vel
- Napi használat tracking (WatchHistory alapján)
- Időkorlát elérése → visszanavigálás a szülői módba

### 3. Watch Stats
- Nézési statisztikák összesítése profilonként
- Statisztika screen a parent dashboard-on
- Napi/heti/havi bontás

### 4. Export/Import (core:export modul)
- JSON export (whitelist items + profiles)
- JSON import (merge/overwrite opció)
- File picker integráció

### Opcionális
- Kiosk mode (kid mode-ból nem tud kilépni az alkalmazásból)
- Playlist detail screen (tartalom listázás)

### Megjegyzések

- **Build parancs**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew test`
- **237 teszt** van jelenleg, mind zöld
- **TDD skill használata kötelező!**
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- Google Cloud Console YouTube API key **még nincs** — runtime teszteléshez szükséges
