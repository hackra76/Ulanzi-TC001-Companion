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
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
    @Test
    fun uiState_initiallyDisconnected() = runTest {
        val viewModel = MainScreenViewModel(FakeUlanziRepository())
        assertEquals(ConnectionState.Disconnected, viewModel.uiState.first().connectionState)
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
