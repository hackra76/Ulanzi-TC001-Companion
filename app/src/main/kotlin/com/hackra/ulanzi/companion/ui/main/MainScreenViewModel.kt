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
import kotlinx.coroutines.Job
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

enum class DashboardViewMode {
    LIVE_STREAM,
    CANVAS_STUDIO
}

enum class CanvasTool {
    PEN,
    ERASER,
    BUCKET
}

data class CanvasState(
    val dashboardViewMode: DashboardViewMode = DashboardViewMode.LIVE_STREAM,
    val pixels: List<List<String>> = List(8) { List(32) { "#000000" } },
    val selectedColor: String = "#FF0055",
    val tool: CanvasTool = CanvasTool.PEN,
    val isSending: Boolean = false,
    val durationSec: Int = 60
)

data class FirmwareUpdateState(
    val isChecking: Boolean = false,
    val hasUpdate: Boolean = false,
    val currentVersion: String? = null,
    val latestVersion: String? = null,
    val releaseTitle: String? = null,
    val releaseNotes: String? = null,
    val downloadUrl: String? = null,
    val publishedAt: String? = null,
    val isUpdating: Boolean = false,
    val progressPercent: Int = 0,
    val updateStage: String? = null,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

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
    val actionMessage: String? = null,
    val dashboardViewMode: DashboardViewMode = DashboardViewMode.LIVE_STREAM,
    val canvasPixels: List<List<String>> = List(8) { List(32) { "#000000" } },
    val canvasSelectedColor: String = "#FF0055",
    val canvasTool: CanvasTool = CanvasTool.PEN,
    val isSendingCanvas: Boolean = false,
    val canvasDisplayDurationSec: Int = 60,
    val firmwareUpdateState: FirmwareUpdateState = FirmwareUpdateState(),
    val showAboutDialog: Boolean = false
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
    private val _canvasState = MutableStateFlow(CanvasState())
    private val _firmwareUpdateState = MutableStateFlow(FirmwareUpdateState())
    private val _showAboutDialog = MutableStateFlow(false)

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

    private val rebootAndCanvasFlow = combine(
        _isRebooting,
        _actionMessage,
        _canvasState,
        _firmwareUpdateState,
        _showAboutDialog
    ) { isReboot, msg, canvas, fwState, showAbout ->
        Tuple5(isReboot, msg, canvas, fwState, showAbout)
    }

    val uiState: StateFlow<MainScreenUiState> = combine(
        repositoryFlow,
        notificationFlow,
        mediaFlow,
        extraFlow,
        rebootAndCanvasFlow
    ) { repo, notif, media, extra, rebootCanvas ->
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
            isRebooting = rebootCanvas.first,
            actionMessage = rebootCanvas.second,
            dashboardViewMode = rebootCanvas.third.dashboardViewMode,
            canvasPixels = rebootCanvas.third.pixels,
            canvasSelectedColor = rebootCanvas.third.selectedColor,
            canvasTool = rebootCanvas.third.tool,
            isSendingCanvas = rebootCanvas.third.isSending,
            canvasDisplayDurationSec = rebootCanvas.third.durationSec,
            firmwareUpdateState = rebootCanvas.fourth,
            showAboutDialog = rebootCanvas.fifth
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

    private var actionMessageJob: Job? = null

    fun setActionMessage(message: String?, autoDismissDelayMs: Long = 3500L) {
        actionMessageJob?.cancel()
        _actionMessage.value = message
        if (message != null && autoDismissDelayMs > 0) {
            actionMessageJob = viewModelScope.launch {
                delay(autoDismissDelayMs)
                if (_actionMessage.value == message) {
                    _actionMessage.value = null
                }
            }
        }
    }

    fun clearActionMessage() {
        actionMessageJob?.cancel()
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
            setActionMessage("Notification sent to clock display!")
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

    fun triggerBuzzerPreset(preset: String) {
        viewModelScope.launch {
            repository.controlBuzzer(sound = preset).onSuccess {
                setActionMessage("Buzzer: ${preset.uppercase()} triggered")
            }.onFailure {
                setActionMessage("Buzzer request failed: ${it.localizedMessage}")
            }
        }
    }

    fun stopBuzzer() {
        viewModelScope.launch {
            repository.controlBuzzer(stop = true).onSuccess {
                setActionMessage("Buzzer playback stopped")
            }.onFailure {
                setActionMessage("Stop failed: ${it.localizedMessage}")
            }
        }
    }

    fun playCustomRtttl(rtttl: String) {
        val clean = rtttl.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            repository.controlBuzzer(rtttl = clean).onSuccess {
                setActionMessage("Playing RTTTL melody...")
            }.onFailure {
                setActionMessage("RTTTL playback failed: ${it.localizedMessage}")
            }
        }
    }

    fun playCustomTone(freq: Int, durationMs: Int) {
        viewModelScope.launch {
            repository.controlBuzzer(freq = freq, durationMs = durationMs).onSuccess {
                setActionMessage("Emitted ${freq}Hz tone (${durationMs}ms)")
            }.onFailure {
                setActionMessage("Tone emit failed: ${it.localizedMessage}")
            }
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
            setActionMessage("Connecting clock to Wi-Fi slot $slot...")
            repository.connectWifiSlot(slot).onSuccess {
                setActionMessage("Connected to Wi-Fi slot $slot!")
                refreshWifi()
            }.onFailure {
                setActionMessage("Failed to connect to Wi-Fi slot $slot: ${it.message}")
            }
        }
    }

    fun scanWifiNetworks() {
        viewModelScope.launch {
            _isScanningWifi.value = true
            setActionMessage("Scanning nearby Wi-Fi networks from clock RF antenna...")
            repository.scanWifi().onSuccess { results ->
                _scannedWifiList.value = results
                _isScanningWifi.value = false
                setActionMessage("Discovered ${results.size} nearby Wi-Fi networks!")
            }.onFailure { err ->
                _isScanningWifi.value = false
                setActionMessage("Wi-Fi scan failed: ${err.message}")
            }
        }
    }

    fun deleteWifiSlot(slot: Int) {
        viewModelScope.launch {
            setActionMessage("Clearing Wi-Fi Profile #$slot from clock...")
            repository.deleteWifiSlot(slot).onSuccess {
                setActionMessage("Wi-Fi Profile #$slot cleared!")
                refreshWifi()
            }.onFailure { err ->
                setActionMessage("Failed to clear profile #$slot: ${err.message}")
            }
        }
    }

    fun rebootClock() {
        viewModelScope.launch {
            _isRebooting.value = true
            setActionMessage("Sending reboot command to clock...")
            val result = repository.rebootDevice()
            if (result.isSuccess) {
                setActionMessage("Clock reboot initiated. Re-probing connection...")
                delay(3000)
                _isRebooting.value = false
                repository.refreshStatus()
            } else {
                _isRebooting.value = false
                val err = result.exceptionOrNull()?.message ?: "Reboot failed"
                setActionMessage("Reboot failed: $err")
            }
        }
    }

    // ---------------------------------------------------------
    // Firmware Update (OTA & Local Flash) & About Methods
    // ---------------------------------------------------------
    fun setShowAboutDialog(show: Boolean) {
        _showAboutDialog.value = show
    }

    fun dismissFirmwareStatus() {
        _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
            errorMessage = null,
            isSuccess = false,
            updateStage = null,
            isUpdating = false
        )
    }

    fun checkForFirmwareUpdate() {
        viewModelScope.launch {
            val currentVer = uiState.value.deviceStatus?.firmwareVersion
            _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                isChecking = true,
                currentVersion = currentVer,
                errorMessage = null
            )
            repository.checkOtaUpdate(currentVer ?: "")
                .onSuccess { res ->
                    _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                        isChecking = false,
                        hasUpdate = res.hasUpdate,
                        currentVersion = res.currentVersion ?: currentVer,
                        latestVersion = res.latestVersion,
                        releaseTitle = res.title ?: res.latestVersion,
                        releaseNotes = res.releaseNotes,
                        downloadUrl = res.downloadUrl,
                        publishedAt = res.publishedAt,
                        errorMessage = null
                    )
                }
                .onFailure { err ->
                    _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                        isChecking = false,
                        errorMessage = err.message ?: "Failed to check for updates"
                    )
                }
        }
    }

    fun installCloudOta(downloadUrl: String? = null) {
        viewModelScope.launch {
            val targetUrl = downloadUrl ?: _firmwareUpdateState.value.downloadUrl
            if (targetUrl.isNullOrBlank()) {
                _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                    errorMessage = "No OTA firmware binary URL specified"
                )
                return@launch
            }
            _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                isUpdating = true,
                isSuccess = false,
                progressPercent = 0,
                updateStage = "Requesting device to fetch and install update...",
                errorMessage = null
            )
            repository.installCloudOta(targetUrl)
                .onSuccess {
                    _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                        isUpdating = false,
                        isSuccess = true,
                        progressPercent = 100,
                        updateStage = "Cloud OTA initiated! The device is downloading and will reboot automatically."
                    )
                }
                .onFailure { err ->
                    _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                        isUpdating = false,
                        isSuccess = false,
                        errorMessage = "Cloud OTA failed: ${err.message}"
                    )
                }
        }
    }

    fun uploadLocalFirmware(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            if (bytes.isEmpty()) {
                _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                    errorMessage = "Selected file is empty."
                )
                return@launch
            }
            if (bytes.size > 8 * 1024 * 1024) {
                _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                    errorMessage = "File is too large for ESP32 flash partition (${bytes.size / 1024 / 1024} MB)."
                )
                return@launch
            }
            _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                isUpdating = true,
                isSuccess = false,
                progressPercent = 0,
                updateStage = "Connecting to device update service...",
                errorMessage = null
            )
            repository.uploadFirmwareBinary(bytes, fileName) { pct ->
                val totalKb = bytes.size / 1024
                val sentKb = (bytes.size * pct) / 100 / 1024
                _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                    progressPercent = pct,
                    updateStage = "Flashing: $pct% ($sentKb KB / $totalKb KB)"
                )
            }.onSuccess {
                _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                    isUpdating = false,
                    isSuccess = true,
                    progressPercent = 100,
                    updateStage = "Firmware flashed successfully! Device is rebooting..."
                )
            }.onFailure { err ->
                _firmwareUpdateState.value = _firmwareUpdateState.value.copy(
                    isUpdating = false,
                    isSuccess = false,
                    errorMessage = "Firmware flashing failed: ${err.message}"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Canvas Studio Methods
    // ---------------------------------------------------------
    fun setDashboardViewMode(mode: DashboardViewMode) {
        _canvasState.value = _canvasState.value.copy(dashboardViewMode = mode)
    }

    fun setCanvasSelectedColor(hex: String) {
        _canvasState.value = _canvasState.value.copy(selectedColor = hex)
    }

    fun setCanvasTool(tool: CanvasTool) {
        _canvasState.value = _canvasState.value.copy(tool = tool)
    }

    fun setCanvasDuration(seconds: Int) {
        _canvasState.value = _canvasState.value.copy(durationSec = seconds)
    }

    fun drawPixel(x: Int, y: Int) {
        if (x !in 0..31 || y !in 0..7) return
        val current = _canvasState.value
        val newPixels = when (current.tool) {
            CanvasTool.PEN -> {
                if (current.pixels.getOrNull(y)?.getOrNull(x) == current.selectedColor) return
                current.pixels.mapIndexed { r, row ->
                    if (r == y) {
                        row.mapIndexed { c, col -> if (c == x) current.selectedColor else col }
                    } else row
                }
            }
            CanvasTool.ERASER -> {
                if (current.pixels.getOrNull(y)?.getOrNull(x) == "#000000") return
                current.pixels.mapIndexed { r, row ->
                    if (r == y) {
                        row.mapIndexed { c, col -> if (c == x) "#000000" else col }
                    } else row
                }
            }
            CanvasTool.BUCKET -> {
                floodFill(current.pixels, x, y, current.selectedColor)
            }
        }
        _canvasState.value = current.copy(pixels = newPixels)
    }

    fun clearCanvas() {
        val blank = List(8) { List(32) { "#000000" } }
        _canvasState.value = _canvasState.value.copy(pixels = blank)
    }

    fun fillCanvas(colorHex: String) {
        val filled = List(8) { List(32) { colorHex } }
        _canvasState.value = _canvasState.value.copy(pixels = filled)
    }

    fun grabDisplayToCanvas() {
        val matrix = uiState.value.displayMatrix?.pixels
        if (matrix != null && matrix.size == 8 && matrix.all { it.size == 32 }) {
            _canvasState.value = _canvasState.value.copy(pixels = matrix)
            setActionMessage("📥 Display cloned into Canvas Studio!")
        } else {
            setActionMessage("No active display matrix stream to clone.")
        }
    }

    fun loadCanvasPreset(presetName: String) {
        val preset = CanvasPresets.getPreset(presetName)
        _canvasState.value = _canvasState.value.copy(pixels = preset)
        setActionMessage("Loaded preset: ${presetName.uppercase()}")
    }

    fun sendCanvasToClock() {
        viewModelScope.launch {
            val state = _canvasState.value
            _canvasState.value = state.copy(isSending = true)
            val res = repository.sendCanvasPixels(state.pixels, state.durationSec)
            _canvasState.value = _canvasState.value.copy(isSending = false)
            if (res.isSuccess) {
                setActionMessage("🎨 Drawing sent to Ulanzi TC001 matrix (${state.durationSec}s)!")
                delay(300)
                repository.refreshDisplayMatrix()
            } else {
                setActionMessage("Failed to send canvas: ${res.exceptionOrNull()?.message}")
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
        deleteWifiSlot: Int? = null,

        // Optional custom feedback message
        successMessage: String? = "Hardware EEPROM settings saved to clock!"
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
            if (!ytKey.isNullOrBlank()) {
                params["yt_key"] = ytKey
            } else if (current?.ytHasKey == true) {
                params["yt_key"] = "********"
            }

            // 7. MQTT & Home Assistant
            if (effectiveMqttEn) params["mqtt_en"] = "1"
            if (effectiveMqttSrv.isNotBlank()) params["mqtt_srv"] = effectiveMqttSrv
            params["mqtt_port"] = effectiveMqttPort.toString()
            if (effectiveMqttUser.isNotBlank()) params["mqtt_user"] = effectiveMqttUser
            if (!mqttPass.isNullOrBlank()) {
                params["mqtt_pass"] = mqttPass
            } else if (current?.mqttHasPass == true) {
                params["mqtt_pass"] = "********"
            }
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
                if (successMessage != null) {
                    setActionMessage(successMessage)
                }
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
                setActionMessage("Failed to save settings: ${res.exceptionOrNull()?.message}")
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

private fun floodFill(pixels: List<List<String>>, startX: Int, startY: Int, fillColor: String): List<List<String>> {
    val targetColor = pixels.getOrNull(startY)?.getOrNull(startX) ?: return pixels
    if (targetColor.equals(fillColor, ignoreCase = true)) return pixels

    val mutable = pixels.map { it.toMutableList() }.toMutableList()
    val queue = ArrayDeque<Pair<Int, Int>>()
    queue.add(startX to startY)
    val visited = Array(8) { BooleanArray(32) }
    visited[startY][startX] = true

    while (queue.isNotEmpty()) {
        val (cx, cy) = queue.removeFirst()
        mutable[cy][cx] = fillColor

        val neighbors = listOf(
            cx + 1 to cy,
            cx - 1 to cy,
            cx to cy + 1,
            cx to cy - 1
        )
        for ((nx, ny) in neighbors) {
            if (nx in 0..31 && ny in 0..7 && !visited[ny][nx]) {
                if (mutable[ny][nx].equals(targetColor, ignoreCase = true)) {
                    visited[ny][nx] = true
                    queue.add(nx to ny)
                }
            }
        }
    }
    return mutable
}

object CanvasPresets {
    fun getPreset(name: String): List<List<String>> {
        val grid = MutableList(8) { MutableList(32) { "#000000" } }
        when (name.lowercase()) {
            "heart" -> {
                val heartCoords = listOf(
                    1 to 3, 1 to 4, 1 to 6, 1 to 7,
                    2 to 2, 2 to 3, 2 to 4, 2 to 5, 2 to 6, 2 to 7, 2 to 8,
                    3 to 2, 3 to 3, 3 to 4, 3 to 5, 3 to 6, 3 to 7, 3 to 8,
                    4 to 3, 4 to 4, 4 to 5, 4 to 6, 4 to 7,
                    5 to 4, 5 to 5, 5 to 6,
                    6 to 5
                )
                heartCoords.forEach { (r, c) -> grid[r][c] = "#FF0055" }
                grid[2][3] = "#FFAACC"
                val textPattern = listOf(
                    1 to 11, 2 to 11, 3 to 11, 4 to 11, 5 to 11, 5 to 12, 5 to 13, 1 to 13, 2 to 13, 3 to 13, 4 to 13,
                    1 to 15, 2 to 15, 3 to 15, 4 to 15, 5 to 15, 5 to 16, 5 to 17,
                    1 to 20, 2 to 19, 2 to 21, 3 to 19, 3 to 20, 3 to 21, 4 to 19, 4 to 21, 5 to 19, 5 to 21,
                    1 to 23, 2 to 23, 3 to 23, 4 to 23, 5 to 23, 2 to 24, 3 to 24, 1 to 25, 2 to 25, 3 to 25, 4 to 25, 5 to 25,
                    1 to 27, 1 to 28, 1 to 29, 2 to 29, 3 to 28, 4 to 27, 5 to 27, 5 to 28, 5 to 29
                )
                textPattern.forEach { (r, c) -> grid[r][c] = "#00E5FF" }
            }
            "pacman" -> {
                val pacman = listOf(
                    1 to 23, 1 to 24, 1 to 25,
                    2 to 22, 2 to 23, 2 to 24, 2 to 25, 2 to 26,
                    3 to 21, 3 to 22, 3 to 23, 3 to 24,
                    4 to 21, 4 to 22,
                    5 to 21, 5 to 22, 5 to 23, 5 to 24,
                    6 to 22, 6 to 23, 6 to 24, 6 to 25, 6 to 26,
                    7 to 23, 7 to 24, 7 to 25
                )
                pacman.forEach { (r, c) -> grid[r][c] = "#FFD600" }
                grid[4][28] = "#FFFFFF"
                grid[4][30] = "#FFFFFF"

                val ghost = listOf(
                    1 to 4, 1 to 5, 1 to 6,
                    2 to 3, 2 to 4, 2 to 5, 2 to 6, 2 to 7,
                    3 to 3, 3 to 4, 3 to 5, 3 to 6, 3 to 7,
                    4 to 3, 4 to 4, 4 to 5, 4 to 6, 4 to 7,
                    5 to 3, 5 to 4, 5 to 5, 5 to 6, 5 to 7,
                    6 to 3, 6 to 5, 6 to 7
                )
                ghost.forEach { (r, c) -> grid[r][c] = "#FF1744" }
                grid[3][4] = "#FFFFFF"; grid[3][6] = "#FFFFFF"
                grid[3][5] = "#2979FF"; grid[3][7] = "#2979FF"

                val ghost2 = listOf(
                    1 to 13, 1 to 14, 1 to 15,
                    2 to 12, 2 to 13, 2 to 14, 2 to 15, 2 to 16,
                    3 to 12, 3 to 13, 3 to 14, 3 to 15, 3 to 16,
                    4 to 12, 4 to 13, 4 to 14, 4 to 15, 4 to 16,
                    5 to 12, 5 to 13, 5 to 14, 5 to 15, 5 to 16,
                    6 to 12, 6 to 14, 6 to 16
                )
                ghost2.forEach { (r, c) -> grid[r][c] = "#00E5FF" }
                grid[3][13] = "#FFFFFF"; grid[3][15] = "#FFFFFF"
                grid[3][14] = "#2979FF"; grid[3][16] = "#2979FF"
            }
            "space_invaders" -> {
                val invader = listOf(
                    1 to 6, 1 to 10,
                    2 to 7, 2 to 9,
                    3 to 5, 3 to 6, 3 to 7, 3 to 8, 3 to 9, 3 to 10, 3 to 11,
                    4 to 4, 4 to 5, 4 to 7, 4 to 8, 4 to 9, 4 to 11, 4 to 12,
                    5 to 4, 5 to 5, 5 to 6, 5 to 7, 5 to 8, 5 to 9, 5 to 10, 5 to 11, 5 to 12,
                    6 to 4, 6 to 6, 6 to 10, 6 to 12,
                    7 to 3, 7 to 5, 7 to 11, 7 to 13
                )
                invader.forEach { (r, c) -> grid[r][c] = "#00E676" }

                val invader2 = listOf(
                    1 to 22, 1 to 26,
                    2 to 23, 2 to 25,
                    3 to 21, 3 to 22, 3 to 23, 3 to 24, 3 to 25, 3 to 26, 3 to 27,
                    4 to 20, 4 to 21, 4 to 23, 4 to 24, 4 to 25, 4 to 27, 4 to 28,
                    5 to 20, 5 to 21, 5 to 22, 5 to 23, 5 to 24, 5 to 25, 5 to 26, 5 to 27, 5 to 28,
                    6 to 20, 6 to 22, 6 to 26, 6 to 28,
                    7 to 19, 7 to 21, 7 to 27, 7 to 29
                )
                invader2.forEach { (r, c) -> grid[r][c] = "#D500F9" }
            }
            "vu_meter" -> {
                val heights = listOf(3, 5, 7, 6, 8, 4, 6, 7, 5, 8, 6, 7, 4, 3, 5, 2)
                for (i in 0 until 16) {
                    val h = heights[i]
                    val c1 = i * 2
                    val c2 = i * 2 + 1
                    for (r in 0 until 8) {
                        val rowFromBottom = 7 - r
                        if (rowFromBottom < h) {
                            val color = when {
                                rowFromBottom >= 6 -> "#FF1744"
                                rowFromBottom >= 4 -> "#FFD600"
                                else -> "#00E676"
                            }
                            grid[r][c1] = color
                            grid[r][c2] = color
                        }
                    }
                }
            }
            "rainbow" -> {
                val rainbowColors = listOf(
                    "#FF0055", "#FF5722", "#FFC107", "#4CAF50",
                    "#00BCD4", "#2196F3", "#9C27B0", "#E91E63"
                )
                for (r in 0 until 8) {
                    val col = rainbowColors[r % rainbowColors.size]
                    for (c in 0 until 32) {
                        grid[r][c] = col
                    }
                }
            }
            "fire" -> {
                val fireGradients = listOf(
                    "#FF1744", "#FF5722", "#FF9100", "#FFAB00",
                    "#FFD600", "#FFFF00", "#FFFFFF", "#FFFFFF"
                )
                for (r in 0 until 8) {
                    val color = fireGradients[r]
                    for (c in 0 until 32) {
                        val skip = (r < 2 && (c % 3 == 0 || c % 5 == 0))
                        grid[r][c] = if (skip) "#000000" else color
                    }
                }
            }
            "smiley" -> {
                // Happy face with cool shades
                val face = listOf(
                    1 to 14, 1 to 15, 1 to 16, 1 to 17,
                    2 to 12, 2 to 13, 2 to 18, 2 to 19,
                    3 to 11, 3 to 20,
                    4 to 11, 4 to 20,
                    5 to 11, 5 to 20,
                    6 to 12, 6 to 13, 6 to 18, 6 to 19,
                    7 to 14, 7 to 15, 7 to 16, 7 to 17
                )
                face.forEach { (r, c) -> grid[r][c] = "#FFD600" }
                // Shades (Sunglasses)
                val glasses = listOf(
                    3 to 12, 3 to 13, 3 to 14, 3 to 15, 3 to 16, 3 to 17, 3 to 18, 3 to 19,
                    4 to 13, 4 to 14, 4 to 17, 4 to 18
                )
                glasses.forEach { (r, c) -> grid[r][c] = "#2196F3" }
                // Smile
                grid[5][14] = "#FF1744"; grid[5][17] = "#FF1744"
                grid[6][15] = "#FF1744"; grid[6][16] = "#FF1744"
            }
        }
        return grid
    }
}
