# Session 4 Starting Prompt

Ez a 4. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző sessionök összefoglalójához.

## Fókusz: M2 folytatás - Parent Mode UI

### 1. Build verifikáció (ha elérhető JDK 17 + Android SDK)

- Gradle build ellenőrzés
- Összes unit teszt futtatása (core:common, core:network, core:data, core:auth, app modulok)
- Kompilációs hibák javítása

### 2. Parent Mode ViewModels (feature:parent)

TDD-vel:
- `WhitelistManagerViewModel` - whitelist elemek listázása, szűrés típus szerint, URL-ből hozzáadás, törlés
- `WebViewBrowserViewModel` - URL detektálás, FAB megjelenítés, whitelist-hez adás
- `ParentDashboardViewModel` - profil választó, navigáció kezelés

### 3. Parent Mode UI (feature:parent)

- `ParentDashboardScreen` kibővítés (jelenlegi placeholder felváltása)
- `WhitelistManagerScreen` - elemek listája szűrő tabokkal, hozzáadás/törlés
- `WebViewBrowserScreen` - AndroidView WebView + lebegő FAB gomb

### 4. Navigáció frissítés

- Új route-ok: `WhitelistManager(profileId)`, `WebViewBrowser`
- `AppNavigation.kt` frissítés az új destination-ökkel
- ParentDashboard-ról navigáció a browser-hez és whitelist manager-hez

### Megjegyzések

- **FONTOS**: TDD skill használata kötelező! Test-driven development: tesztek először, implementáció utána
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
- Google Cloud Console YouTube API key szükséges lesz runtime teszteléshez
