package com.hackra.ulanzi.companion.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatus(
    @SerialName("state") val state: String = "unknown",
    @SerialName("ip") val ip: String = "",
    @SerialName("ap_ip") val apIp: String? = null,
    @SerialName("ssid") val ssid: String? = null,
    @SerialName("rssi") val rssi: Int = 0,
    @SerialName("rotate_sec") val rotateSec: Int = 8,
    @SerialName("auto_rotate") val autoRotate: Boolean = true,
    @SerialName("carousel_mask") val carouselMask: Int = 127,
    @SerialName("transition_fx") val transitionFx: Int = 0,
    @SerialName("active_app") val activeApp: Int = 0,
    @SerialName("app_name") val appName: String = "Clock",
    @SerialName("firmware_version") val firmwareVersion: String = "2.2.0",
    @SerialName("temperature_in") val temperatureIn: Double = 0.0,
    @SerialName("humidity_in") val humidityIn: Double = 0.0,
    @SerialName("battery_volts") val batteryVolts: Double = 0.0,
    @SerialName("battery_pct") val batteryPct: Int = 0,
    @SerialName("ldr_raw") val ldrRaw: Int = 0,
    @SerialName("auto_bri") val autoBri: Boolean = true,
    @SerialName("day_bri") val dayBri: Int = 60,
    @SerialName("night_bri") val nightBri: Int = 9,
    @SerialName("night_mode") val nightMode: Boolean = false,
    @SerialName("weather_temp") val weatherTemp: String? = null,
    @SerialName("weather_city") val weatherCity: String? = null,
    @SerialName("crypto_symbol") val cryptoSymbol: String? = null,
    @SerialName("crypto_price") val cryptoPrice: String? = null,
    @SerialName("yt_subscribers") val ytSubscribers: String? = null,
    @SerialName("timer_mode") val timerMode: String? = null,
    @SerialName("timer_state") val timerState: String? = null,
    @SerialName("timer_duration") val timerDuration: Int? = null,
    @SerialName("timer_remaining") val timerRemaining: Int? = null,
    @SerialName("timer_elapsed_ms") val timerElapsedMs: Long? = null,
    @SerialName("screensaver_style") val screensaverStyle: Int? = null,
    @SerialName("screensaver_timeout") val screensaverTimeout: Int? = null,
    @SerialName("screensaver_active") val screensaverActive: Boolean? = null,
    @SerialName("car_ord") val carOrd: String? = null,
    @SerialName("bat_mode") val batMode: Int = 0,
    @SerialName("clk_mode") val clkMode: Int = 0,
    @SerialName("show_sec") val showSeconds: Boolean = false,
    @SerialName("wd_bar") val weekdayBar: Boolean = false,
    @SerialName("wd_mon") val weekdayStartMon: Boolean = true,
    @SerialName("wd_acol") val weekdayActiveColIdx: Int = 0,
    @SerialName("wd_icol") val weekdayInactColIdx: Int = 0,
    @SerialName("wd_wcol") val weekdayWeekendColIdx: Int = 0,
    @SerialName("date_fmt") val dateFormat: Int = 0,
    @SerialName("clk_icon") val clockIcon: Int = 0,
    @SerialName("txt_align") val textAlign: Int = 0,
    @SerialName("night_start") val nightStart: Int = 23,
    @SerialName("night_end") val nightEnd: Int = 7,
    @SerialName("weather_lat") val weatherLat: Float? = null,
    @SerialName("weather_lon") val weatherLon: Float? = null,
    @SerialName("yt_channel") val ytChannel: String? = null,
    @SerialName("yt_has_key") val ytHasKey: Boolean = false,
    @SerialName("mqtt_en") val mqttEn: Boolean = false,
    @SerialName("mqtt_srv") val mqttSrv: String? = null,
    @SerialName("mqtt_port") val mqttPort: Int = 1883,
    @SerialName("mqtt_user") val mqttUser: String? = null,
    @SerialName("mqtt_has_pass") val mqttHasPass: Boolean = false,
    @SerialName("mqtt_pre") val mqttPre: String = "ulanzi-tc001",
    @SerialName("ui_lang") val uiLang: String? = null,
    @SerialName("mqtt_connected") val mqttConnected: Boolean = false,
    @SerialName("buzzer_playing") val buzzerPlaying: Boolean = false,
    @SerialName("free_heap") val freeHeap: Long? = null,
    @SerialName("uptime_sec") val uptimeSec: Long? = null,
    @SerialName("wifi_profiles") val wifiProfiles: List<WifiProfile> = emptyList()
)

@Serializable
data class ScreenSwitchResult(
    @SerialName("active_app") val activeApp: Int = 0,
    @SerialName("auto_rotate") val autoRotate: Boolean = false,
    @SerialName("app_name") val appName: String = ""
)

@Serializable
data class NotifyPayload(
    @SerialName("text") val text: String,
    @SerialName("icon") val icon: String? = "bell",
    @SerialName("sound") val sound: Int = 1,
    @SerialName("duration") val duration: Int = 5
)

@Serializable
data class CustomAppPayload(
    @SerialName("text") val text: String,
    @SerialName("icon") val icon: String? = "spotify",
    @SerialName("color") val color: List<Int>? = listOf(30, 215, 96),
    @SerialName("progress") val progress: Int? = null,
    @SerialName("duration") val duration: Int = 8,
    @SerialName("lifetime") val lifetime: Int = 60,
    @SerialName("scroll") val scroll: Boolean = true
)

@Serializable
data class TimerResponse(
    @SerialName("mode") val mode: String = "countdown",
    @SerialName("state") val state: String = "stopped",
    @SerialName("duration") val duration: Int = 300,
    @SerialName("remaining") val remaining: Int = 300,
    @SerialName("elapsed_ms") val elapsedMs: Long = 0L
)

@Serializable
data class ScreensaverResponse(
    @SerialName("style") val style: Int = 0,
    @SerialName("active_style") val activeStyle: Int = 0,
    @SerialName("timeout") val timeout: Int = 5,
    @SerialName("active") val active: Boolean = false,
    @SerialName("idle_ms") val idleMs: Long = 0L
)

@Serializable
data class DisplayMatrixResponse(
    @SerialName("pixels") val pixels: List<List<String>> = emptyList()
)

@Serializable
data class WifiProfile(
    @SerialName("slot") val slot: Int,
    @SerialName("ssid") val ssid: String = "",
    @SerialName("has_pass") val hasPass: Boolean = false,
    @SerialName("connected") val connected: Boolean = false,
    @SerialName("active") val active: Boolean = false
) {
    val isCurrentlyActive: Boolean get() = connected || active
}

@Serializable
data class WifiResponse(
    @SerialName("connected") val connected: Boolean = false,
    @SerialName("active_ssid") val activeSsid: String = "",
    @SerialName("ip") val ip: String = "",
    @SerialName("rssi") val rssi: Int = 0,
    @SerialName("profiles") val profiles: List<WifiProfile> = emptyList()
)

@Serializable
data class GenericStatusResponse(
    @SerialName("status") val status: String = "ok",
    @SerialName("app") val app: String? = null
)

@Serializable
data class WifiScanResult(
    @SerialName("ssid") val ssid: String = "",
    @SerialName("rssi") val rssi: Int = 0,
    @SerialName("secure") val secure: Boolean = false
)

@Serializable
data class CanvasPayload(
    @SerialName("pixels") val pixels: List<List<String>>,
    @SerialName("duration") val duration: Int = 60
)

@Serializable
data class OtaCheckResponse(
    @SerialName("current_version") val currentVersion: String = "",
    @SerialName("latest_version") val latestVersion: String = "",
    @SerialName("has_update") val hasUpdate: Boolean = false,
    @SerialName("download_url") val downloadUrl: String = "",
    @SerialName("title") val title: String? = null,
    @SerialName("release_notes") val releaseNotes: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null
)

@Serializable
data class GitHubReleaseAsset(
    @SerialName("name") val name: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = "",
    @SerialName("size") val size: Long = 0L
)

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("published_at") val publishedAt: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("assets") val assets: List<GitHubReleaseAsset> = emptyList()
)

@Serializable
data class BuzzerResponse(
    @SerialName("status") val status: String = "ok",
    @SerialName("sound") val sound: String? = null,
    @SerialName("action") val action: String? = null,
    @SerialName("playing") val playing: Boolean = false
)


