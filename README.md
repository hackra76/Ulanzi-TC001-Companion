# Ulanzi TC001 Companion 📱✨

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-brightgreen?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firmware](https://img.shields.io/badge/Ulanzi_TC001_Evolved-v2.2.0+-orange)](https://github.com/hackra76/Ulanzi-TC001-Evolved)
[![Release](https://img.shields.io/badge/Release-v1.0.0-success?logo=github)](https://github.com/hackra76/Ulanzi-TC001-Companion/releases)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Official Android Companion App for the **[Ulanzi TC001 Smart Pixel Desktop Clock](https://github.com/hackra76/Ulanzi-TC001-Evolved)** running **Ulanzi TC001 Evolved** firmware.

Engineered with modern **Material 3 Design**, tactile retro-futuristic telemetry decks, and full hardware integration, this companion app serves as a **complete, 100% standalone replacement for the ESP32 web portal**.

---

## 📑 Feature Matrix & Web Portal Parity

| Feature | ESP32 Web Portal | Android Companion App | Notes |
| :--- | :---: | :---: | :--- |
| **Live 32×8 Matrix Mirror** | ✅ | ✅ | Real-time NeoPixel stream with high-contrast LED rendering |
| **Real-time Telemetry Deck** | ✅ | ✅ | SHT30 Temp & Humidity, Battery Gauge %, Volts, RSSI dBm, LDR |
| **9-Screen Rapid Switcher** | ✅ | ✅ | Clock, Weather, Crypto, YouTube, Battery, SHT30, Timer, Screensaver, Wi-Fi |
| **Carousel & Auto-Rotate** | ✅ | ✅ | Toggle rotation, configure cycle (3s–30s), transition FX (Instant, Slide, Fade) |
| **Screen Rotation Mask** | ✅ | ✅ | Bitmask checkboxes to include/exclude individual screens |
| **Hardware Countdown / Stopwatch** | ✅ | ✅ | Mode selector, digital simulation readout, 1m/3m/5m/10m/15m/25m Pomodoro |
| **Ambient Screensavers** | ✅ | ✅ | 5 styles (Matrix Rain, Starfield, Fire, Retro Clock, Plasma Flow) + timeout |
| **Multi-Network Wi-Fi Memory** | ✅ | ✅ | 4 profile slots, slot roaming, credential editor, connection switcher |
| **LDR Auto-Brightness & Limits** | ✅ | ✅ | Sensor-driven auto-brightness, daytime slider (0–100%), nighttime slider |
| **Timezone & NTP Sync** | ✅ | ✅ | POSIX timezone string, 12h/24h toggle, custom NTP server |
| **Online APIs & Market Feeds** | ✅ | ✅ | OpenWeatherMap City & API key, CoinGecko Crypto symbol & currency, YouTube |
| **Remote Hardware Reboot** | ✅ | ✅ | Instant `/reboot` trigger with automated reconnect handshake |
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

### 5. Hardware EEPROM Configuration Deck (Web Portal Replacement)
- Permanent clock configuration updates via `/save` committed directly to ESP32 EEPROM flash.
- **Illumination & Auto-Brightness**: LDR ambient auto-brightness toggle, daytime slider (0–100%), nighttime slider (0–100%).
- **Carousel & Transitions**: Auto-rotate toggle, rotate cycle slider (3s–30s), transition FX selector (0: Instant Cut, 1: Smooth Slide, 2: Cross Fade), screen rotation mask checkboxes.
- **Time & Localization**: POSIX timezone string (with quick presets for CET/CEST, UTC, EST, PST), 12h/24h toggle, NTP server.
- **Online Feeds**: OpenWeatherMap city & API key, CoinGecko crypto symbol & currency, YouTube channel ID.
- **Multi-Network Wi-Fi Memory**: Multi-slot profile switcher and credential editor.
- **Device Operations**: One-tap commit to EEPROM flash, remote hardware reboot, and auto-reconnect switch.

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
