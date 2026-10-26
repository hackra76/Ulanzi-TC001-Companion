package com.hackra.ulanzi.companion

import android.app.Application
import com.hackra.ulanzi.companion.data.DefaultUlanziRepository
import com.hackra.ulanzi.companion.data.UlanziRepository
import com.hackra.ulanzi.companion.data.network.UlanziApiClient
import com.hackra.ulanzi.companion.data.notification.NotificationSettingsManager

class UlanziCompanionApp : Application() {

    lateinit var apiClient: UlanziApiClient
        private set

    lateinit var repository: UlanziRepository
        private set

    lateinit var notificationSettingsManager: NotificationSettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        apiClient = UlanziApiClient()
        repository = DefaultUlanziRepository(this, apiClient)
        notificationSettingsManager = NotificationSettingsManager(this)
    }

    companion object {
        lateinit var instance: UlanziCompanionApp
            private set

        fun get(): UlanziCompanionApp = instance
    }
}
