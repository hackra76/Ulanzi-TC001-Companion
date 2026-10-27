# Ulanzi TC001 Companion 📱✨

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-brightgreen?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firmware](https://img.shields.io/badge/Ulanzi_TC001_Evolved-v2.4.0+-orange)](https://github.com/hackra76/Ulanzi-TC001-Evolved)
[![Release](https://img.shields.io/badge/Release-v1.0.5-success?logo=github)](https://github.com/hackra76/Ulanzi-TC001-Companion/releases)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Official Android Companion App for the **[Ulanzi TC001 Smart Pixel Desktop Clock](https://github.com/hackra76/Ulanzi-TC001-Evolved)** running **Ulanzi TC001 Evolved** firmware.

Engineered with modern **Material 3 Design**, tactile retro-futuristic telemetry decks, and full hardware integration, this companion app serves as a **complete, 100% standalone replacement for the ESP32 web portal**.

---

## 🌟 What's New in v1.0.5

- **🎨 Direct Hardware Canvas Matrix Broadcast**:
  - Full end-to-end integration with the newly released firmware v2.4.0 `POST /api/display` and `POST /api/draw` endpoints.
  - Verified live broadcast: drawing on the 32×8 canvas in the Android app renders instantly onto the physical Ulanzi TC-001 NeoPixel matrix with audible chime confirmation and hardware button dismissal (`OK`, `LEFT`, `RIGHT`).
- **🛡️ Robust Network Client & Error Handling**:
  - Fixed fallback status code propagation in `UlanziApiClient` (`sendCanvasPixels`) ensuring precise HTTP error reporting.
  - Added resilient empty/blank response body handling to guard against atypical HTTP 200/204 payloads.

---

## 🌟 What's New in v1.0.4

- **🎨 Interactive 32×8 Pixel Canvas Studio**: Added an interactive drawing canvas directly in the Live Matrix tab:
  - **Touch & Drag Drawing Engine**: Draw, tap, or stroke across the 32×8 Neopixel LED grid with instant live rendering.
  - **Tactile Tool Suite**: Pen (✏️), Eraser (🧼), and Flood-Fill Bucket (🪣).
  - **13-Color Neon & 8-Bit Palette**: Curated neon swatches (Hot Pink, Crimson, Amber, Volt Green, Cyan, Cobalt, Magenta, White, LED Off).
  - **7 Retro Preset Stamps**: Instantly load retro art templates: ❤️ Love, 👻 Pac-Man & Blinky, 👾 Space Invaders, 📊 VU Equalizer, 🌈 Rainbow, 🔥 Flame, and 😎 Sunglasses Smiley.
  - **Clone Clock Display**: 1-tap "Clone Clock" button that downloads the live hardware matrix into the Canvas Studio for instant editing.
  - **Direct Hardware Broadcast**: 1-tap "Send to Clock Matrix" targeting `/api/display` with customizable display duration (15s, 30s, 60s, 180s).
- **Streamlined, Uncluttered Settings Tab**: Completely reorganized the Settings tab from 10 sprawling cards into 5 clean, cohesive decks:
  1. *Clock & Display Architecture*
  2. *Carousel & Screen Sequence*
  3. *Photometry & Ambient Display*
  4. *Integrations & Data Feeds*
  5. *Wi-Fi Roaming & Scanner*
- **Compact Dropdown Popups for Colors & Selectors**: Replaced 32 loose color chips and dozens of mode chips with elegant, space-saving popup menus:
  - `ColorDropdownPicker`: Features live circular color swatches, descriptive names, and checkmark indicators for Active Day, Inactive Day, and Weekend colors.
  - `CompactDropdownSelector`: Streamlined 2-column dropdowns for Clock Modes, Date Notations, Clock Icons, Text Alignments, Transition FX, Default Screen, City Presets, Asset Tickers, Battery Telemetry, and Screensaver Styles/Timeouts.
- **Dynamic Firmware Version in Top Bar**: The app header now dynamically displays the actual firmware version reported in device telemetry (`FW v<version> • <status>`) rather than a hardcoded string.
- **Custom Hardware-Inspired App Icon**: Designed and integrated a bespoke adaptive Android app icon featuring the Ulanzi TC001's titanium enclosure, illuminated 8×32 matrix clock display with cyan/amber glow, and knurled buttons.

---

## 🌟 What's New in v1.0.3

- **9-Screen Switcher Alignment**: Fixed screen index mismatch where clicking YouTube Analytics previously switched to Crypto. The screen list now matches the exact firmware `AppMode` order (`0: Clock`, `1: Indoor SHT30`, `2: Outdoor Weather`, `3: Crypto Ticker`, `4: YouTube Analytics`, `5: Live Airspace Radar`, `6: Battery Telemetry`, `7: Stopwatch & Timer`, `8: Ambient Screensaver`).
- **Form State Stability During Telemetry Polling**: Fixed an issue where the 2-second background telemetry refresh caused user form changes (including the Weekday Indicators switch, sliders, and dropdowns) to revert before saving. Form inputs are now preserved cleanly with manual "🔄 Reload from Device" on demand.
- **Save Feedback & State Refresh**: Enhanced "Save All Settings to Clock EEPROM & Flash" with immediate hardware status re-sync to ensure the UI strictly matches the committed flash state.
- **Hardware Reboot Dialog & Endpoint Fallback**: Added a tactile confirmation dialog to prevent accidental restarts, backed by dual endpoint routing (`POST /api/reboot` with automatic `/reboot` fallback) and live reconnection handling.
- **Virtual Clock Simulator**: Bundled a complete Python-based ESP32 REST API clock simulator (`virtual_clock_simulator.py` + `run-virtual-clock.bat`) for offline automated feature testing in Android emulators and host environments.

---

## 📑 Feature Matrix & 100% Web Portal Parity

| Feature | ESP32 Web Portal | Android Companion App | Notes |
| :--- | :---: | :---: | :--- |
| **Live Device State Loading** | ✅ | ✅ | Automatically reads active configuration from `/status` and `/api/wifi` |
| **Live 32×8 Matrix Mirror** | ✅ | ✅ | Real-time NeoPixel stream with high-contrast LED rendering |
| **Real-time Telemetry Deck** | ✅ | ✅ | SHT30 Temp & Humidity, Battery Gauge %, Volts, RSSI dBm, LDR |
| **Clock & Date Architecture** | ✅ | ✅ | Clock mode, date format, icon style, alignment, HH:mm:ss seconds |
| **7-Day Weekday Indicators** | ✅ | ✅ | 11 active phosphor colors, 10 inactive, 11 weekend, live row 7 preview |
| **9-Screen Rapid Switcher** | ✅ | ✅ | Clock, Indoor SHT30, Weather, Crypto, YouTube, Airspace, Battery, Timer, Screensaver |
| **Carousel & Auto-Rotate** | ✅ | ✅ | Auto-rotate, cycle interval (3s–120s), 8 transition effects (Slide, Curtain, Dissolve, etc.) |
| **9-Screen Rotation Mask** | ✅ | ✅ | Bitmask checkboxes to include/exclude any of the 9 firmware screens |
| **Ambient & Night Cycle** | ✅ | ✅ | LDR Auto-brightness, day ceiling (5–100%), quiet red night mode & hours schedule |
| **Battery Telemetry Format** | ✅ | ✅ | Alternating (% & V every 3s), Percentage Only, Voltage Only |
| **Hardware Timer / Stopwatch** | ✅ | ✅ | Mode selector, digital simulation readout, 1m/3m/5m/10m/15m/25m Pomodoro |
| **Ambient Screensavers** | ✅ | ✅ | 5 styles (Matrix Rain, Fire, Pac-Man, Game of Life, Dynamic Shuffle) + timeout |
| **Multi-Profile Wi-Fi (4 Slots)** | ✅ | ✅ | 4 profile slots, slot roaming, credential editor, 1-tap connect, 1-tap clear |
| **Wi-Fi RF Scanner** | ✅ | ✅ | Scan nearby APs via clock antenna and assign to any slot |
| **Meteorological Telemetry** | ✅ | ✅ | Open-Meteo city presets (Bratislava, Prague, Vienna, Berlin, London, NY) & coords |
| **Financial Markets & Crypto** | ✅ | ✅ | Asset presets (BTC, ETH, SOL, BNB, AAPL, TSLA, NVDA, SPX, GOLD) + custom ticker |
| **YouTube Channel Telemetry** | ✅ | ✅ | Channel @handle / ID & Google Cloud API key |
| **Home Assistant & MQTT** | ✅ | ✅ | Auto-discovery switch, broker IP/port, auth, topic prefix, live status |
| **Remote Hardware Reboot** | ✅ | ✅ | Instant `/reboot` trigger with automated reconnect handshake |
| **Interactive Pixel Canvas Studio** | ❌ | ✅ **Exclusive** | 32×8 touch/drag drawing engine, 13-color neon palette, pen/eraser/bucket, 7 retro presets, clock cloner & hardware push |
| **Android Push Notifications** | ❌ | ✅ **Exclusive** | Background `NotificationListenerService` forwarding calls, SMS, WhatsApp, etc. |
| **Spotify / Media Broadcaster** | ❌ | ✅ **Exclusive** | Live track title, artist, progress bar streaming via `/api/custom` |
| **Zero-Config Network Discovery** | ❌ | ✅ **Exclusive** | Dual-engine mDNS (`ulanzi-tc001.local`) + automated subnet scanner |
| **Auto-Reconnect Engine** | ❌ | ✅ **Exclusive** | Reactive `DataStore<Preferences>` restoring connection on app launch |

---

## 📱 Navigation & App Structure

The app is built around a modern Material 3 `NavigationBar` with 5 dedicated functional hubs:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          ULANZI TC001 EVOLVED                          │
│                     FW v2.2.0 • CONNECTED: 192.168.1.50                 │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  [Tab 1] Live Matrix   - Real-time 32×8 NeoPixel stream, telemetry     │
│                          gauges, rapid 9-screen switcher.              │
│                                                                        │
│  [Tab 2] Controls      - Hardware countdown & stopwatch timer with      │
│                          digital readout; 5-style screensaver engine.  │
│                                                                        │
│  [Tab 3] Media Sync    - Real-time Spotify/Now Playing broadcaster      │
│                          with playback transport controls.             │
│                                                                        │
│  [Tab 4] Push Alerts   - Android notification forwarding service,       │
│                          granular category filters, and push tester.   │
│                                                                        │
│  [Tab 5] Hardware      - Full EEPROM configuration deck (brightness,   │
│                          carousel, timezone, weather/crypto, Wi-Fi).   │
│                                                                        │
├────────────────────────────────────────────────────────────────────────┤
│  [ 🎛️ Live Matrix ] [ ⏱️ Controls ] [ 🎵 Media ] [ 🔔 Alerts ] [ ⚙️ Setup ] │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Key Capabilities

### 1. Live 32×8 NeoPixel Matrix & Telemetry Mirror
- Real-time display stream polling from `/api/display`.
- High-contrast pixel matrix rendering with accurate RGB LED simulation.
- Instrument telemetry deck:
  - 🌡️ **Temperature**: Sensirion SHT30 indoor sensor (`°C`).
  - 💧 **Relative Humidity**: Indoor moisture percentage (`%`).
  - 🔋 **Battery Gauge**: Battery percentage with linear progress bar and voltage (`V`).
  - 📶 **Wi-Fi Signal**: RSSI in `dBm` with connected SSID name.
  - 💡 **LDR Ambient Sensor**: Onboard photoresistor light level.
- Rapid screen switcher for all 9 firmware screens with previous/next stepping buttons.

### 2. Hardware Timer, Stopwatch & Screensavers
- **Interactive Timer Deck** (`/api/timer`):
  - Toggle between **Countdown Mode** and **Stopwatch Mode**.
  - Digital readout simulation with glowing monospace font.
  - One-tap quick presets: **1m**, **3m**, **5m**, **10m**, **15m**, and **25m (Pomodoro)**.
  - Remote Start, Pause, and Reset actions.
- **Ambient Screensavers** (`/api/screensaver`):
  - 5 visual presets: **Matrix Rain 🟢** (style 0), **Starfield ✨** (style 1), **Fire Flames 🔥** (style 2), **Retro Clock ⏳** (style 3), and **Plasma Flow 🌊** (style 4).
  - Configurable idle activation timeout selector (1m, 3m, 5m, 10m, 15m, 30m).
  - Immediate Start and Exit triggers.

### 3. Media Player & Spotify Live Broadcaster
- Background `MediaSessionManager` listener detecting active playback from Spotify, YouTube Music, Apple Music, podcasts, and local players.
- Streams live track title, artist, duration, and progress percentage to matrix via `POST /api/custom?name=spotify`.
- Brand-specific adaptive pixel styling (Spotify Green `#1ED760`, YouTube Red, Cyan) with 8×8 Spotify icon and dynamic bottom progress bar.
- Interactive tactile media transport deck in app (Play, Pause, Skip Next, Skip Previous, and Instant Push).
- Configurable update intervals (3s Realtime, 5s Balanced, 10s Eco), marquee formatting options (`Title • Artist` vs `Artist - Title`), and optional auto-clean on pause (`DELETE /api/custom`).

### 4. Android Notification Forwarding Engine
- Background `NotificationListenerService` capturing incoming calls, messaging (WhatsApp, Telegram, Signal, SMS), calendar, and third-party alerts.
- Granular category filtering, suppression of ongoing/persistent notifications (music players, download bars), and 2.5-second duplicate debounce cache.
- Automatic 8×8 pixel icon mapping (`bell`, `mail`, `clock`, `spotify`, `warning`, `heart`, `flame`, `wifi`), acoustic buzzer chime (0–3), and configurable display duration.
- In-app permission detection, quick system settings launcher, and live event stream ticker.
- Manual alert tester to compose and push custom notifications with icon and sound.

### 5. Hardware EEPROM Configuration Deck (100% Web Portal Replacement)
- Permanent clock configuration updates committed directly to ESP32 EEPROM/NVS flash via `/save`, with automatic initial state synchronization from `/status`.
- **Clock & Date Architecture**:
  - Clock Display Mode (Time Only, Combined Alternating, Date Only).
  - Date Notation Formats (DD.MM., DD MMM, MM/DD, DAY DD).
  - Clock Icon Styles (Dynamic Calendar 1-31, Classic Heart, Clock Face, None).
  - Text Alignment (Left-Aligned with Icon vs Centered in Window).
  - High-precision seconds counter switch (`show_sec` -> HH:mm:ss).
  - 7 Weekday indicator micro-bars along row 7 (`wd_bar`) with week start day (Monday ISO vs Sunday US).
  - Weekday Phosphor Color Palettes with real-time row 7 visualizer preview:
    - 11 Active Day Colors (Cyan, Orange, Green, Yellow, Red, White, Magenta, Electric Blue, Lime Green, Amber Gold, Soft Pink).
    - 10 Inactive Day Colors (Dim Auto, Slate Gray, Dim Amber, Dark Blue, Deep Wine, Forest Green, Charcoal, Muted Purple, Dark Cyan, Off).
    - 11 Weekend Colors (Same as Active, Bright Red, Coral Orange, Gold, Hot Pink, Purple, Cyan, Lime, Pure White, Sky Blue, Emerald).
- **Ambient Photometry & Night Cycle**:
  - LDR ambient auto-brightness toggle, daytime slider ceiling (5–100%).
  - Night Mode switch (ultra-dim red clock for dark environments).
  - Nighttime brightness floor slider (1–25%).
  - Quiet hours schedule start and end (0–23h).
- **Carousel Engine & Screen Sequence**:
  - Auto-rotate toggle and cycle dwell interval slider (3s–120s).
  - 8 Screen transition FX (Instant Cut, Slide Left, Slide Right, Slide Up, Slide Down, Crossfade, Curtain Wipe, Random FX).
  - 9-screen rotation mask bitmask checkboxes (Clock, Indoor SHT30, Weather, Crypto, YouTube, Airspace Radar, Battery, Timer, Screensaver).
- **Battery Telemetry Format**:
  - Alternating (% and Voltage every 3s), Percentage Only, Voltage Only.
- **Retro Ambient Screensaver**:
  - 5 procedural styles (Matrix Digital Rain, NeoPixel Fire, Pac-Man, Conway's Game of Life, Dynamic Shuffle).
  - Inactivity timeout selector (Disabled, 1m, 2m, 5m, 10m, 15m, 30m).
- **Meteorological Telemetry (Open-Meteo)**:
  - 13 Predefined European & worldwide city presets with auto-filling latitude and longitude.
- **Financial Markets & Crypto Ticker**:
  - Asset presets (BTC, ETH, SOL, BNB, AAPL, TSLA, NVDA, SPX, GOLD, EUR, USD) + custom ticker override.
- **YouTube Channel Telemetry**:
  - Creator channel handle / ID and Google Cloud Data API key.
- **Home Assistant & MQTT Auto-Discovery**:
  - Auto-discovery switch, broker IP/port, auth, topic prefix, and live MQTT connection status badge.
- **Multi-Network Wi-Fi Memory (4 Slots)**:
  - 4 hardware flash profile slots with best-signal roaming.
  - Active RF link badges and stored password indicators.
  - 1-tap connect, 1-tap clear, and onboard Wi-Fi RF scanner detecting nearby SSIDs.
- **Device Operations**: One-tap commit to EEPROM flash, remote hardware reboot, and "Reload from Device" re-synchronization.

---

## 📦 Installation & Release Binaries

Download the official signed release APK from **[GitHub Releases](https://github.com/hackra76/Ulanzi-TC001-Companion/releases)**.

### 📲 Installing on Samsung Galaxy (S26 Ultra / S25 / S24 / One UI 6 & 7) & Play Protect
1. **Samsung Auto Blocker (Unknown App Protection)**:
   - On recent Samsung Galaxy flagship devices (including S24, S25, and S26 Ultra running One UI 6 / 7), Samsung enables **Auto Blocker** by default. Auto Blocker blocks the installation of any app downloaded outside the Google Play Store or Galaxy Store.
   - **To install**: Navigate to **Settings** ➔ **Security and privacy** ➔ **Auto Blocker** (or *Unknown app protection*) and toggle it **OFF** temporarily during installation, or enable **Install unknown apps** for your browser / file manager.
2. **Google Play Protect**:
   - Official release APKs are signed with a dedicated release certificate (`CN=Ulanzi TC001 Companion`) using Android APK Signature Schemes v2 + v3 with debugging disabled.
   - If Android displays a prompt stating "Unrecognized developer", click **More details** ➔ **Install anyway**.

---

## 🛠️ Tech Stack & Architecture

- **Language & UI**: 100% Kotlin with modern **Jetpack Compose** & **Material 3**.
- **Architecture**: Single-Activity, declarative Compose navigation, MVI/MVVM pattern with StateFlow and Coroutines.
- **Persistence**: Jetpack DataStore Preferences (`androidx.datastore.preferences`).
- **Networking**: OkHttp 4.12 & Kotlinx Serialization 1.8 for fast, non-blocking REST API calls.
- **DPI & Multi-Ratio Scaling**: Hardware-accelerated Compose `Canvas` for 32×8 matrix rendering, flexible `FlowRow` chips, and auto-wrapping responsive layouts supporting ultra-high DPI flagships (S26 Ultra), standard phones, and foldables.
- **Requirements**:
  - Minimum SDK: Android 8.0 (API 26)
  - Target SDK: Android 15 (API 35)
  - Build Toolchain: Gradle 9.1 + OpenJDK 17 LTS

---

## 📦 Building from Source

```powershell
# Clone the repository
git clone https://github.com/hackra76/Ulanzi-TC001-Companion.git
cd Ulanzi-TC001-Companion

# Run unit tests
.\gradlew.bat test

# Build production signed release APK
.\gradlew.bat assembleRelease
```

The compiled release APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 📚 Documentation & Wiki

Explore the full documentation on our **[GitHub Wiki](https://github.com/hackra76/Ulanzi-TC001-Companion/wiki)**:
- **[User Guide](https://github.com/hackra76/Ulanzi-TC001-Companion/wiki/User-Guide)**
- **[Web Portal Parity Guide](https://github.com/hackra76/Ulanzi-TC001-Companion/wiki/Web-Portal-Parity)**
- **[Architecture & Tech Stack](https://github.com/hackra76/Ulanzi-TC001-Companion/wiki/Architecture-and-Tech-Stack)**
- **[Troubleshooting Guide](https://github.com/hackra76/Ulanzi-TC001-Companion/wiki/Troubleshooting)**

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
