package com.hackra.ulanzi.companion.data.notification

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class NotificationDispatchStatus {
    SENT,
    SKIPPED_DISCONNECTED,
    SKIPPED_DISABLED,
    SKIPPED_FILTERED,
    SKIPPED_DUPLICATE,
    ERROR
}

data class ForwardedNotificationLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val appName: String,
    val packageName: String,
    val title: String,
    val text: String,
    val icon: String,
    val status: NotificationDispatchStatus,
    val errorMessage: String? = null
)

data class NotificationSettings(
    val isForwardingEnabled: Boolean = true,
    val forwardCalls: Boolean = true,
    val forwardMessaging: Boolean = true,
    val forwardCalendar: Boolean = true,
    val forwardOther: Boolean = false,
    val sound: Int = 1,
    val duration: Int = 6,
    val ignoreOngoing: Boolean = true,
    val includeAppName: Boolean = true
)

open class NotificationSettingsManager {

    private val context: Context?
    private val prefs: SharedPreferences?

    private val _settings: MutableStateFlow<NotificationSettings>
    val settings: StateFlow<NotificationSettings>

    private val _logs = MutableStateFlow<List<ForwardedNotificationLog>>(emptyList())
    val logs: StateFlow<List<ForwardedNotificationLog>> = _logs.asStateFlow()

    private val _hasListenerPermission: MutableStateFlow<Boolean>
    val hasListenerPermission: StateFlow<Boolean>

    constructor(context: Context) {
        this.context = context
        val sp = context.getSharedPreferences("ulanzi_notification_prefs", Context.MODE_PRIVATE)
        this.prefs = sp
        this._settings = MutableStateFlow(loadSettings(sp))
        this.settings = _settings.asStateFlow()
        val perm = checkPermission(context)
        this._hasListenerPermission = MutableStateFlow(perm)
        this.hasListenerPermission = _hasListenerPermission.asStateFlow()
    }

    constructor(
        initialSettings: NotificationSettings = NotificationSettings(),
        initialHasPermission: Boolean = false
    ) {
        this.context = null
        this.prefs = null
        this._settings = MutableStateFlow(initialSettings)
        this.settings = _settings.asStateFlow()
        this._hasListenerPermission = MutableStateFlow(initialHasPermission)
        this.hasListenerPermission = _hasListenerPermission.asStateFlow()
    }

    fun refreshPermission(): Boolean {
        val ctx = context
        val granted = if (ctx != null) checkPermission(ctx) else _hasListenerPermission.value
        _hasListenerPermission.value = granted
        return granted
    }

    open fun getSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    private fun checkPermission(ctx: Context): Boolean {
        val packageName = ctx.packageName
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(ctx)
        return listeners.contains(packageName)
    }

    private fun loadSettings(sp: SharedPreferences): NotificationSettings {
        return NotificationSettings(
            isForwardingEnabled = sp.getBoolean(KEY_FORWARDING_ENABLED, true),
            forwardCalls = sp.getBoolean(KEY_FORWARD_CALLS, true),
            forwardMessaging = sp.getBoolean(KEY_FORWARD_MESSAGING, true),
            forwardCalendar = sp.getBoolean(KEY_FORWARD_CALENDAR, true),
            forwardOther = sp.getBoolean(KEY_FORWARD_OTHER, false),
            sound = sp.getInt(KEY_SOUND, 1),
            duration = sp.getInt(KEY_DURATION, 6),
            ignoreOngoing = sp.getBoolean(KEY_IGNORE_ONGOING, true),
            includeAppName = sp.getBoolean(KEY_INCLUDE_APP_NAME, true)
        )
    }

    fun setForwardingEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FORWARDING_ENABLED, enabled)?.apply()
        _settings.value = _settings.value.copy(isForwardingEnabled = enabled)
    }

    fun setForwardCalls(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FORWARD_CALLS, enabled)?.apply()
        _settings.value = _settings.value.copy(forwardCalls = enabled)
    }

    fun setForwardMessaging(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FORWARD_MESSAGING, enabled)?.apply()
        _settings.value = _settings.value.copy(forwardMessaging = enabled)
    }

    fun setForwardCalendar(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FORWARD_CALENDAR, enabled)?.apply()
        _settings.value = _settings.value.copy(forwardCalendar = enabled)
    }

    fun setForwardOther(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FORWARD_OTHER, enabled)?.apply()
        _settings.value = _settings.value.copy(forwardOther = enabled)
    }

    fun setSound(sound: Int) {
        prefs?.edit()?.putInt(KEY_SOUND, sound)?.apply()
        _settings.value = _settings.value.copy(sound = sound)
    }

    fun setDuration(duration: Int) {
        val clamped = duration.coerceIn(2, 20)
        prefs?.edit()?.putInt(KEY_DURATION, clamped)?.apply()
        _settings.value = _settings.value.copy(duration = clamped)
    }

    fun setIgnoreOngoing(ignore: Boolean) {
        prefs?.edit()?.putBoolean(KEY_IGNORE_ONGOING, ignore)?.apply()
        _settings.value = _settings.value.copy(ignoreOngoing = ignore)
    }

    fun setIncludeAppName(include: Boolean) {
        prefs?.edit()?.putBoolean(KEY_INCLUDE_APP_NAME, include)?.apply()
        _settings.value = _settings.value.copy(includeAppName = include)
    }

    fun recordLog(log: ForwardedNotificationLog) {
        val current = _logs.value.toMutableList()
        current.add(0, log)
        if (current.size > 25) {
            current.removeAt(current.size - 1)
        }
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    companion object {
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
        private const val KEY_FORWARD_CALLS = "forward_calls"
        private const val KEY_FORWARD_MESSAGING = "forward_messaging"
        private const val KEY_FORWARD_CALENDAR = "forward_calendar"
        private const val KEY_FORWARD_OTHER = "forward_other"
        private const val KEY_SOUND = "sound"
        private const val KEY_DURATION = "duration"
        private const val KEY_IGNORE_ONGOING = "ignore_ongoing"
        private const val KEY_INCLUDE_APP_NAME = "include_app_name"
    }
}
