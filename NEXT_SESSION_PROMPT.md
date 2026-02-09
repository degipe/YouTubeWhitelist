# Session 2 Starting Prompt

Ez a 2. fejlesztési session. Olvasd be a CLAUDE.md fájlt a projekt kontextushoz és az előző session összefoglalójához.

## Fókusz: M1 Infrastruktúra - Autentikáció és Navigáció

A PRD M1 milestone-ja alapján a következő feladatokat kell elvégezni ebben a sessionben:

### 1. PIN kezelés implementálása
- PIN beállítás képernyő (első indításkor)
- PIN bevitel képernyő (szülői módba váltáskor)
- bcrypt hash-elés a PIN tárolásához
- Brute-force védelem (SEC-05): 5 hibás kísérlet → 30mp várakozás, progresszív
- PIN módosítás a szülői beállításokban

### 2. Alapvető navigáció
- Szülői mód / Gyerek mód közötti váltás PIN-nel védve
- Navigation graph felépítése (Compose Navigation)
- Első indítás flow: Google bejelentkezés → PIN beállítás → Profil létrehozás → Gyerek mód

### 3. Google OAuth 2.0 bejelentkezés (előkészítés)
- Google Sign-In SDK integráció a :core:auth modulba
- Token tárolás Android Keystore-ban (SEC-02)
- Alapvető bejelentkezési flow UI

### Megjegyzések
- Test-driven fejlesztés: tesztek először, implementáció utána
- A session végén: CLAUDE.md frissítés, NEXT_SESSION_PROMPT.md frissítés, git push
- Kommunikáció magyarul, dokumentáció angolul
