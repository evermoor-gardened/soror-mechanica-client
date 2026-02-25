# Soror Mechanica Client (v0) — Phone-first build

This repo builds a **debug APK** on every push using **GitHub Actions**.

## Dummies guide (phone-only)

### 1) Create a public GitHub repo
GitHub → **+** → **New repository** → set **Public** → Create.

### 2) Upload this whole zip
Repo → **Add file** → **Upload files** → upload everything → Commit.

### 3) Download the APK
Repo → **Actions** → open the latest run **Android Debug APK** → **Artifacts** → download `app-debug` → install `app-debug.apk`.

### 4) Set OpenRouter API key in the app
Open app → tab **Dev** → paste key → Save → go to **Rooms** → Dev tab set room Engine to **API** → use **Invoke**.

## Notes
- WebView URL is `https://example.com` (replace in `RoomsScreen.kt`).
- This is intentionally minimal v0.
