package com.hackra.ulanzi.companion.ui.main

import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.datastore.DevicePreferences
import com.hackra.ulanzi.companion.data.datastore.DevicePreferencesManager
import com.hackra.ulanzi.companion.data.discovery.DiscoveryState
import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.GenericStatusResponse
import com.hackra.ulanzi.companion.data.model.NotifyPayload
import com.hackra.ulanzi.companion.data.model.ScreenSwitchResult
import com.hackra.ulanzi.companion.data.model.ScreensaverResponse
import com.hackra.ulanzi.companion.data.model.TimerResponse
import com.hackra.ulanzi.companion.data.model.WifiResponse
import com.hackra.ulanzi.companion.data.media.MediaPlaybackState
import com.hackra.ulanzi.companion.data.media.MediaSyncManager
import com.hackra.ulanzi.companion.data.media.MediaSyncSettings
import com.hackra.ulanzi.companion.data.media.MediaTextFormat
import com.hackra.ulanzi.companion.data.notification.NotificationSettings
import com.hackra.ulanzi.companion.data.notification.NotificationSettingsManager
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initiallyDisconnected() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)
        assertEquals(ConnectionState.Disconnected, viewModel.uiState.first().connectionState)
    }

    @Test
    fun notificationSettings_updatesProperly() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager(
            initialSettings = NotificationSettings(isForwardingEnabled = false),
            initialHasPermission = true
        )
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)
        assertEquals(false, viewModel.uiState.value.notificationSettings.isForwardingEnabled)
        assertTrue(viewModel.uiState.value.hasListenerPermission)

        viewModel.setNotificationForwarding(true)
        assertEquals(true, notifManager.settings.value.isForwardingEnabled)

        viewModel.setNotificationSound(3)
        assertEquals(3, notifManager.settings.value.sound)

        viewModel.setNotificationDuration(12)
        assertEquals(12, notifManager.settings.value.duration)
    }

    @Test
    fun mediaSyncSettings_updatesProperly() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(
            repository = repo,
            initialSettings = MediaSyncSettings(isEnabled = false, pushIntervalSec = 5),
            initialPlaybackState = MediaPlaybackState(title = "Starlight", artist = "Muse", isPlaying = true, progressPct = 42)
        )
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        assertEquals("Starlight", viewModel.uiState.value.mediaState.title)
        assertEquals("Muse", viewModel.uiState.value.mediaState.artist)
        assertEquals(42, viewModel.uiState.value.mediaState.progressPct)
        assertEquals(false, viewModel.uiState.value.mediaSyncSettings.isEnabled)

        viewModel.setMediaSyncEnabled(true)
        assertEquals(true, mediaManager.settings.value.isEnabled)

        viewModel.setMediaPushInterval(10)
        assertEquals(10, mediaManager.settings.value.pushIntervalSec)

        viewModel.setMediaFormat(MediaTextFormat.ARTIST_TITLE)
        assertEquals(MediaTextFormat.ARTIST_TITLE, mediaManager.settings.value.format)
    }

    @Test
    fun timerControls_invokeRepository() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        viewModel.startCountdown(600)
        assertEquals(Triple("start", "countdown", 600), repo.lastTimerCall)

        viewModel.startStopwatch()
        assertEquals(Triple("start", "stopwatch", null), repo.lastTimerCall)

        viewModel.pauseTimer()
        assertEquals(Triple("pause", null, null), repo.lastTimerCall)

        viewModel.resetTimer()
        assertEquals(Triple("reset", null, null), repo.lastTimerCall)
    }

    @Test
    fun screensaverControls_invokeRepository() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        viewModel.triggerScreensaver(style = 2, timeoutMin = 15)
        assertEquals(Triple(2, 15, "start"), repo.lastScreensaverCall)

        viewModel.stopScreensaver()
        assertEquals(Triple(null, null, "stop"), repo.lastScreensaverCall)
    }

    @Test
    fun devicePreferences_andHardwareSave_updatesProperly() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        viewModel.setAutoReconnect(false)
        assertEquals(false, prefsManager.preferences.value.autoReconnect)

        viewModel.setLastDeviceHost("192.168.1.188")
        assertEquals("192.168.1.188", prefsManager.preferences.value.lastDeviceHost)

        viewModel.setTimerDefaultDuration(900)
        assertEquals(900, prefsManager.preferences.value.timerDefaultDurationSec)

        viewModel.setScreensaverDefaults(style = 3, timeoutMin = 10)
        assertEquals(3, prefsManager.preferences.value.screensaverStyle)
        assertEquals(10, prefsManager.preferences.value.screensaverTimeoutMin)

        viewModel.saveHardwareSettings(dayBri = 80, nightBri = 15, autoBri = false, rotateSec = 12)
        assertEquals("80", repo.lastSavedSettings?.get("day_bri"))
        assertEquals("15", repo.lastSavedSettings?.get("night_bri"))
        assertEquals(null, repo.lastSavedSettings?.get("auto_bri")) // Unchecked checkboxes are omitted in form POST
        assertEquals("12", repo.lastSavedSettings?.get("rotate"))

        assertEquals(80, prefsManager.preferences.value.dayBrightness)
        assertEquals(15, prefsManager.preferences.value.nightBrightness)
        assertEquals(false, prefsManager.preferences.value.autoBrightness)
        assertEquals(12, prefsManager.preferences.value.carouselRotateSec)
    }

    @Test
    fun navigationTab_selectionUpdatesState() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertEquals(NavigationTab.DASHBOARD, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(NavigationTab.CONTROLS)
        assertEquals(NavigationTab.CONTROLS, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(NavigationTab.SETTINGS)
        assertEquals(NavigationTab.SETTINGS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun fullHardwareSettings_serializesAllParams() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        viewModel.saveHardwareSettings(
            clkMode = 1,
            dateFormat = 2,
            clockIcon = 0,
            textAlign = 1,
            showSeconds = true,
            weekdayBar = true,
            weekdayStartMon = false,
            weekdayActiveColIdx = 2,
            weekdayInactColIdx = 5,
            weekdayWeekendColIdx = 1,
            dayBri = 75,
            nightBri = 5,
            autoBri = true,
            nightMode = true,
            nightStart = 22,
            nightEnd = 6,
            rotateSec = 10,
            autoRotate = true,
            carouselMask = 0x07, // apps 0, 1, 2
            transitionFx = 1,
            weatherCity = "Prague",
            weatherLat = 50.0755f,
            weatherLon = 14.4378f,
            cryptoSymbol = "SOL",
            ytChannel = "@test",
            mqttEn = true,
            mqttSrv = "192.168.1.100",
            mqttPort = 1883,
            wifiSlot = 1,
            wifiSsid = "Office-5G",
            wifiPass = "secretpass"
        )

        val saved = repo.lastSavedSettings
        assertEquals("1", saved?.get("clk_mode"))
        assertEquals("2", saved?.get("date_fmt"))
        assertEquals("0", saved?.get("clk_icon"))
        assertEquals("1", saved?.get("txt_align"))
        assertEquals("1", saved?.get("show_sec"))
        assertEquals("1", saved?.get("wd_bar"))
        assertEquals("0", saved?.get("wd_mon"))
        assertEquals("2", saved?.get("wd_acol"))
        assertEquals("75", saved?.get("day_bri"))
        assertEquals("5", saved?.get("night_bri"))
        assertEquals("1", saved?.get("auto_bri"))
        assertEquals("1", saved?.get("night_mode"))
        assertEquals("22", saved?.get("night_start"))
        assertEquals("6", saved?.get("night_end"))
        assertEquals("10", saved?.get("rotate"))
        assertEquals("1", saved?.get("auto_rotate"))
        assertEquals("1", saved?.get("car_app_0"))
        assertEquals("1", saved?.get("car_app_1"))
        assertEquals("1", saved?.get("car_app_2"))
        assertEquals(null, saved?.get("car_app_3"))
        assertEquals("1", saved?.get("trans_fx"))
        assertEquals("Prague", saved?.get("w_city"))
        assertEquals("50.0755", saved?.get("w_lat"))
        assertEquals("14.4378", saved?.get("w_lon"))
        assertEquals("SOL", saved?.get("c_sym_custom"))
        assertEquals("@test", saved?.get("yt_channel"))
        assertEquals("1", saved?.get("mqtt_en"))
        assertEquals("192.168.1.100", saved?.get("mqtt_srv"))
        assertEquals("Office-5G", saved?.get("ssid"))
        assertEquals("secretpass", saved?.get("password"))
    }

    @Test
    fun canvasStudio_drawPixel_and_tools() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Initial default state
        assertEquals(DashboardViewMode.LIVE_STREAM, viewModel.uiState.value.dashboardViewMode)
        viewModel.setDashboardViewMode(DashboardViewMode.CANVAS_STUDIO)
        assertEquals(DashboardViewMode.CANVAS_STUDIO, viewModel.uiState.value.dashboardViewMode)

        // Draw pixel with PEN
        viewModel.setCanvasSelectedColor("#00E676")
        viewModel.drawPixel(5, 2)
        assertEquals("#00E676", viewModel.uiState.value.canvasPixels[2][5])

        // Draw with ERASER
        viewModel.setCanvasTool(CanvasTool.ERASER)
        viewModel.drawPixel(5, 2)
        assertEquals("#000000", viewModel.uiState.value.canvasPixels[2][5])

        // Clear canvas
        viewModel.setCanvasTool(CanvasTool.PEN)
        viewModel.drawPixel(0, 0)
        viewModel.clearCanvas()
        assertEquals("#000000", viewModel.uiState.value.canvasPixels[0][0])

        // Fill canvas
        viewModel.fillCanvas("#2979FF")
        assertEquals("#2979FF", viewModel.uiState.value.canvasPixels[7][31])
    }

    @Test
    fun canvasStudio_loadPreset_and_sendToClock() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.loadCanvasPreset("pacman")
        // Pacman has yellow pixels
        assertTrue(viewModel.uiState.value.canvasPixels.any { row -> row.contains("#FFD600") })

        viewModel.setCanvasDuration(30)
        assertEquals(30, viewModel.uiState.value.canvasDisplayDurationSec)

        viewModel.sendCanvasToClock()
        assertEquals(30, repo.lastSentDuration)
        assertEquals(8, repo.lastSentPixels?.size)
        assertEquals(32, repo.lastSentPixels?.get(0)?.size)
    }

    @Test
    fun actionMessage_autoDismissesAfterDelay() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.setActionMessage("Test Notification", autoDismissDelayMs = 2000L)
        assertEquals("Test Notification", viewModel.uiState.value.actionMessage)

        testScheduler.advanceTimeBy(1000L)
        assertEquals("Test Notification", viewModel.uiState.value.actionMessage)

        testScheduler.advanceTimeBy(1500L)
        assertEquals(null, viewModel.uiState.value.actionMessage)
    }

    @Test
    fun saveHardwareSettings_autoSyncsAndEmitsFeedback() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val prefsManager = DevicePreferencesManager()
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager, prefsManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveHardwareSettings(
            clkMode = 1,
            showSeconds = true,
            autoBri = false,
            dayBri = 80,
            successMessage = "Setting synced to clock ✓"
        )

        assertEquals("1", repo.lastSavedSettings?.get("clk_mode"))
        assertEquals("1", repo.lastSavedSettings?.get("show_sec"))
        assertEquals("80", repo.lastSavedSettings?.get("day_bri"))
        assertEquals("Setting synced to clock ✓", viewModel.uiState.value.actionMessage)

        testScheduler.advanceTimeBy(3600L)
        assertEquals(null, viewModel.uiState.value.actionMessage)
    }
}

private class FakeUlanziRepository : UlanziRepository {
    override val connectionState: StateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Disconnected)
    override val deviceStatus: StateFlow<DeviceStatus?> = MutableStateFlow(null)
    override val displayMatrix: StateFlow<DisplayMatrixResponse?> = MutableStateFlow(null)
    override val discoveryState: StateFlow<DiscoveryState> = MutableStateFlow(DiscoveryState.Idle)

    var lastTimerCall: Triple<String, String?, Int?>? = null
    var lastScreensaverCall: Triple<Int?, Int?, String?>? = null
    var lastSavedSettings: Map<String, String>? = null
    var lastSentPixels: List<List<String>>? = null
    var lastSentDuration: Int? = null

    override fun startDiscovery(scope: CoroutineScope) {}
    override fun stopDiscovery() {}
    override fun connectToDevice(hostOrIp: String, scope: CoroutineScope) {}
    override fun disconnect() {}

    override suspend fun refreshStatus(): Result<DeviceStatus> = Result.success(DeviceStatus())
    override suspend fun refreshDisplayMatrix(): Result<DisplayMatrixResponse> = Result.success(DisplayMatrixResponse())
    override suspend fun sendCanvasPixels(pixels: List<List<String>>, durationSec: Int): Result<GenericStatusResponse> {
        lastSentPixels = pixels
        lastSentDuration = durationSec
        return Result.success(GenericStatusResponse(status = "ok"))
    }
    override suspend fun switchScreen(appIndex: Int, autoRotate: Boolean?): Result<ScreenSwitchResult> =
        Result.success(ScreenSwitchResult(activeApp = appIndex))
    override suspend fun sendNotification(payload: NotifyPayload): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun pushCustomApp(name: String, payload: CustomAppPayload): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun deleteCustomApp(name: String): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun controlTimer(action: String, mode: String?, duration: Int?): Result<TimerResponse> {
        lastTimerCall = Triple(action, mode, duration)
        return Result.success(TimerResponse())
    }
    override suspend fun controlScreensaver(style: Int?, timeout: Int?, action: String?): Result<ScreensaverResponse> {
        lastScreensaverCall = Triple(style, timeout, action)
        return Result.success(ScreensaverResponse())
    }
    override suspend fun getWifiStatus(): Result<WifiResponse> = Result.success(WifiResponse())
    override suspend fun scanWifi(): Result<List<com.hackra.ulanzi.companion.data.model.WifiScanResult>> = Result.success(emptyList())
    override suspend fun connectWifiSlot(slot: Int): Result<Unit> = Result.success(Unit)
    override suspend fun deleteWifiSlot(slot: Int): Result<Unit> = Result.success(Unit)
    override suspend fun saveSettings(params: Map<String, String>): Result<Unit> {
        lastSavedSettings = params
        return Result.success(Unit)
    }
    override suspend fun rebootDevice(): Result<Unit> = Result.success(Unit)
    override suspend fun checkOtaUpdate(currentVersion: String): Result<com.hackra.ulanzi.companion.data.model.OtaCheckResponse> =
        Result.success(com.hackra.ulanzi.companion.data.model.OtaCheckResponse())
    override suspend fun installCloudOta(downloadUrl: String): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun uploadFirmwareBinary(
        bytes: ByteArray,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
}
