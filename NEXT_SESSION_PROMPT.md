# Session 6 Starting Prompt

Ez a 6. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

**FIGYELEM**: Ez a 6. session — Session 1-et archiváld CLAUDE_ARCHIVE_1.md-be a CLAUDE.md-ből!

## Fókusz: M3 befejezés + M4 kezdés

### 1. M3 befejezés

- **Coil image loading** integrálása thumbnailokhoz (channel/video/playlist képek)
  - `libs.versions.toml`-ba Coil dependency
  - `AsyncImage` használata az összes thumbnail helyen
- **KidSearch screen** implementálása (searchItems DAO query már kész)
  - `KidSearchViewModel` TDD-vel
  - `KidSearchScreen` UI
  - `Route.KidSearch` bekötés AppNavigation-be
- **Playlist detail screen** (opcionális)

### 2. M4 kezdés - Sleep Mode

- `SleepTimerViewModel` TDD-vel
- Sleep timer UI (fade-out animáció, dark theme)
- Sleep playlist integráció (sleepPlaylistId a KidProfile-ban már van)
- feature:sleep modul

### Megjegyzések

- **Build parancs**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew test`
- **212 teszt** van jelenleg, mind zöld
- **TDD skill használata kötelező!**
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- Google Cloud Console YouTube API key **még nincs** — runtime teszteléshez szükséges
