package com.hackra.ulanzi.companion.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackra.ulanzi.companion.data.ConnectionState
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.discovery.DiscoveryState
import com.hackra.ulanzi.companion.data.media.MediaPlaybackState
import com.hackra.ulanzi.companion.data.media.MediaSyncManager
import com.hackra.ulanzi.companion.data.media.MediaSyncSettings
import com.hackra.ulanzi.companion.data.media.MediaTextFormat
import com.hackra.ulanzi.companion.data.model.CustomAppPayload
import com.hackra.ulanzi.companion.data.model.DeviceStatus
import com.hackra.ulanzi.companion.data.model.DisplayMatrixResponse
import com.hackra.ulanzi.companion.data.model.NotifyPayload
import com.hackra.ulanzi.companion.data.notification.ForwardedNotificationLog
import com.hackra.ulanzi.companion.data.notification.NotificationSettings
import com.hackra.ulanzi.companion.data.notification.NotificationSettingsManager
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
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val hasListenerPermission: Boolean = false,
    val notificationLogs: List<ForwardedNotificationLog> = emptyList(),
    val mediaState: MediaPlaybackState = MediaPlaybackState(),
    val mediaSyncSettings: MediaSyncSettings = MediaSyncSettings(),
    val actionMessage: String? = null
)

class MainScreenViewModel(
    private val repository: UlanziRepository,
    private val notificationSettingsManager: NotificationSettingsManager,
    private val mediaSyncManager: MediaSyncManager
) : ViewModel() {

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

    val uiState: StateFlow<MainScreenUiState> = combine(
        repositoryFlow,
        notificationFlow,
        mediaFlow
    ) { repo, notif, media ->
        MainScreenUiState(
            connectionState = repo.first,
            deviceStatus = repo.second,
            displayMatrix = repo.third,
            discoveryState = repo.fourth,
            notificationSettings = notif.first,
            hasListenerPermission = notif.second,
            notificationLogs = notif.third,
            mediaState = media.first,
            mediaSyncSettings = media.second
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
            mediaSyncSettings = mediaSyncManager.settings.value
        )
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
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Tuple3<A, B, C>(val first: A, val second: B, val third: C)
