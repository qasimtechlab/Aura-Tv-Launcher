# Aura TV - Apple TV Inspired Android TV Launcher

Aura TV is a high-performance, ultra-modern Android TV launcher engineered with the fluid aesthetic and cinematic feel of Apple tvOS. It replaces cluttered standard Android TV interfaces with a dynamic Top Shelf preview carousel, frosted glass surfaces, seamless D-Pad remote navigation, custom app categories, 4K aerial wallpaper backgrounds, and PIN-protected app privacy.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions were confirmed through Phase 1 clarification:
> - **Top Shelf Hero Carousel**: A signature Apple TV style dynamic hero showcase displaying rich preview cards, featured backdrops, metadata, and quick-action play/launch buttons for focused apps.
> - **D-Pad TV Remote Navigation**: Responsive spatial focus engine with smooth zoom scaling (`1.08x - 1.12x`), elevation glow, focus borders, and hardware key event interceptors (`DPAD_UP`, `DPAD_DOWN`, `DPAD_LEFT`, `DPAD_RIGHT`, `DPAD_CENTER`, `BACK`, `MENU`).
> - **Categorization & App Management**: Built-in smart categories (Favorites, Streaming & Video, Games, Music, System & Tools) with the ability to rearrange apps and add apps to custom rows.
> - **Apple Aerial 4K Wallpapers**: Dynamic background switcher with curated ultra-HD aerial wallpapers (cinematic cityscapes, nature, space, twilight) and ambient blur dimming behind cards.
> - **Privacy & App Protection**: PIN-protected security vault allowing users to hide specific installed apps behind a 4-digit TV remote keypad.
> - **Default TV Launcher Integration**: Fully declared in `AndroidManifest.xml` with `android.intent.category.HOME`, `android.intent.category.DEFAULT`, and `android.intent.category.LEANBACK_LAUNCHER` so it can be selected as the permanent default launcher on any Android TV or Google TV box.

---

### 1. Overview & Core Concept

- **What It Does**: Aura TV is an Android TV home launcher APK. Once installed on any Android TV, Google TV, or Android TV box (such as Xiaomi Mi Box, Nvidia Shield, Chromecast with Google TV, FireTV, or generic Android TV sets), it can be selected as the default Home screen. It scans and displays all installed TV apps with their native icons and high-resolution banners, providing a responsive Apple TV-like home screen.
- **Target Audience**: Users who want a clean, ad-free, luxurious TV operating experience that feels like a $200 Apple TV 4K device on their existing Android TV hardware.
- **Key Value**: Eliminates sponsored ads, clutter, and slow menus found on generic Android TV interfaces in favor of instantaneous remote response, cinematic visuals, and personalized control.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Cold Boot & Home Display**:
   - The user turns on the TV or presses the Home button. Aura TV boots instantly, displaying the current time, date, subtle weather chip, active wallpaper, Top Shelf hero banner, and the horizontal Dock of favorite apps.
2. **D-Pad Spatial Navigation**:
   - As the user clicks Left/Right on their TV remote, the focused app scales up smoothly with a luminous white frosted glass border.
   - When hovering over a streaming app (e.g., Netflix, YouTube, Prime Video, Plex), the Top Shelf dynamically morphs its background image, title, and synopsis to match the focused content.
3. **Category Browsing**:
   - Navigating Down reveals horizontal content shelves: *Favorites*, *Streaming*, *Gaming*, *Utilities*, and *All Apps*.
4. **App Launching & Options Menu**:
   - Pressing `DPAD_CENTER` (Select) immediately launches the app package via `PackageManager`.
   - Long-pressing `DPAD_CENTER` or pressing the `MENU` button opens a floating frosted glass Quick Actions sheet (Add to Favorites, Move Category, Hide App, Open App Info).
5. **Settings & Customization Overlay**:
   - Pressing Up from the Top Shelf opens the top status bar containing Profile, Wallpaper Gallery, Category Editor, and Settings (PIN setup, wallpaper style, reset).

#### Visual Identity & Theme
- **Aesthetic Direction**: Apple tvOS luxury minimalism. Dark, cinematic, translucent frosted glass (`backdrop-filter` blur emulation with semi-transparent surfaces), and luminous focus outlines.
- **Color Palette**:
  - Deep Backdrop: `#0A0D14` with dynamic aerial imagery.
  - Surface Glass: `rgba(255, 255, 255, 0.08)` to `rgba(255, 255, 255, 0.16)`.
  - Focused Card Surface: `rgba(255, 255, 255, 0.28)` with a crisp `2.dp` border of `#FFFFFF`.
  - Accent Highlight: `#0A84FF` (Apple Blue) and `#30D158` (Vibrant Green).
  - Typography: Crisp white `#F5F5F7` for titles and `#A1A1A6` for secondary descriptions.
- **Typography & Hierarchy**:
  - Time & Hero Display: Ultra-clean 48sp headline with subtle drop-shadow.
  - Category Titles: 16sp bold uppercase tracking with generous 24dp vertical breathing room.
  - App Labels: 13sp medium with auto-marquee or truncation when unfocused.
- **Interactive Feedback & Motion**:
  - Spring-physics focus animations (`spring(dampingRatio = 0.8f, stiffness = 400f)`).
  - Cross-fading background transitions (400ms duration) when changing Top Shelf banners to prevent visual jarring.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native PackageManager Scanning vs Hardcoded Apps**
  - *Chosen Approach*: Real-time query of installed applications using `PackageManager.getInstalledApplications` and `Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)` (with fallback to `CATEGORY_LAUNCHER` for sideloaded mobile apps).
  - *Why*: Ensures every installed app and game on the user's specific TV appears immediately, with real application icons and proper launch intents.
  - *Alternatives Considered*: Hardcoded mock icons would not launch user apps; querying real packages provides true launcher utility.

- **Decision 2: Local Persistence Engine (Room Database vs Cloud)**
  - *Chosen Approach*: Local Room SQLite database for categories, app favorites ordering, hidden package IDs, and SHA-256 PIN hash.
  - *Why*: A TV launcher must load in under 100 milliseconds without internet access. Local SQLite guarantees instant startup even when the TV is disconnected from Wi-Fi.

- **Decision 3: Remote Input Handling via Jetpack Compose TV Patterns**
  - *Chosen Approach*: Hardware key interception via `Modifier.onKeyEvent` paired with `FocusRequester` and `isFocused` state tracking.
  - *Why*: Standard touch-only Compose composables can drop remote DPAD focus; dedicated TV key event listeners ensure 100% reliable navigation with standard TV remotes (FireStick, Shield, generic IR/Bluetooth remotes).

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Android TV OS / D-Pad Remote                    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ DPAD_UP/DOWN/LEFT/RIGHT/SELECT
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             MainActivity                               │
│  • android.intent.category.HOME & LEANBACK_LAUNCHER intent filters     │
│  • KeyEvent interceptor (DPAD, BACK, MENU)                             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        AuraLauncherViewModel                           │
│  • installedApps: StateFlow<List<AppItem>>                             │
│  • categories: StateFlow<List<AppCategory>>                            │
│  • focusedApp: StateFlow<AppItem?>                                     │
│  • activeWallpaper: StateFlow<WallpaperTheme>                          │
│  • hiddenPackageNames: StateFlow<Set<String>>                          │
│  • isPinLocked: StateFlow<Boolean>                                     │
└───────────────┬────────────────────────────────────────┬───────────────┘
                │                                        │
                ▼                                        ▼
┌──────────────────────────────┐        ┌────────────────────────────────┐
│      PackageManager Layer    │        │       Room Database / Repo     │
│  • Scan Leanback/Mobile apps │        │  • AppCategoryEntity           │
│  • Extract App Icons & Names │        │  • UserPreferencesDao          │
│  • Launch Intent Dispatcher  │        │  • HiddenAppEntity & PIN Store │
└──────────────────────────────┘        └────────────────────────────────┘
                │                                        │
                └───────────────────┬────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Compose TV UI Tree                            │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Top Bar: Clock, Date, Weather Chip, Settings & Wallpaper Buttons │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Top Shelf Hero: Cinematic Banner, Dynamic Title, Synopsis, Play  │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Favorites Row (Top Shelf Dock): Glass cards with 1.1x Zoom Focus │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Categorized Rows: Streaming, Games, Music, Tools, Sideloaded     │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Dialogs & Sheets: PIN Entry Keypad, App Context Menu, Wallpapers │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

#### Key Components & State Mapping
1. **`AuraLauncherViewModel`**:
   - Scans installed apps via `PackageManager` on background coroutine dispatchers (`Dispatchers.IO`).
   - Maintains reactive `StateFlow` streams for categories, favorites, focus updates, and preferences.
2. **`TopShelfHero`**:
   - Displays dynamic backdrop for the currently focused app or curated Apple-style cinematic previews.
   - Includes quick "Open", "App Info", and metadata pills (Category, Package Size, Version).
3. **`TvAppCard`**:
   - Focusable card component with `animateFloatAsState` for scale (`1.0f` to `1.10f`), `animateColorAsState` for border illumination, and high-quality icon rendering.
4. **`PinVaultDialog`**:
   - On-screen numeric 0-9 TV keypad navigable with D-Pad to lock/unlock hidden apps.
5. **`WallpaperGallerySheet`**:
   - Visual selector for aerial wallpapers (e.g., San Francisco Night, Hawaii Coastline, Aurora Borealis, Deep Space, Apple Park).
