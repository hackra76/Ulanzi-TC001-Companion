package com.hackra.ulanzi.companion.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.discovery.DiscoveryState
import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.NotifyPayload
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainScreenUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val deviceStatus: DeviceStatus? = null,
    val displayMatrix: DisplayMatrixResponse? = null,
    val discoveryState: DiscoveryState = DiscoveryState.Idle,
    val actionMessage: String? = null
)

class MainScreenViewModel(
    private val repository: UlanziRepository
) : ViewModel() {

    val uiState: StateFlow<MainScreenUiState> = combine(
        repository.connectionState,
        repository.deviceStatus,
        repository.displayMatrix,
        repository.discoveryState
    ) { conn, status, matrix, discovery ->
        MainScreenUiState(
            connectionState = conn,
            deviceStatus = status,
            displayMatrix = matrix,
            discoveryState = discovery
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenUiState()
    )

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

    fun sendNotification(text: String, icon: String = "bell", sound: Int = 1, duration: Int = 5) {
        viewModelScope.launch {
            repository.sendNotification(NotifyPayload(text = text, icon = icon, sound = sound, duration = duration))
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
}
