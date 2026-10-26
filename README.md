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
- **🎵 Now Playing Media Sync**: Broadcasts real-time media player metadata (Spotify, YouTube Music, podcasts) with artist, title, smooth scrolling ticker, and bottom progress bar (`/api/custom`).
- **🎛️ Remote Navigation & Controls**: Switch screens on the fly, toggle carousel auto-rotation, start/pause/reset stopwatch & countdown timers, and trigger ambient screensavers.
- **📊 Live Telemetry & Quick Adjustments**: View real-time indoor temperature & humidity (Sensirion SHT30), battery level/voltage, Wi-Fi RSSI, and adjust day/night brightness.

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
