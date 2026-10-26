# Ulanzi TC001 Companion 📱✨

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-brightgreen?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firmware](https://img.shields.io/badge/Ulanzi_TC001_Evolved-v2.2.0+-orange)](https://github.com/hackra76/Ulanzi-TC001-Evolved)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Official Android Companion App for the **[Ulanzi TC001 Smart Pixel Desktop Clock](https://github.com/hackra76/Ulanzi-TC001-Evolved)** running **Ulanzi TC001 Evolved** firmware.

---

## 🚀 Key Capabilities

- **🔍 Zero-Config Auto-Discovery**: Automatically discovers your Ulanzi TC001 clock on the local network via mDNS (`ulanzi-tc001.local`) or subnet scan.
- **📺 Live Matrix Mirror**: Real-time 32×8 NeoPixel stream rendered directly in the app with BMP snapshot export (`/api/display/screenshot`).
- **🔔 Android Notification Forwarding Engine (Phase 2)**:
  - Background `NotificationListenerService` capturing incoming calls, messaging (WhatsApp, Telegram, Signal, SMS), calendar, and third-party alerts.
  - Granular category filtering, suppression of ongoing/persistent notifications (music players, download bars), and 2.5-second duplicate debounce cache.
  - Automatic 8×8 pixel icon mapping (`bell`, `mail`, `clock`, `spotify`, `warning`, `heart`), acoustic buzzer chime (0-3), and configurable display duration.
  - In-app permission detection, quick system settings launcher, and live event stream ticker.
- **🎵 Now Playing Media Player & Spotify Live Broadcaster (Phase 3)**:
  - Background `MediaSessionManager` listener detecting active playback from Spotify, YouTube Music, Apple Music, podcasts, and local players.
  - Real-time track title, artist, album, duration, and live progress percentage streamed directly to the matrix display via `POST /api/custom?name=spotify`.
  - Brand-specific adaptive pixel styling (Spotify Green `#1ED760`, YouTube Red, Cyan) with 8×8 Spotify icon and dynamic bottom progress bar.
  - Interactive tactile media transport deck in app (Play, Pause, Skip Next, Skip Previous, and Instant Push).
  - Configurable update intervals (3s Realtime, 5s Balanced, 10s Eco), marquee formatting options (`Title • Artist` vs `Artist - Title`), and optional auto-clean on pause (`DELETE /api/custom`).
- **⏱️ Remote Hardware Countdown Timer & Stopwatch (Phase 4)**:
  - Direct hardware timer controller interfacing with `/api/timer` supporting both **Countdown** and **Stopwatch** modes.
  - Digital readout simulation display with large monospace retro typography and live sync with clock telemetry.
  - One-tap quick presets: 1m, 3m, 5m, 10m, 15m, and 25m Pomodoro cycles.
  - Tactile transport controls for instant remote Start, Pause, and Reset.
- **✨ Ambient Screensaver Engine (Phase 4)**:
  - Full remote control of clock screensavers via `/api/screensaver` with instant Start and Exit triggers.
  - 5 visual ambient presets: **Matrix Rain 🟢** (style 0), **Starfield ✨** (style 1), **Fire Flames 🔥** (style 2), **Retro Clock ⏳** (style 3), and **Plasma Flow 🌊** (style 4).
  - Configurable idle activation timeout selector (1m, 3m, 5m, 10m, 15m, 30m) persisted directly to device preferences.
- **💾 Jetpack DataStore Preferences & Auto-Reconnect Engine (Phase 5)**:
  - Reactive `DataStore<Preferences>` persistence storing last connected host, auto-reconnect preferences, default timer duration, screensaver styles, and hardware brightness presets.
  - Non-blocking background auto-reconnect engine on application startup with quick-connect chips for previous devices.
- **🎛️ Hardware EEPROM Configuration Deck (Phase 5)**:
  - Permanent clock configuration updates via `/save` committed directly to ESP32 EEPROM flash.
  - LDR ambient auto-brightness toggle with automatic sensor-driven adaptation.
  - Smooth slider controls for Daytime Brightness (0–100%), Nighttime Brightness (0–100%), and Carousel Auto-Rotate interval (3s–30s).
- **📊 Live Telemetry & System Monitoring**: View real-time indoor temperature & humidity (Sensirion SHT30), battery level/voltage, Wi-Fi RSSI, and active application state.

---

## 🛠️ Tech Stack & Architecture

- **Language & UI**: 100% Kotlin with modern **Jetpack Compose** & **Material 3** (Tactile Field Instrument industrial design aesthetic).
- **Architecture**: Single-Activity, declarative Compose navigation, MVI/MVVM pattern with StateFlow and Coroutines.
- **Networking**: OkHttp & Kotlinx Serialization for fast, non-blocking REST API calls.
- **Requirements**:
  - Minimum SDK: Android 8.0 (API 26)
  - Target SDK: Android 15 (API 35)
  - Build Toolchain: Gradle 9.1 + OpenJDK 17

---

## 📦 Building from Source

```powershell
# Clone the repository
git clone https://github.com/hackra76/Ulanzi-TC001-Companion.git
cd Ulanzi-TC001-Companion

# Build debug APK
.\gradlew.bat assembleDebug
```
The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
