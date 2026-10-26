package com.hackra.ulanzi.companion.data

import android.content.Context
import com.hackra.ulanzi.companion.data.datastore.DevicePreferencesManager
import com.hackra.ulanzi.companion.data.discovery.DeviceDiscoveryManager
import com.hackra.ulanzi.companion.data.discovery.DiscoveredDevice
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
import com.hackra.ulanzi.companion.data.network.UlanziApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val target: String) : ConnectionState
    data class Connected(val device: DiscoveredDevice) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

interface UlanziRepository {
    val connectionState: StateFlow<ConnectionState>
    val deviceStatus: StateFlow<DeviceStatus?>
    val displayMatrix: StateFlow<DisplayMatrixResponse?>
    val discoveryState: StateFlow<DiscoveryState>

    fun startDiscovery(scope: CoroutineScope)
    fun stopDiscovery()
    fun connectToDevice(hostOrIp: String, scope: CoroutineScope)
    fun disconnect()

    suspend fun refreshStatus(): Result<DeviceStatus>
    suspend fun refreshDisplayMatrix(): Result<DisplayMatrixResponse>
    suspend fun switchScreen(appIndex: Int, autoRotate: Boolean? = null): Result<ScreenSwitchResult>
    suspend fun sendNotification(payload: NotifyPayload): Result<GenericStatusResponse>
    suspend fun pushCustomApp(name: String, payload: CustomAppPayload): Result<GenericStatusResponse>
    suspend fun deleteCustomApp(name: String): Result<GenericStatusResponse>
    suspend fun controlTimer(action: String, mode: String? = null, duration: Int? = null): Result<TimerResponse>
    suspend fun controlScreensaver(style: Int? = null, timeout: Int? = null, action: String? = null): Result<ScreensaverResponse>
    suspend fun getWifiStatus(): Result<WifiResponse>
    suspend fun connectWifiSlot(slot: Int): Result<Unit>
    suspend fun saveSettings(params: Map<String, String>): Result<Unit>
    suspend fun rebootDevice(): Result<Unit>
}

class DefaultUlanziRepository(
    context: Context,
    private val apiClient: UlanziApiClient = UlanziApiClient(),
    private val preferencesManager: DevicePreferencesManager? = null
) : UlanziRepository {

    private val discoveryManager = DeviceDiscoveryManager(context.applicationContext, apiClient)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    override val deviceStatus: StateFlow<DeviceStatus?> = _deviceStatus.asStateFlow()

    private val _displayMatrix = MutableStateFlow<DisplayMatrixResponse?>(null)
    override val displayMatrix: StateFlow<DisplayMatrixResponse?> = _displayMatrix.asStateFlow()

    override val discoveryState: StateFlow<DiscoveryState> = discoveryManager.discoveryState

    private var pollingJob: Job? = null

    override fun startDiscovery(scope: CoroutineScope) {
        discoveryManager.startDiscovery(scope)
    }

    override fun stopDiscovery() {
        discoveryManager.stopDiscovery()
    }

    override fun connectToDevice(hostOrIp: String, scope: CoroutineScope) {
        pollingJob?.cancel()
        _connectionState.value = ConnectionState.Connecting(hostOrIp)
        apiClient.setDeviceAddress(hostOrIp)

        scope.launch {
            val statusResult = apiClient.getStatus()
            statusResult.onSuccess { status ->
                val device = DiscoveredDevice(ip = status.ip.ifBlank { hostOrIp }, hostname = hostOrIp, status = status)
                _deviceStatus.value = status
                _connectionState.value = ConnectionState.Connected(device)

                // Persist last successful device IP/host
                preferencesManager?.setLastDeviceHost(hostOrIp)

                // Start background polling loop for status and live display stream
                startPolling(scope)
            }.onFailure { err ->
                _connectionState.value = ConnectionState.Error(err.message ?: "Failed to connect to $hostOrIp")
            }
        }
    }

    override fun disconnect() {
        pollingJob?.cancel()
        pollingJob = null
        _connectionState.value = ConnectionState.Disconnected
        _deviceStatus.value = null
        _displayMatrix.value = null
    }

    private fun startPolling(scope: CoroutineScope) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                // Poll status every 2 seconds
                apiClient.getStatus().onSuccess { status ->
                    _deviceStatus.value = status
                }

                // Poll display matrix
                apiClient.getDisplayMatrix().onSuccess { matrix ->
                    _displayMatrix.value = matrix
                }

                delay(2000L)
            }
        }
    }

    override suspend fun refreshStatus(): Result<DeviceStatus> {
        val res = apiClient.getStatus()
        res.onSuccess { _deviceStatus.value = it }
        return res
    }

    override suspend fun refreshDisplayMatrix(): Result<DisplayMatrixResponse> {
        val res = apiClient.getDisplayMatrix()
        res.onSuccess { _displayMatrix.value = it }
        return res
    }

    override suspend fun switchScreen(appIndex: Int, autoRotate: Boolean?): Result<ScreenSwitchResult> {
        val res = apiClient.switchScreen(appIndex, autoRotate)
        res.onSuccess { refreshStatus() }
        return res
    }

    override suspend fun sendNotification(payload: NotifyPayload): Result<GenericStatusResponse> =
        apiClient.sendNotification(payload)

    override suspend fun pushCustomApp(name: String, payload: CustomAppPayload): Result<GenericStatusResponse> =
        apiClient.pushCustomApp(name, payload)

    override suspend fun deleteCustomApp(name: String): Result<GenericStatusResponse> =
        apiClient.deleteCustomApp(name)

    override suspend fun controlTimer(action: String, mode: String?, duration: Int?): Result<TimerResponse> {
        val res = apiClient.controlTimer(action, mode, duration)
        res.onSuccess { refreshStatus() }
        return res
    }

    override suspend fun controlScreensaver(style: Int?, timeout: Int?, action: String?): Result<ScreensaverResponse> {
        val res = apiClient.controlScreensaver(style, timeout, action)
        res.onSuccess { refreshStatus() }
        return res
    }

    override suspend fun getWifiStatus(): Result<WifiResponse> =
        apiClient.getWifiStatus()

    override suspend fun connectWifiSlot(slot: Int): Result<Unit> {
        val res = apiClient.connectWifiSlot(slot)
        res.onSuccess { refreshStatus() }
        return res
    }

    override suspend fun saveSettings(params: Map<String, String>): Result<Unit> {
        val res = apiClient.saveSettings(params)
        res.onSuccess { refreshStatus() }
        return res
    }

    override suspend fun rebootDevice(): Result<Unit> =
        apiClient.rebootDevice()
}
