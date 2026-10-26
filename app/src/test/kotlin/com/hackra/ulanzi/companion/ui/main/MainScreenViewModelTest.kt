package com.hackra.ulanzi.companion.ui.main

import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.UlanziRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
    @Test
    fun uiState_initiallyDisconnected() = runTest {
        val repo = FakeUlanziRepository()
        val notifManager = NotificationSettingsManager()
        val mediaManager = MediaSyncManager(repo)
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager)
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
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager)
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
        val viewModel = MainScreenViewModel(repo, notifManager, mediaManager)

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
}

private class FakeUlanziRepository : UlanziRepository {
    override val connectionState: StateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Disconnected)
    override val deviceStatus: StateFlow<DeviceStatus?> = MutableStateFlow(null)
    override val displayMatrix: StateFlow<DisplayMatrixResponse?> = MutableStateFlow(null)
    override val discoveryState: StateFlow<DiscoveryState> = MutableStateFlow(DiscoveryState.Idle)

    override fun startDiscovery(scope: CoroutineScope) {}
    override fun stopDiscovery() {}
    override fun connectToDevice(hostOrIp: String, scope: CoroutineScope) {}
    override fun disconnect() {}

    override suspend fun refreshStatus(): Result<DeviceStatus> = Result.success(DeviceStatus())
    override suspend fun refreshDisplayMatrix(): Result<DisplayMatrixResponse> = Result.success(DisplayMatrixResponse())
    override suspend fun switchScreen(appIndex: Int, autoRotate: Boolean?): Result<ScreenSwitchResult> =
        Result.success(ScreenSwitchResult(activeApp = appIndex))
    override suspend fun sendNotification(payload: NotifyPayload): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun pushCustomApp(name: String, payload: CustomAppPayload): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun deleteCustomApp(name: String): Result<GenericStatusResponse> =
        Result.success(GenericStatusResponse())
    override suspend fun controlTimer(action: String, mode: String?, duration: Int?): Result<TimerResponse> =
        Result.success(TimerResponse())
    override suspend fun controlScreensaver(style: Int?, timeout: Int?, action: String?): Result<ScreensaverResponse> =
        Result.success(ScreensaverResponse())
    override suspend fun getWifiStatus(): Result<WifiResponse> = Result.success(WifiResponse())
    override suspend fun saveSettings(params: Map<String, String>): Result<Unit> = Result.success(Unit)
}
