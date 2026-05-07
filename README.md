# NotionTasks — Android

A minimal Android app that syncs Notion checklist blocks and database checkbox properties into a unified, filterable task list — with a home screen widget. Inspired by SyncTasks (iOS).

## Features

- 🔗 **Notion OAuth** — securely connect your Notion workspace
- 📄 **Page support** — extracts all `to_do` (checklist) blocks from connected pages, recursively
- 🗄 **Database support** — reads rows with checkbox properties from connected databases
- ✅ **Two-way sync** — checking a task syncs back to Notion (block or page property)
- 🔍 **Filter / Sort / Group** — identical to what you see in the screenshots
- 📦 **Home screen widget** — minimal monochrome widget showing today's pending tasks
- 🌙 **Dark mode** — full Material3 dark/light theming

## Project Structure

```
NotionTasks/
├── app/src/main/java/com/notiontasks/
│   ├── data/
│   │   ├── api/          — Retrofit API service + Room database/DAOs
│   │   ├── model/        — Notion API models + domain models (Task, NotionSource, etc.)
│   │   └── repository/   — NotionRepository (sync logic) + PreferencesRepository (DataStore)
│   ├── di/               — Hilt dependency injection modules
│   ├── ui/
│   │   ├── screens/      — TaskListScreen, LoginScreen, ManageSourcesScreen, ViewSettingsSheet
│   │   ├── theme/        — Monochrome Material3 theme
│   │   └── widget/       — Glance-based home screen widget
│   ├── MainActivity.kt   — Navigation host + OAuth deep link handler
│   ├── MainViewModel.kt  — State management, filter/sort/group logic
│   └── NotionTasksApp.kt — Hilt application class
├── .github/workflows/
│   └── build.yml         — GitHub Actions: builds debug + release APK
└── gradle/
    └── libs.versions.toml — Version catalog
```

## Setup

### 1. Create a Notion Integration

1. Go to [https://www.notion.so/my-integrations](https://www.notion.so/my-integrations)
2. Click **"New integration"** → select type **"Public"**
3. Set the redirect URI to: `com.notiontasks://oauth/callback`
4. Copy your **Client ID** and **Client Secret**

### 2. Configure credentials

**For local development**, create `local.properties` (copy from `local.properties.template`):
```properties
sdk.dir=/path/to/your/android/sdk
NOTION_CLIENT_ID=your_client_id_here
NOTION_CLIENT_SECRET=your_client_secret_here
```

**For GitHub Actions**, add repository secrets:
- `NOTION_CLIENT_ID`
- `NOTION_CLIENT_SECRET`

Optionally, for signed release builds:
- `KEYSTORE_BASE64` — base64-encoded keystore file
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `STORE_PASSWORD`

### 3. Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease
```

## GitHub Actions

Every push to `main` or `develop`:
1. Injects `NOTION_CLIENT_ID` + `NOTION_CLIENT_SECRET` from GitHub Secrets
2. Builds debug APK → uploads as artifact
3. Builds signed release APK (if keystore secrets exist) → uploads as artifact
4. Runs lint check

## Architecture

- **MVVM** with Kotlin Coroutines + Flow
- **Hilt** for dependency injection
- **Room** for local task + source caching
- **DataStore** for auth token + view settings
- **Retrofit + Moshi** for Notion API
- **Jetpack Compose** + **Material3** for UI
- **Glance** for home screen widget

## How sync works

### Pages
The app calls `GET /blocks/{page_id}/children` recursively and collects all `to_do` type blocks. Checking a task calls `PATCH /blocks/{block_id}` with `{"to_do": {"checked": true}}`.

### Databases
The app calls `POST /databases/{database_id}/query` and reads all rows. It finds the first checkbox property on each row and uses it as the task status. Checking a task calls `PATCH /pages/{page_id}` with the updated checkbox property.

## Widget

The home screen widget uses **Jetpack Glance** and shows:
- App logo + "INBOX" quick-add button
- Up to 8 pending tasks with circle checkboxes
- Tapping opens the main app

The widget refreshes every 30 minutes automatically, and is also updated whenever a sync completes.

## Roadmap

- [ ] Per-widget filter/sort settings
- [ ] Background periodic sync (WorkManager)
- [ ] Push notifications for due tasks
- [ ] Support nested page checklists
- [ ] Due date filtering from calendar view
