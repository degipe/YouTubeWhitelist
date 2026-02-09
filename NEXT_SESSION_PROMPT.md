# Session 5 Starting Prompt

Ez az 5. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M2 befejezés + M3 kezdés

### 1. Build verifikáció (PRIORITÁS)

- **JDK 17 + Android SDK beállítás ellenőrzése**
- Gradle build futtatás: `./gradlew assembleDebug`
- Összes unit teszt futtatás: `./gradlew test`
  - core:common (27 URL parser teszt + AppResult)
  - core:network (9 DTO + 2 interceptor teszt)
  - core:data (10 API repo + 12 whitelist repo + 7 profile repo teszt)
  - core:auth (PIN hasher, brute force, PIN repo, account repo, auth repo tesztek)
  - feature:parent (15 whitelist manager + 13 browser + 9 dashboard teszt)
  - app (5 PIN entry + splash + sign-in + pin setup + pin change VM tesztek)
- Kompilációs hibák javítása

### 2. M3 kezdés - Kid Mode UI

TDD-vel:
- `KidHomeViewModel` - whitelist tartalom megjelenítés, grid layout
- `ChannelDetailViewModel` - csatorna tartalom listázása
- `VideoPlayerViewModel` - YouTube IFrame Player integráció

### 3. Kid Mode UI (feature:kid)

- `KidHomeScreen` kibővítés - tartalom grid whitelist elemekkel
- `ChannelDetailScreen` - csatorna videói/playlistjei
- `VideoPlayerScreen` - YouTube lejátszó

### Megjegyzések

- **FONTOS**: TDD skill használata kötelező!
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- **FIGYELEM**: Ez az 5. session — ha lesz 6. session, az archíválás indul (CLAUDE_ARCHIVE_1.md)
- Kommunikáció magyarul, dokumentáció angolul
