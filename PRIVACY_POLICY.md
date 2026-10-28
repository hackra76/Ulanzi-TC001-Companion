# Privacy Policy for Ulanzi TC001 Companion

**Last updated:** October 28, 2026

**Ulanzi TC001 Companion** is an open-source Android application designed to configure, control, and stream telemetry to your local Ulanzi TC001 Smart Pixel Clock.

## 1. Zero Personal Data Collection
The application does not collect, record, transmit, or monetize any personal information, location data, analytics, device identifiers, or usage logs.

## 2. Local Network Communication
All communication occurs strictly over your local Wi-Fi network (LAN) directly between your Android device and the ESP32 microcontroller running on your Ulanzi TC001 clock via local HTTP REST endpoints (e.g. `http://<local-clock-ip>/api/...`). No data is sent to external cloud servers or third-party services.

## 3. Permissions Used
- **`INTERNET` & `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE`**: Required solely to communicate with your clock on your local Wi-Fi subnet and detect network connectivity.
- **`CHANGE_WIFI_MULTICAST_STATE`**: Used for local device discovery (mDNS / SSDP) across your home network.
- **Notification Access (`BIND_NOTIFICATION_LISTENER_SERVICE`)**:
  - Optional feature used exclusively to format and display incoming app notifications (title/text/icon) on the physical LED matrix display of your clock.
  - Notification content is processed ephemerally in real-time in device memory. It is never logged, saved, or transmitted to any server or third party.

## 4. Third-Party Services & Ads
The app contains no advertisements, no tracking SDKs, no analytics tools (such as Google Analytics or Firebase), and no third-party data-sharing integrations.

## 5. Security & Storage
Preferences (such as your clock's local IP address and UI options) are stored locally on your device using Android Jetpack DataStore and are deleted upon uninstalling the application.

## 6. Open Source & Transparency
The source code for this application is publicly available for audit at:  
https://github.com/hackra76/Ulanzi-TC001-Companion

## 7. Contact
For questions regarding this privacy policy, open an issue on the GitHub repository or contact the developer via the official repository.

