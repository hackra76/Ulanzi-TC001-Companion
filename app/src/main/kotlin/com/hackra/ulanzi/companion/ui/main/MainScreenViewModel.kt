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

enum class NavigationTab(val title: String, val iconName: String) {
    DASHBOARD("Live Matrix", "dashboard"),
    CONTROLS("Controls", "timer"),
    MEDIA("Media Sync", "music"),
    ALERTS("Push Alerts", "notifications"),
    SETTINGS("Hardware Setup", "settings")
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
        _isRebooting,
        _actionMessage
    ) { prefs, tab, wifi, isReboot, msg ->
        ExtraUiState(prefs, tab, wifi, isReboot, msg)
    }

    val uiState: StateFlow<MainScreenUiState> = combine(
        repositoryFlow,
        notificationFlow,
        mediaFlow,
        extraFlow
    ) { repo, notif, media, extra ->
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
            devicePreferences = extra.prefs,
            selectedTab = extra.tab,
            wifiStatus = extra.wifi,
            isRebooting = extra.isRebooting,
            actionMessage = extra.actionMessage
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

    fun rebootClock() {
        viewModelScope.launch {
            _isRebooting.value = true
            _actionMessage.value = "Rebooting Ulanzi TC001 clock..."
            repository.rebootDevice()
            delay(4000)
            _isRebooting.value = false
            _actionMessage.value = "Clock reboot initiated. Re-probing connection..."
            repository.refreshStatus()
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

    // Phase 5: Hardware Settings & DataStore Persistence
    fun saveHardwareSettings(
        dayBri: Int? = null,
        nightBri: Int? = null,
        autoBri: Boolean? = null,
        rotateSec: Int? = null,
        autoRotate: Boolean? = null,
        carouselMask: Int? = null,
        transitionFx: Int? = null,
        city: String? = null,
        weatherKey: String? = null,
        cryptoSymbol: String? = null,
        cryptoCurrency: String? = null,
        ytChannel: String? = null,
        timezone: String? = null,
        timeFormat24h: Boolean? = null,
        ntpServer: String? = null,
        wifiSlot: Int? = null,
        wifiSsid: String? = null,
        wifiPass: String? = null
    ) {
        viewModelScope.launch {
            val params = mutableMapOf<String, String>()
            dayBri?.let { params["day_bri"] = it.toString() }
            nightBri?.let { params["night_bri"] = it.toString() }
            autoBri?.let { params["auto_bri"] = if (it) "1" else "0" }
            rotateSec?.let { params["rotate_sec"] = it.toString() }
            autoRotate?.let { params["auto_rotate"] = if (it) "1" else "0" }
            carouselMask?.let { params["carousel_mask"] = it.toString() }
            transitionFx?.let { params["transition_fx"] = it.toString() }
            city?.let { if (it.isNotBlank()) params["city"] = it }
            weatherKey?.let { if (it.isNotBlank()) params["weather_key"] = it }
            cryptoSymbol?.let { if (it.isNotBlank()) params["crypto_symbol"] = it }
            cryptoCurrency?.let { if (it.isNotBlank()) params["crypto_currency"] = it }
            ytChannel?.let { if (it.isNotBlank()) params["yt_channel"] = it }
            timezone?.let { if (it.isNotBlank()) params["timezone"] = it }
            timeFormat24h?.let { params["time_format"] = if (it) "24" else "12" }
            ntpServer?.let { if (it.isNotBlank()) params["ntp_server"] = it }
            if (wifiSlot != null && !wifiSsid.isNullOrBlank()) {
                params["slot"] = wifiSlot.toString()
                params["ssid"] = wifiSsid
                if (!wifiPass.isNullOrBlank()) params["pass"] = wifiPass
            }

            if (params.isNotEmpty()) {
                val res = repository.saveSettings(params)
                if (res.isSuccess) {
                    _actionMessage.value = "Hardware EEPROM settings saved to clock!"
                } else {
                    _actionMessage.value = "Failed to save settings: ${res.exceptionOrNull()?.message}"
                }
                if (dayBri != null || nightBri != null || autoBri != null) {
                    val current = uiState.value.devicePreferences
                    devicePreferencesManager.setHardwareBrightnessDefaults(
                        dayBri = dayBri ?: current.dayBrightness,
                        nightBri = nightBri ?: current.nightBrightness,
                        autoBri = autoBri ?: current.autoBrightness
                    )
                }
                rotateSec?.let { devicePreferencesManager.setCarouselRotateSec(it) }
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

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Tuple3<A, B, C>(val first: A, val second: B, val third: C)
private data class ExtraUiState(
    val prefs: DevicePreferences,
    val tab: NavigationTab,
    val wifi: WifiResponse?,
    val isRebooting: Boolean,
    val actionMessage: String?
)
