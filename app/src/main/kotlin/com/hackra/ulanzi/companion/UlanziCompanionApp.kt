package com.hackra.ulanzi.companion

import android.app.Application
import com.hackra.ulanzi.companion.data.DefaultUlanziRepository
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.datastore.DevicePreferencesManager
import com.hackra.ulanzi.companion.data.media.MediaSyncManager
import com.hackra.ulanzi.companion.data.network.UlanziApiClient
import com.hackra.ulanzi.companion.data.notification.NotificationSettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UlanziCompanionApp : Application() {

    lateinit var apiClient: UlanziApiClient
        private set

    lateinit var repository: UlanziRepository
        private set

    lateinit var notificationSettingsManager: NotificationSettingsManager
        private set

    lateinit var mediaSyncManager: MediaSyncManager
        private set

    lateinit var devicePreferencesManager: DevicePreferencesManager
        private set

    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        apiClient = UlanziApiClient()
        devicePreferencesManager = DevicePreferencesManager(this, appScope)
        repository = DefaultUlanziRepository(this, apiClient, devicePreferencesManager)
        notificationSettingsManager = NotificationSettingsManager(this)
        mediaSyncManager = MediaSyncManager(this, repository, notificationSettingsManager, appScope)

        // Phase 5: Auto-reconnect to last known clock if enabled
        appScope.launch {
            devicePreferencesManager.preferences.collect { prefs ->
                if (prefs.autoReconnect && prefs.lastDeviceHost.isNotBlank()) {
                    val currentConn = repository.connectionState.value
                    if (currentConn is com.hackra.ulanzi.companion.data.ConnectionState.Disconnected) {
                        repository.connectToDevice(prefs.lastDeviceHost, appScope)
                    }
                }
            }
        }
    }

    companion object {
        lateinit var instance: UlanziCompanionApp
            private set

        fun get(): UlanziCompanionApp = instance
    }
}
