# Project: PTS Mobile Native Android Jetpack Compose Rewrite

## Architecture
- **Application Type**: 100% Native Jetpack Compose Android Application (Zero WebViews).
- **Target Location**: `/Data/AppData/pts-mobile`
- **Design System**: Pixel-perfect "Sketch" theme matching `/Data/AppData/` web apps (`--bg-pitch: #040404`, `--bg-card: #0a0a0a`, `--accent-green: #22c55e`, chunky `2.5.dp` borders, and asymmetric 8-value radii `--sketch-radius: 255px 15px 225px 15px / 15px 225px 15px 255px` via custom `SketchShape` / `Modifier.sketchBorder`).
- **Core Technology Stack**:
  - Jetpack Compose + Material 3 + Compose Navigation
  - Google Media3 ExoPlayer (`androidx.media3:media3-exoplayer:1.3.1`)
  - Retrofit 2.11 + OkHttp 4.12 + Gson
  - Room SQLite 2.6.1 for offline caching
  - AndroidX Biometric 1.2.0 for hardware biometric authentication
  - JSch 0.1.55 for direct SSH-2.0 terminal sessions to Pi server
  - Coil Compose 2.6.0 for image caching

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Pitch Black / Emerald Theme | `--bg-pitch: #040404`, `--bg-card: #0a0a0a`, `--accent-green: #22c55e`, graphite scale | M1 | Survey (index.css) |
| 2 | Sketch Border Shape & Modifiers | Asymmetric `--sketch-radius` 8-value curve + chunky pencil borders | M1 | Survey (index.css) |
| 3 | Sketch Custom Typography | Space Grotesk, Architects Daughter, JetBrains Mono, Share Tech Mono | M1 | Survey (index.css) |
| 4 | Stream Media Catalog & Grid | Multi-category video grid, poster art, search, metadata display | M2 | Survey (Stream App) |
| 5 | Stream Fuzzy Search Matcher | Diacritic normalization, roman numeral parsing, acronym matching | M2 | Survey (Stream App) |
| 6 | Native Media3 ExoPlayer | Dual-track scrubber, playback speeds (0.5x-2x), 10s double-tap seek | M2 | Survey (Stream App) |
| 7 | Watchlist & Progress Sync | SQLite / REST progress sync (`/api/progress/update`) and watchlist toggle | M2 | Survey (Stream App) |
| 8 | Hub Queue Live Polling | 1500ms auto-poll queue status with lifecycle indicators | M3 | Survey (Hub Queue) |
| 9 | Real-time Duplicate Warning | Instant disk (`/Data/Downloads`) and queue duplicate check banner | M3 | Survey (Hub Queue) |
| 10 | Bulk URL Multi-Column Parser | 1-col, 2-col (`S01E01 \| URL`), and 3-col (`Season \| Episode \| URL`) parsing | M3 | Survey (Hub Queue) |
| 11 | Queue Task Actions | Edit download URL, retry failed task, delete item, clear queue | M3 | Survey (Hub Queue) |
| 12 | Vault Biometric Auth | AndroidX BiometricPrompt fingerprint/face unlock with fallback | M4 | Survey (Vault App) |
| 13 | Stacked Card Deck UI | Overlapping card deck layout with hover/touch lift offsets | M4 | Survey (Vault App) |
| 14 | Multi-Template Cards | Aadhaar, PAN, Passport, Driving License, Cards, Secrets templates | M4 | Survey (Vault App) |
| 15 | Secure Notes CRUD | Encrypted note creation, editing, deletion, and 1-tap copy | M4 | Survey (Vault App) |
| 16 | Files Folder Browser | POSIX path breadcrumbs, file tree listing, size formatting | M5 | Survey (Files App) |
| 17 | File & Directory Operations | Single & batch delete, directory creation, rename, move, copy | M5 | Survey (Files App) |
| 18 | Archive & Permissions Tools | Zip/unzip extraction and chmod (644/755/777) editing | M5 | Survey (Files App) |
| 19 | In-App Editor & Media Preview | Text file editor and image/video/audio native previewers | M5 | Survey (Files App) |
| 20 | Real JSch SSH Connection | SSH-2.0 socket session to Pi port 22 with PTY allocation | M6 | Survey (WebSSH) |
| 21 | Interactive Terminal & ANSI Buffer | ANSI escape sequences, color styling, terminal buffer display | M6 | Survey (WebSSH) |
| 22 | Mobile Accessory Key Row | ESC, TAB, sticky CTRL, sticky ALT, Up/Down/Left/Right arrows | M6 | Survey (WebSSH) |
| 23 | Quick Command Drawer | Predefined command shortcuts (htop, docker ps, pm2, etc.) | M6 | Survey (WebSSH) |
| 24 | DynamicBottomDock Navigation | Global dock with active indicator and badge counts | M7 | Survey (MainActivity) |
| 25 | Robust BackHandler Routing | Safe back navigation to catalog/previous state without app exit | M7 | Survey (MainActivity) |
| 26 | Unified Retrofit Client | Complete REST endpoint mapping for all 5 services with JWT auth | M7 | Survey (Backend) |
| 27 | Room Offline Caching | Offline caching for media, documents, and download queue | M7 | Survey (Backend) |
| 28 | Comprehensive E2E Test Suite | 4-tier requirement-driven opaque-box test suite passing 100% | M8 | Dual Track |
| 29 | Adversarial Coverage Hardening | Tier 5 white-box edge case hardening and stress verification | M8 | Dual Track |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Theme & Design System | SketchShape, SketchBorder Modifier, Color.kt, Type.kt, Theme.kt | None | PLANNED |
| M2 | Stream & Media3 Player | Catalog, Fuzzy search, ExoPlayer Compose surface, Watchlist | M1 | PLANNED |
| M3 | Hub Queue & Downloads | 1.5s Poller, Duplicate checker banner, Bulk URL parser, Task actions | M1 | PLANNED |
| M4 | Vault & Biometrics | BiometricPrompt, Stacked card deck UI, Card templates, Note editor | M1 | PLANNED |
| M5 | Files & Storage Tools | Folder browser, Breadcrumbs, CRUD, Chmod, Zip, In-app editor | M1 | PLANNED |
| M6 | SSH Terminal & JSch | Real JSch session, Interactive PTY, Mobile accessory keys, Drawer | M1 | PLANNED |
| M7 | Navigation & Integration | DynamicBottomDock, BackHandler, Retrofit ApiService, Room DB | M2, M3, M4, M5, M6 | PLANNED |
| M8 | E2E Testing Pass & Hardening | 100% pass on Tiers 1-4 + Tier 5 Adversarial Hardening | M7 | PLANNED |

## Interface Contracts
### Theme ↔ Screens
- `SketchShape(radius: Dp = 16.dp, asymmetry: Float = 0.85f): Shape`
- `Modifier.sketchBorder(width: Dp = 2.dp, color: Color = SketchTheme.colors.border, shape: Shape = SketchShape)`
- `SketchTheme`: Provides `colors` (pitchBlack, cardBg, accentGreen, graphite), `typography`, `shapes`.

### Navigation ↔ Screens
- `NavigationDestination`: Enum / Sealed class for `Stream`, `HubQueue`, `Vault`, `Files`, `Terminal`, `Settings`.
- `BackHandler`: Intercepts system back gesture; pops internal screen stack or returns to `NavigationDestination.Stream`.

### ApiService ↔ Repositories
- `RetrofitClient`: Singleton providing authenticated `ApiService` with Bearer token interceptor and base URL `https://hub.ptsmovies.online`.
- `ApiService`: Full mapping of `/api/media/*`, `/api/downloads/*`, `/api/vault/*`, `/api/fs/*`, `/api/system/*`.

### SSH Manager ↔ TerminalScreen
- `SshSessionManager`: Manages JSch `Session` and `ChannelShell` with coroutine-based `InputStream` reader and `OutputStream` writer.

## Code Layout
```
/Data/AppData/pts-mobile/app/src/main/java/com/pts/mobile/
├── ui/
│   ├── theme/          # Color.kt, Shape.kt, Type.kt, Theme.kt, SketchModifiers.kt
│   ├── components/     # DynamicBottomDock.kt, SketchCard.kt, CommonButtons.kt
│   ├── stream/         # StreamScreen.kt, VideoPlayerActivity.kt, StreamViewModel.kt
│   ├── hub/            # HubQueueScreen.kt, BulkUrlDialog.kt, HubViewModel.kt
│   ├── vault/          # VaultDeckScreen.kt, BiometricHelper.kt, VaultViewModel.kt
│   ├── files/          # FilesScreen.kt, FileEditorDialog.kt, FilesViewModel.kt
│   └── terminal/       # TerminalScreen.kt, AccessoryKeyRow.kt, TerminalViewModel.kt
├── data/
│   ├── api/            # ApiService.kt, RetrofitClient.kt, AuthInterceptor.kt
│   ├── models/         # MediaModels.kt, DownloadModels.kt, VaultModels.kt, FileModels.kt
│   ├── local/          # AppDatabase.kt, Entities.kt, Daos.kt
│   └── ssh/            # SshSessionManager.kt, AnsiParser.kt
└── MainActivity.kt
```
