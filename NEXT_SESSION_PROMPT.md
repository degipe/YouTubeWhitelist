# Session 3 Starting Prompt

Ez a 3. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M1 befejezés + M2 kezdése

### 1. Build verifikáció és teszt futtatás

- Gradle build ellenőrzés (ha elérhető JDK 17 + Android SDK)
- Unit tesztek futtatása: core:auth és app modul tesztek
- Kompilációs hibák javítása ha szükséges

### 2. M2 kezdés: Parent Mode - WebView böngésző

A PRD M2 milestone-ja alapján:
- WebView alapú YouTube böngésző a :feature:parent modulban
- URL parsing: YouTube video/channel/playlist URL-ek felismerése
- Whitelist CRUD: videó/csatorna/playlist hozzáadása a whitelist-hez
- YouTube Data API v3 kliens a :core:network modulban

### 3. YouTube API integráció

- Retrofit service a YouTube Data API v3-hoz
- Video/channel/playlist metadata lekérdezés
- Thumbnail URL-ek kezelése

### Megjegyzések

- **FONTOS**: TDD skill használata kötelező! Test-driven development: tesztek először, implementáció utána
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
