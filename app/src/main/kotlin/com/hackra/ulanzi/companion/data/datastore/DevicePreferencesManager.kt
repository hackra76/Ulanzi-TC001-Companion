package com.hackra.ulanzi.companion.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_preferences")

data class DevicePreferences(
    val lastDeviceHost: String = "ulanzi-tc001.local",
    val autoReconnect: Boolean = true,
    val dayBrightness: Int = 60,
    val nightBrightness: Int = 9,
    val autoBrightness: Boolean = true,
    val carouselRotateSec: Int = 8,
    val timerDefaultDurationSec: Int = 300,
    val screensaverStyle: Int = 0,
    val screensaverTimeoutMin: Int = 5
)

open class DevicePreferencesManager {

    private val context: Context?
    private val scope: CoroutineScope

    private val _preferences: MutableStateFlow<DevicePreferences>
    val preferences: StateFlow<DevicePreferences>

    constructor(
        context: Context,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        this.context = context
        this.scope = scope
        this._preferences = MutableStateFlow(DevicePreferences())
        this.preferences = _preferences.asStateFlow()

        scope.launch {
            context.dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .collect { prefs ->
                    _preferences.value = DevicePreferences(
                        lastDeviceHost = prefs[KEY_LAST_DEVICE_HOST] ?: "ulanzi-tc001.local",
                        autoReconnect = prefs[KEY_AUTO_RECONNECT] ?: true,
                        dayBrightness = prefs[KEY_DAY_BRIGHTNESS] ?: 60,
                        nightBrightness = prefs[KEY_NIGHT_BRIGHTNESS] ?: 9,
                        autoBrightness = prefs[KEY_AUTO_BRIGHTNESS] ?: true,
                        carouselRotateSec = prefs[KEY_ROTATE_SEC] ?: 8,
                        timerDefaultDurationSec = prefs[KEY_TIMER_DEFAULT_DURATION] ?: 300,
                        screensaverStyle = prefs[KEY_SCREENSAVER_STYLE] ?: 0,
                        screensaverTimeoutMin = prefs[KEY_SCREENSAVER_TIMEOUT] ?: 5
                    )
                }
        }
    }

    constructor(
        initialPreferences: DevicePreferences = DevicePreferences(),
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        this.context = null
        this.scope = scope
        this._preferences = MutableStateFlow(initialPreferences)
        this.preferences = _preferences.asStateFlow()
    }

    fun setLastDeviceHost(hostOrIp: String) {
        val trimmed = hostOrIp.trim()
        if (trimmed.isBlank()) return
        _preferences.value = _preferences.value.copy(lastDeviceHost = trimmed)
        val ctx = context ?: return
        scope.launch {
            ctx.dataStore.edit { it[KEY_LAST_DEVICE_HOST] = trimmed }
        }
    }

    fun setAutoReconnect(enabled: Boolean) {
        _preferences.value = _preferences.value.copy(autoReconnect = enabled)
        val ctx = context ?: return
        scope.launch {
            ctx.dataStore.edit { it[KEY_AUTO_RECONNECT] = enabled }
        }
    }

    fun setHardwareBrightnessDefaults(dayBri: Int, nightBri: Int, autoBri: Boolean) {
        val clampedDay = dayBri.coerceIn(0, 100)
        val clampedNight = nightBri.coerceIn(0, 100)
        _preferences.value = _preferences.value.copy(
            dayBrightness = clampedDay,
            nightBrightness = clampedNight,
            autoBrightness = autoBri
        )
        val ctx = context ?: return
        scope.launch {
            ctx.dataStore.edit {
                it[KEY_DAY_BRIGHTNESS] = clampedDay
                it[KEY_NIGHT_BRIGHTNESS] = clampedNight
                it[KEY_AUTO_BRIGHTNESS] = autoBri
            }
        }
    }

    fun setCarouselRotateSec(sec: Int) {
        val clamped = sec.coerceIn(3, 60)
        _preferences.value = _preferences.value.copy(carouselRotateSec = clamped)
        val ctx = context ?: return
        scope.launch {
            ctx.dataStore.edit { it[KEY_ROTATE_SEC] = clamped }
        }
    }

    fun setTimerDefaultDuration(sec: Int) {
        val clamped = sec.coerceIn(10, 7200)
        _preferences.value = _preferences.value.copy(timerDefaultDurationSec = clamped)
        val ctx = context ?: return
        scope.launch {
            ctx.dataStore.edit { it[KEY_TIMER_DEFAULT_DURATION] = clamped }
        }
    }

    fun setScreensaverDefaults(style: Int, timeoutMin: Int) {
        val clampedStyle = style.coerceIn(0, 4)
        val clampedTimeout = timeoutMin.coerceIn(1, 60)
        _preferences.value = _preferences.value.copy(
            screensaverStyle = clampedStyle,
            screensaverTimeoutMin = clampedTimeout
        )
        val ctx = context ?: return
        scope.launch {
            ctx.dataStore.edit {
                it[KEY_SCREENSAVER_STYLE] = clampedStyle
                it[KEY_SCREENSAVER_TIMEOUT] = clampedTimeout
            }
        }
    }

    companion object {
        val KEY_LAST_DEVICE_HOST = stringPreferencesKey("last_device_host")
        val KEY_AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
        val KEY_DAY_BRIGHTNESS = intPreferencesKey("day_brightness")
        val KEY_NIGHT_BRIGHTNESS = intPreferencesKey("night_brightness")
        val KEY_AUTO_BRIGHTNESS = booleanPreferencesKey("auto_brightness")
        val KEY_ROTATE_SEC = intPreferencesKey("carousel_rotate_sec")
        val KEY_TIMER_DEFAULT_DURATION = intPreferencesKey("timer_default_duration")
        val KEY_SCREENSAVER_STYLE = intPreferencesKey("screensaver_style")
        val KEY_SCREENSAVER_TIMEOUT = intPreferencesKey("screensaver_timeout")
    }
}
