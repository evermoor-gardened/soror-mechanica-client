# Soror Mechanica Client — Plug‑and‑Play Spec (v0)

This document is meant to be dropped into a repo as `SPEC.md` and handed to any builder (human or AI).  
It assumes **phone-only** workflow and targets **GitHub Actions** for APK builds.

---

## 1) Goal

Build an **Android APK** (Kotlin + Jetpack Compose) that:

- Wraps a **PC web client** inside a **WebView** (Web Engine)
- Also supports an **OpenRouter API** engine with **streaming** (API Engine)
- Provides **Rooms** (workspaces) with a minimal right-side **Scratchpad** (Tasks, Notes, Templates)
- Supports per-room **engine routing**: **Web / API / Hybrid**
- Includes a hidden **Dev Mode** hatch for diagnostics
- Keeps UI minimal and mechanistic; avoids safety theater and feature creep

**Not in v0:** Personas, multi-model DAG UI, Discord bot forge (placeholder tab ok), complex library sync (stub ok).

---

## 2) Non‑negotiable UI laws

### 2.1 Surface vs spirit
- **Mechanistic** naming/behavior for tools + pipelines + UI actions.
- Occult naming is reserved for **internal titles only** (Dev Mode labels), not normal UI copy.

### 2.2 Rooms lifecycle + list behavior
Room lifecycle states: **ACTIVE / BACKGROUND / ABSENT**

- **ACTIVE:** the **only animated** room in the room list.
- **BACKGROUND:** “lights on” indicator (steady, non-animated).
- **ABSENT:** smaller + darkened; can be **pulled/magnified** to restore.

Room title is **inline editable**. No deep settings screen.

### 2.3 Scratchpad tray (right side)
A slide-in/out tray from the right edge. Not modal.

Tabs (minimal): **TASKS | NOTES | TEMPLATES**

#### Tasks (room-scoped)
Glyph states only:
- `□` open
- `■` running
- higher-contrast `□` = needs review
- completed = **crossed out**

Gestures:
- swipe right = complete
- swipe left = archive
- tap = inline edit

No progress bars. No badges.

#### Notes (room-scoped)
Free text blocks.

Pinned notes get a sloppy **orange highlighter** background.

#### Templates (room-scoped, user-owned)
Editable **in-app** (no GitHub JSON digging). One template is “active” per room.

**Project Start** inserts the active template into the composer.  
No model call. Cursor jumps to first placeholder token `[[...]]` if present.

### 2.4 CRT calibration effect (rare)
A short CRT calibration sweep is allowed **only** after **large injections** (bulk insert / merge / huge paste).

- 300–500ms max
- Never triggers during normal token streaming
- Threshold configurable in Dev Mode (default e.g., 2000 chars)

---

## 3) Architecture laws (discipline)

### 3.1 UI never talks directly to database
UI calls **services** (“Servitoria”). Services own persistence and side effects.

### 3.2 Secrets never go into Room DB
OpenRouter API key and connector tokens are stored in **encrypted storage** (Keystore/EncryptedSharedPreferences), not Room.

### 3.3 Everything is room-scoped unless explicitly global
Tasks, notes, templates are **room/conversation based**.

---

## 4) Engines & routing

### 4.1 Engines
- **Web Engine (WebView):** loads target site; supports inject/send/stop/read-last (best effort).
- **API Engine (OpenRouter):** streaming chat completion into transcript.

### 4.2 Routing (per-room)
Per room setting: **Engine Target = Web | API | Hybrid** (toggle is fine in Dev Mode).

- **Web:** send uses WebView injection.
- **API:** send uses OpenRouter streaming.
- **Hybrid:** reserved; may do local ingest → API summarize → inject into WebView.

### 4.3 Task visibility
Every send/run should create or update a **Task** so the scratchpad reflects activity:

- Task enters `■` during streaming
- Task becomes crossed out on completion
- Mark “needs review” when appropriate (manual toggle ok for v0)

---

## 5) Core services (Servitoria)

These are app-scoped, resident services (not “daemons”):

- **Archivum:** Room persistence (Room DB access)
- **Status:** active room pointer + UI state
- **Logica:** run/event logs (Dev Mode)
- **Pons:** transport multiplexer
  - `PonsWebView`
  - `PonsOpenRouter`
- **Registrum:** library sync stub + manifest hash tracking
- **Nexus:** connector hub (future: Supabase/GitHub/GitLab/Airtable/Drive/Discord)

---

## 6) Data model (minimum tables/entities)

Owned by **Archivum**. UI never touches tables directly.

### Room
- id, title, lifecycleState (ACTIVE/BACKGROUND/ABSENT)
- activeTemplateId
- engineTarget (WEB/API/HYBRID)
- createdAt, updatedAt

### Task
- id, roomId
- title, body?
- status (OPEN/RUNNING/DONE/ARCHIVED)
- needsReview (bool)
- createdAt, updatedAt, orderIndex

### Note
- id, roomId
- content
- pinned (bool)
- createdAt, updatedAt, orderIndex

### Template
- id, roomId
- name
- content
- isActive (bool)
- createdAt, updatedAt

### Message (transcript)
- id, roomId
- role (user/assistant/system)
- content
- createdAt

### SyncState (single-row)
- manifestHash
- lastSyncAt
- status (OK/ERROR/OFFLINE)

*(Optional v0)* RunLog
- id, roomId/taskId
- timestamp
- eventType
- payloadJson

---

## 7) WebView requirements (Web Engine)

Minimum UI controls:
- Back / Forward
- Refresh
- Open in Chrome

Automation (best-effort):
- Insert text into composer
- Click send
- Click stop
- Read last assistant message

**Selector profile**:
- Store selectors in a JSON “profile” editable in Dev Mode (so breakage can be patched without rebuilding APK).

Security boundaries:
- Do not intercept credentials.
- Keep JS bridge minimal; do not expose file/network primitives to arbitrary page JS.

---

## 8) OpenRouter requirements (API Engine)

### Settings
- Field to paste OpenRouter API key
- Stored securely (encrypted prefs/Keystore)

### Streaming
- Stream tokens into the transcript UI
- Commit final message on completion
- Errors displayed plainly (no silent rewrites)

---

## 9) Dev Mode hatch (coder panel)

Dev Mode is a small diagnostic tray/panel (not a new UX world).

Expose:
- per-room Engine Target toggle (Web/API/Hybrid)
- Web selector probe (can find composer/send/stop)
- OpenRouter streaming test
- CRT injection threshold setting
- SyncState (manifest hash + last sync time)
- connector status stubs (future)
- basic logs/errors list

---

## 10) Build order (the only sane sequence)

1. Compose app skeleton + navigation/tabs
2. Rooms list + lifecycle visuals + inline rename
3. Archivum service + DB entities
4. Scratchpad tray (Tasks/Notes/Templates) room-scoped
5. Project Start inserts template
6. WebView screen loads target site + manual use
7. WebView injection (set composer → send → stop → read last)
8. OpenRouter streaming engine into transcript
9. Engine routing toggle per room
10. CRT effect on large injection only
11. Dev Mode probes + toggles

---

## 11) Acceptance criteria (v0 “done”)

v0 is complete when:

- Rooms: create/switch/archive/restore works; lifecycle visuals follow rules
- Scratchpad: tasks/notes/templates are room-scoped and editable
- Project Start inserts active template (no model call)
- WebView loads target site and app provides open-in-chrome/back/forward
- OpenRouter key can be entered securely and streaming works into transcript
- Engine routing (Web/API) changes where send/Invoke goes
- CRT effect triggers only on large injection; never during streaming
- Dev Mode shows probes and toggles

---

## 12) CI: GitHub Actions APK build (phone-friendly)

Add a workflow in `.github/workflows/android.yml` that:
- checks out repo
- sets up JDK
- runs `./gradlew assembleDebug`
- uploads `app-debug.apk` as an artifact

(Builder should implement this immediately so you can download APKs from your phone.)

---

## 13) Scope control (explicit)

Do NOT implement in v0:
- Persona system
- Multi-model DAG orchestration UI
- Discord bot forge
- Full connector set (only stubs)

This spec is intentionally minimal and must ship as an APK first.

