package com.hackra.ulanzi.companion.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.datastore.DevicePreferences
import com.hackra.ulanzi.companion.data.datastore.DevicePreferencesManager
import com.hackra.ulanzi.companion.data.discovery.DiscoveryState
import com.hackra.ulanzi.companion.data.media.MediaPlaybackState
import com.hackra.ulanzi.companion.data.media.MediaSyncManager
import com.hackra.ulanzi.companion.data.media.MediaSyncSettings
import com.hackra.ulanzi.companion.data.media.MediaTextFormat
import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.NotifyPayload
import com.hackra.ulanzi.companion.data.model.WifiResponse
import com.hackra.ulanzi.companion.data.notification.ForwardedNotificationLog
import com.hackra.ulanzi.companion.data.notification.NotificationSettings
import com.hackra.ulanzi.companion.data.notification.NotificationSettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab(val title: String, val shortTitle: String, val iconName: String) {
    DASHBOARD("Live Matrix", "Matrix", "dashboard"),
    CONTROLS("Controls", "Controls", "timer"),
    MEDIA("Media Sync", "Media", "music"),
    ALERTS("Push Alerts", "Alerts", "notifications"),
    SETTINGS("Hardware Setup", "Settings", "settings")
}

data class MainScreenUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val deviceStatus: DeviceStatus? = null,
    val displayMatrix: DisplayMatrixResponse? = null,
    val discoveryState: DiscoveryState = DiscoveryState.Idle,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val hasListenerPermission: Boolean = false,
    val notificationLogs: List<ForwardedNotificationLog> = emptyList(),
    val mediaState: MediaPlaybackState = MediaPlaybackState(),
    val mediaSyncSettings: MediaSyncSettings = MediaSyncSettings(),
    val devicePreferences: DevicePreferences = DevicePreferences(),
    val selectedTab: NavigationTab = NavigationTab.DASHBOARD,
    val wifiStatus: WifiResponse? = null,
    val scannedWifiList: List<com.hackra.ulanzi.companion.data.model.WifiScanResult> = emptyList(),
    val isScanningWifi: Boolean = false,
    val isRebooting: Boolean = false,
    val actionMessage: String? = null
)

class MainScreenViewModel(
    private val repository: UlanziRepository,
    private val notificationSettingsManager: NotificationSettingsManager,
    private val mediaSyncManager: MediaSyncManager,
    private val devicePreferencesManager: DevicePreferencesManager
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(NavigationTab.DASHBOARD)
    private val _wifiStatus = MutableStateFlow<WifiResponse?>(null)
    private val _scannedWifiList = MutableStateFlow<List<com.hackra.ulanzi.companion.data.model.WifiScanResult>>(emptyList())
    private val _isScanningWifi = MutableStateFlow(false)
    private val _isRebooting = MutableStateFlow(false)
    private val _actionMessage = MutableStateFlow<String?>(null)

    private val repositoryFlow = combine(
        repository.connectionState,
        repository.deviceStatus,
        repository.displayMatrix,
        repository.discoveryState
    ) { conn, status, matrix, discovery ->
        Tuple4(conn, status, matrix, discovery)
    }

    private val notificationFlow = combine(
        notificationSettingsManager.settings,
        notificationSettingsManager.hasListenerPermission,
        notificationSettingsManager.logs
    ) { settings, perm, logs ->
        Tuple3(settings, perm, logs)
    }

    private val mediaFlow = combine(
        mediaSyncManager.mediaState,
        mediaSyncManager.settings
    ) { media, settings ->
        media to settings
    }

    private val extraFlow = combine(
        devicePreferencesManager.preferences,
        _selectedTab,
        _wifiStatus,
        _scannedWifiList,
        _isScanningWifi
    ) { prefs, tab, wifi, scanned, isScanning ->
        Tuple5(prefs, tab, wifi, scanned, isScanning)
    }

    private val rebootAndMsgFlow = combine(
        _isRebooting,
        _actionMessage
    ) { isReboot, msg ->
        isReboot to msg
    }

    val uiState: StateFlow<MainScreenUiState> = combine(
        repositoryFlow,
        notificationFlow,
        mediaFlow,
        extraFlow,
        rebootAndMsgFlow
    ) { repo, notif, media, extra, rebootMsg ->
        MainScreenUiState(
            connectionState = repo.first,
            deviceStatus = repo.second,
            displayMatrix = repo.third,
            discoveryState = repo.fourth,
            notificationSettings = notif.first,
            hasListenerPermission = notif.second,
            notificationLogs = notif.third,
            mediaState = media.first,
            mediaSyncSettings = media.second,
            devicePreferences = extra.first,
            selectedTab = extra.second,
            wifiStatus = extra.third,
            scannedWifiList = extra.fourth,
            isScanningWifi = extra.fifth,
            isRebooting = rebootMsg.first,
            actionMessage = rebootMsg.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenUiState(
            connectionState = repository.connectionState.value,
            deviceStatus = repository.deviceStatus.value,
            displayMatrix = repository.displayMatrix.value,
            discoveryState = repository.discoveryState.value,
            notificationSettings = notificationSettingsManager.settings.value,
            hasListenerPermission = notificationSettingsManager.hasListenerPermission.value,
            notificationLogs = notificationSettingsManager.logs.value,
            mediaState = mediaSyncManager.mediaState.value,
            mediaSyncSettings = mediaSyncManager.settings.value,
            devicePreferences = devicePreferencesManager.preferences.value
        )
    )

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun startDiscovery() {
        repository.startDiscovery(viewModelScope)
    }

    fun stopDiscovery() {
        repository.stopDiscovery()
    }

    fun connect(hostOrIp: String) {
        repository.connectToDevice(hostOrIp, viewModelScope)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun switchScreen(appIndex: Int, autoRotate: Boolean? = null) {
        viewModelScope.launch {
            repository.switchScreen(appIndex, autoRotate)
        }
    }

    fun toggleAutoRotate(enabled: Boolean) {
        viewModelScope.launch {
            val currentApp = uiState.value.deviceStatus?.activeApp ?: 0
            repository.switchScreen(currentApp, enabled)
        }
    }

    fun stepScreenNext() {
        viewModelScope.launch {
            val current = uiState.value.deviceStatus?.activeApp ?: 0
            val next = (current + 1) % 9
            repository.switchScreen(next)
        }
    }

    fun stepScreenPrev() {
        viewModelScope.launch {
            val current = uiState.value.deviceStatus?.activeApp ?: 0
            val prev = (current + 8) % 9
            repository.switchScreen(prev)
        }
    }

    fun sendNotification(text: String, icon: String = "bell", sound: Int = 1, duration: Int = 5) {
        viewModelScope.launch {
            repository.sendNotification(NotifyPayload(text = text, icon = icon, sound = sound, duration = duration))
            _actionMessage.value = "Notification sent to clock display!"
        }
    }

    fun pushCustomApp(name: String, text: String, icon: String = "spotify", progress: Int? = null) {
        viewModelScope.launch {
            repository.pushCustomApp(name, CustomAppPayload(text = text, icon = icon, progress = progress))
        }
    }

    fun controlTimer(action: String, mode: String? = null, duration: Int? = null) {
        viewModelScope.launch {
            repository.controlTimer(action, mode, duration)
        }
    }

    fun controlScreensaver(style: Int? = null, timeout: Int? = null, action: String? = null) {
        viewModelScope.launch {
            repository.controlScreensaver(style, timeout, action)
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            repository.refreshStatus()
            repository.refreshDisplayMatrix()
        }
    }

    fun refreshWifi() {
        viewModelScope.launch {
            repository.getWifiStatus().onSuccess {
                _wifiStatus.value = it
            }
        }
    }

    fun connectWifiSlot(slot: Int) {
        viewModelScope.launch {
            _actionMessage.value = "Connecting clock to Wi-Fi slot $slot..."
            repository.connectWifiSlot(slot).onSuccess {
                _actionMessage.value = "Connected to Wi-Fi slot $slot!"
                refreshWifi()
            }.onFailure {
                _actionMessage.value = "Failed to connect to Wi-Fi slot $slot: ${it.message}"
            }
        }
    }

    fun scanWifiNetworks() {
        viewModelScope.launch {
            _isScanningWifi.value = true
            _actionMessage.value = "Scanning nearby Wi-Fi networks from clock RF antenna..."
            repository.scanWifi().onSuccess { results ->
                _scannedWifiList.value = results
                _isScanningWifi.value = false
                _actionMessage.value = "Discovered ${results.size} nearby Wi-Fi networks!"
            }.onFailure { err ->
                _isScanningWifi.value = false
                _actionMessage.value = "Wi-Fi scan failed: ${err.message}"
            }
        }
    }

    fun deleteWifiSlot(slot: Int) {
        viewModelScope.launch {
            _actionMessage.value = "Clearing Wi-Fi Profile #$slot from clock..."
            repository.deleteWifiSlot(slot).onSuccess {
                _actionMessage.value = "Wi-Fi Profile #$slot cleared!"
                refreshWifi()
            }.onFailure { err ->
                _actionMessage.value = "Failed to clear profile #$slot: ${err.message}"
            }
        }
    }

    fun rebootClock() {
        viewModelScope.launch {
            _isRebooting.value = true
            _actionMessage.value = "Sending reboot command to clock..."
            val result = repository.rebootDevice()
            if (result.isSuccess) {
                _actionMessage.value = "Clock reboot initiated. Re-probing connection..."
                delay(3000)
                _isRebooting.value = false
                repository.refreshStatus()
            } else {
                _isRebooting.value = false
                val err = result.exceptionOrNull()?.message ?: "Reboot failed"
                _actionMessage.value = "Reboot failed: $err"
            }
        }
    }

    // Notification forwarding management methods
    fun refreshNotificationPermission(): Boolean {
        return notificationSettingsManager.refreshPermission()
    }

    fun getNotificationSettingsIntent(): Intent {
        return notificationSettingsManager.getSettingsIntent()
    }

    fun setNotificationForwarding(enabled: Boolean) {
        notificationSettingsManager.setForwardingEnabled(enabled)
    }

    fun setForwardCalls(enabled: Boolean) {
        notificationSettingsManager.setForwardCalls(enabled)
    }

    fun setForwardMessaging(enabled: Boolean) {
        notificationSettingsManager.setForwardMessaging(enabled)
    }

    fun setForwardCalendar(enabled: Boolean) {
        notificationSettingsManager.setForwardCalendar(enabled)
    }

    fun setForwardOther(enabled: Boolean) {
        notificationSettingsManager.setForwardOther(enabled)
    }

    fun setNotificationSound(sound: Int) {
        notificationSettingsManager.setSound(sound)
    }

    fun setNotificationDuration(duration: Int) {
        notificationSettingsManager.setDuration(duration)
    }

    fun setIgnoreOngoing(ignore: Boolean) {
        notificationSettingsManager.setIgnoreOngoing(ignore)
    }

    fun setIncludeAppName(include: Boolean) {
        notificationSettingsManager.setIncludeAppName(include)
    }

    fun clearNotificationLogs() {
        notificationSettingsManager.clearLogs()
    }

    // Media Player & Spotify Live Broadcaster methods (Phase 3)
    fun setMediaSyncEnabled(enabled: Boolean) {
        mediaSyncManager.setSyncEnabled(enabled)
    }

    fun setMediaAutoSwitch(autoSwitch: Boolean) {
        mediaSyncManager.setAutoSwitch(autoSwitch)
    }

    fun setMediaDeleteOnPause(deleteOnPause: Boolean) {
        mediaSyncManager.setDeleteOnPause(deleteOnPause)
    }

    fun setMediaPushInterval(intervalSec: Int) {
        mediaSyncManager.setPushInterval(intervalSec)
    }

    fun setMediaFormat(format: MediaTextFormat) {
        mediaSyncManager.setFormat(format)
    }

    fun mediaPlay() {
        mediaSyncManager.play()
    }

    fun mediaPause() {
        mediaSyncManager.pause()
    }

    fun mediaSkipNext() {
        mediaSyncManager.skipNext()
    }

    fun mediaSkipPrevious() {
        mediaSyncManager.skipPrevious()
    }

    fun forceSyncMedia() {
        mediaSyncManager.triggerSyncToClock()
    }

    // Phase 4: Timer & Stopwatch Remote Controls
    fun startCountdown(durationSec: Int) {
        viewModelScope.launch {
            repository.controlTimer(action = "start", mode = "countdown", duration = durationSec)
        }
    }

    fun startStopwatch() {
        viewModelScope.launch {
            repository.controlTimer(action = "start", mode = "stopwatch")
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            repository.controlTimer(action = "pause")
        }
    }

    fun resetTimer() {
        viewModelScope.launch {
            repository.controlTimer(action = "reset")
        }
    }

    // Phase 4: Screensaver Controls
    fun triggerScreensaver(style: Int, timeoutMin: Int) {
        viewModelScope.launch {
            repository.controlScreensaver(style = style, timeout = timeoutMin, action = "start")
        }
    }

    fun stopScreensaver() {
        viewModelScope.launch {
            repository.controlScreensaver(action = "stop")
        }
    }

    // Hardware Settings & DataStore Persistence
    fun saveHardwareSettings(
        // Clock & Date Architecture
        clkMode: Int? = null,
        dateFormat: Int? = null,
        clockIcon: Int? = null,
        textAlign: Int? = null,
        showSeconds: Boolean? = null,
        weekdayBar: Boolean? = null,
        weekdayStartMon: Boolean? = null,
        weekdayActiveColIdx: Int? = null,
        weekdayInactColIdx: Int? = null,
        weekdayWeekendColIdx: Int? = null,

        // Ambient Photometry & Night Cycle
        autoBri: Boolean? = null,
        dayBri: Int? = null,
        nightMode: Boolean? = null,
        nightBri: Int? = null,
        nightStart: Int? = null,
        nightEnd: Int? = null,

        // Carousel Engine & Screen Rotation
        autoRotate: Boolean? = null,
        rotateSec: Int? = null,
        transitionFx: Int? = null,
        activeApp: Int? = null,
        carouselMask: Int? = null,
        carOrd: String? = null,

        // Battery Telemetry & Screensaver
        batMode: Int? = null,
        ssStyle: Int? = null,
        ssTime: Int? = null,

        // Weather & Feeds
        weatherCity: String? = null,
        weatherLat: Float? = null,
        weatherLon: Float? = null,
        cryptoSymbol: String? = null,
        ytChannel: String? = null,
        ytKey: String? = null,

        // Home Assistant & MQTT
        mqttEn: Boolean? = null,
        mqttSrv: String? = null,
        mqttPort: Int? = null,
        mqttUser: String? = null,
        mqttPass: String? = null,
        mqttPre: String? = null,

        // Wi-Fi Multi-Profile Management
        wifiSlot: Int? = null,
        wifiSsid: String? = null,
        wifiPass: String? = null,
        deleteWifiSlot: Int? = null
    ) {
        viewModelScope.launch {
            val current = uiState.value.deviceStatus

            val effectiveClkMode = clkMode ?: current?.clkMode ?: 0
            val effectiveDateFmt = dateFormat ?: current?.dateFormat ?: 0
            val effectiveClkIcon = clockIcon ?: current?.clockIcon ?: 0
            val effectiveTextAlign = textAlign ?: current?.textAlign ?: 0
            val effectiveShowSec = showSeconds ?: current?.showSeconds ?: false
            val effectiveWdBar = weekdayBar ?: current?.weekdayBar ?: false
            val effectiveWdMon = weekdayStartMon ?: current?.weekdayStartMon ?: true
            val effectiveWdAcol = weekdayActiveColIdx ?: current?.weekdayActiveColIdx ?: 0
            val effectiveWdIcol = weekdayInactColIdx ?: current?.weekdayInactColIdx ?: 0
            val effectiveWdWcol = weekdayWeekendColIdx ?: current?.weekdayWeekendColIdx ?: 0

            val effectiveAutoBri = autoBri ?: current?.autoBri ?: true
            val effectiveDayBri = dayBri ?: current?.dayBri ?: 35
            val effectiveNightMode = nightMode ?: current?.nightMode ?: false
            val effectiveNightBri = nightBri ?: current?.nightBri ?: 2
            val effectiveNightStart = nightStart ?: current?.nightStart ?: 23
            val effectiveNightEnd = nightEnd ?: current?.nightEnd ?: 7

            val effectiveAutoRotate = autoRotate ?: current?.autoRotate ?: true
            val effectiveRotate = rotateSec ?: current?.rotateSec ?: 8
            val effectiveTransFx = transitionFx ?: current?.transitionFx ?: 0
            val effectiveActiveApp = activeApp ?: current?.activeApp ?: 0
            val effectiveMask = carouselMask ?: current?.carouselMask ?: 0x1FF
            val effectiveCarOrd = carOrd ?: current?.carOrd ?: "0,1,2,3,4,5,6,7,8"

            val effectiveBatMode = batMode ?: current?.batMode ?: 2
            val effectiveSsStyle = ssStyle ?: current?.screensaverStyle ?: 0
            val effectiveSsTime = ssTime ?: current?.screensaverTimeout ?: 5

            val effectiveCity = weatherCity ?: current?.weatherCity ?: "Bratislava"
            val effectiveLat = weatherLat ?: current?.weatherLat ?: 48.1486f
            val effectiveLon = weatherLon ?: current?.weatherLon ?: 17.1077f
            val effectiveCrypto = cryptoSymbol ?: current?.cryptoSymbol ?: "BTC"
            val effectiveYtChannel = ytChannel ?: current?.ytChannel ?: ""
            val effectiveMqttEn = mqttEn ?: current?.mqttEn ?: false
            val effectiveMqttSrv = mqttSrv ?: current?.mqttSrv ?: ""
            val effectiveMqttPort = mqttPort ?: current?.mqttPort ?: 1883
            val effectiveMqttUser = mqttUser ?: current?.mqttUser ?: ""
            val effectiveMqttPre = mqttPre ?: current?.mqttPre ?: "ulanzi-tc001"

            val params = mutableMapOf<String, String>()

            // 1. Clock & Date Architecture
            params["clk_mode"] = effectiveClkMode.toString()
            params["date_fmt"] = effectiveDateFmt.toString()
            params["clk_icon"] = effectiveClkIcon.toString()
            params["txt_align"] = effectiveTextAlign.toString()
            if (effectiveShowSec) params["show_sec"] = "1"
            if (effectiveWdBar) params["wd_bar"] = "1"
            params["wd_mon"] = if (effectiveWdMon) "1" else "0"
            params["wd_acol"] = effectiveWdAcol.toString()
            params["wd_icol"] = effectiveWdIcol.toString()
            params["wd_wcol"] = effectiveWdWcol.toString()

            // 2. Ambient Photometry & Night Cycle
            if (effectiveAutoBri) params["auto_bri"] = "1"
            params["day_bri"] = effectiveDayBri.coerceIn(5, 100).toString()
            if (effectiveNightMode) params["night_mode"] = "1"
            params["night_bri"] = effectiveNightBri.coerceIn(1, 25).toString()
            params["night_start"] = effectiveNightStart.coerceIn(0, 23).toString()
            params["night_end"] = effectiveNightEnd.coerceIn(0, 23).toString()

            // 3. Carousel Engine & Screen Rotation
            if (effectiveAutoRotate) params["auto_rotate"] = "1"
            params["rotate"] = effectiveRotate.coerceIn(3, 120).toString()
            params["trans_fx"] = effectiveTransFx.coerceIn(0, 7).toString()
            params["active_app"] = effectiveActiveApp.coerceIn(0, 8).toString()
            params["car_ord"] = effectiveCarOrd
            for (i in 0..8) {
                if ((effectiveMask and (1 shl i)) != 0) {
                    params["car_app_$i"] = "1"
                }
            }

            // 4. Battery Telemetry & Screensaver
            params["bat_mode"] = effectiveBatMode.toString()
            params["ss_style"] = effectiveSsStyle.toString()
            params["ss_time"] = effectiveSsTime.toString()

            // 5. Weather & Feeds
            params["w_city"] = effectiveCity
            params["w_lat"] = effectiveLat.toString()
            params["w_lon"] = effectiveLon.toString()
            params["c_sym_custom"] = effectiveCrypto

            // 6. YouTube
            if (effectiveYtChannel.isNotBlank()) params["yt_channel"] = effectiveYtChannel
            if (!ytKey.isNullOrBlank()) params["yt_key"] = ytKey

            // 7. MQTT & Home Assistant
            if (effectiveMqttEn) params["mqtt_en"] = "1"
            if (effectiveMqttSrv.isNotBlank()) params["mqtt_srv"] = effectiveMqttSrv
            params["mqtt_port"] = effectiveMqttPort.toString()
            if (effectiveMqttUser.isNotBlank()) params["mqtt_user"] = effectiveMqttUser
            if (!mqttPass.isNullOrBlank()) params["mqtt_pass"] = mqttPass
            params["mqtt_pre"] = effectiveMqttPre

            // 8. Wi-Fi Multi-Profile Management
            if (deleteWifiSlot != null && deleteWifiSlot in 1..4) {
                params["del_wifi_$deleteWifiSlot"] = "1"
            } else if (wifiSlot != null && !wifiSsid.isNullOrBlank() && wifiSlot in 1..4) {
                val ssidKey = if (wifiSlot == 1) "ssid" else "ssid_$wifiSlot"
                val passKey = if (wifiSlot == 1) "password" else "password_$wifiSlot"
                params[ssidKey] = wifiSsid
                if (!wifiPass.isNullOrBlank()) {
                    params[passKey] = wifiPass
                }
            }

            val res = repository.saveSettings(params)
            if (res.isSuccess) {
                _actionMessage.value = "Hardware EEPROM settings saved to clock!"
                devicePreferencesManager.setHardwareBrightnessDefaults(
                    dayBri = effectiveDayBri,
                    nightBri = effectiveNightBri,
                    autoBri = effectiveAutoBri
                )
                devicePreferencesManager.setCarouselRotateSec(effectiveRotate)
                delay(400)
                repository.refreshStatus()
                refreshWifi()
            } else {
                _actionMessage.value = "Failed to save settings: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun setAutoReconnect(enabled: Boolean) {
        devicePreferencesManager.setAutoReconnect(enabled)
    }

    fun setLastDeviceHost(host: String) {
        devicePreferencesManager.setLastDeviceHost(host)
    }

    fun setTimerDefaultDuration(durationSec: Int) {
        devicePreferencesManager.setTimerDefaultDuration(durationSec)
    }

    fun setScreensaverDefaults(style: Int, timeoutMin: Int) {
        devicePreferencesManager.setScreensaverDefaults(style, timeoutMin)
    }
}

private data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Tuple3<A, B, C>(val first: A, val second: B, val third: C)
